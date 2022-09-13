package com.bajins.demo.cache;

import com.alibaba.fastjson.support.spring.FastJsonRedisSerializer;
import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.lettuce.LettuceConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;
import org.springframework.data.redis.listener.adapter.MessageListenerAdapter;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Component;

@Configuration
public class RedisTemplateConfig {

    /**
     * 设置Redis序列化方式，默认使用的JDKSerializer的序列化方式，效率低，这里我们使用 FastJsonRedisSerializer
     * redis分布式锁 https://blog.csdn.net/qq_15421685/article/details/120550846
     * RedisTemplate方法 https://www.cnblogs.com/dw3306/p/12840012.html
     * https://blog.csdn.net/h273979586/article/details/89646954
     * https://blog.csdn.net/minghao0508/article/details/124129910
     * RedisPipeline https://blog.csdn.net/xiaoliu598906167/article/details/82218525
     *
     * @param redisConnectionFactory
     * @return
     */
    @Bean
    public RedisTemplate<String, Object> redisTemplate(LettuceConnectionFactory redisConnectionFactory) {
        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
        // key序列化
        redisTemplate.setKeySerializer(new StringRedisSerializer());
        // value序列化
        redisTemplate.setValueSerializer(new FastJsonRedisSerializer<>(Object.class));
        // Hash key序列化
        redisTemplate.setHashKeySerializer(new StringRedisSerializer());
        // Hash value序列化
        redisTemplate.setHashValueSerializer(new StringRedisSerializer());
        redisTemplate.setConnectionFactory(redisConnectionFactory);

        redisTemplate.afterPropertiesSet();
        return redisTemplate;
    }

    /**
     * Redis消息监听
     * https://mp.weixin.qq.com/s/bUCMabTlBzdzQvJeIFxnwQ
     * https://cloud.tencent.com/developer/article/1816938
     * https://blog.csdn.net/weixin_53287520/article/details/119543049
     *
     * @param redisConnectionFactory redis连接工厂
     * @param listenerAdapter        监听器
     * @return 结果
     */
    @Bean
    public RedisMessageListenerContainer container(RedisConnectionFactory redisConnectionFactory,
                                                   MessageListenerAdapter listenerAdapter) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(redisConnectionFactory);
        // 可以添加多个 messageListener，配置不同的交换机，用于发布订阅消息的主题
        container.addMessageListener(listenerAdapter, new PatternTopic("test"));
        return container;
    }

    /**
     * 绑定消息监听者和接收监听的方法,必须要注入这个监听器，不然会报错
     */
    @Bean
    public MessageListenerAdapter listenerAdapter(RedisReceiver redisReceiver) {
        MessageListenerAdapter messageListenerAdapter = new MessageListenerAdapter(redisReceiver, "receiveMessage");
        //配置序列化对象
        Jackson2JsonRedisSerializer<Object> serializer = new Jackson2JsonRedisSerializer<>(Object.class);
        ObjectMapper objectMapper = new ObjectMapper();
        objectMapper.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);
        objectMapper.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);
        serializer.setObjectMapper(objectMapper);
        messageListenerAdapter.setSerializer(serializer);
        return messageListenerAdapter;
    }

    /**
     * 继承MessageListener才能拿到消息体和频道名
     */
    @Component
    public static class RedisReceiver implements MessageListener {

        @Override
        public void onMessage(Message message, byte[] pattern) {
            System.out.println(new String(message.getBody()));
            System.out.println(new String(message.getChannel()));
        }

        /**
         * 不继承MessageListener，接收的时候只接收到消息，没有频道名
         *
         * @param message
         */
        public void receiveMessage(String message) {
            // TODO 这里是收到通道的消息之后执行的方法
            System.out.println(message);
        }
    }
}
