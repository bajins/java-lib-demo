package com.bajins.demo;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.StringUtils;
import org.apache.http.HttpEntity;
import org.apache.http.NameValuePair;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.*;
import org.apache.http.client.utils.URIBuilder;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.NoopHostnameVerifier;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.entity.ContentType;
import org.apache.http.entity.StringEntity;
import org.apache.http.entity.mime.HttpMultipartMode;
import org.apache.http.entity.mime.MultipartEntityBuilder;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.ssl.SSLContextBuilder;
import org.apache.http.util.EntityUtils;

import javax.net.ssl.SSLContext;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/**
 * apache HttpComponents
 * 需要包httpmime
 *
 * @author Administrator
 */
public class HttpClient {

    /**
     * 创建忽略ssl验证HttpClient
     *
     * @return
     * @throws Exception
     */
    public static CloseableHttpClient getHttpClient() throws Exception {
        /*SSLContext sslcontext = SSLContext.getInstance("TLS");
        sslcontext.init(null, new TrustManager[]{}, new SecureRandom());

        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(sslcontext, (paramString, paramSSLSession)
        -> true);*/

        // 全部信任 不做身份鉴定
        SSLContext builder = new SSLContextBuilder().loadTrustMaterial(null, (x509Certificates, s) -> true).build();

        String[] protocols = {"SSLv2Hello", "SSLv3", "TLSv1", "TLSv1.2"};
        SSLConnectionSocketFactory sslsf = new SSLConnectionSocketFactory(builder, protocols, null,
                NoopHostnameVerifier.INSTANCE);
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http"
                , new PlainConnectionSocketFactory()).register("https", sslsf).build();
        PoolingHttpClientConnectionManager cm = new PoolingHttpClientConnectionManager(registry);
        cm.setMaxTotal(200);// max connection

        return HttpClients.custom().setSSLSocketFactory(sslsf).setConnectionManager(cm).setConnectionManagerShared(true).build();
    }

    /**
     * @param method
     * @param url
     * @param contentType
     * @param params
     * @param authorization 请求头鉴权，比如激光服务器就是base64编码后的字符串
     * @return
     * @throws IOException
     * @throws IllegalArgumentException
     * @throws URISyntaxException
     */
    public static String request(String method, String url, ContentType contentType, Map<String, Object> params,
                                 String authorization) throws IOException, IllegalArgumentException,
            URISyntaxException {
        if (StringUtils.isBlank(method)) {
            throw new IllegalArgumentException("method为空");
        }
        if (StringUtils.isBlank(url)) {
            throw new IllegalArgumentException("url为空");
        }
        // 通过址默认配置创建一个httpClient实例
        CloseableHttpClient httpClient = HttpClients.createDefault();
        CloseableHttpResponse response = null;
        // 配置请求参数实例
        RequestConfig requestConfig = RequestConfig.custom().setConnectTimeout(600000)// 连接主机服务超时时间
                .setConnectionRequestTimeout(600000)// 连接请求超时时间
                .setSocketTimeout(600000)// 数据读取超时时间(套接字超时)
                .build();
        try {
            if ("POST".equalsIgnoreCase(method) || "PATCH".equalsIgnoreCase(method) || "PUT".equalsIgnoreCase(method)) {
                // Content-Type只会存在于有请求数据实体时，而GET等请求没有请求数据实体
                if (null == contentType) {
                    contentType = ContentType.APPLICATION_JSON;
                }
                // 创建HttpEntity远程连接实例
                HttpEntityEnclosingRequestBase httpEntity;
                switch (method.toUpperCase()) {
                    case "POST":
                        httpEntity = new HttpPost(url);
                        break;
                    case "PATCH":
                        httpEntity = new HttpPatch(url);
                        break;
                    case "PUT":
                        httpEntity = new HttpPut(url);
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + method.toUpperCase());
                }
                // 为httpEntity实例设置配置
                httpEntity.setConfig(requestConfig);
                // setHeader(name, value)：如果Header中没有定义则添加，如果已定义则用新的value覆盖原用value值。
                // addHeader(name, value)：如果Header中没有定义则添加，如果已定义则保持原有value不改变。
                httpEntity.setHeader("Content-Type", contentType.getMimeType());
                // 设置请求头信息，鉴权
                if (StringUtils.isNotBlank(authorization)) {
                    httpEntity.setHeader("Authorization", authorization.trim());
                }
                // 封装post请求参数
                if (null != params && params.size() > 0) {
                    if (ContentType.APPLICATION_FORM_URLENCODED.equals(contentType)) {
                        List<NameValuePair> nvps = new ArrayList<>();
                        // 通过map集成entrySet方法获取entity循环遍历，获取迭代器
                        Iterator<Entry<String, Object>> iterator = params.entrySet().iterator();
                        while (iterator.hasNext()) {
                            Entry<String, Object> mapEntry = iterator.next();
                            nvps.add(new BasicNameValuePair(mapEntry.getKey(), mapEntry.getValue().toString()));
                        }
                        // 使用URL实体转换工具为httpEntity设置封装好的请求参数
                        httpEntity.setEntity(new UrlEncodedFormEntity(nvps, contentType.getCharset().displayName()));
                    } else if (ContentType.MULTIPART_FORM_DATA.equals(contentType)) {
                        MultipartEntityBuilder builder = MultipartEntityBuilder.create();
                        builder.setCharset(contentType.getCharset());
                        builder.setMode(HttpMultipartMode.BROWSER_COMPATIBLE);
                        // 把文件加到HTTP的post请求中
                        /*for (int i = 0; i < multipartFiles.size(); i++) {
                            builder.addBinaryBody(fileParName, multipartFiles.get(i).getInputStream(),
                                    ContentType.MULTIPART_FORM_DATA, multipartFile.getOriginalFilename());// 文件流
                        }
                        File f = new File("d:\test.text");
                        builder.addBinaryBody("img", new FileInputStream(f), ContentType.APPLICATION_OCTET_STREAM
                                , f.getName());*/
                        //决中文乱码
                        for (Entry<String, Object> entry : params.entrySet()) {
                            // 类似浏览器表单提交，对应input的name和value
                            builder.addTextBody(entry.getKey(), entry.getValue().toString());
                        }
                        // 使用URL实体转换工具为httpEntity设置封装好的请求参数
                        httpEntity.setEntity(builder.build());
                    } else if (ContentType.TEXT_XML.equals(contentType)) {
                        // 构造post参数
                        List<NameValuePair> nameValuePairList = new ArrayList<>();
                        // 封装请求参数
                        for (String key : params.keySet()) {
                            nameValuePairList.add(new BasicNameValuePair(key, params.get(key).toString()));
                        }
                        // 使用URL实体转换工具为httpEntity设置封装好的请求参数
                        httpEntity.setEntity(new UrlEncodedFormEntity(nameValuePairList));
                    } else if (ContentType.APPLICATION_JSON.equals(contentType)) {
                        // 使用URL实体转换工具为httpEntity设置请求参数:json字符串
                        httpEntity.setEntity(new StringEntity(new ObjectMapper().writeValueAsString(params),
                                contentType.getCharset()));
                    }
                }
                // httpClient对象执行post请求,并返回响应参数对象
                response = httpClient.execute(httpEntity);
            } else {
                // 由于GET请求的参数都是拼装在URL地址后方，所以我们要构建一个URL，带参数
                URIBuilder uriBuilder = new URIBuilder(url);
                // 封装请求参数
                for (String key : params.keySet()) {
                    uriBuilder.setParameter(key, params.get(key).toString());
                }
                // 通过址默认配置创建一个httpClient实例
                // httpClient = HttpClients.custom().setRetryHandler(myRetryHandler).build();
                // 创建httpGet远程连接实例
                HttpRequestBase httpRequestBase;
                switch (method.toUpperCase()) {
                    case "GET":
                        httpRequestBase = new HttpGet(uriBuilder.build());
                        break;
                    case "DELETE":
                        httpRequestBase = new HttpPatch(uriBuilder.build());
                        break;
                    case "HEAD":
                        httpRequestBase = new HttpHead(uriBuilder.build());
                        break;
                    case "OPTIONS":
                        httpRequestBase = new HttpOptions(uriBuilder.build());
                        break;
                    case "TRACE":
                        httpRequestBase = new HttpTrace(uriBuilder.build());
                        break;
                    default:
                        throw new IllegalStateException("Unexpected value: " + method.toUpperCase());
                }
                // 设置请求头信息，鉴权
                // setHeader(name, value)：如果Header中没有定义则添加，如果已定义则用新的value覆盖原用value值。
                // addHeader(name, value)：如果Header中没有定义则添加，如果已定义则保持原有value不改变。
                if (StringUtils.isNotBlank(authorization)) {
                    httpRequestBase.setHeader("Authorization", authorization.trim());
                }
                // 为httpGet实例设置配置
                httpRequestBase.setConfig(requestConfig);
                // 执行get请求得到返回对象
                response = httpClient.execute(httpRequestBase);
            }
            //获取响应状态码
            // httpResponse.getStatusLine().getStatusCode();
            HttpEntity entity = response.getEntity();
            // 通过EntityUtils中的toString方法将结果转换为字符串
            String s = EntityUtils.toString(entity);
            //必须完全的consume，否则connection manager可能无法复用连接
            EntityUtils.consume(entity);
            return s;
        } finally {
            // 关闭资源
            if (null != response) {
                response.close();
            }
            if (null != httpClient) {
                httpClient.close();
            }
        }
    }

    /**
     * GET请求，由于GET等请求没有请求数据实体，所以不存在Content-Type字段
     *
     * @param url
     * @param params
     * @param authorization
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String get(String url, Map<String, Object> params, String authorization) throws IOException,
            URISyntaxException {
        return request("GET", url, null, params, authorization);
    }

    public static String get(String url, Map<String, Object> params) throws IOException, URISyntaxException {
        return get(url, params, "");
    }

    public static String get(String url) throws IOException, URISyntaxException {
        return get(url, null, "");
    }

    /**
     * POST请求
     *
     * @param url
     * @param contentType
     * @param params
     * @param authorization
     * @return
     * @throws IOException
     * @throws URISyntaxException
     */
    public static String post(String url, ContentType contentType, Map<String, Object> params, String authorization) throws IOException, URISyntaxException {
        return request("POST", url, contentType, params, authorization);
    }

    public static String post(String url, ContentType contentType, Map<String, Object> params) throws IOException,
            URISyntaxException {
        return post(url, contentType, params, "");
    }

    public static String postJson(String url, Map<String, Object> params, String authorization) throws IOException,
            URISyntaxException {
        return post(url, ContentType.APPLICATION_JSON, params, authorization);
    }

    public static String postJson(String url, Map<String, Object> params) throws IOException, URISyntaxException {
        return post(url, ContentType.APPLICATION_JSON, params, "");
    }
}
