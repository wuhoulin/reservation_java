package com.microservice.skeleton.user.domain.Response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;

@Data
@Schema(description = "预约信息响应对象")
public class ReservationResponse {

    @Schema(description = "预约ID", example = "1")
    private Integer id;

    @Schema(description = "预约编号", example = "R202305150001")
    private String reservationNo;

    @Schema(description = "预约用户ID", example = "1")
    private Integer userId;

    @Schema(description = "预约用户名", example = "张三")
    private String userName;

    @Schema(description = "预约教室ID", example = "1")
    private Integer roomId;

    @Schema(description = "预约教室名称", example = "党团活动室")
    private String roomName;

    @Schema(description = "所属社区名称", example = "学生第一社区")
    private String communityName;

    @Schema(description = "预约日期", example = "2023-05-15")
    private LocalDate reservationDate;

    @Schema(description = "预约时间段ID", example = "1")
    private Integer TimePointId;

    @Schema(description = "开始时间", example = "08:30:00")
    private LocalTime startTime;

    @Schema(description = "结束时间", example = "10:30:00")
    private LocalTime endTime;

    @Schema(description = "预约用途", example = "举办学生会会议")
    private String purpose;

    @Schema(description = "参与人数", example = "20")
    private Integer attendees;

    @Schema(description = "状态：0-待审核，1-已通过，2-已拒绝，3-已取消，4-已完成", example = "0")
    private Integer status;

    @Schema(description = "备注", example = "需要准备投影仪和白板")
    private String remark;

    @Schema(description = "创建时间", example = "2023-05-01T10:00:00")
    private LocalDateTime createdAt;

    @Schema(description = "更新时间", example = "2023-05-01T10:00:00")
    private LocalDateTime updatedAt;
}
