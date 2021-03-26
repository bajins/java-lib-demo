package com.bajins.demo.cache;

import org.ehcache.event.CacheEvent;
import org.ehcache.event.CacheEventListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author bajins.com
 * @program com.bajins.demo.cache
 * @description EhCacheEventListener
 * @create 2019-05-17 09:03
 */
public class EhCache3EventListener implements CacheEventListener {

    private static final Logger logger = LoggerFactory.getLogger(EhCache3EventListener.class);

    @Override
    public void onEvent(CacheEvent event) {
        // 一级缓存超时放进二级缓存中
        logger.info("ehcache事件监听类型：{},key：{}", event.getType(), event.getKey());
        /*EhCache3Util.getCache().set(event.getKey().toString(), event.getOldValue().toString());
        EhCache3Util.getCache().expire(event.getKey().toString(), 60 * 5);*/
    }
}
