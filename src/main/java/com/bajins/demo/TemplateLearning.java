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
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.*;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.support.AbstractPlatformTransactionManager;
import org.springframework.transaction.support.ResourceTransactionManager;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.transaction.support.TransactionTemplate;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import org.springframework.ws.client.core.WebServiceTemplate;

import javax.annotation.PostConstruct;
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
 * 各种模板
 *
 * @see VelocityContext
 * @see Template
 * @see StringTemplateLoader
 * @see RestTemplate
 * @see WebServiceTemplate
 * https://docs.spring.io/spring-ws/docs/4.0.3-SNAPSHOT/reference/html/#client-web-service-template
 * @see org.springframework.jdbc.core.JdbcTemplate SQL中使用（?）占位符
 * @see org.springframework.jdbc.core.SimpleJdbcTemplate SQL中使用（?）占位符
 * @see org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate SQL中使用命名参数（:name）
 * @see HIbernateTemplate
 * @see TransactionTemplate
 * @see DataSourceUtils
 * @see SmartDataSource
 * @see AbstractDataSource
 * @see SingleConnectionDataSource
 * @see DriverManagerDataSource
 * @see PlatformTransactionManager 事务管理
 * @see AbstractPlatformTransactionManager
 * @see ResourceTransactionManager
 * @see DataSourceTransactionManager
 * @see TransactionAwareDataSourceProxy
 * @see TransactionSynchronizationManager
 * @see TransactionSynchronization
 * @see TransactionAspectSupport
 * @see TransactionCallbackWithoutResult
 * <br/>
 * @see MultiValueMap
 * @see LinkedMultiValueMap
 */
public class TemplateLearning {

    private JdbcTemplate jdbcTemplate;

    @PostConstruct
    public void init() {
        DriverManagerDataSource dataSource = new DriverManagerDataSource();
        dataSource.setDriverClassName("org.postgresql.Driver");
        dataSource.setUrl("jdbc:postgresql://localhost:5432/your_database_name");
        dataSource.setUsername("your_username");
        dataSource.setPassword("your_password");

        /*DataSource dataSource = DataSourceBuilder.create()
                .driverClassName("oracle.jdbc.OracleDriver")
                .url("jdbc:oracle:thin:@//localhost:1521/ORCL")
                .username("your_username")
                .password("your_password")
                .build();*/
        this.jdbcTemplate = new JdbcTemplate(dataSource); // 使用JdbcTemplate连接第三方数据库
    }

    /**
     * 自定义渲染模板
     *
     * @param template 模版
     * @param params   参数
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
     * 使用Velocity作为变量替换引擎，渲染模板
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
        //拼装velocity用模板参数
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
     * Freemarker渲染模板
     * https://freemarker.apache.org/docs/ref_builtins_date.html
     *
     * @param template 模版
     * @param params   参数
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
            // 在ftl前加入<!--#setting classic_compatible=true-->;
            // <#escape x as x!"">null替换为空字符串</#escape> <#noescape>不处理null</#noescape>
            /* ${user?if_exists} ${user!''} ${user!} ${user?default('')} ${user???string(user,'')} */
            cfg.setClassicCompatible(true);// 为null则替换为空字符串
            cfg.setTimeZone(TimeZone.getTimeZone("GMT+8:00"));// 获取东八区时间
            cfg.setTimeFormat("HH:mm:ss.SSS");// 时间格式化
            cfg.setDateFormat("yyyy-MM-dd");// 日期格式化
            cfg.setDateTimeFormat("yyyy-MM-dd HH:mm:ss.SSS");// 日期时间格式化
            cfg.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
            //Template tpl = new Template("content", template, cfg);
            Template tpl = cfg.getTemplate("content");
            StringWriter writer = new StringWriter();
            tpl.process(params, writer);
            return writer.toString();
            /*Environment env = tpl.createProcessingEnvironment(params, writer);
            env.setClassicCompatible(true);// 为null则替换为空字符串
            env.setCustomAttribute("paramMap", params);// 自定义属性
            env.process();
            return writer.toString();*/
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 在spring中使用aop注入时需注意：spring可能有RestTemplate的默认配置（请求头等），导致在某些情况下会有差异，所以最好是自己进行初始化
     */
    public static void restTemplate() throws JsonProcessingException {
        SimpleClientHttpRequestFactory requestFactory = new SimpleClientHttpRequestFactory();
        requestFactory.setConnectTimeout(1000);
        RestTemplate restTemplate = new RestTemplate(requestFactory); // 根据工厂创建

        MappingJackson2HttpMessageConverter converter = new MappingJackson2HttpMessageConverter();
        List<MediaType> mediaTypes = new ArrayList<>();
        mediaTypes.add(MediaType.APPLICATION_OCTET_STREAM);
        converter.setSupportedMediaTypes(mediaTypes);
        restTemplate.getMessageConverters().add(converter); // 添加支持的类型
        /*converter.setSupportedMediaTypes(Arrays.asList(MediaType.ALL));
        restTemplate.getMessageConverters().add(0, converter);*/

        //restTemplate.getMessageConverters().set(1, new StringHttpMessageConverter(Charset.forName("GBK")));
        restTemplate.getMessageConverters().forEach(httpMessageConverter -> { // 请求设置编码
            if (httpMessageConverter instanceof StringHttpMessageConverter) {
                StringHttpMessageConverter messageConverter = (StringHttpMessageConverter) httpMessageConverter;
                messageConverter.setDefaultCharset(StandardCharsets.UTF_8); // 编码格式
            }
        });
        HttpHeaders headers = new HttpHeaders();
        // 表单
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("shopid", "1");
        /*MultiValueMap<String, FileSystemResource> multiValueMap = new LinkedMultiValueMap();
        FileSystemResource fsr = new FileSystemResource(new File("")); // 上传文件
        multiValueMap.add("file",fsr);*/

        ResponseEntity<String> stringResponseEntity = restTemplate.postForEntity("http://posturl",
                new HttpEntity<>(map, headers), String.class);
        if (stringResponseEntity.getStatusCode() != HttpStatus.OK) { // 请求异常
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
         * GET请求
         */
        String url = "https://test.com/tags/{1}/test?page={2}&count={3}&order=new&before_timestamp=";
        ResponseEntity<Map<String, Object>> exchange = restTemplate.exchange(url, HttpMethod.GET,
                new HttpEntity<String>(headers), parameterizedTypeReference, "test", 1, 100);

        URI uri = UriComponentsBuilder.fromHttpUrl("http://posturl").build(true).toUri();
        RequestEntity<Void> accept = RequestEntity.get(uri).header("Accept", type.toString()).build();
        ResponseEntity<Map<String, Object>> exchange1 = restTemplate.exchange(accept, parameterizedTypeReference);


        // 上传表单参数（含文件）
        MultiValueMap<String, String> map1 = new LinkedMultiValueMap<>();
        map1.add("field1", "value1");
        map1.add("field2", "value2");
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map1, headers);
        ResponseEntity<String> response = restTemplate.postForEntity("http://posturl", request, String.class);
    }

    /**
     * 支持HTTP、HTTPS 方式一
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
                // 服务器返回数据(response)的时间，超时抛出read timeout
                .setSocketTimeout(65000)
                // 连接上服务器(握手成功)的时间，超时抛出connect timeout
                .setConnectTimeout(5000)
                // 从连接池中获取连接的超时时间，超时抛出ConnectionPoolTimeoutException
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
     * 支持HTTP、HTTPS 方式二
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
     * 声明:此代码摘录自https://blog.csdn.net/wltsysterm/article/details/80977455
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
         * SSLSocketFactory用于创建 SSLSockets
         */
        private class MyCustomSSLSocketFactory extends SSLSocketFactory {

            private final SSLSocketFactory delegate;

            public MyCustomSSLSocketFactory(SSLSocketFactory delegate) {
                this.delegate = delegate;
            }

            // 返回默认启用的密码套件。除非一个列表启用，对SSL连接的握手会使用这些密码套件。
            // 这些默认的服务的最低质量要求保密保护和服务器身份验证
            @Override
            public String[] getDefaultCipherSuites() {
                return delegate.getDefaultCipherSuites();
            }

            // 返回的密码套件可用于SSL连接启用的名字
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
     * ResultSetExtractor：用于处理整个 ResultSet，即一次性处理所有行，需要自行调用ResultSet的next()方法。
     * RowMapper：用于逐行处理 ResultSet，每次处理一行数据。不需要调用ResultSet的next()方法，会调用RowMapperResultSetExtractor的extractData()方法默认实现。
     * BeanPropertyRowMapper
     * ParameterizedBeanPropertyRowMapper
     * ColumnMapRowMapper
     * SingleColumnRowMapper 适用于查询结果只有一列，且你只想获取该列的值，而不需要将其封装到自定义对象中的场景
     * RowCallbackHandler：用于处理ResultSet的每一行结果集，并不返回任何值。不需要调用ResultSet的next()方法。
     * KeyValueRowMapper：用于将结果集中的每一行映射到一个Map对象，其中Map的key为列名，value为列值。
     * AggregationRowMapper：用于聚合查询结果集，将结果集中的每一行映射到一个对象，并对其进行聚合。
     * SqlRowSetRowMapper：用于将SqlRowSet结果集映射到一个对象。
     * PreparedStatement
     * BatchPreparedStatementSetter
     */
    public void jdbcTemplate() {
        // sql求和
        /*BigDecimal uploadQty = jdbcTemplate.query(sql, new Object[] { id }, new ResultSetExtractor<BigDecimal>() {
            @Override
            public BigDecimal extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (!rs.next()) { // 调用一次next()方法，初始状态下使游标指向第一行，且判断是否有数据
                    return BigDecimal.ZERO;
                }
                BigDecimal bigDecimal = rs.getBigDecimal(1);
                return bigDecimal != null ? bigDecimal : BigDecimal.ZERO;
            }
        });
        BigDecimal uploadQty = jdbcTemplate.query(sql, new Object[] { id }, (rs) -> {
            if (!rs.next()) { // 调用一次next()方法，初始状态下使游标指向第一行，且判断是否有数据
                return BigDecimal.ZERO;
            }
            BigDecimal bigDecimal = rs.getBigDecimal(1);
            return bigDecimal != null ? bigDecimal : BigDecimal.ZERO;
        });
        Date currentTime = erpJdbcTemplate.query("SELECT CURRENT_TIMESTAMP FROM DUAL", (rs) -> {
            if (!rs.next()) { // 调用一次next()方法，初始状态下使游标指向第一行，且判断是否有数据
                return null;
            }
            return rs.getTimestamp(1);
        });
        */

        /*
        Map<String, Object> configMap = jdbcTemplate.query(sql, new ResultSetExtractor<Map<String, Object>>() {
            @Override
            public Map<String, Object> extractData(ResultSet rs) throws SQLException, DataAccessException {
                if (!rs.next()) { // 调用一次next()方法，初始状态下使游标指向第一行，且判断是否有数据
                    return null;
                }
                Map<String, Object> resultMap = new HashMap<>();
                // 获取结果集的元数据
                ResultSetMetaData metaData = rs.getMetaData();
                int columnCount = metaData.getColumnCount();
                // 遍历每一列
                for (int i = 1; i <= columnCount; i++) {
                    // 获取列名和列值
                    String columnName = metaData.getColumnLabel(i).toLowerCase();
                    Object columnValue = rs.getObject(i);
                    // 将列名和列值添加到 Map 中
                    resultMap.put(JdbcUtils.convertUnderscoreNameToPropertyName(columnName), columnValue);
                }
                if (MapUtils.isEmpty(resultMap)) {
                    return null;
                }
                return resultMap;
            }
        }, configCode);
         */

        //List<Map<String, Object>> mapList = jdbcTemplate.queryForList(sql, new Object[]{id}); // 字段为下划线大写风格
        /*
        List<Map<String, Object>> mapList = jdbcTemplate.query(sql, new Object[]{id},
            new RowMapper<Map<String, Object>>() {
                @Override
                public Map<String, Object> mapRow(ResultSet rs, int rowNum) throws SQLException {
                    ResultSetMetaData rsmd = rs.getMetaData();
                    int columnCount = rsmd.getColumnCount();
                    Map<String, Object> mapOfColValues = new LinkedCaseInsensitiveMap<Object>(columnCount);
                    for (int i = 1; i <= columnCount; i++) {
                        String key = JdbcUtils.lookupColumnName(rsmd, i).toLowerCase();
                        key = JdbcUtils.convertUnderscoreNameToPropertyName(key); // 下划线转驼峰
                        Object obj = JdbcUtils.getResultSetValue(rs, i);
                        mapOfColValues.put(key, obj);
                    }
                    return mapOfColValues;
                }
            });*/

        /*List<TestDto> testDtos = jdbcTemplate.query(mlSql, new Object[] { id },
                BeanPropertyRowMapper.newInstance(TestDto.class));*/
        /*List<TestDto> query = jdbcTemplate.query(mlSql, new Object[] { moId }, new RowMapper<TestDto>() {
            @Override
            public TestDto mapRow(ResultSet rs, int rowNum) throws SQLException {
                BigDecimal qty = rs.getBigDecimal("qty") != null ? rs.getBigDecimal("qty")
                        : BigDecimal.ZERO;
                TestDto testDto = new TestDto();
                testDto.setId(rs.getString("id"));
                testDto.setQty(qty);
                return testDto;
                // return DBUtil.getRecord(rs, TestDto.class);
            }
        });*/

        /*
        // https://blog.csdn.net/qq_38737586/article/details/110595655
        jdbcTemplate().batchUpdate(sql, new BatchPreparedStatementSetter() { // 小批量插入或更新数据（批量执行多条SQL）
            public int getBatchSize() { // 返回批次的大小
                return list.size();
            }
            public void setValues(PreparedStatement ps, int i) throws SQLException {
                // i：在这个批次中，正在执行操作的索引，从0算起
                Map<?, ?> obj = (Map<?, ?>)list.get(i);
                if(obj == null){
                    return;
                }
                ps.setString(1,Z); // 参数填充从1开始
                ps.setString(2, X);
                ps.setString(3, B);
                ps.setString(4, obj.get("O") == null ? "" : obj.get("O").toString());
                ps.setString(5, N);
            }
        });
        // 大批量插入或更新数据（批量执行多条SQL）
        DataSource dataSource = jdbcTemplate.getDataSource();
        BatchSqlUpdate bsu = new BatchSqlUpdate(dataSource, "insert into user(name,number) values (?,?)");
        bsu.setBatchSize(1000);
        bsu.setTypes(new int[]{Types.VARCHAR, Types.VARCHAR});

        for (User user : users) {
            if(user == null){
                continue;
            }
            bsu.update(new Object[]{user.getName(), user.getName()});
        }
        bsu.flush();
        */
    }

    public static void main(String[] args) {
        Map<String, Object> map = new HashMap<>();
        map.put("date", new Date());
        System.out.println(processFreemarker("<#setting locale=\"zh_CN\">${date?string('yyyy-MM-dd HH:mm:ss')}", map));
    }
}
