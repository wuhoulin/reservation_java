package com.microservice.skeleton.user.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.microservice.skeleton.user.domain.entity.RoomReserveDate;
import com.microservice.skeleton.user.mapper.RoomReserveDateMapper;
import com.microservice.skeleton.user.service.RoomReserveDateService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RoomReserveDateServiceImpl extends ServiceImpl<RoomReserveDateMapper, RoomReserveDate>
        implements RoomReserveDateService {

    @Autowired
    private RoomReserveDateMapper roomReserveDateMapper;

    @Override
    public List<Integer> getOccupiedTimePointIds(Integer roomId, LocalDate reserveDate) {
        return roomReserveDateMapper.selectOccupiedTimePointIds(roomId, reserveDate);
    }

    @Override
    @Transactional
    public void batchUpdateTimePointStatus(Integer roomId, LocalDate reserveDate, List<Integer> timePointIds,
                                           Integer status, Long reservationId, String reservationNo) {
        // 先删除原有记录（如果有）
        LambdaQueryWrapper<RoomReserveDate> deleteWrapper = new LambdaQueryWrapper<>();
        deleteWrapper.eq(RoomReserveDate::getRoomId, roomId)
                .eq(RoomReserveDate::getReserveDate, reserveDate)
                .in(RoomReserveDate::getTimePointId, timePointIds);
        remove(deleteWrapper);

        // 批量插入新状态记录
        if (status == 0) { // 占用（不可用）
            List<RoomReserveDate> records = timePointIds.stream().map(tpId -> {
                RoomReserveDate record = new RoomReserveDate();
                record.setRoomId(roomId);
                record.setReserveDate(reserveDate);
                record.setTimePointId(tpId);
                record.setStatus(status);
                record.setReservationId(reservationId);
                record.setReservationNo(reservationNo);
                return record;
            }).collect(Collectors.toList());
            saveBatch(records);
        }
        // status=1（释放）时，只需删除即可，无需插入
    }

    @Override
    public int countUnavailableTimePoints(Integer roomId, LocalDate reserveDate, Integer startPointId, Integer endPointId) {
        return roomReserveDateMapper.countUnavailableTimePoints(roomId, reserveDate, startPointId, endPointId);
    }
}
