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

/**
 * 全局唯一订单号生成器
 */
/**
 * 分布式ID生成器
 */
public class DistributedIdGenerator {
    // 起始时间戳，用于生成相对时间
    private static final long EPOCH = 1609459200000L;
    // 节点ID占用的位数
    private static final int NODE_BITS = 5;
    // 序列号占用的位数
    private static final int SEQUENCE_BITS = 7;
    // 节点ID
    private final long nodeID;
    // 上次生成ID的时间戳
    private long lastTimestamp = -1L;
    // 当前序列号
    private long sequence = 0L;
    /**
     * 构造方法，接收节点ID作为参数
     *
     * @param nodeID 节点ID
     */
    public DistributedIdGenerator(long nodeID) {
        this.nodeID = nodeID;
    }
    /**
     * 生成分布式ID
     *
     * @return 分布式ID
     */
    public synchronized long generateId() {
        // 获取当前相对时间戳
        long timestamp = System.currentTimeMillis() - EPOCH;
        // 检查时钟是否后退
        if (timestamp < lastTimestamp) {
            throw new RuntimeException("时钟回拨，拒绝生成ID。");
        }
        // 如果时间戳与上次相同，递增序列号；否则重置序列号为0
        if (timestamp == lastTimestamp) {
            sequence = (sequence + 1) & ((1 << SEQUENCE_BITS) - 1);
            if (sequence == 0) {
                timestamp = tilNextMillis(lastTimestamp);
            }
        } else {
            sequence = 0L;
        }
        // 更新上次时间戳
        lastTimestamp = timestamp;
        // 生成最终的ID
        return (timestamp << (NODE_BITS + SEQUENCE_BITS)) | (nodeID << SEQUENCE_BITS) | sequence;
    }

    /**
     * 等待直到下一个毫秒
     *
     * @param lastTimestamp 上次生成ID的时间戳
     * @return 下一个毫秒的时间戳
     */
    private long tilNextMillis(long lastTimestamp) {
        long timestamp = System.currentTimeMillis() - EPOCH;
        while (timestamp <= lastTimestamp) {
            timestamp = System.currentTimeMillis() - EPOCH;
        }
        return timestamp;
    }
}
