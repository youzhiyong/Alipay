package com.example.demo.alipay.service;

import com.example.demo.alipay.entity.PayOrder;

/**
 * Created by youzhiyong on 2018/1/29.
 */
public interface PayOrderService {

    /**
     * 生成订单信息
     * @param payOrder
     * @return
     */
    PayOrder createOrder(PayOrder payOrder);

}
