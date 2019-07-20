package com.sanjin.ibr.core.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;
import java.util.UUID;

/**
 * @description: Cousumer 发送给 Provider 的消息对象
 * @author: sanjin
 * @date: 2019/7/19 20:33
 */
@Data
@Accessors(chain = true)
public class RpcRequest implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息的唯一id
     *
     * 使用 {@link UUID#randomUUID()} 生成
     */
    private String uuid;

    /**
     * 调用的服务
     */
    private String service;

    /**
     * 调用的方法
     */
    private String method;

    /**
     * 参数类型字符串
     */
    private String[] paramsClassList;

    /**
     * 入参
     */
    private Object[] params;
}
