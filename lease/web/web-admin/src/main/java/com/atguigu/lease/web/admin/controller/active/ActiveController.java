package com.atguigu.lease.web.admin.controller.active;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.common.result.ResultCodeEnum;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.annotation.Resource;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/admin/active")
public class ActiveController {

    @Resource
    private StringRedisTemplate stringRedisTemplate;

    @Operation(summary = "获取日常活跃人数")
    @GetMapping("/daily/numbers")
    public Result<Long> daily(@RequestParam(required = false) String date) {
        LocalDate queryDate ;

        try {
            // 如果传入了日期，则解析该日期；否则使用当前日期
            queryDate = (date != null) ? LocalDate.parse(date) : LocalDate.now();
        } catch (DateTimeParseException e) {
            // 处理日期格式不正确的情况
            return  Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(),"无效的日期格式，应为YYYY-MM-DD");
        }
        String key= RedisConstant.DAILY_ACTIVE_PREFIX+queryDate;
        Long size = stringRedisTemplate.opsForHyperLogLog().size(key);
        return Result.ok(size==null?0:size);
    }

    @Operation(summary = "获取月常活跃人数")
    @GetMapping("/monthly/numbers")
    public Result<List<Long>> monthly(@RequestParam(required = false) String month) {
        LocalDate queryMonth;

        try {
            // 如果传入了月份，则解析该月份；否则使用当前月份
            queryMonth = (month != null) ? LocalDate.parse(month + "-01") : LocalDate.now().withDayOfMonth(1);
        } catch (DateTimeParseException e) {
            return Result.fail(ResultCodeEnum.PARAM_ERROR.getCode(),"无效的日期格式，应为YYYY-MM");
        }

        List<Long> dailyActiveCounts = new ArrayList<>();

        // 获取该月份的天数
        int daysInMonth = queryMonth.lengthOfMonth();

        // 遍历该月的每一天，获取日活跃人数
        for (int day = 1; day <= daysInMonth; day++) {
            LocalDate date = queryMonth.withDayOfMonth(day);
            String key = RedisConstant.DAILY_ACTIVE_PREFIX + date;
            Long size = stringRedisTemplate.opsForHyperLogLog().size(key);
            dailyActiveCounts.add(size==null?0:size);
        }

        return Result.ok(dailyActiveCounts);

    }

}
