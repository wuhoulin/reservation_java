package com.microservice.skeleton.user.domain.vo;

import com.baomidou.mybatisplus.annotation.TableName;
import io.swagger.annotations.ApiModelProperty;
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
@TableName("用户登录返回接口")
public class UserLoginVO implements Serializable {
    @ApiModelProperty("用户ID")
    private Long id;
    @ApiModelProperty("用户昵称")
    private String username;
    @ApiModelProperty("用户头像")
    private String avatar;

    private String token;
    private List<String> roles; // 新增字段


}
