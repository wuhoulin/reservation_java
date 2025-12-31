package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.microservice.skeleton.user.domain.Response.ActivitySignupResponse;
import com.microservice.skeleton.user.domain.entity.ActivitySignup;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

@Mapper
public interface ActivitySignupMapper extends BaseMapper<ActivitySignup> {

    /**
     * 分页查询我的报名列表
     * 注意：userId 变为 String 类型
     */
    Page<ActivitySignupResponse> selectMySignupPage(Page<ActivitySignupResponse> page, @Param("userId") String userId);
}
