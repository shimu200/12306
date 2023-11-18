package org.opengoofy.index12306.biz.ticketservice;

import cn.hutool.core.collection.CollUtil;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.toolkit.Wrappers;
import org.junit.jupiter.api.Test;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainStationDO;
import org.opengoofy.index12306.biz.ticketservice.dao.entity.TrainStationRelationDO;
import org.opengoofy.index12306.biz.ticketservice.dao.mapper.TrainStationMapper;
import org.opengoofy.index12306.biz.ticketservice.dao.mapper.TrainStationRelationMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@SpringBootTest
class TicketStationRelationTests {

    @Autowired
    private TrainStationMapper trainStationMapper;

    @Autowired
    private TrainStationRelationMapper trainStationRelationMapper;

    @Test
    void testInitData() {
        String trainId = "4";
        List<TrainStationDO> trainStations = selectTrainStations(trainId);
        List<TrainStationRelationDO> trainStationRelations = buildTrainStationRelations(trainStations);
        trainStationRelations.forEach(each -> trainStationRelationMapper.insert(each));
    }

    private List<TrainStationDO> selectTrainStations(String trainId) {
        LambdaQueryWrapper<TrainStationDO> queryWrapper = Wrappers.lambdaQuery(TrainStationDO.class)
                .eq(TrainStationDO::getTrainId, trainId);
        List<TrainStationDO> trainStationDOS = trainStationMapper.selectList(queryWrapper);
        return trainStationDOS;
    }

    private List<TrainStationRelationDO> buildTrainStationRelations(List<TrainStationDO> trainStations) {
        List<TrainStationRelationDO> result = new ArrayList<>();
        for (int i = 0; i < trainStations.size() - 1; i++) {
            TrainStationDO trainStationDO = trainStations.get(i);
            for (int j = i + 1; j < trainStations.size(); j++) {
                TrainStationRelationDO actual = new TrainStationRelationDO();
                actual.setTrainId(trainStationDO.getTrainId());
                actual.setDeparture(trainStations.get(i).getDeparture());
                actual.setArrival(trainStations.get(j).getDeparture());
                actual.setArrivalTime(trainStations.get(j).getArrivalTime());
                actual.setStartRegion(trainStations.get(i).getStartRegion());
                actual.setEndRegion(trainStations.get(j).getStartRegion());
                actual.setDepartureTime(trainStations.get(i).getDepartureTime());
                actual.setCreateTime(new Date());
                actual.setUpdateTime(new Date());
                actual.setDelFlag(0);
                actual.setDepartureFlag(i == 0);
                TrainStationDO last = CollUtil.getLast(trainStations);
                String departure = trainStations.get(j).getDeparture();
                actual.setArrivalFlag(Objects.equals(departure, last.getDeparture()));
                result.add(actual);
            }
        }
        return result;
    }
}
