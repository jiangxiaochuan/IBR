package com.sanjin.ibr.core.exception;

/**
 * @description:
 * @author: sanjin
 * @date: 2019/7/20 10:10
 */
public class RpcException extends RuntimeException {
    public RpcException(String msg) {
        super(msg);
    }
}
