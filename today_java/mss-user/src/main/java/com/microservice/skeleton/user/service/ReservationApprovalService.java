package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.entity.ReservationApproval;


import java.util.List;

public interface ReservationApprovalService extends IService<ReservationApproval> {

    List<ReservationApproval> getApprovalsByReservation(Integer reservationId);

    List<ReservationApproval> getApprovalsByAdmin(Integer adminId);
    public String getStatusDescription(Integer status);

    String getLatestReasonByReservationId(Long id);
}
