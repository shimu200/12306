package org.opengoofy.index12306.biz.orderservice.dao.algorithm;

import cn.hutool.core.collection.CollUtil;
import lombok.Getter;
import org.apache.shardingsphere.infra.util.exception.ShardingSpherePreconditions;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingAlgorithm;
import org.apache.shardingsphere.sharding.api.sharding.complex.ComplexKeysShardingValue;
import org.apache.shardingsphere.sharding.exception.algorithm.sharding.ShardingAlgorithmInitializationException;

import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Properties;

/**
 * 订单数据库复合分片算法配置
 */
public class OrderCommonDataBaseComplexAlgorithm implements ComplexKeysShardingAlgorithm {

    @Getter
    private Properties props;

    private int shardingCount;
    private int tableShardingCount;

    private static final String SHARDING_COUNT_KEY = "sharding-count";
    private static final String TABLE_SHARDING_COUNT_KEY = "table-sharding-count";

    @Override
    public Collection<String> doSharding(Collection availableTargetNames, ComplexKeysShardingValue shardingValue) {
        // 获取列名和分片值的映射
        Map<String, Collection<Comparable<Long>>> columnNameAndShardingValuesMap = shardingValue.getColumnNameAndShardingValuesMap();

        // 存储分片结果的集合
        Collection<String> result = new LinkedHashSet<>(availableTargetNames.size());

        // 判断列名和分片值映射是否非空
        if (CollUtil.isNotEmpty(columnNameAndShardingValuesMap)) {
            String userId = "user_id";
            // 获取用户ID对应的分片值集合
            Collection<Comparable<Long>> customerUserIdCollection = columnNameAndShardingValuesMap.get(userId);
            // 判断用户ID分片值集合是否非空
            if (CollUtil.isNotEmpty(customerUserIdCollection)) {
                String dbSuffix;
                // 获取第一个用户ID分片值
                Comparable<?> comparable = customerUserIdCollection.stream().findFirst().get();
                // 判断用户ID分片值类型
                if (comparable instanceof String) {
                    String actualUserId = comparable.toString();
                    // 对用户ID进行哈希分片，取后6位，再对分片数和表的分片数取模
                    dbSuffix = String.valueOf(hashShardingValue(actualUserId.substring(Math.max(actualUserId.length() - 6, 0))) % shardingCount / tableShardingCount);
                } else {
                    // 对Long类型的用户ID进行哈希分片，再对分片数和表的分片数取模
                    dbSuffix = String.valueOf(hashShardingValue((Long) comparable % 1000000) % shardingCount / tableShardingCount);
                }
                // 构造数据源名称并加入结果集合
                result.add("ds_" + dbSuffix);
            } else {
                String orderSn = "order_sn";
                String dbSuffix;
                // 获取订单号对应的分片值集合
                Collection<Comparable<Long>> orderSnCollection = columnNameAndShardingValuesMap.get(orderSn);
                // 获取第一个订单号分片值
                Comparable<?> comparable = orderSnCollection.stream().findFirst().get();
                // 判断订单号分片值类型
                if (comparable instanceof String) {
                    String actualOrderSn = comparable.toString();
                    // 对订单号进行哈希分片，取后6位，再对分片数和表的分片数取模
                    dbSuffix = String.valueOf(hashShardingValue(actualOrderSn.substring(Math.max(actualOrderSn.length() - 6, 0))) % shardingCount / tableShardingCount);
                } else {
                    // 对Long类型的订单号进行哈希分片，再对分片数和表的分片数取模
                    dbSuffix = String.valueOf(hashShardingValue((Long) comparable % 1000000) % shardingCount / tableShardingCount);
                }
                // 构造数据源名称并加入结果集合
                result.add("ds_" + dbSuffix);
            }
        }

        return result;
    }


    @Override
    public void init(Properties props) {
        this.props = props;
        shardingCount = getShardingCount(props);
        tableShardingCount = getTableShardingCount(props);
    }

    private int getShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException(getType(), "Sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(SHARDING_COUNT_KEY));
    }

    private int getTableShardingCount(final Properties props) {
        ShardingSpherePreconditions.checkState(props.containsKey(TABLE_SHARDING_COUNT_KEY), () -> new ShardingAlgorithmInitializationException(getType(), "Table sharding count cannot be null."));
        return Integer.parseInt(props.getProperty(TABLE_SHARDING_COUNT_KEY));
    }

    private long hashShardingValue(final Comparable<?> shardingValue) {
        return Math.abs((long) shardingValue.hashCode());
    }

    @Override
    public String getType() {
        return "CLASS_BASED";
    }
}
