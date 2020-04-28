package com.bajins.demo;

import org.springframework.data.redis.core.RedisTemplate;

public class Redis {

    private static RedisTemplate<String, Object> redisTemplate;

    public static void setRedisTemplate(RedisTemplate<String, Object> redisTemplate) {
        Redis.redisTemplate = redisTemplate;
    }

    public static void main(String[] args) {
        /*
        // 指定缓存失效时间
        redisTemplate.expire(key, time, TimeUnit.SECONDS);

        // 根据key 获取过期时间
        redisTemplate.getExpire(key, TimeUnit.SECONDS);

        // 判断key是否存在
        redisTemplate.hasKey(key);

        // 删除缓存
        redisTemplate.delete();
        redisTemplate.delete(CollectionUtils.arrayToList(key));

        // 普通缓存获取
        redisTemplate.opsForValue().get(key);
        // 普通缓存放入
        redisTemplate.opsForValue().set(key, value);
        // 普通缓存放入并设置时间
        redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
        // 递增
        redisTemplate.opsForValue().increment(key, delta);
        // 递减
        redisTemplate.opsForValue().increment(key, -delta);


        // HashGet
        redisTemplate.opsForHash().get(key, item);
        // 获取hashKey对应的所有键值
        redisTemplate.opsForHash().entries(key);
        // HashSet
        redisTemplate.opsForHash().putAll(key, map);
        // 向一张hash表中放入数据,如果不存在将创建
        redisTemplate.opsForHash().put(key, item, value);
        // 删除hash表中的值
        redisTemplate.opsForHash().delete(key, item);
        // 判断hash表中是否有该项的值
        redisTemplate.opsForHash().hasKey(key, item);
        // hash递增 如果不存在,就会创建一个 并把新增后的值返回
        redisTemplate.opsForHash().increment(key, item, by);
        // hash递减
        redisTemplate.opsForHash().increment(key, item, -by);


        // 根据key获取Set中的所有值
        redisTemplate.opsForSet().members(key);
        // 根据value从一个set中查询,是否存在
        redisTemplate.opsForSet().isMember(key, value);
        // 将set数据放入缓存
        Long count = redisTemplate.opsForSet().add(key, values);
        // 获取set缓存的长度
        redisTemplate.opsForSet().size(key);
        // 移除值为value的
        Long count = redisTemplate.opsForSet().remove(key, values);


        // 获取list缓存的内容
        redisTemplate.opsForList().range(key, start, end);
        // 获取list缓存的长度
        redisTemplate.opsForList().size(key);
        // 通过索引 获取list中的值
        redisTemplate.opsForList().index(key, index);
        // 将list放入缓存
        redisTemplate.opsForList().rightPush(key, value);
        // 将list放入缓存
        redisTemplate.opsForList().rightPushAll(key, value);
        // 根据索引修改list中的某条数据
        redisTemplate.opsForList().set(key, index, value);
        // 移除N个值为value
        Long remove = redisTemplate.opsForList().remove(key, count, value);
        */
    }
}
