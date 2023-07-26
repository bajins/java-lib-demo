package com.bajins.demo;

import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.bouncycastle.jce.provider.BouncyCastleProvider;
import org.springframework.util.Base64Utils;

import javax.crypto.KeyGenerator;
import javax.crypto.Mac;
import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import javax.xml.bind.DatatypeConverter;
import java.security.MessageDigest;
import java.security.Security;

/**
 * 封装各种格式的编码解码工具类.
 * 1.Commons-Codec的 hex/base64 编码
 * 3.Commons-Lang的xml/html escape
 * bcprov-jdk15on
 *
 * @description: EncryptUtil
 * @author: claer
 * @create: 2018-04-14 15:21
 */
public class EncryptUtil {



    ////////////////////RipeMD128消息摘要处理///////////////////////////

    /**
     * RipeMD128Hex消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return String 消息摘要
     **/
    public static String encodeRipeMD128Hex(byte[] data) throws Exception {
        //执行消息摘要
        byte[] b = encodeRipeMD128(data);
        //做十六进制的编码处理
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * RipeMD128消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return byte[] 消息摘要
     */
    private static byte[] encodeRipeMD128(byte[] data) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance("RipeMD128");
        //执行消息摘要
        return md.digest(data);
    }
    ////////////////////RipeMD160消息摘要处理///////////////////////////

    /**
     * RipeMD160Hex消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return String 消息摘要
     **/
    public static String encodeRipeMD160Hex(byte[] data) throws Exception {
        //执行消息摘要
        byte[] b = encodeRipeMD160(data);
        //做十六进制的编码处理
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * RipeMD160消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return byte[] 消息摘要
     */
    private static byte[] encodeRipeMD160(byte[] data) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance("RipeMD160");
        //执行消息摘要
        return md.digest(data);
    }
    ////////////////////RipeMD256消息摘要处理///////////////////////////

    /**
     * RipeMD256Hex消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return String 消息摘要
     **/
    public static String encodeRipeMD256Hex(byte[] data) throws Exception {
        //执行消息摘要
        byte[] b = encodeRipeMD256(data);
        //做十六进制的编码处理
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * RipeMD256消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return byte[] 消息摘要
     */
    private static byte[] encodeRipeMD256(byte[] data) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance("RipeMD256");
        //执行消息摘要
        return md.digest(data);
    }

    ////////////////////RipeMD320消息摘要处理///////////////////////////


    /**
     * RipeMD320Hex消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return String 消息摘要
     **/
    public static String encodeRipeMD320Hex(byte[] data) throws Exception {
        //执行消息摘要
        byte[] b = encodeRipeMD320(data);
        //做十六进制的编码处理
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * RipeMD320消息摘要
     *
     * @param data 待处理的消息摘要数据
     * @return byte[] 消息摘要
     */
    private static byte[] encodeRipeMD320(byte[] data) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化MessageDigest
        MessageDigest md = MessageDigest.getInstance("RipeMD320");
        //执行消息摘要
        return md.digest(data);
    }
    ///////////////////////////////HmacRipeMD-BouncyCastle支持的实现//////////////////////////////////

    /**
     * 初始化HmacRipeMD128的密钥
     *
     * @return byte[] 密钥
     */
    public static byte[] initHmacRipeMD128Key() throws Exception {

        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化KeyGenerator
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacRipeMD128");
        //产生密钥
        SecretKey secretKey = keyGenerator.generateKey();
        //获取密钥
        return secretKey.getEncoded();
    }

    /**
     * HmacRipeMD128Hex消息摘要
     *
     * @param data 待做消息摘要处理的数据
     * @param key  密钥
     * @return byte[] 消息摘要
     */
    public static String encodeHmacRipeMD128Hex(byte[] data, byte[] key) throws Exception {
        //执行消息摘要处理
        byte[] b = encodeHmacRipeMD128(data, key);
        //做十六进制转换
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * HmacRipeMD128消息摘要
     *
     * @param data 待做摘要处理的数据
     * @param key  密钥
     * @return byte[] 消息摘要
     */
    private static byte[] encodeHmacRipeMD128(byte[] data, byte[] key) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //还原密钥，因为密钥是以byte形式为消息传递算法所拥有
        SecretKey secretKey = new SecretKeySpec(key, "HmacRipeMD128");
        //实例化Mac
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        //初始化Mac
        mac.init(secretKey);
        //执行消息摘要处理
        return mac.doFinal(data);
    }
    ///////////////////////////////HmacRipeMD-BouncyCastle支持的实现//////////////////////////////////

    /**
     * 初始化HmacRipeMD160的密钥
     *
     * @return byte[] 密钥
     */
    public static byte[] initHmacRipeMD160Key() throws Exception {

        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //初始化KeyGenerator
        KeyGenerator keyGenerator = KeyGenerator.getInstance("HmacRipeMD160");
        //产生密钥
        SecretKey secretKey = keyGenerator.generateKey();
        //获取密钥
        return secretKey.getEncoded();
    }

    /**
     * HmacRipeMD160Hex消息摘要
     *
     * @param data 待做消息摘要处理的数据
     * @param key  密钥
     * @return byte[] 消息摘要
     */
    public static String encodeHmacRipeMD160Hex(byte[] data, byte[] key) throws Exception {
        //执行消息摘要处理
        byte[] b = encodeHmacRipeMD160(data, key);
        //做十六进制转换
        return String.valueOf(Hex.encodeHex(b));
    }

    /**
     * HmacRipeMD160消息摘要
     *
     * @param data 待做摘要处理的数据
     * @param key  密钥
     * @return byte[] 消息摘要
     */
    private static byte[] encodeHmacRipeMD160(byte[] data, byte[] key) throws Exception {
        //加入BouncyCastleProvider的支持
        Security.addProvider(new BouncyCastleProvider());
        //还原密钥，因为密钥是以byte形式为消息传递算法所拥有
        SecretKey secretKey = new SecretKeySpec(key, "HmacRipeMD160");
        //实例化Mac
        Mac mac = Mac.getInstance(secretKey.getAlgorithm());
        //初始化Mac
        mac.init(secretKey);
        //执行消息摘要处理
        return mac.doFinal(data);
    }


    //===================================== PBKDF2 start ==================================================

    public static void main(String[] args) {
        BouncyCastleProvider bouncyCastleProvider = new BouncyCastleProvider();
        // BASE64加密
        String s = Base64.encodeBase64String("str".getBytes());
        String s1 = Base64Utils.encodeToString("str".getBytes());
        byte[] strs = DatatypeConverter.parseBase64Binary("str");

        // Base64编码
        byte[] bytes = Base64.encodeBase64("input".getBytes());
        // Base64解码.
        byte[] bytes1 = Base64.decodeBase64("input".getBytes());


    }


}
