package com.sanjin.ibr.core.message;

import lombok.Data;
import lombok.experimental.Accessors;

import java.io.Serializable;

/**
 * @description: Provider 回复给 RpcConsumer 的消息对象
 * @author: sanjin
 * @date: 2019/7/19 20:40
 */
@Data
@Accessors(chain = true)
public class RpcResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 消息唯一标识,与对应 RpcConsumer 发送的 {@link RpcRequest#getUuid() uuid } 相同
     */
    private String uuid;

    /**
     * 返回对象
     */
    private Object returnObject;

    /**
     * 返回对象类型
     */
    private String returnClass;

    /**
     * 异常
     */
    private String exception;

}
