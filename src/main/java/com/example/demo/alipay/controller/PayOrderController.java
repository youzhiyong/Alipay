package com.example.demo.alipay.controller;

import com.example.demo.alipay.entity.PayOrder;
import com.example.demo.alipay.service.PayOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PostMapping;

/**
 * Created by youzhiyong on 2018/1/29.
 */
@Controller
public class PayOrderController {

    @Autowired
    PayOrderService payOrderService;

    @PostMapping("/payOrders")
    public String  createOrder(PayOrder payOrder, Model model) {
        payOrder.setUserId(1L);
        payOrder = payOrderService.createOrder(payOrder);
        model.addAttribute("orderNo", payOrder.getOrderNo());
        model.addAttribute("amount", payOrder.getAmount());
        return "payment";
    }

}
