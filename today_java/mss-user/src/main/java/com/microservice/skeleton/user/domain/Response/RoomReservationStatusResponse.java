package com.microservice.skeleton.user.domain.Response;

import com.google.common.collect.Range;
import com.microservice.skeleton.user.domain.dto.TimeRangeDto;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Set;

@Data
public class RoomReservationStatusResponse {
    private Integer roomId;
    private String roomName;
    private LocalDate date;
    private List<TimeRangeDto> reservedTimeRanges;
}
