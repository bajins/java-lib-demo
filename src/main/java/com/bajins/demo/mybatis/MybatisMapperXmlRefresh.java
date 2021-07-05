package com.bajins.demo.mybatis;

import com.google.common.collect.Lists;
import org.apache.ibatis.binding.MapperRegistry;
import org.apache.ibatis.builder.xml.XMLMapperBuilder;
import org.apache.ibatis.builder.xml.XMLMapperEntityResolver;
import org.apache.ibatis.executor.ErrorContext;
import org.apache.ibatis.executor.keygen.SelectKeyGenerator;
import org.apache.ibatis.io.Resources;
import org.apache.ibatis.mapping.MappedStatement;
import org.apache.ibatis.parsing.XNode;
import org.apache.ibatis.parsing.XPathParser;
import org.apache.ibatis.session.Configuration;
import org.apache.ibatis.session.SqlSessionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;

import java.io.IOException;
import java.lang.reflect.Field;
import java.util.*;

public class MybatisMapperXmlRefresh {

    private static final Logger logger = LoggerFactory.getLogger(MybatisMapperXmlRefresh.class);

    private Configuration configuration;
    private final SqlSessionFactory sqlSessionFactory;

    public MybatisMapperXmlRefresh(SqlSessionFactory sqlSessionFactory) {
        this.sqlSessionFactory = sqlSessionFactory;
    }

    /**
     * 刷新mapper
     */
    private void refresh(Resource resource)
            throws ClassNotFoundException, NoSuchFieldException, IllegalAccessException {
        this.configuration = sqlSessionFactory.getConfiguration();
        boolean isSupper = configuration.getClass().getSuperclass() == Configuration.class;
        try {
            XPathParser xPathParser = new XPathParser(resource.getInputStream(), true, configuration.getVariables(),
                    new XMLMapperEntityResolver());
            XNode context = xPathParser.evalNode("/mapper");
            String namespace = context.getStringAttribute("namespace");

            Field field = MapperRegistry.class.getDeclaredField("knownMappers");
            field.setAccessible(true);
            Map<?, ?> mapConfig = (Map<?, ?>) field.get(configuration.getMapperRegistry());
            //Collection<String> mappedStatementNames = configuration.getMappedStatementNames();

            mapConfig.remove(Resources.classForName(namespace));

            Field loadedResourcesField = isSupper
                    ? configuration.getClass().getSuperclass().getDeclaredField("loadedResources")
                    : configuration.getClass().getDeclaredField("loadedResources");
            loadedResourcesField.setAccessible(true);
            Set<?> loadedResourcesSet = ((Set<?>) loadedResourcesField.get(configuration));
            loadedResourcesSet.remove(resource.toString());

            configuration.getCacheNames().remove(namespace);

            cleanParameterMap(context.evalNodes("/mapper/parameterMap"), namespace);
            cleanResultMap(context.evalNodes("/mapper/resultMap"), namespace);
            cleanKeyGenerators(context.evalNodes("insert|update|select"), namespace);
            cleanSqlElement(context.evalNodes("/mapper/sql"), namespace);
            XMLMapperBuilder xmlMapperBuilder = new XMLMapperBuilder(resource.getInputStream(),
                    sqlSessionFactory.getConfiguration(), resource.toString(),
                    sqlSessionFactory.getConfiguration().getSqlFragments());
            xmlMapperBuilder.parse();

            logger.debug("refresh: '" + resource + "', success!");
        } catch (IOException e) {
            logger.error("Refresh IOException :" + e.getMessage());
        } finally {
            ErrorContext.instance().reset();
        }
    }

    /**
     * 清理parameterMap
     *
     * @param list
     * @param namespace
     */
    private void cleanParameterMap(List<XNode> list, String namespace) {
        for (XNode parameterMapNode : list) {
            String id = parameterMapNode.getStringAttribute("id");
            configuration.getParameterMaps().remove(namespace + "." + id);
        }
    }

    /**
     * 清理resultMap
     *
     * @param list
     * @param namespace
     */
    private void cleanResultMap(List<XNode> list, String namespace) {
        for (XNode resultMapNode : list) {
            String id = resultMapNode.getStringAttribute("id", resultMapNode.getValueBasedIdentifier());
            configuration.getResultMapNames().remove(id);
            configuration.getResultMapNames().remove(namespace + "." + id);
            clearResultMap(resultMapNode, namespace);
        }
    }

    private void clearResultMap(XNode xNode, String namespace) {
        for (XNode resultChild : xNode.getChildren()) {
            if ("association".equals(resultChild.getName()) || "collection".equals(resultChild.getName())
                    || "case".equals(resultChild.getName())) {
                if (resultChild.getStringAttribute("select") == null) {
                    configuration.getResultMapNames()
                            .remove(resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    configuration.getResultMapNames().remove(namespace + "."
                            + resultChild.getStringAttribute("id", resultChild.getValueBasedIdentifier()));
                    if (resultChild.getChildren() != null && !resultChild.getChildren().isEmpty()) {
                        clearResultMap(resultChild, namespace);
                    }
                }
            }
        }
    }

    /**
     * 清理selectKey
     *
     * @param list
     * @param namespace
     */
    private void cleanKeyGenerators(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getKeyGeneratorNames().remove(id + SelectKeyGenerator.SELECT_KEY_SUFFIX);
            configuration.getKeyGeneratorNames().remove(namespace + "." + id + SelectKeyGenerator.SELECT_KEY_SUFFIX);

            Collection<MappedStatement> mappedStatements = configuration.getMappedStatements();
            List<MappedStatement> objects = Lists.newArrayList();
            for (MappedStatement object : mappedStatements) {
                if (object != null && object.getId().equals(namespace + "." + id)) {
                    objects.add(object);
                }
            }
            mappedStatements.removeAll(objects);
        }
    }

    /**
     * 清理sql节点缓存
     *
     * @param list
     * @param namespace
     */
    private void cleanSqlElement(List<XNode> list, String namespace) {
        for (XNode context : list) {
            String id = context.getStringAttribute("id");
            configuration.getSqlFragments().remove(id);
            configuration.getSqlFragments().remove(namespace + "." + id);
        }
    }
}
