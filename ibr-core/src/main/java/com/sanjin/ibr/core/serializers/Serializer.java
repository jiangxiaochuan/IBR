package com.sanjin.ibr.core.serializers;

import com.sanjin.ibr.core.exception.SerializerException;

/**
 * @description:
 * @author: sanjin
 * @date: 2019/7/19 20:46
 */
public interface Serializer<T,E> {


    /**
     * 将对象 o 序列化为 byte[]
     * @param o 待序列化对象
     * @return byte[] 数组 if success ,or null if failed.
     */
    byte[] serialize(T o);

    /**
     * 将字节数组反序列为对象
     * @param bytes
     * @return T object if success,or null if failed.
     */
    E deserialize(byte[] bytes);

}
