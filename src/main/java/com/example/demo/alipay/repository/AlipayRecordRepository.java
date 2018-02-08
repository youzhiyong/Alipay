package com.example.demo.alipay.repository;

import com.example.demo.alipay.entity.AlipayRecord;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Created by youzhiyong on 2018/1/26.
 */
public interface AlipayRecordRepository extends JpaRepository<AlipayRecord, Long> {

    AlipayRecord findByOrderNo(String orderNo);

}
