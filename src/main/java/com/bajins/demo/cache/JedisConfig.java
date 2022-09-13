package com.bajins.demo.cache;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.redis.RedisProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Scope;
import org.springframework.util.StringUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

/**
 * Jedis配置
 */
@Configuration
public class JedisConfig {
    @Autowired
    private RedisProperties redisProperties; //从配置文件获取redis连接配置
    @Autowired
    private JedisPool jedisPool;
    @Autowired
    private JedisPoolConfig jedisPoolConfig;

    /**
     * 多例模式返回redis客户端，但是在依赖注入时，建议不要使用
     * 因为jedis客户端是阻塞式IO的，而且线程不安全，
     * 所以在操作数据库的时候不要使用单例，建议从连接池获取
     */
    @Bean
    @Scope("prototype")
    public Jedis jedis() {
        if (jedisPool != null) {
            return jedisPool.getResource();
        } else {
            return jedisPool().getResource();
        }
        //        return jedisPool.getResource();
    }

    /**
     * 单例模式返回redis连接池
     * 依赖注入时，建议注入jedisPool，获取连接池，然后调用getResource方法获取jedis客户端
     * 使用完后，调用jedis.close()方法归还连接，节约资源
     */
    @Bean
    public JedisPool jedisPool() {
        synchronized (this) {
            if (jedisPool == null) {
                synchronized (this) {
                    if (StringUtils.isEmpty(redisProperties.getPassword())) {
                        //无密码
                        jedisPool = new JedisPool(jedisPoolConfig, redisProperties.getHost(),
                                redisProperties.getPort(), (int) redisProperties.getTimeout().toMillis());
                    } else {//有密码
                        jedisPool = new JedisPool(jedisPoolConfig, redisProperties.getHost(),
                                redisProperties.getPort(), (int) redisProperties.getTimeout().toMillis(),
                                redisProperties.getPassword());
                    }
                }
            }
        }
        return jedisPool;
    }

    @Bean
    public JedisPoolConfig jedisPoolConfig() {
        // Jedis连接池配置
        JedisPoolConfig jedisPoolConfig = new JedisPoolConfig();
        jedisPoolConfig.setMaxTotal(redisProperties.getLettuce().getPool().getMaxActive());
        // 最大空闲连接数, 默认8个
        // jedisPoolConfig.setMaxIdle(50);
        jedisPoolConfig.setMaxIdle(redisProperties.getLettuce().getPool().getMaxIdle());
        // 最大连接数, 默认8个
        // jedisPoolConfig.setMaxTotal(300);
        //最小空闲连接数, 默认0
        jedisPoolConfig.setMinIdle(redisProperties.getLettuce().getPool().getMinIdle());
        // jedisPoolConfig.setMinIdle(20);
        // 获取连接时的最大等待毫秒数(如果设置为阻塞时BlockWhenExhausted),如果超时就抛异常, 小于零:阻塞不确定的时间,  默认-1
        jedisPoolConfig.setMaxWaitMillis(redisProperties.getLettuce().getPool().getMaxWait().toMillis()); // 设置10秒

        // jedisPoolConfig.setMaxWaitMillis(3000); // 设置2秒
        //对拿到的connection进行validateObject校验
        jedisPoolConfig.setTestOnBorrow(true);
        return jedisPoolConfig;
    }
}
