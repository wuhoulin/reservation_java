package com.microservice.skeleton.user.domain.Request;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import jakarta.validation.constraints.*; // javax -> jakarta
import java.time.LocalDate;
import java.util.List;

@Data
@Schema(description = "预约信息请求对象")
public class ReservationRequest {

    @Schema(description = "预约教室ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "1")
    @NotNull(message = "教室ID不能为空")
    private Long roomId;

    @Schema(description = "预约日期", requiredMode = Schema.RequiredMode.REQUIRED, example = "2025-04-22")
    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去日期")
    private LocalDate reservationDate;

    @Schema(description = "选择的时间点ID列表（开始到结束的所有时间点）", requiredMode = Schema.RequiredMode.REQUIRED, example = "[1,2,3,4]")
    @NotNull(message = "时间点不能为空")
    @Size(min = 2, message = "至少选择2个时间点（开始和结束）")
    private List<Integer> timePointIds;

    @Schema(description = "活动名称", requiredMode = Schema.RequiredMode.REQUIRED, example = "学生会会议")
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 255, message = "活动名称长度不能超过255个字符")
    private String activityName;

    @Schema(description = "申请部门", requiredMode = Schema.RequiredMode.REQUIRED, example = "计算机学院")
    @NotBlank(message = "申请部门不能为空")
    @Size(max = 255, message = "部门名称长度不能超过255个字符")
    private String department;

    @Schema(description = "是否需要投影", example = "false")
    private Boolean needProjection = false;

    @Schema(description = "使用人姓名", requiredMode = Schema.RequiredMode.REQUIRED, example = "张三")
    @NotBlank(message = "使用人姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String userName;

    @Schema(description = "所属学院", requiredMode = Schema.RequiredMode.REQUIRED, example = "计算机学院")
    @NotBlank(message = "所属学院不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100个字符")
    private String college;

    @Schema(description = "专业年级", requiredMode = Schema.RequiredMode.REQUIRED, example = "计算机科学与技术2023级")
    @NotBlank(message = "专业年级不能为空")
    @Size(max = 100, message = "专业年级长度不能超过100个字符")
    private String major;

    @Schema(description = "联系方式", requiredMode = Schema.RequiredMode.REQUIRED, example = "13800138000")
    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String contact;

    @Schema(description = "指导老师姓名", example = "李老师")
    @Size(max = 100, message = "老师姓名长度不能超过100个字符")
    private String teacherName;

    @Schema(description = "指导老师联系方式")
    private String teacherContact;

    @Schema(description = "其他需求", example = "需要白板笔")
    @Size(max = 500, message = "备注内容不能超过500个字符")
    private String otherRequirements;

    @Schema(description = "参与人数", requiredMode = Schema.RequiredMode.REQUIRED, example = "20")
    @NotNull(message = "参与人数不能为空")
    @Min(value = 1, message = "参与人数必须大于0")
    @Max(value = 200, message = "参与人数不能超过200")
    private Integer attendees;
}
