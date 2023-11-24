/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.opengoofy.index12306.biz.orderservice.service.orderid;

import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.util.Optional;

/**
 * 订单 ID 全局唯一生成器管理
 */
/**
 * 订单号生成器管理类
 * 实现InitializingBean接口，用于在所有属性被设置后进行初始化操作
 */
@Component
@RequiredArgsConstructor
public final class OrderIdGeneratorManager implements InitializingBean {
    private final RedissonClient redissonClient;
    private final DistributedCache distributedCache;
    // 静态变量，用于存储分布式ID生成器实例
    private static DistributedIdGenerator DISTRIBUTED_ID_GENERATOR;
    /**
     * 生成订单全局唯一ID
     *
     * @param userId 用户ID
     * @return 订单ID
     */
    public static String generateId(long userId) {
        // 生成订单ID，将分布式ID和用户ID相加，并转换为字符串
        return DISTRIBUTED_ID_GENERATOR.generateId() + String.valueOf(userId % 1000000);
    }
    /**
     * 在所有属性被设置后进行初始化操作
     * @throws Exception 如果在初始化过程中发生异常
     */
    @Override
    public void afterPropertiesSet() throws Exception {
        // 定义分布式ID生成器锁的键
        String LOCK_KEY = "distributed_id_generator_lock_key";
        // 获取Redisson分布式锁
        RLock lock = redissonClient.getLock(LOCK_KEY);
        lock.lock();
        try {
            // 获取StringRedisTemplate实例
            StringRedisTemplate instance = (StringRedisTemplate) distributedCache.getInstance();
            // 定义分布式ID生成器配置键
            String DISTRIBUTED_ID_GENERATOR_KEY = "distributed_id_generator_config";
            // 获取并增加分布式ID生成器的值
            long incremented = Optional.ofNullable(instance.opsForValue().increment(DISTRIBUTED_ID_GENERATOR_KEY)).orElse(0L);
            // 注意：这里只是提供一种分库分表基因法的实现思路，所以将标识位定义32。其次，如果对比TB网站订单号，应该不是在应用内生成，而是有一个全局服务调用获取
            int NODE_MAX = 32;
            // 判断分布式ID生成器是否超过最大值，超过则重新开始
            if (incremented > NODE_MAX) {
                incremented = 0;
                instance.opsForValue().set(DISTRIBUTED_ID_GENERATOR_KEY, "0");
            }
            // 初始化分布式ID生成器实例
            DISTRIBUTED_ID_GENERATOR = new DistributedIdGenerator(incremented);
        } finally {
            // 释放分布式锁
            lock.unlock();
        }
    }
}
