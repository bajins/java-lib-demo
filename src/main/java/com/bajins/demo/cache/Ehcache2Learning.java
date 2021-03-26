package com.bajins.demo.cache;

import net.sf.ehcache.Cache;
import net.sf.ehcache.CacheManager;
import net.sf.ehcache.config.*;
import net.sf.ehcache.distribution.RMICacheManagerPeerListenerFactory;
import net.sf.ehcache.distribution.RMICacheManagerPeerProviderFactory;
import net.sf.ehcache.distribution.RMICacheReplicatorFactory;
import net.sf.ehcache.store.MemoryStoreEvictionPolicy;

import java.net.URL;

/**
 * Ehcache2.x
 */
public class Ehcache2Learning {

    /**
     * 硬编码方式配置缓存获取管理器
     *
     * @return
     */
    public static CacheManager getEhcacheManager() {
        // 缓存名称(必须唯一),maxElements内存最多可以存放的元素的数量
        CacheConfiguration metaCache = new CacheConfiguration("metaCache", 10000)
                // 清理机制：LRU最近最少使用，FIFO先进先出，LFU较少使用
                .memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)
                .timeToIdleSeconds(1000)// 元素最大闲置时间
                .timeToLiveSeconds(2000)// 元素最大生存时间
                .eternal(false)// 元素是否永久缓存
                .diskExpiryThreadIntervalSeconds(120)// 缓存清理时间(默认120秒)
                // LOCALTEMPSWAP 当缓存容量达到上限时，将缓存对象（包含堆和非堆中的）交换到磁盘中
                // NONE 当缓存容量达到上限时，将缓存对象（包含堆和非堆中的）交换到磁盘中
                // DISTRIBUTED 按照_terracotta标签配置的持久化方式执行。非分布式部署时，此选项不可用
                .persistence(new PersistenceConfiguration().strategy(PersistenceConfiguration.Strategy.NONE))
                .maxEntriesLocalDisk(0)// 磁盘中最大缓存对象数0表示无穷大)
                .cacheEventListenerFactory(new CacheConfiguration.CacheEventListenerFactoryConfiguration()
                        .className(RMICacheReplicatorFactory.class.getName()));

        Configuration configuration = new Configuration()
                .diskStore(new DiskStoreConfiguration().path("java.io.tmpdir"))// 临时文件目录
                // 指定除自身之外的网络群体中其他提供同步的主机列表，用“|”分开不同的主机
                .cacheManagerPeerProviderFactory(new FactoryConfiguration<FactoryConfiguration<?>>()
                        .className(RMICacheManagerPeerProviderFactory.class.getName())
                        .properties("peerDiscovery=manual,rmiUrls=//localhost:40004/metaCache|//localhost:40005" +
                                "/metaCache")
                )
                // 配宿主主机配置监听程序
                .cacheManagerPeerListenerFactory(new FactoryConfiguration<FactoryConfiguration<?>>()//
                        .className(RMICacheManagerPeerListenerFactory.class.getName())//
                        .properties("port=40004,socketTimeoutMillis=2000")//
                )
                .cache(metaCache);
        return CacheManager.create(configuration);
    }


    public static void main(String[] args) {
        // 通过xml配置方式获取缓存
        URL url = Ehcache2Learning.class.getResource("ehcache2.xml");
        CacheManager manager = new CacheManager(url);
        Cache cache = manager.getCache("metaCache"); // 获得缓存

        // 通过硬编码配置方式获取缓存
        Cache cache1 = getEhcacheManager().getCache("metaCache");
    }
}
