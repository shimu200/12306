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

package org.opengoofy.index12306.biz.ticketservice.job;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.map.MapUtil;
import cn.hutool.core.util.BooleanUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.xxl.job.core.handler.annotation.XxlJob;
import lombok.RequiredArgsConstructor;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainDO;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainStationRelationDO;
import org.opengoofy.index12306.biz.ticketservice.dao.mapper.TrainStationRelationMapper;
import org.opengoofy.index12306.biz.ticketservice.job.base.AbstractTrainStationJobHandlerTemplate;
import org.opengoofy.index12306.framework.starter.cache.DistributedCache;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

import static cn.hutool.core.date.DatePattern.NORM_DATETIME_MINUTE_FORMAT;
import static org.opengoofy.index12306.biz.ticketservice.common.constant.Index12306Constant.ADVANCE_TICKET_DAY;
import static org.opengoofy.index12306.biz.ticketservice.common.constant.RedisKeyConstant.TRAIN_STATION_DETAIL;

/**
 * 站点详细信息定时任务
 */
@RestController
@RequiredArgsConstructor
public class TrainStationDetailJobHandler extends AbstractTrainStationJobHandlerTemplate {

    private final TrainStationRelationMapper trainStationRelationMapper;  // 列车站点关系映射器
    private final DistributedCache distributedCache;  // 分布式缓存

    @XxlJob(value = "trainStationDetailJobHandler")
    @GetMapping("/api/ticket-service/train-station-detail/job/cache-init/execute")
    @Override
    public void execute() {
        super.execute();  // 调用父类的执行方法
    }

    @Override
    protected void actualExecute(List<TrainDO> trainDOPageRecords) {
        // 遍历列车信息列表
        for (TrainDO each : trainDOPageRecords) {
            // 查询列车站点关系
            LambdaQueryWrapper<TrainStationRelationDO> relationQueryWrapper = Wrappers.lambdaQuery(TrainStationRelationDO.class)
                    .eq(TrainStationRelationDO::getTrainId, each.getId());
            List<TrainStationRelationDO> trainStationRelationDOList = trainStationRelationMapper.selectList(relationQueryWrapper);
            // 如果关系列表为空，继续下一个列车
            if (CollUtil.isEmpty(trainStationRelationDOList)) {
                return;
            }
            // 遍历列车站点关系
            for (TrainStationRelationDO item : trainStationRelationDOList) {
                // 构建实际缓存数据
                Map<String, String> actualCacheHashValue = MapUtil.builder("trainNumber", each.getTrainNumber())
                        .put("departureFlag", BooleanUtil.toStringTrueFalse(item.getDepartureFlag()))
                        .put("arrivalFlag", BooleanUtil.toStringTrueFalse(item.getArrivalFlag()))
                        .put("departureTime", DateUtil.format(item.getDepartureTime(), "HH:mm"))
                        .put("arrivalTime", DateUtil.format(item.getArrivalTime(), "HH:mm"))
                        .put("saleTime", DateUtil.format(each.getSaleTime(), NORM_DATETIME_MINUTE_FORMAT))
                        .put("trainTag", each.getTrainTag().toString())
                        .build();
                // 获取分布式缓存的实例
                StringRedisTemplate stringRedisTemplate = (StringRedisTemplate) distributedCache.getInstance();

                // 构建缓存
                String buildCacheKey = TRAIN_STATION_DETAIL + StrUtil.join("_", each.getId(), item.getDeparture(), item.getArrival());
                // 将数据添加到缓存哈希中，并设置过期时间
                stringRedisTemplate.opsForHash().putAll(buildCacheKey, actualCacheHashValue);
                stringRedisTemplate.expire(buildCacheKey, ADVANCE_TICKET_DAY, TimeUnit.DAYS);
            }
        }
    }
}
