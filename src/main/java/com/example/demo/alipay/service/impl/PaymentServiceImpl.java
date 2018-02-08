package com.example.demo.alipay.service.impl;

import com.example.demo.alipay.entity.PayOrder;
import com.example.demo.alipay.entity.PaymentRecord;
import com.example.demo.alipay.exception.OrderException;
import com.example.demo.alipay.model.Transaction;
import com.example.demo.alipay.repository.PayOrderRepository;
import com.example.demo.alipay.repository.PaymentRecordRepository;
import com.example.demo.alipay.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Date;
import java.util.UUID;

/**
 * Created by youzhiyong on 2018/1/26.
 */
@Service
public class PaymentServiceImpl implements PaymentService {

    @Autowired
    private PayOrderRepository payOrderRepository;

    @Autowired
    private PaymentRecordRepository paymentRecordRepository;

    @Override
    public Transaction initPay(String orderNo, String returnUrl) {
        PayOrder order = payOrderRepository.findByOrderNo(orderNo);
        if (order == null) {
            throw new OrderException(OrderException.ORDER_NOT_FOUND, OrderException.ORDER_NOT_FOUND_MSG);
        }
        if (order.getRemainSeconds() < 0) {
            throw new OrderException(OrderException.INVALID_ORDER, OrderException.INVALID_ORDER_MSG);
        }
        if (order.getStatus() == PayOrder.STATUS_PAID) {
            throw new OrderException(OrderException.REPEAT_PAYMENT, OrderException.REPEAT_PAYMENT_MSG);
        }
        PaymentRecord paymentRecord = createPaymentRecord(order, returnUrl);
        return createTransactionFromPaymentRecord(paymentRecord);
    }

    @Override
    public void finishOrder(String orderNo) {
        PayOrder payOrder = payOrderRepository.findByOrderNo(orderNo);
        payOrder.setStatus(PayOrder.STATUS_PAID);
        payOrder.setPaytime(new Date());
        payOrderRepository.save(payOrder);
    }

    @Override
    public Transaction retrieveTransaction(String transactionId) {
        PaymentRecord paymentRecord = paymentRecordRepository.findByTransactionId(transactionId);
        return createTransactionFromPaymentRecord(paymentRecord);
    }

    private PaymentRecord createPaymentRecord(PayOrder order, String returnUrl) {
        PaymentRecord paymentRecord = new PaymentRecord();
        Date now = new Date();
        paymentRecord.setAmount(order.getAmount());
        paymentRecord.setOrderNo(order.getOrderNo());
        paymentRecord.setReturnUrl(returnUrl);
        paymentRecord.setTransactionId(generateTrackId());
        paymentRecord.setGmtCreated(now);
        paymentRecord.setGmtModified(now);
        paymentRecordRepository.save(paymentRecord);
        return paymentRecord;
    }

    private String generateTrackId() {
        String uuid = UUID.randomUUID().toString();
        return uuid.replaceAll("-", "");
    }


    private Transaction createTransactionFromPaymentRecord(PaymentRecord paymentRecord) {
        Transaction transaction= new Transaction();
        transaction.setOrderNo(paymentRecord.getOrderNo());
        transaction.setTransactionId(paymentRecord.getTransactionId());
        transaction.setReturnUrl(paymentRecord.getReturnUrl());
        transaction.setAmount(paymentRecord.getAmount());
        return transaction;
    }
}
