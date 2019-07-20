package com.sanjin.ibr.core.rpc;

import com.sanjin.ibr.core.message.RpcRequest;
import com.sanjin.ibr.core.message.RpcResponse;
import com.sanjin.ibr.core.serializers.Serializer;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @description:
 * @author: sanjin
 * @date: 2019/7/20 9:08
 */
@Slf4j
public class RpcConsumerProcessor implements MessageProcessor<byte[]> {

    /**
     * socket session
     */
    private AioSession<byte[]> session;

    /**
     * 序列化器(serialize/deserialize)
     */
    private Serializer<RpcRequest, RpcResponse> serializer;

    public RpcConsumerProcessor(Serializer<RpcRequest, RpcResponse> serializer) {
        this.serializer = serializer;
        rpcResponseMap = new ConcurrentHashMap<>();
    }

    /**
     * 保存 rpcresponse
     * key -> uuid
     * value -> rpcResponse
     */
    private Map<String, CompletableFuture<RpcResponse>> rpcResponseMap;

    /**
     * 发送 rpc request，返回 rpc response
     *
     * @param rpcRequest
     * @return
     */
    private void sendRpcRequest(RpcRequest rpcRequest) {
        log.debug("发送rpc请求: rpcRequest={}", rpcRequest);
        this.rpcResponseMap.put(rpcRequest.getUuid(), new CompletableFuture<RpcResponse>());
        // 将 rpcRequest 序列化为 字节数组
        byte[] serialize = serializer.serialize(rpcRequest);

        // 通过socket 发送出去
        synchronized (session) {
            try {
                // session.write() 会调用 encode 方法
                session.write(serialize);
            } catch (IOException e) {
                log.warn("发送socket异常");
                e.printStackTrace();
            }
        }
    }

    /**
     * @param serviceInterface 远程服务接口 Class
     * @param <T>              代理对象接口类型
     * @return 代理对象
     */
    @SuppressWarnings("unchecked")
    public <T> T getServiceProxyInstance(Class<T> serviceInterface) {
        return (T) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{serviceInterface},
                (proxy, method, args) -> {
                    // 获取方法参数类型
                    String[] paramsClassList;

                    Class<?>[] parameterTypes = method.getParameterTypes();
                    paramsClassList = new String[parameterTypes.length];
                    for (int i = 0; i < parameterTypes.length; i++) {
                        paramsClassList[i] = parameterTypes[i].getName();
                    }

                    // 发送 rpc 请求 调用远程服务
                    RpcRequest rpcRequest =
                            new RpcRequest()
                                    .setUuid(UUID.randomUUID().toString())
                                    .setService(serviceInterface.getName())
                                    .setMethod(method.getName())
                                    .setParamsClassList(paramsClassList)
                                    .setParams(args);
                    this.sendRpcRequest(rpcRequest);

                    RpcResponse rpcResponse = this.rpcResponseMap.get(rpcRequest.getUuid()).get();
                    log.debug("【Concumer】收到rpc响应: rpcResponse={}", rpcResponse);

                    if (rpcResponse == null) {
                        return null;
                    }
                    // 判断是否有异常产生
                    if (rpcResponse.getException() != null && !"".equals(rpcResponse.getException())) {
                        return null;
                    }
                    // 返回rpc结果

                    return rpcResponse.getReturnObject();
                }
        );
    }

    @Override
    public void process(AioSession<byte[]> session, byte[] msg) {
        // 将 msg 转换为 RpcResponse 对象
        RpcResponse rpcResponse = serializer.deserialize(msg);
        this.rpcResponseMap.get(rpcResponse.getUuid()).complete(rpcResponse);
    }

    @Override
    public void stateEvent(AioSession<byte[]> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
        switch (stateMachineEnum) {
            case NEW_SESSION:
                this.session = session;
                break;
        }
    }
}
