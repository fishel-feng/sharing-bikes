package com.fx.sharingbikes.bike.service;

import com.fx.sharingbikes.bike.dao.BikeMapper;
import com.fx.sharingbikes.bike.entity.Bike;
import com.fx.sharingbikes.bike.entity.BikeLocation;
import com.fx.sharingbikes.bike.entity.BikeNoGen;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.utils.BaiduPushUtil;
import com.fx.sharingbikes.common.utils.DateUtil;
import com.fx.sharingbikes.common.utils.RandomNumberCode;
import com.fx.sharingbikes.fee.dao.RideFeeMapper;
import com.fx.sharingbikes.fee.entity.RideFee;
import com.fx.sharingbikes.record.dao.RideRecordMapper;
import com.fx.sharingbikes.record.entity.RideRecord;
import com.fx.sharingbikes.user.dao.UserMapper;
import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.entity.UserElement;
import com.fx.sharingbikes.wallet.dao.WalletMapper;
import com.fx.sharingbikes.wallet.entity.Wallet;
import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class BikeServiceImpl implements BikeService {

    private static final Byte NOT_VERIFY = 1;

    private static final Object BIKE_UNLOCK = 2;

    private static final Byte RIDE_END = 2;

    private static final Object BIKE_LOCK = 1;

    @Autowired
    private BikeMapper bikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RideRecordMapper rideRecordMapper;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private RideFeeMapper rideFeeMapper;

    @Autowired
    private MongoTemplate mongoTemplate;

    @Transactional
    @Override
    public void generateBike() throws SharingBikesException {
        BikeNoGen bikeNoGen = new BikeNoGen();
        bikeMapper.generateBikeNo(bikeNoGen);
        Bike bike = new Bike();
        bike.setType((byte) 1);
        bike.setNumber(bikeNoGen.getAutoIncNo());
        bikeMapper.insertSelective(bike);
    }

    @Override
    @Transactional
    public void unLockBike(UserElement currentUser, Long bikeNo) throws SharingBikesException {
        try {
            User user = userMapper.selectByPrimaryKey(currentUser.getUserId());
            if (Objects.equals(user.getVerifyFlag(), NOT_VERIFY)) {
                throw new SharingBikesException("用户尚未认证");
            }
            RideRecord record = rideRecordMapper.selectRecordNotClosed(currentUser.getUserId());
            if (record != null) {
                throw new SharingBikesException("存在未关闭骑行订单");
            }
            Wallet wallet = walletMapper.selectByUserId(currentUser.getUserId());
            if (wallet.getRemainSum().compareTo(new BigDecimal(1)) < 0) {
                throw new SharingBikesException("余额不足");
            }
//        BaiduPushUtil.pushMsgToSingleDevice(currentUser, "{\"title\":\"TEST\",\"description\":\"Hello baidu push!\"}");
            Query query = Query.query(Criteria.where("bike_no").is(bikeNo));
            Update update = Update.update("status", BIKE_UNLOCK);
            mongoTemplate.updateFirst(query, update, "bike_position");
            RideRecord rideRecord = new RideRecord();
            rideRecord.setBikeNo(bikeNo);
            String recordNo = System.currentTimeMillis() + RandomNumberCode.randomNo();
            rideRecord.setRecordNo(recordNo);
            rideRecord.setStartTime(new Date());
            rideRecord.setUserId(currentUser.getUserId());
            rideRecordMapper.insertSelective(rideRecord);
        } catch (Exception e) {
            log.error("Fail to unlock bike", e);
            throw new SharingBikesException("解锁单车失败");
        }
    }

    @Override
    @Transactional
    public void lockBike(BikeLocation bikeLocation) throws SharingBikesException {
        try {
            RideRecord record = rideRecordMapper.selectBikeRecordOnGoing(bikeLocation.getBikeNumber());
            if (record == null) {
                throw new SharingBikesException("骑行记录不存在");
            }
            Long userId = record.getUserId();
            Bike bike = bikeMapper.selectByBikeNo(bikeLocation.getBikeNumber());
            if (bike == null) {
                throw new SharingBikesException("单车不存在");
            }
            RideFee fee = rideFeeMapper.selectBikeTypeFee(bike.getType());
            if (fee == null) {
                throw new SharingBikesException("计费信息异常");
            }
            BigDecimal cost = BigDecimal.ZERO;
            record.setEndTime(new Date());
            record.setStatus(RIDE_END);
            Long min = DateUtil.getBetweenMin(new Date(), record.getStartTime());
            record.setRideTime(min.intValue());
            int minUnit = fee.getMinUnit();
            int intMin = min.intValue();
            if (intMin / minUnit == 0) {
                cost = fee.getFee();
            } else if (intMin % minUnit == 0) {
                cost = fee.getFee().multiply(new BigDecimal(intMin / minUnit));
            } else if (intMin % minUnit != 0) {
                cost = fee.getFee().multiply(new BigDecimal((intMin / minUnit) + 1));
            }
            record.setRideCost(cost);
            rideRecordMapper.updateByPrimaryKeySelective(record);
            Wallet wallet = walletMapper.selectByUserId(userId);
            wallet.setRemainSum(wallet.getRemainSum().subtract(cost));
            walletMapper.updateByPrimaryKeySelective(wallet);
            Query query = Query.query(Criteria.where("bike_no").is(bikeLocation.getBikeNumber()));
            Update update = Update.update("status", BIKE_LOCK).set("location.coordinates", bikeLocation.getCoordinates());
            mongoTemplate.updateFirst(query, update, "bike_position");
        } catch (Exception e) {
            log.error("Fail to lock bike", e);
            throw new SharingBikesException("锁定单车失败");
        }
    }

    @Override
    public void reportLocation(BikeLocation bikeLocation) throws SharingBikesException {
        try {
            RideRecord record = rideRecordMapper.selectBikeRecordOnGoing(bikeLocation.getBikeNumber());
            if (record == null) {
                throw new SharingBikesException("骑行记录不存在");
            }
            DBObject object = mongoTemplate.getCollection("ride_contrail").findOne(new BasicDBObject("record_no", record.getBikeNo()));
            if (object == null) {
                List<BasicDBObject> list = new ArrayList<>();
                BasicDBObject temp = new BasicDBObject("loc", bikeLocation.getCoordinates());
                list.add(temp);
                BasicDBObject insertObj = new BasicDBObject("record_no", record.getRecordNo()).append("bike_no", record.getBikeNo()).append("contrail", list);
                mongoTemplate.insert(insertObj,"ride_contrail");
            }else {
                Query query=new Query(Criteria.where("record_no").is(record.getRecordNo()));
                Update update=new Update().push("contrail",new BasicDBObject("loc",bikeLocation.getCoordinates()));
                mongoTemplate.updateFirst(query,update,"ride_contrail");
            }
        } catch (SharingBikesException e) {
            log.error("Fail to report location", e);
            throw new SharingBikesException("上报坐标失败");
        }
    }

}
