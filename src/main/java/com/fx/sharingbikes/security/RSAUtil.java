package com.fx.sharingbikes.security;


import org.bouncycastle.asn1.*;
import org.bouncycastle.asn1.pkcs.PKCSObjectIdentifiers;

import javax.crypto.Cipher;
import java.io.IOException;
import java.io.InputStream;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

public class RSAUtil {

    public static final String KEY_ALGORITHM = "RSA";

    private static String PUBLIC_KEY = "MIGfMA0GCSqGSIb3DQEBAQUAA4GNADCBiQKBgQC6BrrZJ4Tj2YRaiP/um6ZAdbIC4RmrRRTsom80y0wwt1IGNmc8z1zGK7bEJcZ0fgDjGn76xM95AQfxxrlA0Jp95PFicMg1bA5gOG1QmQR+IYYADjmteKJatBQljIawbjNBN3IpbwyZj8wn25zvDTAbnRg+oQaCL6K4mdobpwJVSwIDAQAB";

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
//        byte[] keyBytes = Base64Util.decode(PRIVATE_KEY);
//        PKCS8EncodedKeySpec pkcs8KeySpec = new PKCS8EncodedKeySpec(keyBytes);
//        KeyFactory keyFactory = KeyFactory.getInstance(KEY_ALGORITHM);
//        Key privateKey = keyFactory.generatePrivate(pkcs8KeySpec);
//        Cipher cipher = Cipher.getInstance(keyFactory.getAlgorithm());
        Key privateKey = makePrivateKey(PRIVATE_KEY);
        Cipher cipher = Cipher.getInstance("RSA");
        cipher.init(Cipher.DECRYPT_MODE, privateKey);
        return cipher.doFinal(data);
    }

    private static Key makePrivateKey(String stored) throws IOException, NoSuchAlgorithmException, InvalidKeySpecException {
        byte[] data = Base64Util.decode(stored);
        ASN1EncodableVector v = new ASN1EncodableVector();
        v.add(new ASN1Integer(0));
        ASN1EncodableVector v2 = new ASN1EncodableVector();
        v2.add(new ASN1ObjectIdentifier(PKCSObjectIdentifiers.rsaEncryption.getId()));
        v2.add(DERNull.INSTANCE);
        v.add(new DERSequence(v2));
        v.add(new DEROctetString(data));
        ASN1Sequence seq = new DERSequence(v);
        byte[] privKey = seq.getEncoded("DER");
        PKCS8EncodedKeySpec spec = new PKCS8EncodedKeySpec(privKey);
        KeyFactory fact = KeyFactory.getInstance("RSA");
        PrivateKey key = fact.generatePrivate(spec);
        return key;
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
