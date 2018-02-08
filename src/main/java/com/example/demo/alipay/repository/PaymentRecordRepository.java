package com.example.demo.alipay.repository;

import com.example.demo.alipay.entity.PaymentRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public interface PaymentRecordRepository extends JpaRepository<PaymentRecord, Long> {

    PaymentRecord findByTransactionId(String transactionId);

}
