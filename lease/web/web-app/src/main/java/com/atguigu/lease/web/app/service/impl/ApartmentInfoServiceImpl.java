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
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.concurrent.TimeUnit;

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
}




