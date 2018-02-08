package com.example.demo.alipay.exception;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public class ServiceException extends RuntimeException {

    private int code;

    public ServiceException() {
    }

    public ServiceException(int code) {
        this.code = code;
    }

    public ServiceException(int code, String message) {
        super(message);
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }
}
