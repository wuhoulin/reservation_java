package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.entity.ReservationApproval;
import com.microservice.skeleton.user.mapper.ReservationApprovalMapper;
import com.microservice.skeleton.user.service.ReservationApprovalService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ReservationApprovalServiceImpl extends ServiceImpl<ReservationApprovalMapper, ReservationApproval> implements ReservationApprovalService {


    @Autowired
    private ReservationApprovalMapper reservationApprovalMapper;
    @Override
    public List<ReservationApproval> getApprovalsByReservation(Integer reservationId) {
        LambdaQueryWrapper<ReservationApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationApproval::getReservationId, reservationId);
        return this.list(queryWrapper);
    }

    @Override
    public List<ReservationApproval> getApprovalsByAdmin(Integer adminId) {
        LambdaQueryWrapper<ReservationApproval> queryWrapper = new LambdaQueryWrapper<>();
        queryWrapper.eq(ReservationApproval::getAdminId, adminId);
        return this.list(queryWrapper);
    }

    /**
     * 获取审核状态描述
     * @param status 状态码
     * @return 状态描述
     */
    @Override
    public String getStatusDescription(Integer status) {
        if (status == null) {
            return "未知状态";
        }
        switch(status) {
            case 1: return "已通过";
            case 2: return "已拒绝";
            default: return "未知状态(" + status + ")";
        }
    }

    @Override
    public String getLatestReasonByReservationId(Long id) {
        return reservationApprovalMapper.findLatestReasonByReservationId(id);
    }

}
