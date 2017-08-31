package com.fx.sharingbikes;

import com.fx.sharingbikes.bike.entity.BikeLocation;
import com.fx.sharingbikes.bike.entity.Point;
import com.fx.sharingbikes.bike.service.BikeGeoService;
import com.fx.sharingbikes.bike.service.BikeService;
import com.fx.sharingbikes.common.exception.SharingBikesException;
import com.fx.sharingbikes.user.entity.UserElement;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import java.util.List;

@RunWith(SpringRunner.class)
@Slf4j
@SpringBootTest(classes = SharingBikesApplication.class, webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
public class SharingBikesApplicationTests {

    @Autowired
    @Qualifier("bikeGeoService")
    private BikeGeoService geoService;

    @Autowired
    @Qualifier("bikeServiceImpl")
    private BikeService bikeService;

    @Test
    public void geoTest() throws SharingBikesException {
//        List<BikeLocation> locations = geoService.geoNearSphere("bike-position", "location",
//                new Point(126.729343, 45.759871), 0, 50, null, null, 10);
        List<BikeLocation> locations = geoService.geoNear("bike-position", null, new Point(126.729343, 45.759872), 10, 50);
        System.out.println(locations);
    }

    @Test
    public void unlockTest() throws SharingBikesException {
        UserElement userElement = new UserElement();
        userElement.setUserId(1L);
        userElement.setPushChannelId("13242");
        userElement.setPushChannelId("android");
        bikeService.unLockBike(userElement, 28000001L);
    }

    @Test
    public void lockTest() throws SharingBikesException {
        BikeLocation bikeLocation = new BikeLocation();
        bikeLocation.setBikeNumber(28000001L);
        bikeLocation.setCoordinates(new Double[]{126.732086, 45.741842});
        bikeService.lockBike(bikeLocation);
    }

}
