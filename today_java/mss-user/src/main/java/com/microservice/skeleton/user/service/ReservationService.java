package com.microservice.skeleton.user.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.IService;
import com.microservice.skeleton.user.domain.Request.CheckInRequest;
import com.microservice.skeleton.user.domain.Request.ReservationApprovalRequest;
import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.Response.RoomReservationStatusResponse;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.domain.vo.CheckInStateVO;
import com.microservice.skeleton.user.domain.vo.ReservationVO;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

public interface ReservationService extends IService<Reservation> {

    ReservationResponse createReservation(ReservationRequest request);

    RoomReservationStatusResponse getRoomReservationStatus(Integer roomId, LocalDate date);



    void cancelReservation(String reservationNo, String userId);

    List<ReservationVO> getLatestReservations(String userId);

    /**
     * 重新提交被退回的预约
     * @param reservationId 预约ID
     * @param userId 用户ID
     */
    void resubmitReservation(Integer reservationId, String userId);
    public void completeReservation(String reservationNo);

    ReservationVO getReservationDetail(Integer id);



    void performCheckIn(String userId, CheckInRequest request);

    ReservationVO findCurrentCheckInTask(String openid);

    CheckInStateVO getCheckInState(String openid);

    Map<Integer, Integer> getPendingCounts(Integer roomId, LocalDate date);

    List<ReservationVO> getReservationsByUserId(String openid, Integer status, Integer roomId);
}
