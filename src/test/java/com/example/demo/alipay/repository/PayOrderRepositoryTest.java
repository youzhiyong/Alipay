package com.example.demo.alipay.repository;

import com.example.demo.alipay.entity.PayOrder;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.Date;

/**
 * Created by youzhiyong on 2018/1/26.
 */
@SpringBootTest
@RunWith(SpringRunner.class)
public class PayOrderRepositoryTest {

    @Autowired
    private PayOrderRepository repository;

    @Test
    public void createOrder() {

        String orderNo = "no201801291119001";

        PayOrder order = new PayOrder();
        order.setStatus(0);
        order.setAmount(100.00);
        Date now = new Date();
        Date expire = new Date(now.getTime() + 100000000);
        order.setExpiretime(expire);
        order.setGmtCreated(now);
        order.setGmtModified(now);
        order.setUserId(1L);
        order.setOrderNo(orderNo);
        order.setIsDeleted(false);

        repository.saveAndFlush(order);
    }

    @Test
    public void findByOrderNo() {

        String orderNo = "no201801261740004";

        PayOrder newOrder = repository.findByOrderNo(orderNo);

        assert newOrder != null : "Error";
    }


}
