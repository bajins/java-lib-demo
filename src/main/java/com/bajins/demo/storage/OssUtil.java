package com.bajins.demo.storage;

import com.aliyun.oss.OSS;
import com.aliyun.oss.OSSClient;
import com.aliyun.oss.common.auth.CredentialsProvider;
import com.aliyun.oss.common.auth.DefaultCredentialProvider;
import com.aliyun.oss.model.Callback;
import com.aliyun.oss.model.PutObjectRequest;
import com.aliyun.oss.model.PutObjectResult;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * 阿里云OSS工具类
 *
 * @author claer https://www.bajins.com
 * @program com.bajins.api.utils.storage
 * @description OssUtil
 * @create 2018-12-16 14:21
 */
public class OssUtil {


    // 域名
    private static final String endpoint = "https://oss-cn-beijing.aliyuncs.com";
    // 阿里云主账号AccessKey拥有所有API的访问权限，风险很高。
    // 强烈建议您创建并使用RAM账号进行API访问或日常运维，请登录 https://ram.console.aliyun.com 创建RAM账号。
    private static final String accessKeyId = "";
    private static final String accessKeySecret = "";

    // 存储桶名称
    private static final String bucketName = "claer";

    // 您的回调服务器地址，如http://oss-demo.aliyuncs.com:23450或http://127.0.0.1:9090。
    private static final String callbackUrl = "http://i.ngrok.xiaomiqiu.cn/trusteeship_return_web/";

    public static void uploadFile() throws IOException {
        CredentialsProvider cp = new DefaultCredentialProvider(accessKeyId, accessKeySecret);
        // 创建OSSClient实例。
        OSS client = new OSSClient(endpoint, cp, null);

        File localFile = new File("/data/dog.jpg");
        // getCanonicalPath() 方法返回绝对路径，会把 ..\ .\ 这样的符号解析掉
        String key = localFile.getName();
        try (FileInputStream fileInputStream = new FileInputStream(localFile);) {
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, fileInputStream);
            // 上传回调参数。
            Callback callback = new Callback();
            callback.setCallbackUrl(callbackUrl);
            // 设置回调请求消息头中Host的值，如oss-cn-hangzhou.aliyuncs.com。
            callback.setCallbackHost("oss-cn-beijing.aliyuncs.com");
            // 设置发起回调时请求body的值。
            callback.setCallbackBody("{\\\"bucket\\\":${bucket},\\\"object\\\":${object},"
                    + "\\\"mimeType\\\":${mimeType},\\\"size\\\":${size},"
                    + "\\\"my_var1\\\":${x:var1},\\\"my_var2\\\":${x:var2}}");
            // 设置发起回调请求的Content-Type。
            callback.setCalbackBodyType(Callback.CalbackBodyType.JSON);
            // 设置发起回调请求的自定义参数，由Key和Value组成，Key必须以x:开始。
            callback.addCallbackVar("x:var1", "value1");
            callback.addCallbackVar("x:var2", "value2");

            // 判断文件是否存在。
            boolean found = client.doesObjectExist(bucketName, key);
            // 删除文件。
            if (found) {
                client.deleteObject(bucketName, key);
            }
            // 设置回调
            putObjectRequest.setCallback(callback);
            PutObjectResult putObject = client.putObject(putObjectRequest);

            // 读取上传回调返回的消息内容。
            byte[] buffer = new byte[1024];
            int read = putObject.getResponse().getContent().read(buffer);
            System.out.println(read);
            // 数据读取完成后，获取的流必须关闭，否则会造成连接泄漏，导致请求无连接可用，程序无法正常工作。
            putObject.getResponse().getContent().close();
        } finally {
            client.shutdown();
        }
    }

}