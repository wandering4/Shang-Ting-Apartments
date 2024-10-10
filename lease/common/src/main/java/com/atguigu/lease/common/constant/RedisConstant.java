package com.atguigu.lease.common.constant;

public class RedisConstant {
    public static final String ADMIN_LOGIN_PREFIX = "admin:login:";
    public static final Integer ADMIN_LOGIN_CAPTCHA_TTL_SEC = 60;
    public static final String DAILY_ACTIVE_PREFIX = "dau:";
    public static final String APP_LOGIN_PREFIX = "app:login:";
    public static final Integer APP_LOGIN_CODE_RESEND_TIME_SEC = 60;
    public static final Integer APP_LOGIN_CODE_TTL_SEC = 60 * 10;
    public static final String APP_ROOM_PREFIX = "app:room:";
    public static final String ADMIN_ROOM_PREFIX = "admin:room:";
    public static final String APP_ROOM_INFO_PREFIX = "app:roomInfo:";
    public static final String APP_APARTMENT_PREFIX = "app:apartment:";
    public static final String APP_GRAPHVOLIST_PREFIX = "app:graphVoList:";
    public static final String APP_LABELINFO_PREFIX = "app:labelInfo:";
    public static final String PROVINCE_PREFIX = "province:";
    public static final String CITY_PREFIX = "city:";
    public static final String DISTRICT_PREFIX = "district:";
    public static final int offSet= 24 * 60 * 60 ;

}
