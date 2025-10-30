package com.microservice.skeleton.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;

import com.microservice.skeleton.user.domain.entity.User.User;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.entity.Example;

@Mapper
public interface UserMapper  extends BaseMapper<User> {


    /**
     * 根据 Example 构造查询条件查找单个 User
     * @param userExample 查询条件
     * @return 返回符合条件的 User 对象
     */
    User selectOneByExample(@Param("userExample") Example userExample);



}
