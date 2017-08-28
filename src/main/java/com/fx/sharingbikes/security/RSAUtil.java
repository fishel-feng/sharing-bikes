package com.fx.sharingbikes.security;


import javax.crypto.Cipher;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.*;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {

    public static final String KEY_ALGORITHM = "RSA";

    private static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC5boVUQ95FTf3WUbf4ApFt 6RXY9dAgBDrEqxFoEhsYZSWRmsu7w8NKjGS0nTMUKTHl9Lm3Vjc2IOrohnK6 g2b5CSGFgBU453KF3dHYWSB6+lcugsbby71OU0GsQZ96n0FlJRaIzi4t+2RH PnOq/mk/KVWjpv4Rl7qbTdgPvaDMqwIDAQAB";

    private static String PRIVATE_KEY = "";

    public static void convert() throws Exception {
        byte[] data = null;

        try {
            InputStream is = RSAUtil.class.getResourceAsStream("/enc_pri");
            int length = is.available();
            data = new byte[length];
            is.read(data);
        } catch (Exception e) {
        }

        String dataStr = new String(data);
        try {
            PRIVATE_KEY = dataStr;
        } catch (Exception e) {
        }

        if (PRIVATE_KEY == null) {
            throw new Exception("Fail to retrieve key");
        }
    }

    public static byte[] decryptByPrivateKey(byte[] data) throws Exception {
        convert();
        byte[] keyBytes = Base64Util.decode(PRIVATE_KEY);
        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.DECRYPT_MODE, privateKey);

        return cipher.doFinal(data);
    }

    public static byte[] encryptByPublicKey(byte[] data, String key) throws Exception {
        byte[] keyBytes = Base64Util.decode(key);
        X509EncodedKeySpec pkcs8KeySpec = new X509EncodedKeySpec(keyBytes);
        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
        Key publicKey = keyFactory.generatePublic(pkcs8KeySpec);

        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        cipher.init(Cipher.ENCRYPT_MODE, publicKey);
        return cipher.doFinal(data);
    }


//    public static void main(String[] args) throws Exception {
//        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(KEY_ALGORITHM);
//        keyPairGenerator.initialize(1024);
//        KeyPair keyPair = keyPairGenerator.generateKeyPair();
//        PublicKey publicKey = keyPair.getPublic();
//        PrivateKey privateKey = keyPair.getPrivate();
//        System.out.println(Base64Util.encode(publicKey.getEncoded()));
//        System.out.println(Base64Util.encode(privateKey.getEncoded()));
//    }
}
