package com.microservice.skeleton.user.domain.vo;

import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@ApiModel("预约记录视图对象")

public class ReservationVO {
    @ApiModelProperty("预约ID")
    private Long id;

    @ApiModelProperty("预约编号")
    private String reservationNo;

    @ApiModelProperty("教室ID")
    private Long roomId;

    @ApiModelProperty("教室名称")
    private String roomName;

    @JsonFormat(pattern = "yyyy-MM-dd")
    @ApiModelProperty("预约日期")
    private LocalDate reservationDate;

    @ApiModelProperty("活动名称")
    private String activityName;

    @ApiModelProperty("开始时间")
    private String startTime;

    @ApiModelProperty("结束时间")
    private String endTime;

    @ApiModelProperty("状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成")
    private Integer status;

    @ApiModelProperty("状态描述")
    private String statusDesc;

    @ApiModelProperty("参与人数")
    private Integer attendees;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @ApiModelProperty("创建时间")
    private LocalDateTime createdAt;

    // 新增字段：审核意见
    @ApiModelProperty("审核意见")
    private String auditReason;

    @ApiModelProperty("申请部门")
    private String department;

    @ApiModelProperty("申请人姓名")
    private String userName;

    @ApiModelProperty("所属学院")
    private String college;

    @ApiModelProperty("年级专业")
    private String major;

    @ApiModelProperty("联系方式")
    private String contact;

    @ApiModelProperty("指导老师姓名")
    private String teacherName;

    @ApiModelProperty("老师联系方式")
    private String teacherContact;

    @ApiModelProperty("是否需要多媒体投屏")
    private Integer needProjection;

    @ApiModelProperty("其他需求")
    private String otherRequirements;

    @ApiModelProperty("审核时间")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime auditTime;

    @ApiModelProperty("所属社区名称")
    private String communityName;
    private BigDecimal longitude;    // 经度
    private BigDecimal latitude;     // 纬度
}
