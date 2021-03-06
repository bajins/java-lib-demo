package com.bajins.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import freemarker.cache.StringTemplateLoader;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateExceptionHandler;
import org.apache.http.client.config.RequestConfig;
import org.apache.http.config.Registry;
import org.apache.http.config.RegistryBuilder;
import org.apache.http.conn.socket.ConnectionSocketFactory;
import org.apache.http.conn.socket.PlainConnectionSocketFactory;
import org.apache.http.conn.ssl.SSLConnectionSocketFactory;
import org.apache.http.conn.ssl.TrustStrategy;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClientBuilder;
import org.apache.http.impl.client.HttpClients;
import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.apache.http.ssl.SSLContexts;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.Velocity;
import org.apache.velocity.context.Context;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.*;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.HttpComponentsClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;

import javax.net.ssl.*;
import java.io.IOException;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.Socket;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.security.KeyManagementException;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.X509Certificate;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * ????????????
 *
 * @see VelocityContext
 * @see Template
 * @see StringTemplateLoader
 * @see RestTemplate
 * @see org.springframework.jdbc.core.JdbcTemplate SQL?????????????????????????
 * @see org.springframework.jdbc.core.SimpleJdbcTemplate SQL?????????????????????????
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate SQL????????????????????????:name???
 * @see HIbernateTemplate
 * @see TransactionTemplate
 * @see DataSourceUtils
 * @see SmartDataSource
 * @see AbstractDataSource
 * @see SingleConnectionDataSource
 * @see DriverManagerDataSource
 * @see TransactionAwareDataSourceProxy
 * @see DataSourceTransactionManager
 */
public class TemplateLearning {

    /**
     * ?????????????????????
     *
     * @param template ??????
     * @param params   ??????
     * @return
     */
    public static String processTemplate(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        StringBuffer sb = new StringBuffer();
        Matcher m = Pattern.compile("\\$\\{\\w+\\}").matcher(template);
        while (m.find()) {
            String param = m.group();
            Object value = params.get(param.substring(2, param.length() - 1));
            m.appendReplacement(sb, value == null ? "" : value.toString());
        }
        m.appendTail(sb);
        return sb.toString();
    }

    /**
     * ??????Velocity???????????????????????????????????????
     *
     * @param template
     * @param params
     * @return
     */
    public static String processVelocity(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        Context context = new VelocityContext();
        //??????velocity???????????????
        Set<String> keys = params.keySet();
        for (String k : keys) {
            context.put(k, params.get(k));
        }
        StringWriter sw = new StringWriter();
        try {
            Velocity.evaluate(context, sw, "velocity", template);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return sw.toString();
    }

    /**
     * Freemarker????????????
     * https://freemarker.apache.org/docs/ref_builtins_date.html
     *
     * @param template ??????
     * @param params   ??????
     * @return
     */
    public static String processFreemarker(String template, Map<String, Object> params) {
        if (!StringUtils.hasText(template)) {
            return null;
        }
        try {
            StringTemplateLoader stl = new StringTemplateLoader();
            stl.putTemplate("content", template);
            Configuration cfg = new Configuration(Configuration.VERSION_2_3_20);
            cfg.setTemplateLoader(stl);
            /*<bean id="freemarkerConfig"
                class="org.springframework.web.servlet.view.freemarker.FreeMarkerConfigurer">
              <property name="freemarkerSettings">
                <props>
                  <prop key="classic_compatible">true</prop>
                </props>
              </property>
            </bean>*/
            //spring.freemarker.settings.classic_compatible=true
            // ???ftl?????????<!--#setting classic_compatible=true-->;
            // <#escape x as x!"">null?????????????????????</#escape> <#noescape>?????????null</#noescape>
            /* ${user?if_exists} ${user!''} ${user!} ${user?default('')} ${user???string(user,'')} */
            cfg.setClassicCompatible(true);// ???null????????????????????????
            cfg.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));// ?????????????????????
            cfg.setTimeFormat("HH:mm:ss.SSS");// ???????????????
            cfg.setDateFormat("yyyy-MM-dd");// ???????????????
            cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");// ?????????????????????
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            //Template tpl = new Template("content", template, cfg);
            Template tpl = cfg.getTemplate("content");
            StringWriter writer = new StringWriter();
            tpl.process(params, writer);
            return writer.toString();
            /*Environment env = tpl.createProcessingEnvironment(params, writer);
            env.setClassicCompatible(true);// ???null????????????????????????
            env.setCustomAttribute("paramMap", params);// ???????????????
            env.process();
            return writer.toString();*/
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * ???spring?????????aop?????????????????????spring?????????RestTemplate???????????????????????????????????????????????????????????????????????????????????????????????????????????????
     */
    public static void restTemplate() throws JsonProcessingException {
        RestTemplate restTemplate = new RestTemplate();
        //restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(Charset.forName("GBK")));
        restTemplate.getMessageConverters().forEach(httpMessageConverter -> { // ??????????????????
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter messageConverter = (StringHttpMessageConverter) httpMessageConverter;
                messageConverter.setDefaultCharset(StandardCharsets.UTF_8); // ????????????
            }
        });
        HttpHeaders headers = new HttpHeaders();
        // ??????
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("shopid", "1");

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("http://posturl",
                new HttpEntity<>(map, headers), String.class);
        if (stringResponseEntity.getStatusCode() != HttpStatus.OK) { // ????????????
            return;
        }
        System.out.println(stringResponseEntity);

        // JSON
        MediaType type = MediaType.parseMediaType(MediaType.APPLICATION_JSON_VALUE + "; charset=UTF-8");
        headers.setContentType(type);
        //headers.setAccept(Collections.singletonList(type));
        headers.add("Accept", type.toString());
        //headers.add("Accept", MediaType.APPLICATION_JSON.toString());

        ObjectMapper objectMapper = new ObjectMapper();
        String s = objectMapper.writeValueAsString(map);
        String result = restTemplate.postForObject("http://posturl", new HttpEntity<>(s, headers), String.class);

        //Type[] genericParameterTypes = thisMethod.getGenericParameterTypes(); // String url,Class<T> clazz
        //Type[] actualTypeArguments = ((ParameterizedType) genericParameterTypes[1]).getActualTypeArguments(); // T
        //Type ttype = actualTypeArguments[0]; // T.class
        //ParameterizedTypeReference<T> objectParameterizedTypeReference = ParameterizedTypeReference.forType(ttype);

        ParameterizedTypeReference<Map<String, Object>> parameterizedTypeReference =
                new ParameterizedTypeReference<Map<String, Object>>() {
                };
        /*
         * GET??????
         */
        String url = "https://test.com/tags/{1}/test?page={2}&count={3}&order=new&before_timestamp=";
        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<String>(headers), parameterizedTypeReference, "test", 1, 100);

        URI uri = UriComponentsBuilder.fromHttpUrl("http://posturl").build(true).toUri();
        RequestEntity<Void> accept = RequestEntity.get(uri).header("Accept", type.toString()).build();
        ResponseEntity<Map<String, Object>> exchange1 = restTemplate.exchange(accept, parameterizedTypeReference);
    }

    /**
     * ??????HTTP???HTTPS ?????????
     *
     * @return
     */
    public RestTemplate restTemplate1() {
        Registry<ConnectionSocketFactory> registry = RegistryBuilder.<ConnectionSocketFactory>create().register("http"
                        , PlainConnectionSocketFactory.getSocketFactory()).register("https",
                        SSLConnectionSocketFactory.getSocketFactory())//
                .build();
        PoolingHttpClientConnectionManager connectionManager = new PoolingHttpClientConnectionManager(registry);
        connectionManager.setMaxTotal(200);
        connectionManager.setDefaultMaxPerRoute(100);
        connectionManager.setValidateAfterInactivity(2000);
        RequestConfig requestConfig = RequestConfig.custom()
                // ?????????????????????(response)????????????????????????read timeout
                .setSocketTimeout(65000)
                // ??????????????????(????????????)????????????????????????connect timeout
                .setConnectTimeout(5000)
                // ?????????????????????????????????????????????????????????ConnectionPoolTimeoutException
                .setConnectionRequestTimeout(1000)//
                .build();
        CloseableHttpClient closeableHttpClient = HttpClientBuilder.create()//
                .setDefaultRequestConfig(requestConfig)//
                .setConnectionManager(connectionManager)//
                .build();
        ClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory(closeableHttpClient);
        return new RestTemplate(requestFactory);
    }

    /**
     * ??????HTTP???HTTPS ?????????
     *
     * @return
     */
    public RestTemplate restTemplate2() throws NoSuchAlgorithmException, KeyStoreException, KeyManagementException {
        //TrustStrategy acceptingTrustStrategy = (X509Certificate[] chain, String authType) -> true;
        TrustStrategy acceptingTrustStrategy = (x509Certificates, authType) -> true;

        SSLContext sslContext = SSLContexts.custom().loadTrustMaterial(null, acceptingTrustStrategy).build();
        /*KeyStore trustStore = KeyStore.getInstance(KeyStore.getDefaultType());
        SSLContext ctx = SSLContexts.custom().loadTrustMaterial(trustStore, acceptingTrustStrategy).build();*/
        /*SSLContext sslContext = new SSLContextBuilder().loadTrustMaterial(null, new TrustStrategy() {
            @Override
            public boolean isTrusted(X509Certificate[] arg0, String arg1) throws CertificateException {
                return true;
            }
        }).build();*/

        SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext);
        /*SSLConnectionSocketFactory csf = new SSLConnectionSocketFactory(sslContext, new String[]{"TLSv1"}, null,
                NoopHostnameVerifier.INSTANCE);*/

        /*Registry<ConnectionSocketFactory> sfr = RegistryBuilder.<ConnectionSocketFactory>create()
                .register("http", PlainConnectionSocketFactory.getSocketFactory())
                .register("https", csf != null ? csf : SSLConnectionSocketFactory.getSocketFactory())//
                .build();
        PoolingHttpClientConnectionManager pollingConnectionManager = new PoolingHttpClientConnectionManager(sfr);
        // pollingConnectionManager.setMaxTotal(maxTotal);
        // pollingConnectionManager.setDefaultMaxPerRoute(perRoute);

        CloseableHttpClient httpClient = HttpClients.custom()//
                        .setSSLSocketFactory(csf)//
                        .setConnectionManager(pollingConnectionManager)//
                        .build();*/
        CloseableHttpClient httpClient = HttpClients.custom().setSSLSocketFactory(csf).build();

        HttpComponentsClientHttpRequestFactory requestFactory = new HttpComponentsClientHttpRequestFactory();
        requestFactory.setHttpClient(httpClient);
        requestFactory.setConnectTimeout(5000);
        requestFactory.setReadTimeout(6000);
        requestFactory.setConnectionRequestTimeout(60000);

        return new RestTemplate(requestFactory);
    }

    /**
     * ??????:??????????????????https://blog.csdn.net/wltsysterm/article/details/80977455
     */
    public class HttpsClientRequestFactory extends SimpleClientHttpRequestFactory {

        @Override
        protected void prepareConnection(HttpURLConnection connection, String httpMethod) {
            try {
                if (!(connection instanceof HttpsURLConnection)) {
                    throw new RuntimeException("An instance of HttpsURLConnection is expected");
                }

                HttpsURLConnection httpsConnection = (HttpsURLConnection) connection;

                TrustManager[] trustAllCerts = new TrustManager[]{new X509TrustManager() {
                    @Override
                    public java.security.cert.X509Certificate[] getAcceptedIssuers() {
                        return null;
                    }

                    @Override
                    public void checkClientTrusted(X509Certificate[] certs, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] certs, String authType) {
                    }
                }};
                SSLContext sslContext = SSLContext.getInstance("TLS");
                sslContext.init(null, trustAllCerts, new java.security.SecureRandom());
                httpsConnection.setSSLSocketFactory(new MyCustomSSLSocketFactory(sslContext.getSocketFactory()));

                httpsConnection.setHostnameVerifier(new HostnameVerifier() {
                    @Override
                    public boolean verify(String s, SSLSession sslSession) {
                        return true;
                    }
                });

                super.prepareConnection(httpsConnection, httpMethod);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * We need to invoke sslSocket.setEnabledProtocols(new String[] {"SSLv3"});
         * see http://www.oracle.com/technetwork/java/javase/documentation/cve-2014-3566-2342133.html (Java 8 section)
         * SSLSocketFactory???????????? SSLSockets
         */
        private class MyCustomSSLSocketFactory extends SSLSocketFactory {

            private final SSLSocketFactory delegate;

            public MyCustomSSLSocketFactory(SSLSocketFactory delegate) {
                this.delegate = delegate;
            }

            // ??????????????????????????????????????????????????????????????????SSL?????????????????????????????????????????????
            // ??????????????????????????????????????????????????????????????????????????????
            @Override
            public String[] getDefaultCipherSuites() {
                return delegate.getDefaultCipherSuites();
            }

            // ??????????????????????????????SSL?????????????????????
            @Override
            public String[] getSupportedCipherSuites() {
                return delegate.getSupportedCipherSuites();
            }


            @Override
            public Socket createSocket(final Socket socket, final String host, final int port,
                                       final boolean autoClose) throws IOException {
                final Socket underlyingSocket = delegate.createSocket(socket, host, port, autoClose);
                return overrideProtocol(underlyingSocket);
            }


            @Override
            public Socket createSocket(final String host, final int port) throws IOException {
                final Socket underlyingSocket = delegate.createSocket(host, port);
                return overrideProtocol(underlyingSocket);
            }

            @Override
            public Socket createSocket(final String host, final int port, final InetAddress localAddress,
                                       final int localPort) throws IOException {
                final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
                return overrideProtocol(underlyingSocket);
            }

            @Override
            public Socket createSocket(final InetAddress host, final int port) throws IOException {
                final Socket underlyingSocket = delegate.createSocket(host, port);
                return overrideProtocol(underlyingSocket);
            }

            @Override
            public Socket createSocket(final InetAddress host, final int port, final InetAddress localAddress,
                                       final int localPort) throws IOException {
                final Socket underlyingSocket = delegate.createSocket(host, port, localAddress, localPort);
                return overrideProtocol(underlyingSocket);
            }

            private Socket overrideProtocol(final Socket socket) {
                if (!(socket instanceof SSLSocket)) {
                    throw new RuntimeException("An instance of SSLSocket is expected");
                }
                ((SSLSocket) socket).setEnabledProtocols(new String[]{"TLSv1"});
                return socket;
            }
        }
    }


    /**
     * RowMapper?????????????????????????????????????????????????????????????????????????????????mapRow(ResultSet rs, int rowNum)???????????????????????????????????????????????????
     * BeanPropertyRowMapper
     * ParameterizedBeanPropertyRowMapper
     * ColumnMapRowMapper
     * SingleColumnRowMapper
     * RowCallbackHandler???????????????ResultSet??????????????????????????????????????????processRow(ResultSet rs)??????????????????
     * ?????????????????????????????????rs.next()???????????????JdbcTemplate???????????????????????????????????????????????????????????????
     * ResultSetExtractor??????????????????????????????????????????????????????extractData(ResultSet rs)?????????????????????????????????????????????????????????
     */
    public void jdbcTemplate() {
        //List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, new Object[]{id}); // ??????????????????????????????
        /*
        List<Map<String, Object>> mapList = jdbcTemplate.query(sql, new Object[]{id},
            new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<Object>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        String key = JdbcUtils.lookupColumnName(rsmd, i);
                        key = JdbcUtils.convertUnderscoreNameToPropertyName(key); // ??????????????????
                        Object obj = JdbcUtils.getResultSetValue(rs, i);
                        mapOfColValues.put(key, obj);
                    }
                    return mapOfColValues;
                }
            });*/
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", new Date());
        System.out.println(processFreemarker("<#setting locale=\"zh_CN\">${date?string('yyyy-MM-dd HH:mm:ss')}", map));
    }
}
