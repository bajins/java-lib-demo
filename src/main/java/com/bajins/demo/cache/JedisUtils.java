package com.bajins.demo.cache;

import com.alibaba.fastjson.JSONObject;
import org.apache.commons.beanutils.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.CollectionUtils;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.Pipeline;
import redis.clients.jedis.Response;
import redis.clients.jedis.params.ScanParams;
import redis.clients.jedis.resps.ScanResult;

import java.lang.reflect.InvocationTargetException;
import java.util.*;


/**
 * jedis的keys* 或者mget都会造成redis阻塞，使用redis的解决方案：Pipeline（管道）的方式进行对redis内数据的获取
 * <p>
 * findValueByKeyOfPipeline和 findValueByKeyOfPipelineMget 的区别在于 前者根据阈值获取多次 后者是直接一次性获取
 * <p>
 * pipeline会把多个命令集中在一次请求发送到redis执行（减少网络开销） https://juejin.cn/post/6844903922046337038
 * https://blog.csdn.net/aizhupo1314/article/details/121231121
 * https://blog.csdn.net/w1lgy/article/details/84455579
 */
public class JedisUtils {

    private static final int BATCH_SIZE = 10000;

    @Autowired
    private JedisPool jedisPool;

    /**
     * 根据(不完整)key值通过Pipeline的方式获取值
     * 先通过scan获取全部的key,再通过Pipeline获取全部的值
     * 根据countVPT 这个 阈值来控制 获取多次
     * (通过建立一个管道一次提交)
     *
     * @param redisKey
     */
    public List<String> findValueByKeyOfPipeline(String redisKey) {
        List<String> values = new ArrayList<>();
        ArrayList<Response<List<String>>> responses = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource();) {
            //从redis获取值刷新航迹信息
            List<String> allKeys = findAllKeys(redisKey);
            if (Objects.isNull(allKeys) || allKeys.isEmpty()) {
                return values;
            }
            Pipeline pipelined = jedis.pipelined();
            ArrayList<String> keyList = new ArrayList<>();
            for (String key : allKeys) {
                keyList.add(key);
                if (keyList.size() == BATCH_SIZE) {
                    String[] keys = keyList.toArray(new String[0]);
                    Response<List<String>> mget = pipelined.mget(keys);
                    responses.add(mget);
                    keyList = new ArrayList<>();
                }
            }
            //最后的数据
            String[] keys = keyList.toArray(new String[0]);
            Response<List<String>> mget = pipelined.mget(keys);
            responses.add(mget);
            pipelined.sync();
            for (Response<List<String>> respons : responses) {
                values.addAll(respons.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据(不完整)key值通过Pipeline的方式获取值
     * 先通过scan获取全部的key,再通过Pipeline获取全部的值
     * (通过Pipeline.mget直接获取)
     *
     * @param redisKey
     */
    public List<String> findValueByKeyOfPipelineMget(String redisKey) {
        List<String> values = new ArrayList<>();
        ArrayList<Response<List<String>>> responses = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource();) {
            //从redis获取值刷新航迹信息
            List<String> allKeys = findAllKeys(redisKey);
            if (Objects.isNull(allKeys) || allKeys.isEmpty()) {
                return values;
            }
            // allKeys=allKeys.subList(0,5000);
            String[] keys = allKeys.toArray(new String[0]);
            Pipeline pipelined = jedis.pipelined();
            Response<List<String>> mget = pipelined.mget(keys);
            pipelined.sync();
            values = mget.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * jedis的met
     *
     * @param redisKey
     * @return
     */
    public List<String> mget(String redisKey) {
        List<String> values = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource();) {
            //从redis获取值刷新航迹信息
            List<String> allKeys = findAllKeys(redisKey);
            if (Objects.isNull(allKeys) || allKeys.isEmpty()) {
                return values;
            }
            String[] keys = allKeys.toArray(new String[0]);
            values = jedis.mget(keys);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据(不完整)key值通过Pipeline的方式获取值   --使用传入jedis的方式
     * 先通过scan获取全部的key,再通过Pipeline获取全部的值
     *
     * @param redisKey
     */
    public List<String> findValueByKeyOfPipeline(String redisKey, Jedis jedis) {
        List<String> values = new ArrayList<>();
        try {
            //从redis获取值刷新航迹信息
            List<String> allKeys = findAllKeys(redisKey);
            if (allKeys == null || allKeys.isEmpty()) {
                return values;
            }
            //此处采用Pipeline，以提升性能
            Pipeline pipelined = jedis.pipelined();
            Response<List<String>> mget = pipelined.mget(allKeys.toArray(new String[0]));
            pipelined.sync();
            values = mget.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据多个完整(精确)key值通过Pipeline的方式获取值
     * 使用完整的key通过Pipeline获取全部的值
     *
     * @param allKeys
     */
    public List<String> findAllkeysByKeysOfPipeline(String... allKeys) {
        List<String> values = new ArrayList<>();
        try (Jedis jedis = jedisPool.getResource();) {
            ArrayList<Response<List<String>>> responses = new ArrayList<>();
            //从redis获取值刷新航迹信息
            if (Objects.isNull(allKeys) || allKeys.length == 0) {
                return values;
            }
            //此处采用Pipeline，以提升性能
            Pipeline pipelined = jedis.pipelined();
            ArrayList<String> keyList = new ArrayList<>();
            for (String key : allKeys) {
                keyList.add(key);
                if (keyList.size() == BATCH_SIZE) {
                    String[] keys = keyList.toArray(new String[0]);
                    Response<List<String>> mget = pipelined.mget(keys);
                    responses.add(mget);
                    keyList = new ArrayList<>();
                }
            }
            //最后的数据
            String[] keys = keyList.toArray(new String[0]);
            Response<List<String>> mget = pipelined.mget(keys);
            responses.add(mget);
            pipelined.sync();

            for (Response<List<String>> respons : responses) {
                values.addAll(respons.get());
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据多个完整(精确)key值通过Pipeline的方式获取值  --使用传入jedis的方式
     * 使用完整的key通过Pipeline获取全部的值
     *
     * @param redisKeys
     */
    public List<String> findAllkeysByKeysOfPipeline(String[] redisKeys, Jedis jedis) {
        List<String> values = new ArrayList<>();
        try {
            //从redis获取值刷新航迹信息
            if (redisKeys == null || redisKeys.length == 0) {
                return values;
            }
            //此处采用Pipeline，以提升性能
            Pipeline pipelined = jedis.pipelined();
            Response<List<String>> mget = pipelined.mget(redisKeys);
            pipelined.sync();
            values = mget.get();
        } catch (Exception e) {
            e.printStackTrace();
        }
        return values;
    }

    /**
     * 根据keys 获取value
     *
     * @param pattern
     */
    public List<String> findValueByKey(String pattern) {
        List<String> values = null;
        List<String> keys = findAllKeys(pattern);
        if (keys != null && keys.size() > 0) {
            String[] strings = keys.toArray(new String[0]);
            values = findAllkeysByKeysOfPipeline(strings);
        }
        return values;
    }

    /**
     * 根据keys 获取value  --使用传入jedis的方式
     *
     * @param pattern
     */
    public List<String> findValueByKey(String pattern, Jedis jedis) {
        List<String> values = null;
        List<String> keys = findAllKeys(pattern);
        if (keys != null && keys.size() > 0) {
            String[] strings = keys.toArray(new String[0]);
            values = findAllkeysByKeysOfPipeline(strings, jedis);
        }
        return values;
    }

    /**
     * 根据keys 获取value 已map形式返回
     *
     * @param pattern
     */
    public List<Map<?, ?>> findValuetoMapByKey(String pattern, Jedis jedis) {
        List<String> values = new ArrayList<>();
        ArrayList<Map<?, ?>> mapList = new ArrayList<>();
        try {
            List<String> keys = findAllKeys(pattern);
            if (keys != null && keys.size() > 0) {
                String[] strings = keys.toArray(new String[0]);
                values = findAllkeysByKeysOfPipeline(strings);
            }
            if (!CollectionUtils.isEmpty(values)) {
                for (String value : values) {
                    Map<?, ?> map = ((JSONObject) JSONObject.parse(value)).toJavaObject(Map.class);
                    mapList.add(map);
                }
            }
        } catch (Exception e) {
            jedis.close();
            e.printStackTrace();
        }
        return mapList;
    }

    /**
     * 因为Redis是单线程jedis.keys()方法会导致数据库阻塞，
     * 可能导致Redis其它业务无法操作，所有采用迭代模式(scan)获取数据
     * 并且keys()方法的时间复杂度为O(n)，scan()时间复杂度为O(1)
     */
    public List<String> findAllKeys(String pattern) {
        List<String> total = null;
        try (Jedis jedis = jedisPool.getResource();) {
            String cursor = String.valueOf(0);
            total = new ArrayList<>();
            ScanParams params = new ScanParams();
            params.match(pattern);
            do {
                params.count(BATCH_SIZE);
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                total.addAll(result.getResult());
            } while (Integer.parseInt(cursor) > 0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * 获取全都的key --使用传入jedis的方式
     * 因为Redis是单线程jedis.keys()方法会导致数据库阻塞，
     * 可能导致Redis其它业务无法操作，所有采用迭代模式(scan)获取数据
     * 并且keys()方法的时间复杂度为O(n)，scan()时间复杂度为O(1)
     */
    public List<String> findAllKeys(String pattern, Jedis jedis) {
        List<String> total = null;
        try {
            String cursor = String.valueOf(0);
            total = new ArrayList<>();
            ScanParams params = new ScanParams();
            params.match(pattern);
            do {
                params.count(BATCH_SIZE);
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                total.addAll(result.getResult());
            } while (Integer.parseInt(cursor) > 0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * 取代jedis.keys(*)
     *
     * @param pattern
     * @return
     */
    public Set<String> findAllKeysToSet(String pattern) {
        Set<String> total = null;
        try (Jedis jedis = jedisPool.getResource();) {
            String cursor = String.valueOf(0);
            total = new HashSet<>();
            ScanParams params = new ScanParams();
            params.match(pattern);
            do {
                params.count(BATCH_SIZE);
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                total.addAll(result.getResult());
            } while (Integer.parseInt(cursor) > 0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * 取代jedis.keys(*)  --使用传入jedis的方式
     *
     * @param pattern
     * @return
     */
    public Set<String> findAllKeysToSet(String pattern, Jedis jedis) {
        Set<String> total = null;
        try {
            String cursor = String.valueOf(0);
            total = new HashSet<>();
            ScanParams params = new ScanParams();
            params.match(pattern);
            do {
                params.count(BATCH_SIZE);
                ScanResult<String> result = jedis.scan(cursor, params);
                cursor = result.getCursor();
                total.addAll(result.getResult());
            } while (Integer.parseInt(cursor) > 0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return total;
    }

    /**
     * 实时数据取出，封装成指定的对象集合
     *
     * @param list        redis取出的value集合
     * @param entityClass 需要封装的对象类型
     * @param <T>
     * @return
     */
    public <T> List<T> valueToClass(List<String> list, Class<T> entityClass) {
        List<T> result = new ArrayList<>();
        for (String value : list) {
            Map<?, ?> map = ((JSONObject) JSONObject.parse(value)).toJavaObject(Map.class);
            Set<?> keySet = map.keySet();
            // 将所有key转换为小写
            Map<String, Object> map1 = new HashMap<>();
            for (Object set : keySet) {
                map1.put(((String) set).toLowerCase(), map.get(set));
            }
            T instance = null;
            try {
                instance = entityClass.newInstance();
                BeanUtils.populate(instance, map1);
            } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
                e.printStackTrace();
            }
            result.add(instance);
        }
        return result;

    }

    public List<Map<?, ?>> valueToMap(List<String> list) {
        List<Map<?, ?>> result = new ArrayList<>();
        for (String value : list) {
            if (org.apache.commons.lang3.StringUtils.isNotBlank(value)) {
                Map<?, ?> map = ((JSONObject) JSONObject.parse(value)).toJavaObject(Map.class);
                result.add(map);
            }
        }
        return result;
    }

    /**
     * redis常用方法封装get
     */
    public String get(String key) {
        try (Jedis jedis = jedisPool.getResource();) {
            return jedis.get(key);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * redis常用方法封装set
     */
    public String set(String key, String value) {
        try (Jedis jedis = jedisPool.getResource();) {
            return jedis.set(key, value);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }


    public String hget(String key, String field) {
        try (Jedis jedis = jedisPool.getResource();) {
            return jedis.hget(key, field);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 获取redis的hash值
     * 用scan方式模仿hgetAll
     *
     * @param pattern key
     * @return
     */
    public Map<String, String> hScan(String pattern) {
        Map<String, String> result = new HashMap<>();
        try (Jedis jedis = jedisPool.getResource();) {
            String cursor = String.valueOf(0);
            ScanParams params = new ScanParams();
            params.count(BATCH_SIZE);
            do {
                ScanResult<Map.Entry<String, String>> scanResult = jedis.hscan(pattern, cursor, params);
                cursor = scanResult.getCursor();
                scanResult.getResult().forEach(entry -> result.put(entry.getKey(), entry.getValue()));
            } while (Integer.parseInt(cursor) > 0);
        } catch (NumberFormatException e) {
            e.printStackTrace();
        }
        return result;
    }

    public static void main(String[] args) {
        /*try (Jedis jedis = jedisPool.getResource();) {
            Pipeline pipeline = jedis.pipelined();
            pipeline.select(1); // 使用第1个库
            pipeline.flushDB(); // 清空第1个库所有数据
            // 管道批量插入
            ArrayList<String> strings = new ArrayList<>();
            strings.add("aa");
            strings.add("bb");
            strings.add("cc");
            for (String s : strings) {
                pipeline.set("pipeline1", s + "11");
                pipeline.set("pipeline2", s + "22");
            }
            // 提交批量插入的命令
            pipeline.sync();

            // 管道批量查询
            for (int i = 0; i < 10000; i++) {
                pipeline.get("pipeline" + i);
            }
            // 关闭管道，将不同类型的操作命令合并提交，同步获取所有结果（不加这句服务器也会放入数据，但是拿不到所有结果）
            List<Object> objects = pipeline.syncAndReturnAll();
            System.out.println(objects);
        }*/
    }

}
