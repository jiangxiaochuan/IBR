package com.sanjin.ibr.core.serializers;

import lombok.extern.slf4j.Slf4j;

import java.io.*;

/**
 * @description: 使用 jdk 默认序列化方式进行 serialize/deserialize
 * @author: sanjin
 * @date: 2019/7/19 20:59
 */
@Slf4j
@SuppressWarnings("unchecked")
public class DefaultSerializer<T,E> implements Serializer<T,E> {

    @Override
    public byte[] serialize(T o) {
        try(
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ObjectOutputStream oos = new ObjectOutputStream(baos);
                )
        {

            oos.writeObject(o);
            oos.flush();

            return baos.toByteArray();
        } catch (IOException e) {
            log.warn("序列化对象失败: object={}", o);
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public E deserialize(byte[] bytes) {
        try (
                ByteArrayInputStream bais = new ByteArrayInputStream(bytes);
                ObjectInputStream ois = new ObjectInputStream(bais);
                ) {

            return (E) ois.readObject();
        } catch (IOException e) {
            log.warn("反序列化对象失败");
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return null;
    }
}
