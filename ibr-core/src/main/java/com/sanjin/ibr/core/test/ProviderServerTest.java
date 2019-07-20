package com.sanjin.ibr.core.test;

import com.sanjin.ibr.core.message.RpcRequest;
import com.sanjin.ibr.core.message.RpcResponse;
import com.sanjin.ibr.core.protocol.RpcProtocol;
import com.sanjin.ibr.core.rpc.RpcProviderProcessor;
import com.sanjin.ibr.core.serializers.DefaultSerializer;
import com.sanjin.ibr.core.serializers.Serializer;
import org.smartboot.socket.transport.AioQuickServer;

import java.io.IOException;

/**
 * @description: 服务提供者
 * @author: sanjin
 * @date: 2019/7/20 12:17
 */
public class ProviderServerTest {
    public static void main(String[] args) throws IOException {
        Serializer<RpcResponse, RpcRequest> serializer = new DefaultSerializer<>();
        RpcProviderProcessor providerProcessor = new RpcProviderProcessor(serializer);
        AioQuickServer<byte[]> providerServer =
                new AioQuickServer<>(
                        10086, new RpcProtocol(),
                        providerProcessor
                );
        providerServer.setBannerEnabled(false);
        providerServer.start();

        providerProcessor.addService(DemoApi.class,new DemoApiImpl());
    }
}
