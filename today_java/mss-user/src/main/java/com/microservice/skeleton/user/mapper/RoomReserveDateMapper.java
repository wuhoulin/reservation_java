package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.RoomReserveDate;
import org.apache.ibatis.annotations.Param;
import java.time.LocalDate;
import java.util.List;

/**
 * 教室日期-时间点配置 Mapper
 */
public interface RoomReserveDateMapper extends BaseMapper<RoomReserveDate> {

    /**
     * 查询指定教室+日期下已被占用的时间点ID
     */
    List<Integer> selectOccupiedTimePointIds(@Param("roomId") Integer roomId,
                                             @Param("reserveDate") LocalDate reserveDate);

    /**
     * 统计区间内不可用的时间点数量
     */
    int countUnavailableTimePoints(@Param("roomId") Integer roomId,
                                   @Param("reserveDate") LocalDate reserveDate,
                                   @Param("startPointId") Integer startPointId,
                                   @Param("endPointId") Integer endPointId);

    /**
     * 批量更新时间点状态（兼容原有调用）
     */
    void batchUpdateTimePointStatus(@Param("roomId") Integer roomId,
                                    @Param("reserveDate") LocalDate reserveDate,
                                    @Param("timePointIds") List<Integer> timePointIds,
                                    @Param("status") Integer status,
                                    @Param("reservationId") Long reservationId,
                                    @Param("reservationNo") String reservationNo);


}
