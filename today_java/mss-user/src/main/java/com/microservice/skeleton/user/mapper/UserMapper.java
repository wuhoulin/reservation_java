package com.microservice.skeleton.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.microservice.skeleton.user.domain.entity.User.User;
import io.swagger.v3.oas.models.examples.Example;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;


@Mapper
public interface UserMapper  extends BaseMapper<User> {




}
