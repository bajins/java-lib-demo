package com.bajins.demo.mybatis;

import org.apache.ibatis.io.Resources;
import org.xml.sax.EntityResolver;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;


/**
 * 解决内网情况下，DTD验证失败<br/>
 * 在spring里面存在spring.schemas spring在解析的时候会自动映射，mybatis也是同样道理<br/>
 * 当解析到dtd时自动映射成对应jar下面的文件，这样无需去网络获取<br/>
 * https://www.jianshu.com/p/6c279b4d7f5c
 */
public class XMLMapperEntityResolver implements EntityResolver {

    private static final Map<String, String> doctypeMap = new HashMap<>();

    private static final String IBATIS_CONFIG_PUBLIC =
            "-//ibatis.apache.org//DTD Config 3.0//EN".toUpperCase(Locale.ENGLISH);
    private static final String IBATIS_CONFIG_SYSTEM =
            "http://ibatis.apache.org/dtd/ibatis-3-config.dtd".toUpperCase(Locale.ENGLISH);

    private static final String IBATIS_MAPPER_PUBLIC =
            "-//ibatis.apache.org//DTD Mapper 3.0//EN".toUpperCase(Locale.ENGLISH);
    private static final String IBATIS_MAPPER_SYSTEM =
            "http://ibatis.apache.org/dtd/ibatis-3-mapper.dtd".toUpperCase(Locale.ENGLISH);

    private static final String MYBATIS_CONFIG_PUBLIC =
            "-//mybatis.org//DTD Config 3.0//EN".toUpperCase(Locale.ENGLISH);
    private static final String MYBATIS_CONFIG_SYSTEM =
            "http://mybatis.org/dtd/mybatis-3-config.dtd".toUpperCase(Locale.ENGLISH);

    private static final String MYBATIS_MAPPER_PUBLIC =
            "-//mybatis.org//DTD Mapper 3.0//EN".toUpperCase(Locale.ENGLISH);
    private static final String MYBATIS_MAPPER_SYSTEM =
            "http://mybatis.org/dtd/mybatis-3-mapper.dtd".toUpperCase(Locale.ENGLISH);

    private static final String MYBATIS_CONFIG_DTD = "org/apache/ibatis/builder/xml/mybatis-3-config.dtd";
    private static final String MYBATIS_MAPPER_DTD = "org/apache/ibatis/builder/xml/mybatis-3-mapper.dtd";

    static {
        doctypeMap.put(IBATIS_CONFIG_SYSTEM, MYBATIS_CONFIG_DTD);
        doctypeMap.put(IBATIS_CONFIG_PUBLIC, MYBATIS_CONFIG_DTD);

        doctypeMap.put(IBATIS_MAPPER_SYSTEM, MYBATIS_MAPPER_DTD);
        doctypeMap.put(IBATIS_MAPPER_PUBLIC, MYBATIS_MAPPER_DTD);

        doctypeMap.put(MYBATIS_CONFIG_SYSTEM, MYBATIS_CONFIG_DTD);
        doctypeMap.put(MYBATIS_CONFIG_PUBLIC, MYBATIS_CONFIG_DTD);

        doctypeMap.put(MYBATIS_MAPPER_SYSTEM, MYBATIS_MAPPER_DTD);
        doctypeMap.put(MYBATIS_MAPPER_PUBLIC, MYBATIS_MAPPER_DTD);
    }

    /*
     * Converts a public DTD into a local one
     *
     * @param publicId The public id that is what comes after "PUBLIC"
     * @param systemId The system id that is what comes after the public id.
     * @return The InputSource for the DTD
     *
     * @throws org.xml.sax.SAXException If anything goes wrong
     */
    @Override
    public InputSource resolveEntity(String publicId, String systemId) throws SAXException {

        if (publicId != null) {
            publicId = publicId.toUpperCase(Locale.ENGLISH);
        }
        if (systemId != null) {
            systemId = systemId.toUpperCase(Locale.ENGLISH);
        }
        InputSource source = null;
        try {
            String path = doctypeMap.get(publicId);
            source = getInputSource(path, source);
            if (source == null) {
                path = doctypeMap.get(systemId);
                source = getInputSource(path, source);
            }
        } catch (Exception e) {
            throw new SAXException(e.toString());
        }
        return source;
    }

    private InputSource getInputSource(String path, InputSource source) {
        if (path != null) {
            try {
                source = new InputSource(Resources.getResourceAsStream(path));
            } catch (IOException e) {
                // ignore, null is ok
            }
        }
        return source;
    }

}
