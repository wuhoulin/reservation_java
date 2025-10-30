package com.microservice.skeleton.user.mapper;


import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.Community;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

import java.util.List;

@Mapper
public interface CommunityMapper extends BaseMapper<Community> {

    @Select("SELECT * FROM communities WHERE name LIKE CONCAT('%', #{keyword}, '%')")
    List<Community> searchByKeyword(@Param("keyword") String keyword);

    // 查询所有社区
    @Select("SELECT * FROM communities")
    List<Community> findAll();

}
