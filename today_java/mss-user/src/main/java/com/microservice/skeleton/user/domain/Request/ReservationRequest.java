package com.microservice.skeleton.user.domain.Request;

import com.baomidou.mybatisplus.annotation.TableField;
import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import lombok.Data;

import javax.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;

@Data
@ApiModel(description = "预约信息请求对象")
public class ReservationRequest {

    @ApiModelProperty("单点登录 用户唯一id")
    @TableField("student_id")
    private String studentId;

    @ApiModelProperty(value = "预约教室ID", required = true, example = "1")
    @NotNull(message = "教室ID不能为空")
    private Integer roomId;

    @ApiModelProperty(value = "预约日期", required = true, example = "2025-04-22")
    @NotNull(message = "预约日期不能为空")
    @FutureOrPresent(message = "预约日期不能是过去日期")
    private LocalDate reservationDate;

    @ApiModelProperty(value = "选择的时间点ID列表（必须2个）", required = true, example = "[1, 2]")
    @NotNull(message = "时间点不能为空")
    @Size(min = 2, max = 2, message = "必须选择2个时间点")
    private List<Integer> timePointIds;

    @ApiModelProperty(value = "活动名称", required = true, example = "学生会会议")
    @NotBlank(message = "活动名称不能为空")
    @Size(max = 255, message = "活动名称长度不能超过255个字符")
    private String activityName;

    @ApiModelProperty(value = "申请部门", required = true, example = "计算机学院")
    @NotBlank(message = "申请部门不能为空")
    @Size(max = 255, message = "部门名称长度不能超过255个字符")
    private String department;

    @ApiModelProperty(value = "是否需要投影", example = "false")
    private Boolean needProjection = false;

    @ApiModelProperty(value = "使用人姓名", required = true, example = "张三")
    @NotBlank(message = "使用人姓名不能为空")
    @Size(max = 100, message = "姓名长度不能超过100个字符")
    private String userName;

    @ApiModelProperty(value = "所属学院", required = true, example = "计算机学院")
    @NotBlank(message = "所属学院不能为空")
    @Size(max = 100, message = "学院名称长度不能超过100个字符")
    private String college;

    @ApiModelProperty(value = "专业年级", required = true, example = "计算机科学与技术2023级")
    @NotBlank(message = "专业年级不能为空")
    @Size(max = 100, message = "专业年级长度不能超过100个字符")
    private String major;

    @ApiModelProperty(value = "联系方式", required = true, example = "13800138000")
    @NotBlank(message = "联系方式不能为空")
    @Pattern(regexp = "^1[3-9]\\d{9}$", message = "请输入有效的手机号码")
    private String contact;

    @ApiModelProperty(value = "指导老师姓名", example = "李老师")
    @Size(max = 100, message = "老师姓名长度不能超过100个字符")
    private String teacherName;

    @ApiModelProperty(value = "指导老师联系方式")
    private String teacherContact;

    @ApiModelProperty(value = "其他需求", example = "需要白板笔")
    @Size(max = 500, message = "备注内容不能超过500个字符")
    private String otherRequirements;

    @ApiModelProperty(value = "参与人数", required = true, example = "20")
    @NotNull(message = "参与人数不能为空")
    @Min(value = 1, message = "参与人数必须大于0")
    @Max(value = 200, message = "参与人数不能超过200")
    private Integer attendees;

    @ApiModelProperty(value = "用户 CN（单点登录标识）", example = "zhangsan001") // ✅ 新增字段
    private String userCn;
}
