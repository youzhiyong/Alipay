package com.example.demo.alipay.controller;

import com.alipay.api.AlipayApiException;
import com.example.demo.alipay.exception.PaymentGatewayException;
import com.example.demo.alipay.gateway.AlipayGateway;
import com.example.demo.alipay.model.RefundApply;
import com.example.demo.alipay.model.Transaction;
import com.example.demo.alipay.service.PaymentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by youzhiyong on 2018/1/26.
 */
@Controller
public class AlipayController {

    @Autowired
    private AlipayGateway alipayGateway;

    @Autowired
    private PaymentService paymentService;

    @GetMapping("/alipay/{orderNo}")
    public String payRequest(@PathVariable("orderNo") String orderNo,
                             @RequestParam(value = "from", defaultValue = "pc") String from,
                             @RequestParam("return_url") String returnUrl,
                             Model model) {

        Transaction transaction = paymentService.initPay(orderNo, returnUrl);
        String modelContent = "";
        if (transaction.getAmount() < 0.01) {  //支付金额为 0 时，直接支付成功
            paymentService.finishOrder(transaction.getOrderNo());
            return "redirect:" + returnUrl;
        }
        try {
            if (from.toLowerCase().equals("wap")) {
                modelContent = alipayGateway.createWapPay(transaction);
            } else {
                modelContent = alipayGateway.createPcPay(transaction);
            }
        } catch (AlipayApiException e) {
            e.printStackTrace();
            throw new PaymentGatewayException(PaymentGatewayException.CREATE_PAYMENT_ERROR, PaymentGatewayException.CREATE_PAYMENT_ERROR_MSG);
        }
        model.addAttribute("content", modelContent);
        return "to_alipay";
    }

    /**
     *
     * @param request
     */
    @PostMapping("/alipay/notify")
    @ResponseBody
    public String handleNotify(HttpServletRequest request) {
        Map<String, String> params = getAlipayRequestMap(request);
        alipayGateway.handleNotify(params);
        return "success";   //注意，如果这里不返回success时，支付宝会发送多次通知
    }

    @GetMapping("/alipay/refund/{orderNo}")
    @ResponseBody
    public String refund(@PathVariable("orderNo") String orderNo) {
        RefundApply apply = new RefundApply();
        apply.setOrderNo("201802011134488134");
        apply.setTradeNo("2018020121001004330200415529");
        apply.setRefundAmount(500.0);
        apply.setRefundId("Re" + "2018020121001004330200415529");
        apply.setRefundReason("Refund Test");
        boolean isSuccess = false;
        try {
            isSuccess = alipayGateway.createRefund(apply);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        return isSuccess ? "退款成功" : "退款失败";
    }

    /**
     * 解析支付完成后的返回参数
     * @param request
     * @return
     */
    private Map<String, String> getAlipayRequestMap(HttpServletRequest request) {
        //获取支付宝的反馈信息
        Map<String,String> params = new HashMap<>();
        Map<String,String[]> requestParams = request.getParameterMap();
        for (String key : requestParams.keySet()) {
            String[] values = requestParams.get(key);
            String valueStr = "";
            for (int i = 0; i < values.length; i++) {
                valueStr = (i == values.length - 1) ? valueStr + values[i]
                        : valueStr + values[i] + ",";
            }
            /*//乱码解决
            try {
                valueStr = new String(valueStr.getBytes("ISO-8859-1"), "utf-8");
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
                continue;
            }*/
            params.put(key, valueStr);
        }

        return params;
    }
}
