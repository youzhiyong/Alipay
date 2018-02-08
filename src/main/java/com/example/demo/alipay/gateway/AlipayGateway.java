package com.example.demo.alipay.gateway;

import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.DefaultAlipayClient;
import com.alipay.api.domain.AlipayTradePagePayModel;
import com.alipay.api.domain.AlipayTradeRefundModel;
import com.alipay.api.domain.AlipayTradeWapPayModel;
import com.alipay.api.internal.util.AlipaySignature;
import com.alipay.api.internal.util.AlipayUtils;
import com.alipay.api.request.AlipayTradePagePayRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.request.AlipayTradeWapPayRequest;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.example.demo.alipay.config.AlipayConfig;
import com.example.demo.alipay.entity.AlipayRecord;
import com.example.demo.alipay.model.RefundApply;
import com.example.demo.alipay.model.Transaction;
import com.example.demo.alipay.repository.AlipayRecordRepository;
import com.example.demo.alipay.service.PaymentService;
import com.example.demo.alipay.utils.StringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Map;

/**
 * Created by youzhiyong on 2018/1/26.
 */
@Component
public class AlipayGateway {

    private AlipayClient alipayClient;

    @Autowired
    private AlipayConfig alipayConfig;

    @Autowired
    private AlipayRecordRepository alipayRecordRepository;

    @Autowired
    private PaymentService paymentService;

    @PostConstruct
    public void initAlipay() {
        //初始化 AlipayClient
        alipayClient = new DefaultAlipayClient(alipayConfig.getGatewayUrl(), alipayConfig.getAppId(), alipayConfig.getMerchantPrivateKey(), "json", AlipayConfig.CHARSET, alipayConfig.getAlipayPublicKey(), AlipayConfig.SIGN_TYPE);
    }

    /**
     *  创建PC端支付请求
     * @param transaction  支付信息
     * @return  返回封装请求的form
     * @throws AlipayApiException
     */
    public String createPcPay(Transaction transaction) throws AlipayApiException {
        //设置请求参数
        AlipayTradePagePayRequest alipayRequest = new AlipayTradePagePayRequest();
        alipayRequest.setReturnUrl(transaction.getReturnUrl());    //回调地址
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());    //通知地址

        // 封装支付请求信息
        AlipayTradePagePayModel pagePayModel = new AlipayTradePagePayModel();
        pagePayModel.setTotalAmount(transaction.getAmount().toString());
        pagePayModel.setOutTradeNo(transaction.getOrderNo());
        pagePayModel.setSubject("test-subject");
        pagePayModel.setBody("test-body");
        pagePayModel.setProductCode("FAST_INSTANT_TRADE_PAY");

        alipayRequest.setBizModel(pagePayModel);

        //请求
        return alipayClient.pageExecute(alipayRequest).getBody();
    }

    /**
     * 创建wap端支付请求
     * @param transaction  支付信息
     * @return  返回封装请求的form
     * @throws AlipayApiException
     */
    public String createWapPay(Transaction transaction) throws AlipayApiException {
        AlipayTradeWapPayRequest alipayRequest = new AlipayTradeWapPayRequest();
        alipayRequest.setReturnUrl(transaction.getReturnUrl());    //回调地址
        alipayRequest.setNotifyUrl(alipayConfig.getNotifyUrl());    //通知地址

        // 封装支付请求信息
        AlipayTradeWapPayModel model=new AlipayTradeWapPayModel();
        model.setTotalAmount(transaction.getAmount().toString());
        model.setOutTradeNo(transaction.getOrderNo());
        model.setSubject("test-subject");
        model.setBody("test-body");
        model.setProductCode("QUICK_WAP_PAY");

        alipayRequest.setBizModel(model);

        //请求
        return alipayClient.pageExecute(alipayRequest).getBody();
    }

    /**
     *
     * @param apply
     * @return 退款是否成功
     * @throws AlipayApiException
     */
    public boolean createRefund(RefundApply apply) throws AlipayApiException {

        AlipayTradeRefundRequest refundRequest = new AlipayTradeRefundRequest();

        // 封装请支付求信息
        AlipayTradeRefundModel model=new AlipayTradeRefundModel();
        model.setTradeNo(apply.getTradeNo()); //支付宝交易号
        model.setOutTradeNo(apply.getOrderNo());  //平台交易号
        model.setRefundAmount(apply.getRefundAmount().toString());  //退款金额
        model.setRefundReason(apply.getRefundReason());  //退款原因
        model.setOutRequestNo(apply.getRefundId());  //退款ID，用于唯一标识这笔退款，退全款可不需要，部分退款则必传

        refundRequest.setBizModel(model);

        AlipayTradeRefundResponse refundResponse = alipayClient.execute(refundRequest);
        return handleRefund(refundResponse);
    }

    /**
     * 验证并处理支付完成的回调  notify     对于异步返回的信息，我们需要手动验证信息的真实性
     * @param params 返回的参数
     * @return
     */
    public void handleNotify(Map<String, String> params) {
        String orderNo = params.get("out_trade_no");   //支付订单号  平台订单号
        if (StringUtil.isEmpty(orderNo)) return;
        String tradeNo = params.get("trade_no");       //支付交易号   支付宝支付订单号
        if (StringUtil.isEmpty(tradeNo)) return;
        String status = params.get("trade_status");   //支付状态
        if (StringUtil.isEmpty(status)) return;

        //验证签名信息是否合法
        boolean isValid = checkSignature(params);

        if (isValid && "TRADE_SUCCESS".equals(status)) {
            trySavePaymentAlipay(tradeNo, orderNo);
        }
    }

    /**
     * 处理退款申请的返回, 对于同步返回的数据，Alipay的SDK已经自动进行验证
     * @param refundResponse
     * @return
     */
    private boolean handleRefund(AlipayTradeRefundResponse refundResponse) {
        if (refundResponse.isSuccess()) {
            //todo save refund record to db
            return true;
        }
        return false;
    }

    /**
     * 验证回调信息是否合法
     * @param params 回调参数
     * @return 信息是否合法
     */
    private boolean checkSignature(Map params) {
        boolean signVerified = false; //调用SDK验证签名
        try {
            signVerified = AlipaySignature.rsaCheckV1(params, alipayConfig.getAlipayPublicKey(), AlipayConfig.CHARSET, AlipayConfig.SIGN_TYPE);
        } catch (AlipayApiException e) {
            return false;
        }
        return signVerified;
    }

    /**
     * 记录支付信息    使用同步方法来防止多次回调产生重复记录
     * @param tradeNo  支付宝交易号
     * @param orderNo  平台订单号
     */
    private synchronized void trySavePaymentAlipay(String tradeNo, String orderNo) {
        AlipayRecord alipayRecord = alipayRecordRepository.findByOrderNo(orderNo);
        if (alipayRecord == null) {
            alipayRecord = new AlipayRecord();
            alipayRecord.setOrderNo(orderNo);
            alipayRecord.setTradeNo(tradeNo);
            alipayRecordRepository.save(alipayRecord);
            paymentService.finishOrder(orderNo);
        }
    }
}
