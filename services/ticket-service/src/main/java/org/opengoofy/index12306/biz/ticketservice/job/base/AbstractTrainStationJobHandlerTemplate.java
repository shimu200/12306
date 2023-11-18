package org.opengoofy.index12306.biz.ticketservice.job.base;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.date.DateUtil;
import cn.hutool.core.util.StrUtil;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.xxl.job.core.context.XxlJobHelper;
import com.xxl.job.core.handler.IJobHandler;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainDO;
import org.opengoofy.index12306.biz.ticketservice.dao.mapper.TrainMapper;
import org.opengoofy.index12306.framework.starter.bases.ApplicationContextHolder;
import org.opengoofy.index12306.framework.starter.common.toolkit.EnvironmentUtil;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.util.List;
import java.util.Optional;

/**
 * 抽象列车&车票相关定时任务
 */
public abstract class AbstractTrainStationJobHandlerTemplate extends IJobHandler {

    /**
     * 模板方法模式具体实现子类执行定时任务
     *
     * @param trainDOPageRecords 列车信息分页记录
     */
    protected abstract void actualExecute(List<TrainDO> trainDOPageRecords);

    @Override
    public void execute() {
        // 初始化分页参数
        var currentPage = 1L;
        var size = 1000L;
        // 获取作业的请求参数
        var requestParam = getJobRequestParam();
        // 解析请求参数，如果为空则使用明天的日期
        var dateTime = StrUtil.isNotBlank(requestParam) ? DateUtil.parse(requestParam, "yyyy-MM-dd") : DateUtil.tomorrow();
        // 获取列车信息映射器
        var trainMapper = ApplicationContextHolder.getBean(TrainMapper.class);
        // 循环执行任务，直到所有记录都被处理
        for (; ; currentPage++) {
            // 构建查询条件，查询当天的列车信息
            var queryWrapper = Wrappers.lambdaQuery(TrainDO.class)
                    .between(TrainDO::getDepartureTime, DateUtil.beginOfDay(dateTime), DateUtil.endOfDay(dateTime));
            // 分页查询列车信息
            var trainDOPage = trainMapper.selectPage(new Page<>(currentPage, size), queryWrapper);
            // 如果分页结果为空或记录为空，结束循环
            if (trainDOPage == null || CollUtil.isEmpty(trainDOPage.getRecords())) {
                break;
            }
            // 获取当前分页的列车信息记录
            var trainDOPageRecords = trainDOPage.getRecords();
            // 调用实际执行方法，由子类具体实现
            actualExecute(trainDOPageRecords);
        }
    }

    // 获取作业的请求参数
    private String getJobRequestParam() {
        return EnvironmentUtil.isDevEnvironment()
                ? Optional.ofNullable(((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()))
                .map(ServletRequestAttributes::getRequest)
                .map(each -> each.getHeader("requestParam"))
                .orElse(null)
                : XxlJobHelper.getJobParam();
    }
}

