package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.ReservationApproval;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface ReservationApprovalMapper extends BaseMapper<ReservationApproval> {
    @Select("SELECT reason FROM reservation_approvals WHERE reservation_id = #{reservationId} ORDER BY created_at DESC LIMIT 1")
    String findLatestReasonByReservationId(@Param("reservationId") Long reservationId);

}
