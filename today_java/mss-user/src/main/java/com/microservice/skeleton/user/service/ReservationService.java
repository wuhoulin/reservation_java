package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.ReservationApprovalRequest;
import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.domain.vo.ReservationVO;


import java.time.LocalDate;
import java.util.List;

public interface ReservationService extends IService<Reservation> {

    ReservationResponse createReservation(ReservationRequest request);

    RoomReservationStatusResponse getRoomReservationStatus(Integer roomId, LocalDate date);

    List<ReservationVO> getReservationsByUserId(String userId, Integer status);

    void cancelReservation(String reservationNo, String userId);

    List<ReservationVO> getLatestReservations(String userId);

}
