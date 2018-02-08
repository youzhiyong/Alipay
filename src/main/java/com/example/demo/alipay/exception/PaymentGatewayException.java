package com.example.demo.alipay.exception;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public class PaymentGatewayException extends ServiceException {

    /**
     * 支付请求失败
     */
    public static final int CREATE_PAYMENT_ERROR = 2001;
    public static final String CREATE_PAYMENT_ERROR_MSG = "支付创建失败";

    public PaymentGatewayException(int code, String message) {
        super(code, message);
    }

}
