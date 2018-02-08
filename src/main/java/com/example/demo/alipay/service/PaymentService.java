package com.example.demo.alipay.service;

import com.example.demo.alipay.model.Transaction;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public interface PaymentService {

    /**
     * 初始化支付参数
     *
     * @param orderNo   订单号
     * @param returnUrl 返回地址
     * @return 支付参数
     */
    Transaction initPay(String orderNo, String returnUrl);

    /**
     * 完成支付订单
     *
     * @param orderNo   订单号
     */
    void finishOrder(String orderNo);

    /**
     * 再次获取支付参数
     *
     * @param transactionId 支付记录ID
     * @return
     */
    Transaction retrieveTransaction(String transactionId);

}
