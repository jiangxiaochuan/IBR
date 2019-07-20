package com.sanjin.ibr.core.test;

import com.sanjin.ibr.core.message.RpcRequest;
import com.sanjin.ibr.core.message.RpcResponse;
import com.sanjin.ibr.core.protocol.RpcProtocol;
import com.sanjin.ibr.core.rpc.RpcConsumerProcessor;
import com.sanjin.ibr.core.serializers.DefaultSerializer;
import org.smartboot.socket.transport.AioQuickClient;

import java.io.IOException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * @description: 消费者
 * @author: sanjin
 * @date: 2019/7/20 12:16
 */
public class ConsumerServerTest {
    public static void main(String[] args) throws InterruptedException, ExecutionException, IOException {

        String host = "localhost";
        int port = 10086;
        RpcConsumerProcessor consumerProcessor = new RpcConsumerProcessor(new DefaultSerializer<RpcRequest, RpcResponse>());

        AioQuickClient<byte[]> consumerServer = new AioQuickClient<>(
                host,
                port,
                new RpcProtocol(),
                consumerProcessor
        );
        consumerServer.start();

        // 调用远程服务

        // 1. 获取远程代理对象
        DemoApi serviceProxyInstance = consumerProcessor.getServiceProxyInstance(DemoApi.class);


        ExecutorService service = Executors.newCachedThreadPool();

        service.execute(() -> {
            System.out.println(serviceProxyInstance.test("ice blue rpc test1"));
        });


        service.execute(() -> {
            System.out.println(serviceProxyInstance.test("ice blue rpc test2"));;
        });


        service.execute(() -> {
            System.out.println(serviceProxyInstance.sum(1,2));;
        });
    }
}
