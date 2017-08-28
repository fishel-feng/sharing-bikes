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
}
