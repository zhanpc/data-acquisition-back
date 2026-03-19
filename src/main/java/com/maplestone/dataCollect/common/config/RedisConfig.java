package com.maplestone.dataCollect.common.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisPassword;
import org.springframework.data.redis.connection.RedisStandaloneConfiguration;
import org.springframework.data.redis.connection.jedis.JedisClientConfiguration;
import org.springframework.data.redis.connection.jedis.JedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.serializer.JdkSerializationRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.time.Duration;

/**
 * @description:
 * @Author hmx
 * @CreateTime 2021-07-12 17:06
 */

@Slf4j
@Configuration
@EnableCaching
public class RedisConfig {

    @Value("${redis.host}")
    private String host;
    @Value("${redis.port}")
    private Integer port;
    @Value("${redis.password}")
    private String password;
    @Value("${redis.database}")
    private Integer database;
    @Value("${redis.timeout}")
    private Integer timeout;
    @Value("${redis.pool.maxTotal}")
    private Integer maxTotal;
    @Value("${redis.pool.maxIdle}")
    private Integer maxIdle;
    @Value("${redis.pool.maxWaitMillis}")
    private Integer maxWaitMillis;
    @Value("${redis.pool.minIdle}")
    private Integer minIdle;

    @Bean
    public JedisPool getRedisPool() {
        log.info("CONNECT REDIS");
        JedisPoolConfig redisPoolConfig = new JedisPoolConfig();
        // 设置池配置项值
        // 最多分配多少个redis实例，如果-1，说明没有限制；
        // 如果已经分配了maxActive个数的redis实例，如果再去获取，就会出现exhausted(耗尽状态)
        redisPoolConfig.setMaxTotal(maxTotal);
        // redis连接池最大的idle(空闲状态)连接个数
        redisPoolConfig.setMaxIdle(maxIdle);
        // 当borrow一个redis示例的时候，超过maxWait时间，就会报JedisConnectionException
        redisPoolConfig.setMaxWaitMillis(maxWaitMillis);
        // 表示连接池在创建链接的时候会先测试一下链接是否可用，这样可以保证连接池中的链接都可用的。
        redisPoolConfig.setTestOnBorrow(true);
        redisPoolConfig.setTestOnReturn(true);
        // 根据配置实例化jedis池
        // config.timeout redispool构建时的超时时间，默认时2s；
        // 如果超时，会报SocketTimeOutException：Read timed out exception
        JedisPool jedisPool = new JedisPool(redisPoolConfig, host, port, timeout, password, database);
        return jedisPool;
    }

    @Bean
    JedisConnectionFactory jedisConnectionFactory() {
        log.info("CONNECT REDIS TO GENERATION FACTORY");
        RedisStandaloneConfiguration redisStandaloneConfiguration = new RedisStandaloneConfiguration();
        redisStandaloneConfiguration.setHostName(host);
        redisStandaloneConfiguration.setPort(port);
        redisStandaloneConfiguration.setPassword(RedisPassword.of(password));
        redisStandaloneConfiguration.setDatabase(database);

        JedisClientConfiguration.JedisClientConfigurationBuilder jedisClientConfiguration = JedisClientConfiguration
                .builder();
        jedisClientConfiguration.connectTimeout(Duration.ofMillis(timeout));
        jedisClientConfiguration.usePooling();
        return new JedisConnectionFactory(redisStandaloneConfiguration, jedisClientConfiguration.build());
    }

    @Bean
    public RedisTemplate<?, ?> getRedisTemplate() {
        JedisConnectionFactory factory = jedisConnectionFactory();
        RedisTemplate<?, ?> template = new StringRedisTemplate(factory);
        template.setKeySerializer(new StringRedisSerializer());
        template.setValueSerializer(new JdkSerializationRedisSerializer());
        return template;
    }

    // @Bean(name = "redisTemplate")
    // @SuppressWarnings({"unchecked", "rawtypes"})
    // @ConditionalOnMissingBean(name = "redisTemplate")
    // public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory
    // redisConnectionFactory) {
    // RedisTemplate<Object, Object> template = new RedisTemplate<>();
    //
    // /*//使用 fastjson 序列化
    // JacksonRedisSerializer jacksonRedisSerializer = new
    // JacksonRedisSerializer(Object.class);
    // // value 值的序列化采用 jacksonRedisSerializer
    // template.setValueSerializer(jacksonRedisSerializer);
    // template.setHashValueSerializer(jacksonRedisSerializer);
    // // key 的序列化采用 StringRedisSerializer
    // template.setKeySerializer(new StringRedisSerializer());
    // template.setHashKeySerializer(new StringRedisSerializer());*/
    //
    // template.setKeySerializer(new StringRedisSerializer());
    // template.setValueSerializer(new JdkSerializationRedisSerializer());
    //
    // template.setConnectionFactory(redisConnectionFactory);
    // return template;
    // }

}
