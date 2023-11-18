package org.opengoofy.index12306.biz.aggregationservice;

import cn.crane4j.spring.boot.annotation.EnableCrane4j;
import cn.hippo4j.core.enable.EnableDynamicThreadPool;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.retry.annotation.EnableRetry;

/**
 * 12306 聚合服务应用启动器
 */
@EnableDynamicThreadPool
@SpringBootApplication(scanBasePackages = {
        "org.opengoofy.index12306.biz.userservice",
        "org.opengoofy.index12306.biz.ticketservice",
        "org.opengoofy.index12306.biz.orderservice",
        "org.opengoofy.index12306.biz.payservice"
})
@EnableRetry
@MapperScan(value = {
        "org.opengoofy.index12306.biz.userservice.dao.mapper",
        "org.opengoofy.index12306.biz.ticketservice.dao.mapper",
        "org.opengoofy.index12306.biz.orderservice.dao.mapper",
        "org.opengoofy.index12306.biz.payservice.dao.mapper"
})
@EnableFeignClients(value = {
        "org.opengoofy.index12306.biz.ticketservice.remote",
        "org.opengoofy.index12306.biz.orderservice.remote"
})
@EnableCrane4j(enumPackages = "org.opengoofy.index12306.biz.orderservice.common.enums")
public class AggregationServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AggregationServiceApplication.class, args);
    }
}
