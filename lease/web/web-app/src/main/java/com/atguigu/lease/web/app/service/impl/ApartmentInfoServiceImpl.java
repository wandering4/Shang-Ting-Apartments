package com.atguigu.lease.web.app.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.ApartmentInfo;
import com.atguigu.lease.model.entity.FacilityInfo;
import com.atguigu.lease.model.entity.LabelInfo;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.app.mapper.*;
import com.atguigu.lease.web.app.service.ApartmentInfoService;
import com.atguigu.lease.web.app.vo.apartment.ApartmentDetailVo;
import com.atguigu.lease.web.app.vo.apartment.ApartmentItemVo;
import com.atguigu.lease.web.app.vo.graph.GraphVo;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.data.geo.*;
import org.springframework.data.redis.connection.RedisGeoCommands;
import org.springframework.data.redis.core.GeoOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.domain.geo.GeoReference;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * @author liubo
 * @description 针对表【apartment_info(公寓信息表)】的数据库操作Service实现
 * @createDate 2023-07-26 11:12:39
 */
@Service
public class ApartmentInfoServiceImpl extends ServiceImpl<ApartmentInfoMapper, ApartmentInfo>
        implements ApartmentInfoService {

    @Resource
    private ApartmentInfoMapper apartmentInfoMapper;

    @Resource
    private LabelInfoMapper labelInfoMapper;

    @Resource
    private GraphInfoMapper graphInfoMapper;

    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Resource
    private FacilityInfoMapper facilityInfoMapper;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;



    @Override
    public ApartmentItemVo selectApartmentItemVoById(Long apartmentId) {

        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(apartmentId);

        List<LabelInfo> labelInfoList = labelInfoMapper.selectListByApartmentId(apartmentId);

        List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, apartmentId);

        BigDecimal minRent = roomInfoMapper.selectMinRentByApartmentId(apartmentId);

        ApartmentItemVo apartmentItemVo = new ApartmentItemVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentItemVo);

        apartmentItemVo.setGraphVoList(graphVoList);
        apartmentItemVo.setLabelInfoList(labelInfoList);
        apartmentItemVo.setMinRent(minRent);
        return apartmentItemVo;
    }

    @Override
    public ApartmentDetailVo getApartmentDetailById(Long id) {
        ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(id);

        String graphVoKey=RedisConstant.APP_GRAPHVOLIST_PREFIX +id;
        List<GraphVo> graphVoList =(List<GraphVo>)(List<?>) redisTemplate.opsForList().range(graphVoKey, 0, -1);
        if (graphVoList == null||graphVoList.isEmpty()) {
            graphVoList=graphInfoMapper.selectListByItemTypeAndId(ItemType.APARTMENT, id);
            redisTemplate.opsForList().rightPush(graphVoKey,graphVoList);
            redisTemplate.expire(graphVoKey,RedisConstant.offSet, TimeUnit.SECONDS);
        }

        String labelInfoKey=RedisConstant.APP_LABELINFO_PREFIX+id;
        List<LabelInfo> labelInfoList=(List<LabelInfo>)(List<?>) redisTemplate.opsForList().range(labelInfoKey, 0, -1);
        if (labelInfoList==null||labelInfoList.isEmpty()) {
            labelInfoList=labelInfoMapper.selectListByApartmentId(id);
            redisTemplate.opsForList().rightPush(labelInfoKey,labelInfoList);
            redisTemplate.expire(labelInfoKey,RedisConstant.offSet, TimeUnit.SECONDS);
        }



        List<FacilityInfo> facilityInfoList=facilityInfoMapper.selectListByApartmentId(id);
        BigDecimal minRent=roomInfoMapper.selectMinRentByApartmentId(id);

        ApartmentDetailVo apartmentDetailVo=new ApartmentDetailVo();
        BeanUtils.copyProperties(apartmentInfo, apartmentDetailVo);
        apartmentDetailVo.setGraphVoList(graphVoList);
        apartmentDetailVo.setLabelInfoList(labelInfoList);
        apartmentDetailVo.setFacilityInfoList(facilityInfoList);
        apartmentDetailVo.setMinRent(minRent);
        return apartmentDetailVo;
    }

    /**
     *
     * @param n 方圆n公里
     * @param latitude 纬度
     * @param longitude 经度
     * @return 公寓id的集合
     */
    @Override
    public List<Long> listApartmentByAddress(int n, double latitude, double longitude) {
        Point point = new Point(longitude, latitude);
        //设置半径范围 (KILOMETERS 千米；METERS 米)
        Metric metric = RedisGeoCommands.DistanceUnit.KILOMETERS;
        Distance distance = new Distance(n, metric);
        Circle circle = new Circle(point, distance);

        RedisGeoCommands.GeoRadiusCommandArgs geoRadiusCommandArgs = RedisGeoCommands.GeoRadiusCommandArgs
                .newGeoRadiusArgs()
                .includeDistance()//包含距离
                .includeCoordinates()//包含经纬度
                .sortAscending();//正序排序
//                .limit(50); //条数
        GeoResults<RedisGeoCommands.GeoLocation<Object>> radius = redisTemplate.opsForGeo().radius(RedisConstant.APARTMENT_GEO_PREFIX,circle, geoRadiusCommandArgs);

        List<Long> apartmentIds = new ArrayList<>();
        if (radius != null) {
            Iterator<GeoResult<RedisGeoCommands.GeoLocation<Object>>> iterator = radius.iterator();
            while (iterator.hasNext()) {
                GeoResult<RedisGeoCommands.GeoLocation<Object>> geoResult = iterator.next();
                String apartmentId = geoResult.getContent().getName().toString(); // 直接获取公寓 ID
                apartmentIds.add(Long.parseLong(apartmentId));
            }

        }
        return apartmentIds;
    }
}




