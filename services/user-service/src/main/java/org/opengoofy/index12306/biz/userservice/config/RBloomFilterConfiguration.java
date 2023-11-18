package org.opengoofy.index12306.biz.userservice.config;

import org.redisson.api.RBloomFilter;
import org.redisson.api.RedissonClient;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * 布隆过滤器配置
 */
@Configuration
@EnableConfigurationProperties(UserRegisterBloomFilterProperties.class)
public class RBloomFilterConfiguration {

    /**
     * 防止用户注册缓存穿透的布隆过滤器
     */
    @Bean
    public RBloomFilter<String> userRegisterCachePenetrationBloomFilter(
            RedissonClient redissonClient,
            UserRegisterBloomFilterProperties userRegisterBloomFilterProperties) {

        // 通过 Redisson 客户端获取用户注册布隆过滤器实例
        RBloomFilter<String> cachePenetrationBloomFilter = redissonClient.getBloomFilter(userRegisterBloomFilterProperties.getName());

        // 尝试初始化布隆过滤器，设置期望的插入数量和误差概率
        cachePenetrationBloomFilter.tryInit(
                userRegisterBloomFilterProperties.getExpectedInsertions(),
                userRegisterBloomFilterProperties.getFalseProbability());

        // 返回配置好的用户注册缓存穿透布隆过滤器实例
        return cachePenetrationBloomFilter;
    }
}
