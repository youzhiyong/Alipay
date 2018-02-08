package com.example.demo.alipay.service.impl;

import com.example.demo.alipay.entity.PayOrder;
import com.example.demo.alipay.repository.PayOrderRepository;
import com.example.demo.alipay.service.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Random;

/**
 * Created by youzhiyong on 2018/1/29.
 */
@Service
public class PayOrderServiceImpl implements PayOrderService {

    @Autowired
    PayOrderRepository repository;

    @Override
    public PayOrder createOrder(PayOrder payOrder) {
        payOrder.setStatus(0);
        Date now = new Date();
        Date expire = new Date(now.getTime() + 15 * 60 * 1000);
        payOrder.setExpiretime(expire);
        payOrder.setGmtCreated(now);
        payOrder.setGmtModified(now);
        payOrder.setOrderNo(orderNoGenerator());
        payOrder.setIsDeleted(false);
        return repository.saveAndFlush(payOrder);
    }

    private String orderNoGenerator() {
        return new SimpleDateFormat("yyyyMMddHHmmss").format(new Date()) + (new Random().nextInt(9000) + 1000);
    }

}
