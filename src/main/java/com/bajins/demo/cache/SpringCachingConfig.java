package com.bajins.demo.cache;

import net.sf.ehcache.config.CacheConfiguration;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.CachingConfigurer;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.concurrent.ConcurrentMapCache;
import org.springframework.cache.interceptor.*;
import org.springframework.cache.support.SimpleCacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;


/**
 * @version V1.0
 * @Title: SpringCachingConfig.java
 * @Package com.bajins.demo
 * @Description: ehcache2.x Spring配置
 * @author: https://www.bajins.com
 * @date: 2021年3月26日 上午9:33:14
 * @Copyright: 2021 bajins.com Inc. All rights reserved.
 */
@Configuration
@EnableCaching
public class SpringCachingConfig implements CachingConfigurer {

    /*@Autowired(required = false)
    private CacheManager ehCacheCacheManager;*/

    /*@Bean
    public EhCacheManagerFactoryBean ehCacheManagerFactoryBean() {
        EhCacheManagerFactoryBean cacheManagerFactoryBean = new EhCacheManagerFactoryBean();
        cacheManagerFactoryBean.setConfigLocation(new ClassPathResource("ehcache.xml"));
        cacheManagerFactoryBean.setShared(true);
        return cacheManagerFactoryBean;
    }

    @Bean
    public EhCacheCacheManager ehCacheCacheManager() {
        return new EhCacheCacheManager(ehCacheManagerFactoryBean().getObject());
    }*/

    @Bean(destroyMethod = "shutdown")
    public net.sf.ehcache.CacheManager ehCacheManager() {
        CacheConfiguration cacheConfiguration = new CacheConfiguration();
        cacheConfiguration.setName("myCacheName");
        cacheConfiguration.setMemoryStoreEvictionPolicy("LRU");
        cacheConfiguration.setMaxEntriesLocalHeap(1000);

        net.sf.ehcache.config.Configuration config = new net.sf.ehcache.config.Configuration();
        config.addCache(cacheConfiguration);

        net.sf.ehcache.CacheManager newInstance = net.sf.ehcache.CacheManager.newInstance(config);
        // EhCacheCacheManager ehCacheCacheManager = new EhCacheCacheManager(ehCacheManager);
        return newInstance;
    }

    @Bean
    @Override
    public CacheManager cacheManager() {
        /*List<CacheManager> cacheManagers = new ArrayList<CacheManager>();
        cacheManagers.add(ehCacheCacheManager);
        CompositeCacheManager cacheManager = new CompositeCacheManager();
        cacheManager.setCacheManagers(cacheManagers);
        cacheManager.setFallbackToNoOpCache(false);*/

        SimpleCacheManager cacheManager = new SimpleCacheManager();
        List<Cache> caches = new ArrayList<>();
        caches.add(new ConcurrentMapCache("myCacheName")); // 使用Spring管理的ConcurrentHashMap作为缓存
        cacheManager.setCaches(caches);

        return cacheManager;
        // return new EhCacheCacheManager(ehCacheManager());
    }

    @Bean
    @Override
    public KeyGenerator keyGenerator() {
        // return new DefaultKeyGenerator();
        return new SimpleKeyGenerator();
    }

    @Bean
    @Override
    public CacheResolver cacheResolver() {
        return new SimpleCacheResolver();
    }

    @Bean
    @Override
    public CacheErrorHandler errorHandler() {
        return new SimpleCacheErrorHandler();
    }
}
