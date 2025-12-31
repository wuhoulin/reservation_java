package com.microservice.skeleton.user.domain.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema; // 修改
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("reservations")
@Schema(description = "预约信息")
public class Reservation {

    @Schema(description = "预约ID")
    @TableId(value = "id", type = IdType.AUTO)
    private Long id;

    @Schema(description = "预约编号")
    @TableField("reservation_no")
    private String reservationNo;

    @Schema(description = "预约教室ID")
    @TableField("room_id")
    private Long roomId;

    @Schema(description = "预约日期")
    @TableField("reservation_date")
    private LocalDate reservationDate;

    @Schema(description = "开始时间点ID")
    @TableField("start_time_id")
    private Integer startTimeId;

    @Schema(description = "结束时间点ID")
    @TableField("end_time_id")
    private Integer endTimeId;

    @Schema(description = "活动名称或用途")
    @TableField("activity_name")
    private String activityName;

    @Schema(description = "申请部门")
    @TableField("department")
    private String department;

    @Schema(description = "是否需要多媒体投屏")
    @TableField("need_projection")
    private Boolean needProjection;

    @Schema(description = "申请人姓名")
    @TableField("user_name")
    private String userName;

    @Schema(description = "所属学院")
    @TableField("college")
    private String college;

    @Schema(description = "年级专业")
    @TableField("major")
    private String major;

    @Schema(description = "联系方式")
    @TableField("contact")
    private String contact;

    @Schema(description = "指导老师姓名（如有）")
    @TableField("teacher_name")
    private String teacherName;

    @Schema(description = "指导老师联系方式（如有）")
    @TableField("teacher_contact")
    private String teacherContact;

    @Schema(description = "申请人用户ID（微信授权openID）")
    @TableField("user_id")
    private String userId;

    @Schema(description = "其他需求（如有）")
    @TableField("other_requirements")
    private String otherRequirements;

    @Schema(description = "参与人数")
    @TableField("attendees")
    private Integer attendees;

    @Schema(description = "预约状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成")
    @TableField("status")
    private Integer status;

    @Schema(description = "备注（管理员填写）")
    @TableField("remark")
    private String remark;

    @Schema(description = "创建时间")
    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @Schema(description = "更新时间")
    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    @TableField("audit_time") // 建议补充 TableField
    private LocalDateTime auditTime;

    @Schema(description = "实际签到时间")
    @TableField("check_in_time")
    @JsonFormat(pattern = "yyyy-MM-dd HH:mm:ss")
    private LocalDateTime checkInTime;

    @Schema(description = "签到状态：0-未签到，1-已签到，2-已违约(超时未签)")
    @TableField("check_in_status")
    private Integer checkInStatus;
}
