package com.bajins.demo;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.MediaType;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.http.server.ServletServerHttpResponse;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import javax.net.ssl.HttpsURLConnection;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import javax.servlet.http.Part;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.net.*;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * http工具类
 *
 * @author claer https://www.bajins.com
 * @program com.bajins.api.utils.http
 * @description HttpUtil
 * @create 2019-03-10 20:54
 */
public class HttpUtil {

    /**
     * 私有化，禁止创建
     */
    public HttpUtil() {
    }

    /**
     * 判断是否为内网IP
     *
     * @param ip
     * @return
     */
    public static boolean isIntranetIp(String ip) {
        /**
         * 内网IP范围
         * <pre>
         * 10.0.0.0/8-------10.0.0.0~10.255.255.255（A类）
         * 172.16.0.0/12----172.16.0.0~172.31.255.255（B类）
         * 192.168.0.0/16---192.168.0.0~192.168.255.255（C类）
         * </pre>
         */
        String reg = "^(192\\.168|172\\.(1[6-9]|2\\d|3[0,1]))(\\.(2[0-4]\\d|25[0-5]|[0,1]?\\d?\\d)){2}$"
                + "|^10(\\.([2][0-4]\\d|25[0-5]|[0,1]?\\d?\\d)){3}$";
        /*String reg = "(10|172|192|127)\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}"
                + "|[2][0-5]{0,2}|[3-9][0-9]{0,1})\\.([0-1][0-9]{0,2}|[2][0-5]{0,2}|[3-9][0-9]{0,1})";*/
        Pattern p = Pattern.compile(reg);
        Matcher matcher = p.matcher(ip);
        return matcher.find(); // true：是内网IP
    }

    /**
     * 根据IP获取MAC
     *
     * @param ip
     * @return
     * @throws IOException
     */
    public static String getMacByIp(String ip) throws IOException {
        if ("127.0.0.1".equals(ip) || "0:0:0:0:0:0:0:1".equals(ip)) { // 获取本地MAC地址
            InetAddress inetAddress = InetAddress.getLocalHost();
            NetworkInterface ni = NetworkInterface.getByInetAddress(inetAddress);
            //ni.getInetAddresses().nextElement().getAddress();
            byte[] mac = ni.getHardwareAddress();
            /*String sMAC = null;
            Formatter formatter = new Formatter();
            for (int i = 0; i < mac.length; i++) {
                sMAC = formatter.format(Locale.getDefault(), "%02X%s", mac[i], (i < mac.length - 1) ? "-" : "")
                .toString();
            }
            return sMAC;*/
            StringBuilder sb = new StringBuilder();
            for (int i = 0; i < mac.length; i++) {
                if (i != 0) {
                    sb.append("-");
                }
                String s = Integer.toHexString(mac[i] & 0xFF); // 把byte转换为正整数
                sb.append((s.length() == 1) ? (0 + s) : s);
            }
            return sb.toString().trim().toUpperCase();
        }
        String macAddress = "";
        Runtime runtime = Runtime.getRuntime();
        Process p;
        String osName = System.getProperty("os.name");
        if (osName.contains("Windows")) {
            p = runtime.exec("nbtstat -A " + ip);
        } else {
            runtime.exec("ping " + ip + " -c 1");
            p = runtime.exec("arp -a");
        }
        try (InputStream inputStream = p.getInputStream();
             InputStreamReader isr = new InputStreamReader(inputStream);
             BufferedReader br = new BufferedReader(isr)) {
            String line;
            while ((line = br.readLine()) != null) {
                int index = line.indexOf("MAC Address = ");
                if (index != -1) {
                    macAddress = line.substring(index + "MAC Address = ".length()).trim().toUpperCase();
                }
            }
        }
        return macAddress;
    }

    /**
     * 获取用户真实IP地址
     * <p>
     * 当我们通过request获取客户端IP时，如果对自身服务器做了反向代理。
     * 通过request.getRemoteAddr();可能获取到的是代理服务器的IP，而无法获取到用户请求IP
     *
     * @param request
     * @return java.lang.String
     */
    public static String getIpAddress(HttpServletRequest request) {
        // X-Real-IP：Nginx服务代理
        String ipAddresses = request.getHeader("X-Real-IP");

        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            // Proxy-Client-IP：Apache 服务代理
            ipAddresses = request.getHeader("Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            // WL-Proxy-Client-IP：WebLogic 服务代理
            ipAddresses = request.getHeader("WL-Proxy-Client-IP");
        }
        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            // HTTP_CLIENT_IP：有些代理服务器
            ipAddresses = request.getHeader("HTTP_CLIENT_IP");
        }
        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            // X-Forwarded-For：Squid 服务代理 和 Nginx服务代理
            ipAddresses = request.getHeader("X-Forwarded-For");
        }
        if (StringUtils.isBlank(ipAddresses) || "unknown".equalsIgnoreCase(ipAddresses)) {
            ipAddresses = request.getRemoteAddr();
            if ("127.0.0.1".equals(ipAddresses) || "0:0:0:0:0:0:0:1".equals(ipAddresses)) {
                InetAddress inetAddress = null;
                try {
                    inetAddress = InetAddress.getLocalHost(); // 根据网卡获取本地的IP地址
                } catch (UnknownHostException e) {
                    e.printStackTrace();
                }
                if (inetAddress != null) {
                    ipAddresses = inetAddress.getHostAddress(); // 获取本机的IP地址
                }
            }
        }
        // 有些网络通过多层代理，那么会获取到以逗号（,）分割的多个IP，第一个才是真实IP
        int index = ipAddresses.indexOf(",");
        if (index != -1) {
            ipAddresses = ipAddresses.substring(0, index);
        }
        return ipAddresses;
    }

    /**
     * 从当前HttpServletRequest中获取IP
     *
     * @param
     * @return java.lang.String
     */
    public static String getIpAddress() {
        HttpServletRequest httpServletRequest = HttpUtil.getHttpServletRequest();
        return getIpAddress(httpServletRequest);
    }

    /**
     * 获取HttpServletRequest
     *
     * @param
     * @return javax.servlet.http.HttpServletRequest
     */
    public static HttpServletRequest getHttpServletRequest() {
        return ((ServletRequestAttributes) Objects.requireNonNull(RequestContextHolder.getRequestAttributes())).getRequest();
    }

    /**
     * 请求返回内容
     *
     * @param response
     * @param o
     * @throws IOException
     */
    public static void write(HttpServletResponse response, Object o) throws IOException {
        response.setContentType("application/json; charset=utf-8");
        try (PrintWriter out = response.getWriter();) {
            //json返回
            out.println(new ObjectMapper().writeValueAsString(o));
            out.flush();
        }
    }

    /**
     * 根据域获取上下文路径
     *
     * @param name 文件或文件夹名
     * @return java.lang.String
     */
    public static String getRealPath(String name) {
        if (StringUtils.isBlank(name)) {
            throw new IllegalArgumentException("参数为空");
        }
        return getHttpServletRequest().getSession().getServletContext().getRealPath(name);
    }

    /**
     * 根据request获取域名
     *
     * @return java.lang.String
     */
    public static String getUrl() {
        StringBuffer url = getHttpServletRequest().getRequestURL();
        //获取域名
        return url.delete(url.length() - getHttpServletRequest().getRequestURI().length(),
                url.length()).append(getHttpServletRequest().getServletContext().getContextPath()).append("/").toString();
    }

    /**
     * 从InputStream中获取参数
     *
     * @param inputStream
     * @return
     * @throws IOException
     */
    public static String getStringParam(InputStream inputStream) throws IOException {
        Objects.requireNonNull(inputStream);
        // 从输入流读取返回内容
        try (InputStreamReader inputStreamReader = new InputStreamReader(inputStream);
             BufferedReader bufferedReader = new BufferedReader(inputStreamReader);) {
            String str;
            StringBuilder buffer = new StringBuilder();
            while ((str = bufferedReader.readLine()) != null) {
                buffer.append(str);
            }
            return buffer.toString();
        } finally {
            inputStream.close();
        }
    }

    /**
     * 从InputStream中获取参数并解析为xml
     *
     * @param inputStream
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getXmlParam(InputStream inputStream) throws IOException, ParserConfigurationException
            , SAXException {
        Objects.requireNonNull(inputStream);
        // 从输入流读取返回内容
        try {
            DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
            return documentBuilder.parse(inputStream);
        } finally {
            inputStream.close();
        }
    }

    /**
     * 字符串解析为xml
     *
     * @param xmlText
     * @return
     * @throws IOException
     * @throws ParserConfigurationException
     * @throws SAXException
     */
    public static Document getXmlParam(String xmlText) throws IOException, ParserConfigurationException
            , SAXException {
        DocumentBuilder documentBuilder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
        try (StringReader sr = new StringReader(xmlText)) {
            InputSource is = new InputSource(sr);
            return documentBuilder.parse(is);
        }
    }

    /**
     * 获取HttpServletRequest中的参数并封装到Map<String, Object>
     *
     * @param request
     * @return Map<String, Object>
     */
    public static Map<String, String> getParameterMap(HttpServletRequest request) {
        Objects.requireNonNull(request);
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            String[] values = entry.getValue();
            StringBuilder value = new StringBuilder();
            for (String s : values) { // 用于请求参数中有多个相同名称
                value.append(s).append(",");
            }
            if (value.length() > 1) {
                value.deleteCharAt(value.length() - 1);
            }
            returnMap.put(entry.getKey(), value.toString());
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数使用Arrays.toString转换，并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, String> getParameterMapArrays(HttpServletRequest request) {
        Objects.requireNonNull(request);
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            returnMap.put(entry.getKey(), String.join(",", entry.getValue()));
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数使用StringUtils.join转换，并封装到Map<String, Object>
     *
     * @param request
     * @return Map<String, Object>
     */
    public static Map<String, String> getParameterMapJoin(HttpServletRequest request) {
        Objects.requireNonNull(request);
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            returnMap.put(entry.getKey(), StringUtils.join(entry.getValue(), ","));
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数使用json转换，并封装到Map<String, Object>
     *
     * @param request
     * @return Map<String, Object>
     */
    public static Map<String, String> getParameterMapJson(HttpServletRequest request) throws JsonProcessingException {
        Objects.requireNonNull(request);
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            returnMap.put(entry.getKey(), new ObjectMapper().writeValueAsString(entry.getValue()));
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数使用ArrayUtils.toString转换，并封装到Map<String, Object>
     *
     * @param request
     * @return Map<String, Object>
     */
    public static Map<String, String> getParameterMapArrayUtils(HttpServletRequest request) {
        Objects.requireNonNull(request);
        // 获取HttpServletRequest中的参数map
        Map<String, String[]> properties = request.getParameterMap();
        Map<String, String> returnMap = new HashMap<>();
        for (Map.Entry<String, String[]> entry : properties.entrySet()) {
            returnMap.put(entry.getKey(), ArrayUtils.toString(entry.getValue(), ","));
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的请求头并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, String> getHeaderMap(HttpServletRequest request) {
        Objects.requireNonNull(request);
        Map<String, String> returnMap = new HashMap<>();
        Enumeration<String> headerNames = request.getHeaderNames();
        while (headerNames.hasMoreElements()) {
            String key = headerNames.nextElement();
            String value = request.getHeader(key);
            returnMap.put(key, value);
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的Attribute并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, Object> getAttributeMap(HttpServletRequest request) {
        Objects.requireNonNull(request);
        Map<String, Object> returnMap = new HashMap<>();
        Enumeration<String> attributeNames = request.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            Object attribute = request.getAttribute(key);
            returnMap.put(key, attribute);
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中session的Attribute并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, Object> getAttributeMapBySession(HttpServletRequest request) {
        Objects.requireNonNull(request);
        Map<String, Object> returnMap = new HashMap<>();
        HttpSession session = request.getSession();
        Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String key = attributeNames.nextElement();
            Object attribute = session.getAttribute(key);
            returnMap.put(key, attribute);
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, String> getParameterToMap(HttpServletRequest request) {
        Objects.requireNonNull(request);
        Map<String, String> returnMap = new HashMap<>();
        Enumeration<String> parameterNames = request.getParameterNames();
        while (parameterNames.hasMoreElements()) {
            String key = parameterNames.nextElement();
            String value = request.getParameter(key);
            returnMap.put(key, value);
        }
        return returnMap;
    }

    /**
     * 获取HttpServletRequest中的参数（包含文件）并封装到Map<String, Object>
     *
     * @param request
     * @return
     */
    public static Map<String, Part> getPartsMap(HttpServletRequest request) throws ServletException, IOException {
        Objects.requireNonNull(request);
        Map<String, Part> returnMap = new HashMap<>();
        Collection<Part> parts = request.getParts();
        for (Part part : parts) {
            String name = part.getName();
            returnMap.put(name, part);
        }
        return returnMap;
    }


    /**
     * 批量下载网络文件
     * 传入要下载的图片的url列表，将url所对应的图片下载到本地
     *
     * @param urlList  url链接
     * @param path     保存地址
     * @param fileName 文件名
     * @throws Exception
     */
    public static void multipleDownloadFile(List<String> urlList, String path, String fileName) throws IOException {
        if (urlList == null || urlList.size() == 0) {
            throw new IllegalArgumentException("下载地址为空");
        }
        String ua = "Mozilla/5.0 (Windows NT 6.1; WOW64; rv:55.0) Gecko/20100101 Firefox/55.0";
        // 在Windows下的路径分隔符（\）和在Linux下的路径分隔符（/）是不一样的，如果要考虑跨平台，则最好用File.separator
        for (String urlString : urlList) {
            HttpsURLConnection request = request(urlString, "GET", null, null, null, ua);
            try (FileOutputStream fileOutputStream = new FileOutputStream(path + File.separator + fileName);
                 InputStream inputStream = request.getInputStream()) {
                if (StringUtils.isBlank(fileName)) {
                    // 当传入的文件名为空时用链接中的原名
                    String replace = urlString.substring(urlString.lastIndexOf("/")).replace("/", "");
                    // 对URL解码获取真实名称
                    fileName = URLDecoder.decode(replace, StandardCharsets.UTF_8.name());
                }
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) != -1) {
                    fileOutputStream.write(buffer, 0, length);
                }
            }
        }
    }

    /**
     * 下载网络图片上传到服务器上
     *
     * @param httpUrl    图片网络地址
     * @param filePath   图片保存路径
     * @param myFileName 图片文件名(null时用网络图片原名)
     * @return 返回图片名称
     */
    public static File getUrlFile(String httpUrl, String filePath, String myFileName) throws IOException {
        URL url = new URL(httpUrl);//初始化url对象;
        File file = new File(filePath, myFileName);
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

            if (StringUtils.isBlank(myFileName)) {
                // 当传入的文件名为空时用链接中的原名
                String replace = httpUrl.substring(httpUrl.lastIndexOf("/")).replace("/", "");
                // 对URL解码获取真实名称
                myFileName = URLDecoder.decode(replace, StandardCharsets.UTF_8.name());
            }
            //file = new FileOutputStream(new File(filePath +"\\"+ fileName));
            int t;
            while ((t = in.read()) != -1) {
                fileOutputStream.write(t);
            }
        }
        return file;
    }

    /**
     * 使用MappingJacksonHttpMessageConverter将model转成JSON，然后写入HttpServletResponse返回。
     *
     * @param model
     * @param response
     * @return org.springframework.controller.servlet.ModelAndView
     */
    public static void render(Object model, HttpServletResponse response) throws IOException {
        MappingJackson2HttpMessageConverter jsonConverter = new MappingJackson2HttpMessageConverter();
        jsonConverter.write(model, MediaType.APPLICATION_JSON, new ServletServerHttpResponse(response));
    }


    /**
     * 请求url并返回请求实体
     *
     * @param httpUrl
     * @param method
     * @param param
     * @return
     * @throws IOException
     */
    public static HttpsURLConnection request(String httpUrl, String method, String param, String contentType,
                                             String authorization, String ua) throws IOException {
        if (StringUtils.isBlank(httpUrl)) {
            throw new IllegalArgumentException("url不能为空");
        }
        if (StringUtils.isBlank(contentType)) {
            contentType = "application/x-www-form-urlencoded";
        }
        HttpsURLConnection connection = null;
        try {
            // 创建远程url连接对象
            URL url = new URL(httpUrl);
            // 通过远程url连接对象打开一个连接，强转成httpURLConnection类
            connection = (HttpsURLConnection) url.openConnection();
            // 默认值为：true，当前向远程服务读取数据时，设置为true，该参数可有可无
            connection.setDoInput(true);
            // 设置用户缓存
            connection.setUseCaches(false);
            // 设置连接主机服务器的超时时间：15000毫秒
            connection.setConnectTimeout(15000);
            // 设置读取远程返回的数据时间：60000毫秒
            connection.setReadTimeout(60000);
            // 设置传入参数的格式
            connection.setRequestProperty("Content-Type", contentType);
            if (StringUtils.isNotBlank(authorization)) {
                // 设置鉴权信息 Bearer
                connection.setRequestProperty("Authorization", authorization);
            }
            if (StringUtils.isNotBlank(ua)) {
                connection.setRequestProperty("User-Agent", ua);
            }
            if (StringUtils.isNotBlank(method)) {
                // 请求方法，默认为GET
                connection.setRequestMethod(method);
            }
            // 如果参数不为空则发送参数
            if (StringUtils.isNotBlank(param)) {
                // 默认值为：false，当向远程服务器传送数据/写数据时，需要设置为true
                connection.setDoOutput(true);
                // 通过输出流对象将参数写出去/传输出去,它是通过字节数组写出的
                try (OutputStream outputStream = connection.getOutputStream()) {
                    outputStream.write(param.getBytes(StandardCharsets.UTF_8));
                }
            }
            // 发送请求
            connection.connect();
            return connection;
        } finally {
            if (null != connection) {
                // 关闭远程连接
                connection.disconnect();
            }
        }
    }

    /**
     * GET请求
     *
     * @param httpUrl
     * @return
     */
    public static String doGet(String httpUrl) throws IOException, IllegalArgumentException {
        if (StringUtils.isBlank(httpUrl)) {
            throw new IllegalArgumentException("url参数为空");
        }
        HttpsURLConnection httpsURLConnection = request(httpUrl, "GET", null, null, null, null);
        return getStringParam(httpsURLConnection.getInputStream());
    }

    /**
     * POST请求
     *
     * @param httpUrl
     * @param param
     * @return
     */

    public static String doPost(String httpUrl, String param) throws IOException, IllegalArgumentException {
        if (StringUtils.isBlank(httpUrl)) {
            throw new IllegalArgumentException("url为空");
        }
        if (StringUtils.isBlank(param)) {
            throw new IllegalArgumentException("请求参数为空");
        }

        HttpsURLConnection httpsURLConnection = request(httpUrl, "POST", param, null, null, null);
        return getStringParam(httpsURLConnection.getInputStream());
    }
}
