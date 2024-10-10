package com.atguigu.lease.web.admin.service.impl;

import com.atguigu.lease.common.constant.RedisConstant;
import com.atguigu.lease.model.entity.*;
import com.atguigu.lease.model.enums.ItemType;
import com.atguigu.lease.web.admin.mapper.*;
import com.atguigu.lease.web.admin.service.*;
import com.atguigu.lease.web.admin.vo.attr.AttrValueVo;
import com.atguigu.lease.web.admin.vo.graph.GraphVo;
import com.atguigu.lease.web.admin.vo.room.RoomDetailVo;
import com.atguigu.lease.web.admin.vo.room.RoomItemVo;
import com.atguigu.lease.web.admin.vo.room.RoomQueryVo;
import com.atguigu.lease.web.admin.vo.room.RoomSubmitVo;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import jakarta.annotation.Resource;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author liubo
 * @description 针对表【room_info(房间信息表)】的数据库操作Service实现
 * @createDate 2023-07-24 15:48:00
 */
@Service
public class RoomInfoServiceImpl extends ServiceImpl<RoomInfoMapper, RoomInfo>
        implements RoomInfoService {

    @Autowired
    private GraphInfoService graphInfoService;

    @Autowired
    private RoomAttrValueService roomAttrValueService;

    @Autowired
    private RoomFacilityService roomFacilityService;

    @Autowired
    private RoomLabelService roomLabelService;

    @Autowired
    private RoomPaymentTypeService roomPaymentTypeService;

    @Autowired
    private RoomLeaseTermService roomLeaseTermService;


    @Resource
    private RoomInfoMapper roomInfoMapper;

    @Autowired
    private ApartmentInfoMapper apartmentInfoMapper;

    @Autowired
    private GraphInfoMapper graphInfoMapper;

    @Autowired
    private AttrValueMapper attrValueMapper;

    @Autowired
    private FacilityInfoMapper facilityInfoMapper;

    @Autowired
    private LabelInfoMapper labelInfoMapper;

    @Autowired
    private PaymentTypeMapper paymentTypeMapper;

    @Autowired
    private LeaseTermMapper leaseTermMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public void saveOrUpdateRoom(RoomSubmitVo roomSubmitVo) {
        boolean isUpdate = roomSubmitVo.getId() != null;
        super.saveOrUpdate(roomSubmitVo);

        //若为更新操作，则先删除与Room相关的各项信息列表
        if (isUpdate) {
            //1.删除原有graphInfoList
            LambdaQueryWrapper<GraphInfo> graphQueryWrapper = new LambdaQueryWrapper<>();
            graphQueryWrapper.eq(GraphInfo::getItemType, ItemType.ROOM);
            graphQueryWrapper.eq(GraphInfo::getItemId, roomSubmitVo.getId());
            graphInfoService.remove(graphQueryWrapper);

            //2.删除原有roomAttrValueList
            LambdaQueryWrapper<RoomAttrValue> attrQueryMapper = new LambdaQueryWrapper<>();
            attrQueryMapper.eq(RoomAttrValue::getRoomId, roomSubmitVo.getId());
            roomAttrValueService.remove(attrQueryMapper);

            //3.删除原有roomFacilityList
            LambdaQueryWrapper<RoomFacility> facilityQueryWrapper = new LambdaQueryWrapper<>();
            facilityQueryWrapper.eq(RoomFacility::getRoomId, roomSubmitVo.getId());
            roomFacilityService.remove(facilityQueryWrapper);

            //4.删除原有roomLabelList
            LambdaQueryWrapper<RoomLabel> labelQueryWrapper = new LambdaQueryWrapper<>();
            labelQueryWrapper.eq(RoomLabel::getRoomId, roomSubmitVo.getId());
            roomLabelService.remove(labelQueryWrapper);

            //5.删除原有paymentTypeList
            LambdaQueryWrapper<RoomPaymentType> paymentQueryWrapper = new LambdaQueryWrapper<>();
            paymentQueryWrapper.eq(RoomPaymentType::getRoomId, roomSubmitVo.getId());
            roomPaymentTypeService.remove(paymentQueryWrapper);


            //6.删除原有leaseTermList
            LambdaQueryWrapper<RoomLeaseTerm> termQueryWrapper = new LambdaQueryWrapper<>();
            termQueryWrapper.eq(RoomLeaseTerm::getRoomId, roomSubmitVo.getId());
            roomLeaseTermService.remove(termQueryWrapper);

            //7.删除缓存
            redisTemplate.delete(RedisConstant.APP_LOGIN_PREFIX + roomSubmitVo.getId());
        }

        //1.保存新的graphInfoList
        List<GraphVo> graphVoList = roomSubmitVo.getGraphVoList();
        if (!CollectionUtils.isEmpty(graphVoList)) {
            ArrayList<GraphInfo> graphInfoList = new ArrayList<>();
            for (GraphVo graphVo : graphVoList) {
                GraphInfo graphInfo = new GraphInfo();
                graphInfo.setItemType(ItemType.ROOM);
                graphInfo.setItemId(roomSubmitVo.getId());
                graphInfo.setName(graphVo.getName());
                graphInfo.setUrl(graphVo.getUrl());
                graphInfoList.add(graphInfo);
            }
            graphInfoService.saveBatch(graphInfoList);
        }

        //2.保存新的roomAttrValueList
        List<Long> attrValueIds = roomSubmitVo.getAttrValueIds();
        if (!CollectionUtils.isEmpty(attrValueIds)) {
            List<RoomAttrValue> roomAttrValueList = new ArrayList<>();
            for (Long attrValueId : attrValueIds) {
                RoomAttrValue roomAttrValue = RoomAttrValue.builder().roomId(roomSubmitVo.getId()).attrValueId(attrValueId).build();
                roomAttrValueList.add(roomAttrValue);
            }
            roomAttrValueService.saveBatch(roomAttrValueList);
        }

        //3.保存新的facilityInfoList
        List<Long> facilityInfoIds = roomSubmitVo.getFacilityInfoIds();
        if (!CollectionUtils.isEmpty(facilityInfoIds)) {
            List<RoomFacility> roomFacilityList = new ArrayList<>();
            for (Long facilityInfoId : facilityInfoIds) {
                RoomFacility roomFacility = RoomFacility.builder().roomId(roomSubmitVo.getId()).facilityId(facilityInfoId).build();
                roomFacilityList.add(roomFacility);
            }
            roomFacilityService.saveBatch(roomFacilityList);
        }

        //4.保存新的labelInfoList
        List<Long> labelInfoIds = roomSubmitVo.getLabelInfoIds();
        if (!CollectionUtils.isEmpty(labelInfoIds)) {
            ArrayList<RoomLabel> roomLabelList = new ArrayList<>();
            for (Long labelInfoId : labelInfoIds) {
                RoomLabel roomLabel = RoomLabel.builder().roomId(roomSubmitVo.getId()).labelId(labelInfoId).build();
                roomLabelList.add(roomLabel);
            }
            roomLabelService.saveBatch(roomLabelList);
        }

        //5.保存新的paymentTypeList
        List<Long> paymentTypeIds = roomSubmitVo.getPaymentTypeIds();
        if (!CollectionUtils.isEmpty(paymentTypeIds)) {
            ArrayList<RoomPaymentType> roomPaymentTypeList = new ArrayList<>();
            for (Long paymentTypeId : paymentTypeIds) {
                RoomPaymentType roomPaymentType = RoomPaymentType.builder().roomId(roomSubmitVo.getId()).paymentTypeId(paymentTypeId).build();
                roomPaymentTypeList.add(roomPaymentType);
            }
            roomPaymentTypeService.saveBatch(roomPaymentTypeList);
        }

        //6.保存新的leaseTermList
        List<Long> leaseTermIds = roomSubmitVo.getLeaseTermIds();
        if (!CollectionUtils.isEmpty(leaseTermIds)) {
            ArrayList<RoomLeaseTerm> roomLeaseTerms = new ArrayList<>();
            for (Long leaseTermId : leaseTermIds) {
                RoomLeaseTerm roomLeaseTerm = RoomLeaseTerm.builder().roomId(roomSubmitVo.getId()).leaseTermId(leaseTermId).build();
                roomLeaseTerms.add(roomLeaseTerm);
            }
            roomLeaseTermService.saveBatch(roomLeaseTerms);
        }

    }

    @Override
    public void removeRoomById(Long id) {

        super.removeById(id);

        //删除图片列表
        LambdaQueryWrapper<GraphInfo> graphQueryWrapperqueryWrapper=new LambdaQueryWrapper<GraphInfo>();
        graphQueryWrapperqueryWrapper.eq(GraphInfo::getItemId, ItemType.ROOM);
        graphQueryWrapperqueryWrapper.eq(GraphInfo::getItemId, id);
        graphInfoService.remove(graphQueryWrapperqueryWrapper);

        //删除属性列表
        LambdaQueryWrapper<RoomAttrValue> attrQueryMapper = new LambdaQueryWrapper<>();
        attrQueryMapper.eq(RoomAttrValue::getRoomId, id);
        roomAttrValueService.remove(attrQueryMapper);

        //删除配套列表
        LambdaQueryWrapper<RoomFacility> facilityQueryMapper = new LambdaQueryWrapper<>();
        facilityQueryMapper.eq(RoomFacility::getRoomId, id);
        roomFacilityService.remove(facilityQueryMapper);

        //删除标签列表
        LambdaQueryWrapper<RoomLabel> labelQueryMapper = new LambdaQueryWrapper<>();
        labelQueryMapper.eq(RoomLabel::getRoomId, id);
        roomLabelService.remove(labelQueryMapper);

        //删除支付方式列表
        LambdaQueryWrapper<RoomPaymentType> paymentTypeQueryMapper = new LambdaQueryWrapper<>();
        paymentTypeQueryMapper.eq(RoomPaymentType::getRoomId, id);
        roomPaymentTypeService.remove(paymentTypeQueryMapper);

        //删除可选租期列表
        LambdaQueryWrapper<RoomLeaseTerm> leaseTermQueryMapper = new LambdaQueryWrapper<>();
        leaseTermQueryMapper.eq(RoomLeaseTerm::getRoomId, id);
        roomLeaseTermService.remove(leaseTermQueryMapper);

        //删除缓存
        redisTemplate.delete(RedisConstant.APP_LOGIN_PREFIX + id);
    }

    @Override
    public IPage<RoomItemVo> pageRoomItemByQuery(IPage<RoomItemVo> page, RoomQueryVo queryVo) {
        return roomInfoMapper.pageRoomItemByQuery(page, queryVo);
    }

    @Override
    public RoomDetailVo getRoomDetailById(Long id) {

        String key=RedisConstant.ADMIN_ROOM_PREFIX + id;

        RoomDetailVo roomDetailVo = (RoomDetailVo) redisTemplate.opsForValue().get(key);

        if (roomDetailVo == null) {


            //1.查询RoomInfo
            RoomInfo roomInfo =(RoomInfo)redisTemplate.opsForValue().get(RedisConstant.APP_ROOM_INFO_PREFIX +id);
            if(roomInfo==null){
                roomInfo=roomInfoMapper.selectById(id);
                redisTemplate.opsForValue().set(RedisConstant.APP_ROOM_INFO_PREFIX +id, roomInfo,RedisConstant.offSet, TimeUnit.SECONDS);
            }
            //可能导致缓存穿透
            if(roomInfo==null){
                return null;
            }

            roomDetailVo = new RoomDetailVo();
            //2.查询所属公寓信息
            ApartmentInfo apartmentInfo = apartmentInfoMapper.selectById(roomInfo.getApartmentId());

            //3.查询graphInfoList
            List<GraphVo> graphVoList = graphInfoMapper.selectListByItemTypeAndId(ItemType.ROOM, id);

            //4.查询attrValueList
            List<AttrValueVo> attrvalueVoList = attrValueMapper.selectListByRoomId(id);

            //5.查询facilityInfoList
            List<FacilityInfo> facilityInfoList = facilityInfoMapper.selectListByRoomId(id);

            //6.查询labelInfoList
            List<LabelInfo> labelInfoList = labelInfoMapper.selectListByRoomId(id);

            //7.查询paymentTypeList
            List<PaymentType> paymentTypeList = paymentTypeMapper.selectListByRoomId(id);

            //8.查询leaseTermList
            List<LeaseTerm> leaseTermList = leaseTermMapper.selectListByRoomId(id);



            BeanUtils.copyProperties(roomInfo,roomDetailVo);

            roomDetailVo.setApartmentInfo(apartmentInfo);
            roomDetailVo.setGraphVoList(graphVoList);
            roomDetailVo.setAttrValueVoList(attrvalueVoList);
            roomDetailVo.setFacilityInfoList(facilityInfoList);
            roomDetailVo.setLabelInfoList(labelInfoList);
            roomDetailVo.setPaymentTypeList(paymentTypeList);
            roomDetailVo.setLeaseTermList(leaseTermList);

            redisTemplate.opsForValue().set(key, roomDetailVo,RedisConstant.offSet, TimeUnit.SECONDS);
        }

        return roomDetailVo;
    }
}




