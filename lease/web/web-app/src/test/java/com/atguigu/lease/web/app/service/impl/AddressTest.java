package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.redis.RedisConfiguration;
import jakarta.annotation.Resource;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.geo.Point;
import org.springframework.data.redis.core.RedisTemplate;

import java.util.Set;

@SpringBootTest
public class AddressTest {
    @Resource
    private RedisTemplate<String,Object> redisTemplate;
    @Autowired
    private RedisConfiguration redisConfiguration;

    @Test
    public void test() {
        redisTemplate.opsForGeo().add(RedisConstant.APARTMENT_GEO_PREFIX,new Point(120.1234,30.5678),1l);
        Set<Object> range = redisTemplate.opsForZSet().range(RedisConstant.APARTMENT_GEO_PREFIX, 0, -1);
        range.forEach(System.out::println);
    }
}
