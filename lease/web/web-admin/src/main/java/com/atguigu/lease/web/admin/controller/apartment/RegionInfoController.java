package com.atguigu.lease.web.admin.controller.apartment;


import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.common.result.Result;
import com.atguigu.lease.model.entity.CityInfo;
import com.atguigu.lease.model.entity.DistrictInfo;
import com.atguigu.lease.model.entity.ProvinceInfo;
import com.atguigu.lease.web.admin.service.CityInfoService;
import com.atguigu.lease.web.admin.service.DistrictInfoService;
import com.atguigu.lease.web.admin.service.ProvinceInfoService;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.apache.ibatis.javassist.compiler.ast.Variable;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "地区信息管理")
@RestController
@RequestMapping("/admin/region")
public class RegionInfoController {

    @Autowired
    private ProvinceInfoService provinceInfoService;

    @Autowired
    private CityInfoService cityInfoService;

    @Autowired
    private DistrictInfoService districtInfoService;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;


    @Operation(summary = "查询省份信息列表")
    @GetMapping("province/list")
    public Result<List<ProvinceInfo>> listProvince() {
        List<ProvinceInfo> list = (List<ProvinceInfo>) (List<?>) redisTemplate.opsForList().range(RedisConstant.PROVINCE_PREFIX, 0, -1);
        if(list == null||list.isEmpty()){
            list = provinceInfoService.list();
            for (ProvinceInfo provinceInfo : list) {
                redisTemplate.opsForList().rightPush(RedisConstant.PROVINCE_PREFIX, provinceInfo);
            }
        }
        return Result.ok(list);
    }

    @Operation(summary = "根据省份id查询城市信息列表")
    @GetMapping("city/listByProvinceId")
    public Result<List<CityInfo>> listCityInfoByProvinceId(@RequestParam Long id) {
        String key=RedisConstant.CITY_PREFIX+id;
        List<CityInfo> list=(List<CityInfo>)(List<?>)redisTemplate.opsForList().range(key,0,-1);
        if (list == null||list.isEmpty()) {
            LambdaQueryWrapper<CityInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(CityInfo::getProvinceId, id);
            list = cityInfoService.list(queryWrapper);
            for (CityInfo cityInfo : list) {
                redisTemplate.opsForList().rightPush(key,cityInfo);
            }
        }
        return Result.ok(list);
    }

    @GetMapping("district/listByCityId")
    @Operation(summary = "根据城市id查询区县信息")
    public Result<List<DistrictInfo>> listDistrictInfoByCityId(@RequestParam Long id) {
        String key=RedisConstant.DISTRICT_PREFIX+id;
        List<DistrictInfo> list=(List<DistrictInfo>) (List<?>) redisTemplate.opsForList().range(key, 0, -1);
        if (list == null||list.isEmpty()) {
            LambdaQueryWrapper<DistrictInfo> queryWrapper = new LambdaQueryWrapper<>();
            queryWrapper.eq(DistrictInfo::getCityId,id);
            list = districtInfoService.list(queryWrapper);
            for (DistrictInfo districtInfo : list) {
                redisTemplate.opsForList().rightPush(key,districtInfo);
            }
        }
        return Result.ok(list);
    }

}
