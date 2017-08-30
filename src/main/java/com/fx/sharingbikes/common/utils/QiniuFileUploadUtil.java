package com.fx.sharingbikes.common.utils;

import com.fx.sharingbikes.common.constants.Constants;
import com.google.gson.Gson;
import com.qiniu.common.Zone;
import com.qiniu.http.Response;
import com.qiniu.storage.Configuration;
import com.qiniu.storage.UploadManager;
import com.qiniu.storage.model.DefaultPutRet;
import com.qiniu.util.Auth;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public class QiniuFileUploadUtil {

    public static String uploadHeadImg(MultipartFile file) throws IOException {
        Configuration cfg = new Configuration(Zone.zone2());
        UploadManager uploadManager = new UploadManager(cfg);
        Auth auth = Auth.create(Constants.QINIU_ACCESS_KEY, Constants.QINIU_SECRET_KEY);
        String upToken = auth.uploadToken(Constants.QINIU_HEAD_IMG_BUCKET_NAME);
        Response response = uploadManager.put(file.getBytes(), null, upToken);
        DefaultPutRet putRet = new Gson().fromJson(response.bodyString(), DefaultPutRet.class);
        return putRet.key;
    }

}
