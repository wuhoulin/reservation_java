package com.microservice.skeleton.user.domain.Request;

import lombok.Data;
import java.math.BigDecimal;

@Data
public class CheckInRequest {
    /**
     * 预约记录ID
     */
    private Integer reservationId;

    /**
     * 用户当前的经度 (由前端高德定位获取)
     */
    private BigDecimal longitude;

    /**
     * 用户当前的纬度
     */
    private BigDecimal latitude;
}
