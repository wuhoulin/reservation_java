package com.microservice.skeleton.user.domain.vo;

import lombok.Data;

@Data
public class CheckInStateVO {
    /**
     * 状态码：
     * 0 - 无任务 (Empty)
     * 1 - 可立即签到 (Actionable)
     * 2 - 有任务但未开始 (Upcoming)
     */
    private Integer state;

    /**
     * 提示文本
     */
    private String message;

    /**
     * 任务详情 (如果是状态0，这里为null)
     */
    private ReservationVO taskInfo;

    /**
     * 距离【开始签到】还有多少毫秒 (用于前端倒计时)
     * 仅在 state=2 时有效
     */
    private Long countdownMs;
}
