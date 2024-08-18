package com.atguigu.lease.web.app.service.impl;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SmsServiceImplTest {

    @Autowired
    private SmsServiceImpl smsService;

    @Test
    void sendSms() {
        smsService.sendCode("17891997260","1234");
    }

}