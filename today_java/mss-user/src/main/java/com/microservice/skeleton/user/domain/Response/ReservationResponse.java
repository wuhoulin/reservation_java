package com.microservice.skeleton.user.domain.Response;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@ApiModel(description = "预约信息响应对象")
public class ReservationResponse {

    @ApiModelProperty(value = "预约ID", example = "1")
    private Integer id;

    @ApiModelProperty(value = "预约编号", example = "R202305150001")
    private String reservationNo;

    @ApiModelProperty(value = "预约用户ID", example = "1")
    private Integer userId;

    @ApiModelProperty(value = "预约用户名", example = "张三")
    private String userName;

    @ApiModelProperty(value = "预约教室ID", example = "1")
    private Integer roomId;

    @ApiModelProperty(value = "预约教室名称", example = "党团活动室")
    private String roomName;

    @ApiModelProperty(value = "所属社区名称", example = "学生第一社区")
    private String communityName;

    @ApiModelProperty(value = "预约日期", example = "2023-05-15")
    private LocalDate reservationDate;

    @ApiModelProperty(value = "预约时间段ID", example = "1")
    private Integer TimePointId;

    @ApiModelProperty(value = "开始时间", example = "08:30:00")
    private LocalTime startTime;

    @ApiModelProperty(value = "结束时间", example = "10:30:00")
    private LocalTime endTime;

    @ApiModelProperty(value = "预约用途", example = "举办学生会会议")
    private String purpose;

    @ApiModelProperty(value = "参与人数", example = "20")
    private Integer attendees;

    @ApiModelProperty(value = "状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成", example = "0")
    private Integer status;

    @ApiModelProperty(value = "备注", example = "需要准备投影仪和白板")
    private String remark;

    @ApiModelProperty(value = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @ApiModelProperty(value = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
