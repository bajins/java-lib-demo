package com.bajins.demo.cache;

import org.springframework.dao.DataAccessException;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.*;
import org.springframework.util.CollectionUtils;

import java.util.*;

public class RedisTemplateUtils {

    /**
     * 批量获取hashKey value
     *
     * @param keys
     * @return
     */
    public static Map<?, ?> hgetAll(RedisTemplate<?, ?> redisTemplate, Set<String> keys) {
        return (Map<?, ?>) redisTemplate.execute((RedisCallback<?>) con -> {
            Map<String, Map<String, String>> mapList = new HashMap<>();
            for (String key : keys) {
                Map<byte[], byte[]> result = con.hGetAll(key.getBytes());
                if (CollectionUtils.isEmpty(result)) {
                    return mapList;
                }
                Map<String, String> ans = new HashMap<>(result.size());
                for (Map.Entry<byte[], byte[]> entry : result.entrySet()) {
                    ans.put(new String(entry.getKey()), new String(entry.getValue()));
                }
                mapList.put(key, ans);
            }
            return mapList;
        });
    }

    /**
     * 批量取hash数据
     *
     * @param redisTemplate
     * @param Key
     * @param hashKeys
     * @param db
     * @return
     */
    public static List<Object> hashGetBatch(RedisTemplate<?, ?> redisTemplate, String Key, List<String> hashKeys,
                                            int db) {
        return redisTemplate.executePipelined(new RedisCallback<String>() {
            @Override
            public String doInRedis(RedisConnection connection) throws DataAccessException {
                // StringRedisConnection src = (StringRedisConnection) connection;
                for (String hashKey : hashKeys) {
                    connection.hGet(Key.getBytes(), hashKey.getBytes());
                }
                return null;
            }
        });
    }

    public static List<Object> executePipelined(RedisTemplate<?, ?> redisTemplate, Collection<?> keySet) {
        return redisTemplate.executePipelined(new SessionCallback<Object>() {
            @Override
            public <K, V> Object execute(RedisOperations<K, V> redisOperations) throws DataAccessException {
                HashOperations<K, Object, Object> hashOperations = redisOperations.opsForHash();
                for (Object key : keySet) {
                    hashOperations.entries((K) key);
                }
                return null;
            }
        });
    }
}
