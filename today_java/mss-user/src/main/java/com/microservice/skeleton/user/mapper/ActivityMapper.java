package com.microservice.skeleton.user.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.microservice.skeleton.user.domain.entity.Activity;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Update;

public interface ActivityMapper extends BaseMapper<Activity> {

    /**
     * 浏览量 +1
     */
    @Update("UPDATE activity SET view_count = view_count + 1 WHERE activity_id = #{activityId}")
    int incrementViewCount(@Param("activityId") Long activityId);

    /**
     * 扣减库存（报名）- 乐观锁实现
     */
    @Update("UPDATE activity SET current_people = current_people + 1 WHERE activity_id = #{activityId} AND current_people < max_people")
    int increasePeopleCount(@Param("activityId") Long activityId);
}
