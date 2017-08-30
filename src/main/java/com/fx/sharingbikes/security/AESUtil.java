package com.fx.sharingbikes.security;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class AESUtil {

    public static final String KEY_ALGORITHM = "AES";
    public static final String KEY_ALGORITHM_MODE = "AES/CBC/PKCS5Padding";

    public static String encrypt(String data, String key) {
        try {
            SecretKeySpec spec = new SecretKeySpec(key.getBytes("UTF-8"), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_MODE);
            cipher.init(Cipher.ENCRYPT_MODE, spec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] bs = cipher.doFinal(data.getBytes("UTF-8"));
            return Base64Util.encode(bs);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static String decrypt(String data, String key) {
        try {
            SecretKeySpec spec = new SecretKeySpec(key.getBytes("UTF-8"), KEY_ALGORITHM);
            Cipher cipher = Cipher.getInstance(KEY_ALGORITHM_MODE);
            cipher.init(Cipher.DECRYPT_MODE, spec, new IvParameterSpec(new byte[cipher.getBlockSize()]));
            byte[] originBytes = Base64Util.decode(data);
            byte[] result = cipher.doFinal(originBytes);
            return new String(result, "UTF-8");
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

//    public static void main(String[] args) throws Exception {
//        String key = "123456789abcdfgt";
//        String dataToEn = "{'mobile':'13009715105','code':'6666','platform':'android'}";
//        String enResult = encrypt(dataToEn, key);
//        System.out.println(enResult);
//        /**RSA 加密AES的密钥**/
//        byte[] enKey = RSAUtil.encryptByPublicKey(key.getBytes("UTF-8"), "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6BrrZJ4Tj2YRaiP/um6ZAdbIC4RmrRRTsom80y0wwt1IGNmc8z1zGK7bEJcZ0fgDjGn76xM95AQfxxrlA0Jp95PFicMg1bA5gOG1QmQR+IYYADjmteKJatBQljIawbjNBN3IpbwyZj8wn25zvDTAbnRg+oQaCL6K4mdobpwJVSwIDAQAB");
//        String baseKey = Base64Util.encode(enKey);
//        System.out.println(baseKey);
//        //服务端RSA解密AES的key
//        byte[] keybyte = RSAUtil.decryptByPrivateKey(Base64Util.decode(baseKey));
//        String keyR = new String(keybyte, "UTF-8");
//        System.out.println(keyR);
//    }
}
