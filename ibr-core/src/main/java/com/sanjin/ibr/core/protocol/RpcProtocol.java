package com.sanjin.ibr.core.protocol;


import lombok.extern.slf4j.Slf4j;
import org.smartboot.socket.Protocol;
import org.smartboot.socket.extension.decoder.FixedLengthFrameDecoder;
import org.smartboot.socket.transport.AioSession;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;

/**
 * @description: RPC 编解码，使用 length+data 协议
 * @author: sanjin
 * @date: 2019/7/19 16:13
 */
@Slf4j
public class RpcProtocol implements Protocol<byte[]> {

    @Override
    public byte[] decode(ByteBuffer readBuffer, AioSession<byte[]> session, boolean eof) {
        if (!readBuffer.hasRemaining()) {
            return null;
        }
        /**
         * 当出现接受的数据过大 readBuffer 无法一次获取完整的包,
         * 使用 fixedLengthFrameDecoder(临时缓冲区) 存储上一个包的数据,
         * 通过 AioSession 传递给下一次解码
         */
        FixedLengthFrameDecoder fixedLengthFrameDecoder;
        if (session.getAttachment() != null) {
            fixedLengthFrameDecoder = session.getAttachment();
        } else {
            // 获取消息体长度
            int length = readBuffer.getInt();

            // 构建零时缓冲区
            fixedLengthFrameDecoder = new FixedLengthFrameDecoder(length);
            // 放入 session 中,用于下次解码
            session.setAttachment(fixedLengthFrameDecoder);
        }

        // 验证是否获取到完整的数据
        if (!fixedLengthFrameDecoder.decode(readBuffer)) {
            // 已读取的数据总和不等于 length,返回 null
            return null;
        }

        // 获取到完整数据，进行解码
        ByteBuffer fullBuffer = fixedLengthFrameDecoder.getBuffer();
        byte[] bytes = new byte[fullBuffer.remaining()];
        fullBuffer.get(bytes);

        // 释放临时缓冲区,
        session.setAttachment(null);
        return bytes;
    }

    @Override
    public ByteBuffer encode(byte[] msg, AioSession<byte[]> session) {

        // +Integer.BYTES 是因为消息头占用了 4 个字节
        Integer msgHead = msg.length;
        ByteBuffer buffer = ByteBuffer.allocate(msg.length + Integer.BYTES);

        // put 消息头
        buffer.put(integerToBytes(msgHead));
        // put 消息体
        buffer.put(msg);
        buffer.flip();
        return buffer;
    }

    /**
     * 根据电脑大小端  将 int 转换为 byte[4]
     * 小端模式(高位存放在低地址)
     * @param val
     * @return
     */
    private static byte[] integerToBytes(int val) {
        byte[] bytes = new byte[4];
        boolean isBigEndian = true;
        ByteOrder byteOrder = ByteOrder.nativeOrder();
        if (ByteOrder.LITTLE_ENDIAN.equals(byteOrder)) {
            // 小端
            bytes[3] = (byte) val;
            bytes[2] = (byte) (val >> 8);
            bytes[1] = (byte) (val >> 16);
            bytes[0] = (byte) (val >> 24);
        } else if (ByteOrder.BIG_ENDIAN.equals(ByteOrder.LITTLE_ENDIAN)) {
            // 大端
            bytes[0] = (byte) val;
            bytes[1] = (byte) (val >> 8);
            bytes[2] = (byte) (val >> 16);
            bytes[3] = (byte) (val >> 24);
        } else {
            throw new RuntimeException("识别不出大小端模式");
        }
        return bytes;
    }
}
