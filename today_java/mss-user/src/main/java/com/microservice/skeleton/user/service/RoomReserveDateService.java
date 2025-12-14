package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.entity.RoomReserveDate;

import java.time.LocalDate;
import java.util.List;

public interface RoomReserveDateService extends IService<RoomReserveDate> {

    /**
     * 查询指定教室+日期下已被占用的时间点ID
     */
    List<Integer> getOccupiedTimePointIds(Integer roomId, LocalDate reserveDate);

    /**
     * 批量更新时间点状态（占用/释放）
     */
    void batchUpdateTimePointStatus(Integer roomId, LocalDate reserveDate, List<Integer> timePointIds,
                                    Integer status, Long reservationId, String reservationNo);

    /**
     * 统计区间内不可用的时间点数量
     */
    int countUnavailableTimePoints(Integer roomId, LocalDate reserveDate, Integer startPointId, Integer endPointId);
}
