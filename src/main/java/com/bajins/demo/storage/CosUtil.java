package com.bajins.demo.storage;

import com.qcloud.cos.COSClient;
import com.qcloud.cos.ClientConfig;
import com.qcloud.cos.auth.BasicCOSCredentials;
import com.qcloud.cos.auth.COSCredentials;
import com.qcloud.cos.exception.CosClientException;
import com.qcloud.cos.exception.CosServiceException;
import com.qcloud.cos.model.*;
import com.qcloud.cos.region.Region;
import com.qcloud.cos.transfer.Download;
import com.qcloud.cos.transfer.TransferManager;
import com.qcloud.cos.transfer.Upload;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.net.URL;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 腾讯云对象存储工具
 *
 * @author claer https://www.bajins.com
 * @program com.bajins.api.utils.storage
 * @description CosUtil
 * @create 2018-12-16 12:32
 */
public class CosUtil {

    private static final String ACCESS_KEY = "";
    private static final String SECRET_KEY = "";
    private static final String BUCKET_NAME = "";
    private static final String REGION_NAME = "";

    /**
     * 查询获取 Bucket中的文件列表
     *
     * @return
     * @throws CosClientException
     * @throws CosServiceException
     */
    public List<COSObjectSummary> getlistObjects() {
        COSClient cosClient = null;
        List<COSObjectSummary> objectSummaries = null;
        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式

            // 获取 bucket 下成员（设置 delimiter）
            ListObjectsRequest listObjectsRequest = new ListObjectsRequest();
            listObjectsRequest.setBucketName(BUCKET_NAME);
            // 设置 list 的 prefix, 表示 list 出来的文件 key 都是以这个 prefix 开始
            listObjectsRequest.setPrefix("");
            // 设置 delimiter 为/, 即获取的是直接成员，不包含目录下的递归子成员
            listObjectsRequest.setDelimiter("/");
            // 设置 marker, (marker 由上一次 list 获取到, 或者第一次 list marker 为空)
            listObjectsRequest.setMarker("");
            // 设置最多 list 100个成员,（如果不设置, 默认为1000个，最大允许一次 list 1000个 key）
            listObjectsRequest.setMaxKeys(100);

            ObjectListing objectListing = cosClient.listObjects(listObjectsRequest);
            // 获取下次 list 的 marker
            String nextMarker = objectListing.getNextMarker();
            // 判断是否已经 list 完, 如果 list 结束, 则 isTruncated 为 false, 否则为 true
            boolean isTruncated = objectListing.isTruncated();
            objectSummaries = objectListing.getObjectSummaries();
            for (COSObjectSummary cosObjectSummary : objectSummaries) {
                // 文件路径
                String key = cosObjectSummary.getKey();
                // 获取文件长度
                long fileSize = cosObjectSummary.getSize();
                // 获取文件ETag
                String eTag = cosObjectSummary.getETag();
                // 获取最后修改时间
                Date lastModified = cosObjectSummary.getLastModified();
                // 获取文件的存储类型
                String StorageClassStr = cosObjectSummary.getStorageClass();
            }
        } finally {
            cosClient.shutdown();
        }
        return objectSummaries;
    }

    /**
     * 文件上传
     *
     * @param bucketName 存储桶名称
     * @param filePath   文件路径
     */
    public static void putObject(String bucketName, String filePath) throws IOException {
        COSClient cosClient = null;
        List<COSObjectSummary> objectSummaries = null;
        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
            if (StringUtils.isBlank(bucketName)) {
                bucketName = BUCKET_NAME;
            }

            // 方法3 提供更多细粒度的控制, 常用的设置如下
            // 1 storage-class 存储类型, 用于设置标准(默认)、低频、近线
            // 2 content-type, 对于本地文件上传, 默认根据本地文件的后缀进行映射, 如 jpg 文件映射 为image/jpeg
            //   对于流式上传 默认是 application/octet-stream
            // 3 上传的同时制定权限(也可通过调用 API set object acl 来设置)
            // 4 若要全局关闭上传MD5校验, 则设置系统环境变量, 此设置会对所有的会影响所有的上传校验。 默认是进行校验的。
            // 关闭校验  System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, "true");
            // 再次打开校验  System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, null);
            File localFile = new File(filePath);
            // 指定要上传到 COS 上对象键,https://cloud.tencent.com/document/product/436/13324#.E5.AF.B9.E8.B1.A1.E9.94.AE
            String key = localFile.getName();
            FileInputStream fileInputStream = new FileInputStream(localFile);

            // 设置自定义属性(如 content-type, content-disposition 等)
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // 如果上传为流式，需设置输入流长度
            objectMetadata.setContentLength(fileInputStream.available());
            // 设置 Content type, 默认是 application/octet-stream
            //            objectMetadata.setContentType("image/jpeg");

            // 第三个参数可以为一个File也可以为FileInputStream，如果为fileInputStream那么第四个参数为ObjectMetadata
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, fileInputStream, objectMetadata);
            // 设置存储类型为低频
            //            putObjectRequest.setStorageClass(StorageClass.Standard_IA);
            // 如果上传方式为File，可以设置ObjectMetadata
            //            putObjectRequest.setMetadata(objectMetadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            String etag = putObjectResult.getETag();  // 获取文件的 etag
            // 关闭流
            fileInputStream.close();
        } finally {
            cosClient.shutdown();

        }
    }

    /**
     * 文件上传
     *
     * @param bucketName 存储桶名称
     * @param file       文件
     */
    public static void putObject(String bucketName, File file, String key) throws IOException {
        COSClient cosClient = null;
        List<COSObjectSummary> objectSummaries = null;
        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
            if (StringUtils.isBlank(bucketName)) {
                bucketName = BUCKET_NAME;
            }

            // 方法3 提供更多细粒度的控制, 常用的设置如下
            // 1 storage-class 存储类型, 用于设置标准(默认)、低频、近线
            // 2 content-type, 对于本地文件上传, 默认根据本地文件的后缀进行映射, 如 jpg 文件映射 为image/jpeg
            //   对于流式上传 默认是 application/octet-stream
            // 3 上传的同时制定权限(也可通过调用 API set object acl 来设置)
            // 4 若要全局关闭上传MD5校验, 则设置系统环境变量, 此设置会对所有的会影响所有的上传校验。 默认是进行校验的。
            // 关闭校验  System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, "true");
            // 再次打开校验  System.setProperty(SkipMd5CheckStrategy.DISABLE_PUT_OBJECT_MD5_VALIDATION_PROPERTY, null);
            // 指定要上传到 COS 上对象键,https://cloud.tencent.com/document/product/436/13324#.E5.AF.B9.E8.B1.A1.E9.94.AE
            if (StringUtils.isBlank(key)) {
                key = file.getName();
            } else {
                key = key.replace("/", "") + "/" + file.getName();
            }
            FileInputStream fileInputStream = new FileInputStream(file);

            // 设置自定义属性(如 content-type, content-disposition 等)
            ObjectMetadata objectMetadata = new ObjectMetadata();
            // 如果上传为流式，需设置输入流长度
            objectMetadata.setContentLength(fileInputStream.available());
            // 设置 Content type, 默认是 application/octet-stream
            //            objectMetadata.setContentType("image/jpeg");

            // 第三个参数可以为一个File也可以为FileInputStream，如果为fileInputStream那么第四个参数为ObjectMetadata
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucketName, key, fileInputStream, objectMetadata);
            // 设置存储类型为低频
            //            putObjectRequest.setStorageClass(StorageClass.Standard_IA);
            // 如果上传方式为File，可以设置ObjectMetadata
            //            putObjectRequest.setMetadata(objectMetadata);
            PutObjectResult putObjectResult = cosClient.putObject(putObjectRequest);
            String etag = putObjectResult.getETag();  // 获取文件的 etag
            // 关闭流
            fileInputStream.close();
        } finally {
            cosClient.shutdown();

        }
    }


    /**
     * 获取下载输入流
     */
    public void getObjectInput(String name) {
        COSClient cosClient = null;
        List<COSObjectSummary> objectSummaries = null;
        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, BUCKET_NAME);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
            // 方法1 获取下载输入流
            GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, name);
            COSObject cosObject = cosClient.getObject(getObjectRequest);
            COSObjectInputStream cosObjectInput = cosObject.getObjectContent();

        } finally {
            cosClient.shutdown();

        }
    }

    /**
     * 下载文件到本地
     */
    public void getObjectMetadata(String name) {
        COSClient cosClient = null;
        List<COSObjectSummary> objectSummaries = null;
        try {
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式

            // 方法2 下载文件到本地
            File downFile = new File("src/test/resources/mydown.txt");
            GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, name);
            ObjectMetadata downObjectMeta = cosClient.getObject(getObjectRequest, downFile);
        } finally {
            cosClient.shutdown();

        }
    }

    /**
     * 文件分块上传
     */
    public void putObjectThreadPool(String name) throws InterruptedException {
        COSClient cosClient = null;
        TransferManager transferManager = null;
        try {
            // 线程池大小，建议在客户端与COS网络充足(如使用腾讯云的CVM，同园区上传COS)的情况下，设置成16或32即可, 可较充分的利用网络资源
            // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
            ExecutorService threadPool = Executors.newFixedThreadPool(32);
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // 传入一个 threadpool, 若不传入线程池, 默认 TransferManager 中会生成一个单线程的线程池。
            transferManager = new TransferManager(cosClient, threadPool);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
            File localFile = new File("/data/dog.jpg");
            PutObjectRequest putObjectRequest = new PutObjectRequest(BUCKET_NAME, name, localFile);
            // 本地文件上传
            Upload upload = transferManager.upload(putObjectRequest);
            // 等待传输结束（如果想同步的等待上传结束，则调用 waitForCompletion）
            UploadResult uploadResult = upload.waitForUploadResult();
        } finally {
            cosClient.shutdown();
            // 关闭 TransferManger
            transferManager.shutdownNow();
        }
    }

    /**
     * 文件分块下载
     */
    public void downloadThreadPool(String name) throws InterruptedException {
        COSClient cosClient = null;
        TransferManager transferManager = null;
        try {
            // 线程池大小，建议在客户端与COS网络充足(如使用腾讯云的CVM，同园区上传COS)的情况下，设置成16或32即可, 可较充分的利用网络资源
            // 对于使用公网传输且网络带宽质量不高的情况，建议减小该值，避免因网速过慢，造成请求超时。
            ExecutorService threadPool = Executors.newFixedThreadPool(32);
            // 1 初始化用户身份信息(secretId, secretKey)
            COSCredentials cred = new BasicCOSCredentials(ACCESS_KEY, SECRET_KEY);
            // 2 设置bucket的区域, COS地域的简称请参照 https://cloud.tencent.com/document/product/436/6224
            // clientConfig中包含了设置region, https(默认http), 超时, 代理等set方法, 使用可参见源码或者接口文档FAQ中说明
            ClientConfig clientConfig = new ClientConfig(new Region(REGION_NAME));
            // 3 生成cos客户端
            cosClient = new COSClient(cred, clientConfig);
            // 传入一个 threadpool, 若不传入线程池, 默认 TransferManager 中会生成一个单线程的线程池。
            transferManager = new TransferManager(cosClient, threadPool);
            // bucket 的命名规则为{name}-{appid} ，此处填写的存储桶名称必须为此格式
            File localDownFile = new File("/data/dog.jpg");
            GetObjectRequest getObjectRequest = new GetObjectRequest(BUCKET_NAME, name);
            // 下载文件
            Download download = transferManager.download(getObjectRequest, localDownFile);
            // 等待传输结束（如果想同步的等待上传结束，则调用 waitForCompletion）
            download.waitForCompletion();
        } finally {
            cosClient.shutdown();
            // 关闭 TransferManger
            transferManager.shutdownNow();
        }
    }

    /**
     * 上传网络文件
     *
     * @param savePath   保存到本地的路径
     * @param fileName   文件名称，如果为空就用url中的文件名
     * @param bucketNmae 存储桶名称
     * @param keyDire    key的前缀，通常是文件夹名称
     * @throws IOException
     */
    public static void putUrlObject(String httpUrl, String savePath, String fileName, String bucketNmae,
                                    String keyDire) throws IOException {
        URL url = new URL(httpUrl);//初始化url对象;
        File file = new File(savePath, fileName);
        if (!file.exists()) {
            if (!file.getParentFile().exists()) {
                file.getParentFile().mkdirs();
            }
            file.createNewFile();
        }
        try (InputStream inputStream = url.openStream();
             // 定义输入字节缓冲流对象,获得url字节流
             BufferedInputStream in = new BufferedInputStream(inputStream);
             // 定义文件输出流对象file
             FileOutputStream fileOutputStream = new FileOutputStream(file)) {

            if (StringUtils.isBlank(fileName)) {
                // 当传入的文件名为空时用链接中的原名
                String replace = httpUrl.substring(httpUrl.lastIndexOf("/")).replace("/", "");
                // 对URL解码获取真实名称
                fileName = URLDecoder.decode(replace, StandardCharsets.UTF_8.name());
            }
            //file = new FileOutputStream(new File(filePath +"\\"+ fileName));
            int t;
            while ((t = in.read()) != -1) {
                fileOutputStream.write(t);
            }
        }
        CosUtil.putObject(bucketNmae, file, keyDire);
        FileUtils.forceDelete(file);
    }
}