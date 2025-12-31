package com.microservice.skeleton.user.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@TableName("用户登录返回接口") // 注意：TableName通常映射数据库表名，这里如果是业务描述建议移除或修正
public class UserLoginVO implements Serializable {

    @Schema(description = "用户ID")
    private Long id;

    @Schema(description = "用户昵称")
    private String username;

    @Schema(description = "用户头像")
    private String avatar;

    @Schema(description = "访问令牌")
    private String token;

    @Schema(description = "角色列表")
    private List<String> roles;
}
