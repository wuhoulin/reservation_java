package com.microservice.skeleton.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@Schema(description = "预约记录视图对象")
public class ReservationVO {
    @Schema(description = "预约ID")
    private Long id;

    @Schema(description = "预约编号")
    private String reservationNo;

    @Schema(description = "教室ID")
    private Long roomId;

    @Schema(description = "教室名称")
    private String roomName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @Schema(description = "预约日期")
    private LocalDate reservationDate;

    @Schema(description = "活动名称")
    private String activityName;

    @Schema(description = "开始时间")
    private String startTime;

    @Schema(description = "结束时间")
    private String endTime;

    @Schema(description = "状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成")
    private Integer status;

    @Schema(description = "状态描述")
    private String statusDesc;

    @Schema(description = "参与人数")
    private Integer attendees;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @Schema(description = "创建时间")
    private LocalDateTime createdAt;

    @Schema(description = "审核意见")
    private String auditReason;

    @Schema(description = "申请部门")
    private String department;

    @Schema(description = "申请人姓名")
    private String userName;

    @Schema(description = "所属学院")
    private String college;

    @Schema(description = "年级专业")
    private String major;

    @Schema(description = "联系方式")
    private String contact;

    @Schema(description = "指导老师姓名")
    private String teacherName;

    @Schema(description = "老师联系方式")
    private String teacherContact;

    @Schema(description = "是否需要多媒体投屏")
    private Integer needProjection;

    @Schema(description = "其他需求")
    private String otherRequirements;

    @Schema(description = "审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @Schema(description = "所属社区名称")
    private String communityName;

    private BigDecimal longitude;    // 经度
    private BigDecimal latitude;     // 纬度
    private Integer startTimeId;
    private Integer endTimeId;

    @Schema(description = "签到状态：0-未签到，1-已签到，2-已违约")
    private Integer checkInStatus;

    @Schema(description = "实际签到时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;
}
