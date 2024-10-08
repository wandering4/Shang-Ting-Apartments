package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.exception.LeaseException;
import com.atguigu.lease.common.result.ResultCodeEnum;
import com.atguigu.lease.common.utils.JwtUtil;
import com.atguigu.lease.common.utils.VerifyCodeUtil;
import com.atguigu.lease.model.entity.SystemUser;
import com.atguigu.lease.model.entity.UserInfo;
import com.atguigu.lease.model.enums.BaseStatus;
import com.atguigu.lease.web.app.mapper.UserInfoMapper;
import com.atguigu.lease.web.app.service.LoginService;
import com.atguigu.lease.web.app.service.SmsService;
import com.atguigu.lease.web.app.vo.user.LoginVo;
import com.atguigu.lease.web.app.vo.user.UserInfoVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import org.apache.commons.codec.digest.DigestUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.concurrent.TimeUnit;

@Service
public class LoginServiceImpl implements LoginService {

    @Autowired
    private StringRedisTemplate redisTemplate;

    @Autowired
    private SmsService smsService;

    @Autowired
    private UserInfoMapper userInfoMapper;

    @Override
    public void getCode(String phone) {
        String code= VerifyCodeUtil.getVerifyCode(6);
        String key= RedisConstant.APP_LOGIN_PREFIX+phone;

        //限制频率
        Boolean hasKey = redisTemplate.hasKey(key);
        if(hasKey){
            Long expire = redisTemplate.getExpire(key, TimeUnit.SECONDS);
            if(RedisConstant.APP_LOGIN_CODE_TTL_SEC-expire<RedisConstant.APP_LOGIN_CODE_RESEND_TIME_SEC)
                throw new LeaseException(ResultCodeEnum.APP_SEND_SMS_TOO_OFTEN);
        }

        smsService.sendCode(phone,code);
        redisTemplate.opsForValue().set(key,code,RedisConstant.APP_LOGIN_CODE_TTL_SEC, TimeUnit.SECONDS);

    }

    @Override
    public String login(LoginVo loginVo) {
        if(loginVo.getPhone()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_PHONE_EMPTY);
        }

        if(loginVo.getCode()==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EMPTY);
        }

        String key= RedisConstant.APP_LOGIN_PREFIX+loginVo.getPhone();
/*        String code = redisTemplate.opsForValue().get(key);*/
        String code = "123456";

        if(code==null){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_EXPIRED);
        }

        if(!code.equals(loginVo.getCode())){
            throw new LeaseException(ResultCodeEnum.APP_LOGIN_CODE_ERROR);
        }

        LambdaQueryWrapper<UserInfo> wrapper = new LambdaQueryWrapper<UserInfo>().eq(UserInfo::getPhone, loginVo.getPhone());
        UserInfo userInfo = userInfoMapper.selectOne(wrapper);

        if(userInfo==null){
           //注册
            userInfo = new UserInfo();
            userInfo.setPhone(loginVo.getPhone());
            userInfo.setStatus(BaseStatus.ENABLE);
            userInfo.setNickname("用户"+loginVo.getPhone().substring(7));
            userInfoMapper.insert(userInfo);
        }else {
            //查看是否禁用
            if(userInfo.getStatus()==BaseStatus.DISABLE){
                throw new LeaseException(ResultCodeEnum.APP_ACCOUNT_DISABLED_ERROR);
            }
        }

        String activeKey= RedisConstant.DAILY_ACTIVE_PREFIX+ LocalDate.now();
        redisTemplate.opsForHyperLogLog().add(activeKey,loginVo.getPhone());

        return JwtUtil.createToken(userInfo.getId(),userInfo.getPhone());
    }

    @Override
    public UserInfoVo getUserInfoById(Long userId) {
        UserInfo userInfo = userInfoMapper.selectById(userId);
        return new UserInfoVo(userInfo.getNickname(), userInfo.getAvatarUrl());
    }
}
