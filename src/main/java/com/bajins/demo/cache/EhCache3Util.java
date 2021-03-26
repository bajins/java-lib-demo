package com.bajins.demo.cache;

import org.codehaus.jackson.map.ObjectMapper;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.CacheConfiguration;
import org.ehcache.config.Configuration;
import org.ehcache.config.builders.*;
import org.ehcache.config.units.MemoryUnit;
import org.ehcache.event.EventType;
import org.ehcache.xml.XmlConfiguration;
import org.springframework.util.StringUtils;

import java.io.File;
import java.io.IOException;
import java.time.Duration;

/**
 * ehcache 3.x版本的工具类，cacheManager应该全局共享，否则拿不到同一个cacheManager下的缓存
 * https://gitee.com/JohnYan/codes/s5da4wfngovke2cih70y892
 * <p>
 * Ehcache提供4种存储策略：heap、offheap、disk、clustered
 * 存储速度依次降低，主要使用前3种做本地缓存。
 * （1）heap就是指java堆内存，这个会被GC，所以最好数量不要太大。太多的cache会造成堆内存使用减少，参考JVM堆内存设置。
 * heap不需要序列化和反序列化。（坑点之一）
 * （2）offheap是堆外内存，不会受GC影响，但须以value形式存储cache，需要序列化和反序列化，性能比heap慢。
 * 启用offheap必须要确定是否开启了-XX:MaxDirectMemorySize=size[g|G|m|M|k|K]，如果没设置则默认不开启，就无法使用offheap
 * （3）disk是磁盘存储，需要指定存储路径，速度也更慢。
 * （4）clustered是集群层缓存方式，表示一个客户端连接到存储缓存的服务器集群，也是JVM之间共享缓存的一种方式。一般不用，感兴趣的可以看官方文档了解。
 * 可以搭配使用的存储层方案：
 * heap + offheap
 * heap + offheap + disk
 * heap + disk
 * heap + offheap + clustered
 * heap + clustered
 * <p>
 * Ehcache3.X和2.X版本使用差异，3.X版本不兼容2.X的用法，使用过程的差异点：
 * （1）缓存管理器CacheManager的创建和配置方式不同
 * （2）缓存配置CacheConfiguration的各种属性用法不同
 * （3）存取元素方式不同，2.X版本最大特点是使用了Element作为一个缓存元素对象，3.X版本不支持这种存取方式。
 * <p>
 * 逐出策略
 * 在2.x版本，我们可以选择LRU以及FIFO、LFU，CacheConfiguration配置memoryStoreEvictionPolicy(MemoryStoreEvictionPolicy.LFU)。
 * 在3.x中提供了EvictionAdvisor，通过继承此类可以实现采取哪种方式逐出。3.X版本官方不建议逐出，默认满了就不添加了，这里没做研究，可参考官方文档
 *
 * @author bajins.com
 * @program com.bajins.demo.cache
 * @description EhCache3Util
 * @create 2018-12-21 14:52
 */
public class EhCache3Util {
    /**
     * 堆缓存大小 单位KB
     */
    public static final int HEAP_CACHE_SIZE = 10;
    /**
     * 堆外缓存大小 单位MB
     */
    public static final int OFF_HEAP_CACHE_SIZE = 20;
    /**
     * 磁盘缓存大小 单位MB
     */
    public static final int DISK_CACHE_SIZE = 100;
    /**
     * 堆可缓存的最大对象大小 单位MB
     */
    public static final long HEAP_MAX_OBJECT_SIZE = 1L;
    /**
     * 统计对象大小时对象图遍历深度
     */
    public static final long HEAP_MAX_OBJECT_GRAPH = 1000L;
    /**
     * 磁盘文件路径
     */
    public static final String DISK_CACHE_DIR = "/usr/cache";
    /**
     * ehcache缓存超时时间 单位秒
     */
    public static final int EHCACHE_TTL = 120;
    /**
     * ehcache缓存名称
     */
    public static final String EHCACHE_CACHE_NAME = "lifeEHCache";

    // 监听器配置
    private static final CacheEventListenerConfigurationBuilder cacheEventListenerConfiguration =
            CacheEventListenerConfigurationBuilder
                    .newEventListenerConfiguration(new EhCache3EventListener(), EventType.EXPIRED)
                    .unordered().asynchronous();

    private final static CacheManager cacheManager;

    // 初始化并根据配置创建一个默认缓存
    static {
        cacheManager = CacheManagerBuilder.newCacheManagerBuilder()
                // 持久化缓存文件配置
                .with(CacheManagerBuilder.persistence(new File(DISK_CACHE_DIR,
                        EHCACHE_CACHE_NAME)))
                // 根据配置创建缓存
                .withCache(EHCACHE_CACHE_NAME, createConfig(EHCACHE_TTL,
                        String.class, String.class, true)).build(true);
    }

    // 私有化方法 禁止创建
    private EhCache3Util() {

    }

    /**
     * 获取缓存
     *
     * @param cacheName 缓存名
     * @return org.ehcache.Cache<java.lang.String, java.lang.String>
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:48
     */
    public static Cache<String, String> getEhCache(String cacheName) {
        if (StringUtils.hasText(cacheName)) {
            return cacheManager.getCache(EHCACHE_CACHE_NAME, String.class, String.class);
        }
        return cacheManager.getCache(cacheName, String.class, String.class);
    }


    /**
     * 缓存存值
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param value     值
     * @return void
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:47
     */
    public static void put(String cacheName, String key, Object value) throws IOException {
        String json = new ObjectMapper().writeValueAsString(value);
        getEhCache(cacheName).put(key, json);
    }

    /**
     * 获取缓存值
     *
     * @param cacheName 缓存名
     * @param key       键
     * @param valueType 值类型
     * @return T
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:46
     */
    public static <T> T get(String cacheName, String key, Class<T> valueType) throws IOException {
        String json = getEhCache(cacheName).get(key);
        if (!StringUtils.hasText(json)) {
            return new ObjectMapper().readValue(json, valueType);
        }
        return null;

    }

    /**
     * 清除缓存值
     *
     * @param cacheName 缓存名
     * @param key       键
     * @return
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:45
     */
    public static void remove(String cacheName, String key) {
        getEhCache(cacheName).remove(key);
    }


    /**
     * 移除缓存
     *
     * @param cacheName 缓存名
     * @return void
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:44
     */
    public static void removeCache(String cacheName) {
        cacheManager.removeCache(cacheName);
    }


    /**
     * 根据传值创建配置
     * 因为缓存的过期是访问后才过期，所以要延长过期时间可以在使用get方法之前先put一遍
     *
     * @param withExpiry 有效期单位秒
     * @param keyType    key类型
     * @param valueType  值类型
     * @param isDisk     是否持久化
     * @return org.ehcache.config.CacheConfiguration<K, V>
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:31
     */
    public static <K, V> CacheConfiguration<K, V> createConfig(int withExpiry, Class<K> keyType, Class<V> valueType,
                                                               boolean isDisk) {

        // 资源池生成器配置持久化
        ResourcePoolsBuilder resourcePoolsBuilder;
        if (isDisk) {
            resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
                    // 堆内缓存大小
                    .heap(HEAP_CACHE_SIZE, MemoryUnit.KB)
                    // 堆外缓存大小
                    .offheap(OFF_HEAP_CACHE_SIZE, MemoryUnit.MB)
                    // 文件缓存大小
                    .disk(DISK_CACHE_SIZE, MemoryUnit.MB);

        } else {
            resourcePoolsBuilder = ResourcePoolsBuilder.newResourcePoolsBuilder()
                    // 堆内缓存大小
                    .heap(HEAP_CACHE_SIZE, MemoryUnit.KB)
                    // 堆外缓存大小
                    .offheap(OFF_HEAP_CACHE_SIZE, MemoryUnit.MB);
        }

        // 生成配置
        CacheConfiguration<K, V> cacheConfiguration = CacheConfigurationBuilder.newCacheConfigurationBuilder(keyType,
                valueType, resourcePoolsBuilder)
                // 缓存超时时间
                .withExpiry(ExpiryPolicyBuilder.timeToLiveExpiration(Duration.ofSeconds(withExpiry)))
                // 统计对象大小时对象图遍历深度
                .withSizeOfMaxObjectGraph(HEAP_MAX_OBJECT_GRAPH)
                //可缓存的最大对象大小
                .withSizeOfMaxObjectSize(HEAP_MAX_OBJECT_SIZE, MemoryUnit.MB)
                .add(cacheEventListenerConfiguration)
                //最后调用build()返回一个完整的实例，但是该实例并未初始化。
                .build();
        return cacheConfiguration;
    }


    private static final CacheManager cacheManagerNull;

    // 初始化，默认不创建缓存,不持久化到磁盘
    static {
        cacheManagerNull = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    }


    /**
     * 处于类型安全考虑，我们要求键和值类型都要传递。如果这些和我们期望的不同
     * CacheManager将会抛出一个ClassCastException，在应用程序生命周期的早期这可以保护缓存免受随机类型的污染。
     *
     * @param cacheName  缓存名
     * @param withExpiry 有效期
     * @param keyType    键类型
     * @param valueType  值类型
     * @return org.ehcache.Cache<K, V>
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 12:43
     */
    public static <K, V> Cache<K, V> getCache(String cacheName, int withExpiry, Class<K> keyType,
                                              Class<V> valueType) {
        // 处于类型安全考虑，我们要求键和值类型都要传递。如果这些和我们期望的不同
        // CacheManager将会抛出一个ClassCastException，在应用程序生命周期的早期这可以保护缓存免受随机类型的污染。
        Cache<K, V> cache = cacheManagerNull.getCache(cacheName, keyType, valueType);
        if (cache != null) {
            return cache;
        }
        return cacheManagerNull.createCache(cacheName, createConfig(withExpiry, keyType, valueType, false));
        //新添加的Cache能够通过键值对的形式被用于存储条目。put方法的第一个参数是键，第二个参数是值。
        // 记住，键和值类型必须是与在cacheconfig容器中定义的类型相同的类型。
        // 另外，键必须是惟一的，并且只与一个值相关联。
        //        myCache.put(1L, "da one!");

        //通过调用cache.get(key)方法，从缓存中检索值。它只需要一个参数，这个参数是键，然后返回与该键关联的值。
        // 如果没有与该键相关联的值，则返回null。Cache将释放所有本地持有的临时资源(例如内存)。
        // 对这个缓存的引用变得不可用。
        //        String value = myCache.get(1L);

        //我们可以通过CacheManager.removeCache(String)移除一个给定的Cache,
        // CacheManager不仅会删除它对Cache的引用，而且还会关闭它。
        //        cacheManager.removeCache(cacheName);
        //为了释放一个CacheManager提供的所有临时资源(内存、线程、…)，您必须调用CacheManager.close()，
        // 它将关闭所有在当时已知的缓存实例。
        //        cacheManager.close();
    }


    /**
     * 获取XML配置文件创建CacheManager
     *
     * @param
     * @return org.ehcache.CacheManager
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 13:08
     */
    public static CacheManager cacheManagerByXml() {
        //从配置文件创建配置对象
        Configuration xmlConf = new XmlConfiguration(EhCache3Util.class.getResource("/ehcache3.xml"));
        // 创建缓存管理器
        CacheManager cacheManager = CacheManagerBuilder.newCacheManager(xmlConf);
        // 初始化
        cacheManager.init();
        return cacheManager;
    }

    /**
     * 从XML创建的CacheManager中拿到缓存
     *
     * @param alias     XML中配置的缓存名（alias）
     * @param keyType   键类型
     * @param valueType 值类型
     * @return org.ehcache.Cache<K, V>
     * @Description
     * @author claer bajins.com
     * @date 2019/5/17 13:08
     */
    public static <K, V> Cache<K, V> getCacheByXml(String alias, Class<K> keyType, Class<V> valueType) {
        return cacheManagerByXml().getCache(alias, keyType, valueType);
    }

    public static void main(String[] args) {
        Cache<String, String> foo = getCacheByXml("foo", String.class, String.class);
        foo.put("ww", "1545");
        System.out.println(foo.get("ww"));

        Cache<String, String> test1 = getCache("test1", 5, String.class, String.class);
        test1.put("111", "aaaaaaa");

        System.err.println(test1.get("111"));
        test1.remove("111");
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        test1.put("111", "aaaaaaa");
        System.out.println(test1.get("111"));
        try {
            Thread.sleep(11000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        System.err.println(test1.get("111"));
        // ehcahe2.x api
        // 使缓存中存储的所有元素同步检查是否过期，如果过期，则逐出。
        // System.out.println(cache.evictExpiredElements());
        // 返回缓存中所有元素键的列表。只返回未过期元素的键。
        // System.out.println(cache.getKeysWithExpiryCheck().contains(CACHE_NAME));
        // 判断缓存key是否存在，可能过期失效后还存在
        // System.out.println(cache.isKeyInCache(CACHE_NAME));
        // 从缓存中获取元素，如果不存在则为null。不更新元素统计信息。直到更新缓存统计信息。不调用监听器。
        // Element element = cache.getQuiet(CACHE_NAME);
        // 获取元素是否超时
        // System.out.println(null != element && cache.isExpired(element));
        // 创建有效期的元素，并放进缓存
        // cache.put(new Element(CACHE_NAME, Object, false, 20, 20));
    }
}
