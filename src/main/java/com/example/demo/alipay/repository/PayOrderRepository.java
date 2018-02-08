package com.example.demo.alipay.repository;

import com.example.demo.alipay.entity.PayOrder;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public interface PayOrderRepository extends JpaRepository<PayOrder, Long> {

    PayOrder findByOrderNo(String orderNo);

}
