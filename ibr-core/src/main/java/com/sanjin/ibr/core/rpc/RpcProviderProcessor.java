package com.sanjin.ibr.core.rpc;

import com.sanjin.ibr.core.exception.RpcException;
import com.sanjin.ibr.core.message.RpcRequest;
import com.sanjin.ibr.core.message.RpcResponse;
import com.sanjin.ibr.core.serializers.Serializer;
import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.MessageProcessor;
import org.smartboot.socket.StateMachineEnum;
import org.smartboot.socket.transport.AioSession;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description:
 * @author: sanjin
 * @date: 2019/7/20 11:32
 */
@Slf4j
public class RpcProviderProcessor implements MessageProcessor<byte[]> {

    /**
     * 可提供 rpc 的 map
     * key -> 服务名称
     * value -> impl 对象
     */
    private Map<String, Object> implMap = new HashMap<>();

    /**
     * 序列化器
     */
    private Serializer<RpcResponse, RpcRequest> serializer;

    /**
     * 解析参数类型
     * key -> 参数名称
     * value -> Class
     */
    private Map<String, Class<?>> paramsTypes = new HashMap<>();

    {
        paramsTypes.put("int", int.class);
        paramsTypes.put("double", double.class);
        paramsTypes.put("long", long.class);
    }

    /**
     * 线程池,处理 rpc request
     */
    private ExecutorService service = Executors.newCachedThreadPool();


    public RpcProviderProcessor(Serializer<RpcResponse, RpcRequest> serializer) {
        this.serializer = serializer;
    }

    @Override
    public void process(AioSession<byte[]> session, byte[] msg) {

        // msg -> rpcRequest
        RpcRequest request = serializer.deserialize(msg);
        log.debug("【Provider】收到rpc请求: rpcRequest={}", request);

        // 调用本地服务，返回结果
        RpcResponse response = this.callLocalInterfaceImpl(request);
        log.debug("【Provider】发送rpc响应: rpcResponse={}", response);
        // 将响应返回给 consumer
        try {
            session.write(this.serializer.serialize(response));
        } catch (IOException e) {
            log.warn("发送RPC响应失败");
            e.printStackTrace();
        }
    }

    @Override
    public void stateEvent(AioSession<byte[]> session, StateMachineEnum stateMachineEnum, Throwable throwable) {
    }

    /**
     * 通过反射调用本地服务
     *
     * @return
     */
    private RpcResponse callLocalInterfaceImpl(RpcRequest request) {
        RpcResponse response = new RpcResponse()
                .setUuid(request.getUuid());

        // 判断调用服务是否在可提供服务的 map 中
        Object service = implMap.get(request.getService());
        if (service == null) { // 服务不可用，直接返回
            log.warn("【Provider】没有提供这个服务,serverName={}", request.getService());
            response.setException(new RpcException("服务不可调用").toString());
            return response;
        }

        // 将参数类型字符串 -> 参数类型class
        Class<?>[] paramClasss = null;
        if (request.getParamsClassList() != null) {
            String[] paramsClassList = request.getParamsClassList();
            paramClasss = new Class[paramsClassList.length];
            for (int i = 0; i < paramsClassList.length; i++) {
                Class<?> clazz = paramsTypes.get(paramsClassList[i]);
                if (clazz != null) {
                    paramClasss[i] = clazz;
                } else {
                    try {
                        paramClasss[i] = Class.forName(paramsClassList[i]);
                    } catch (ClassNotFoundException e) {
                        log.warn("【Provider】参数类型找不到,className={}", paramsClassList[i]);
                        e.printStackTrace();
                        throw new UnsupportedOperationException("【Provider】参数类型找不到,className={}");
                    }
                }
            }
        }

        // 反射调用方法
        try {
            Method method = service.getClass().getMethod(request.getMethod(), paramClasss);
            Object returnObject = method.invoke(service, request.getParams());
            response.setReturnObject(returnObject).setReturnClass(method.getReturnType().getName());
        } catch (NoSuchMethodException e) {
            log.warn("【Provider】找不到这个方法,serviceName={},methodName={}",request.getService(),request.getMethod());
            response.setException(e.getMessage());
        } catch (IllegalAccessException | InvocationTargetException e) {
            log.warn("【Provider】执行方法失败,serviceName={},methodName={}",request.getService(),request.getMethod());
            response.setException(e.getMessage());
        }

        return response;
    }


    /**
     * 添加 rpc
     *
     * @param tClass
     * @param <T>
     */
    public <T> void addService(Class<T> tClass, T impl) {
        this.implMap.put(tClass.getName(), impl);
    }
}
