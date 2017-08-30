package com.fx.sharingbikes.bike.service;

import com.fx.sharingbikes.bike.dao.BikeMapper;
import com.fx.sharingbikes.bike.entity.Bike;
import com.fx.sharingbikes.bike.entity.BikeNoGen;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.common.utils.BaiduPushUtil;
import com.fx.sharingbikes.common.utils.RandomNumberCode;
import com.fx.sharingbikes.record.dao.RideRecordMapper;
import com.fx.sharingbikes.record.entity.RideRecord;
import com.fx.sharingbikes.user.dao.UserMapper;
import com.fx.sharingbikes.user.entity.User;
import com.fx.sharingbikes.user.entity.UserElement;
import com.fx.sharingbikes.wallet.dao.WalletMapper;
import com.fx.sharingbikes.wallet.entity.Wallet;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;
import org.springframework.data.mongodb.core.query.Update;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Date;

@Service
@Slf4j
public class BikeServiceImpl implements BikeService {

    private static final int NOT_VERIFY = 1;

    private static final int BIKE_UNLOCK = 2;

    @Autowired
    private BikeMapper bikeMapper;

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private RideRecordMapper rideRecordMapper;

    @Autowired
    private WalletMapper walletMapper;

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
            if (user.getVerifyFlag() == NOT_VERIFY) {
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
            mongoTemplate.updateFirst(query, update, "bike-position");
            RideRecord rideRecord = new RideRecord();
            rideRecord.setBikeNo(bikeNo);
            String recordNo = new Date().getTime() + RandomNumberCode.randomNo();
            rideRecord.setRecordNo(recordNo);
            rideRecord.setStartTime(new Date());
            rideRecord.setUserId(currentUser.getUserId());
            rideRecordMapper.insertSelective(rideRecord);
        } catch (Exception e) {
            log.error("Fail to unlock bike", e);
            throw new SharingBikesException("解锁单车失败");
        }
    }

}
