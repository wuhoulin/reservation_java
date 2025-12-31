package com.microservice.skeleton.user.service.impl;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.microservice.skeleton.user.domain.entity.DelayQueueMessage;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Set;

// RedisDelayQueueService.java
@Service
@Slf4j
public class RedisDelayQueueService {

    private static final String DELAY_QUEUE_KEY = "reservation:delay:queue";

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private ObjectMapper objectMapper; // 需要注入ObjectMapper

    /**
     * 添加延时任务
     */
    public void addDelayTask(DelayQueueMessage message) {
        try {
            // 计算延迟时间（毫秒）
            long delayTime = message.getExecuteTime().atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli() - System.currentTimeMillis();


            if (delayTime <= 0) {
                processMessage(message);
                return;
            }

            // 使用Redis的ZSET实现延时队列
            double score = System.currentTimeMillis() + delayTime;
            Boolean added = redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, message, score);


        } catch (Exception e) {
            log.error("❌ 添加延时任务失败: {}", message.getReservationNo(), e);
        }
    }

    /**
     * 处理延时队列（注释）
     */
//    @Scheduled(fixedRate = 5000)
//    public void processDelayQueue() {
//        try {
//            long now = System.currentTimeMillis();
//
//            // 获取到期的任务
//            Set<Object> expiredMessages = redisTemplate.opsForZSet()
//                    .rangeByScore(DELAY_QUEUE_KEY, 0, now);
//
//            if (expiredMessages == null || expiredMessages.isEmpty()) {
//                return;
//            }
//
//
//            for (Object messageObj : expiredMessages) {
//                try {
//                    // 转换消息对象
//                    DelayQueueMessage message = convertToDelayQueueMessage(messageObj);
//                    if (message == null) {
//
//                        redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, messageObj);
//                        continue;
//                    }
//
//                    String reservationNo = message.getReservationNo();
//
//
//                    // 尝试处理消息
//                    boolean success = false;
//                    try {
//                        reservationService.completeReservation(reservationNo);
//
//                        // 验证是否成功
//                        Thread.sleep(300); // 等待事务提交
//                        Reservation reservation = reservationService.lambdaQuery()
//                                .eq(Reservation::getReservationNo, reservationNo)
//                                .one();
//
//                        success = reservation != null && reservation.getStatus() == 4;
//                    } catch (Exception e) {
//                        log.error("处理预约失败: {}", reservationNo, e);
//                    }
//
//                    if (success) {
//                        // 处理成功才移除
//                        Long removed = redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, messageObj);
//                    } else {
//                        // 不移除，等待下次重试
//                    }
//
//                } catch (Exception e) {
//                    log.error("处理任务异常", e);
//                }
//            }
//
//        } catch (Exception e) {
//            log.error("处理延时队列异常", e);
//        }
//    }

    /**
     * 转换消息对象 - 处理LinkedHashMap问题
     */
    private DelayQueueMessage convertToDelayQueueMessage(Object obj) {
        try {
            if (obj instanceof DelayQueueMessage) {
                return (DelayQueueMessage) obj;
            } else if (obj instanceof LinkedHashMap) {
                // Redis Jackson序列化会返回LinkedHashMap
                LinkedHashMap<?, ?> map = (LinkedHashMap<?, ?>) obj;

                // 使用ObjectMapper转换
                if (objectMapper == null) {
                    log.error("ObjectMapper未注入");
                    return null;
                }

                // 配置ObjectMapper处理LocalDateTime
                objectMapper.registerModule(new JavaTimeModule());
                objectMapper.disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

                return objectMapper.convertValue(map, DelayQueueMessage.class);
            } else {

                return null;
            }
        } catch (Exception e) {
            log.error("❌ 转换消息失败", e);
            return null;
        }
    }

    /**
     * 处理消息 - 返回是否成功
     */
    private boolean processMessage(DelayQueueMessage message) {
        try {


            if ("RESERVATION_COMPLETE".equals(message.getType())) {
                // 调用完成预约服务
                reservationService.completeReservation(message.getReservationNo());


                // 查询更新后的状态
                Reservation reservation = reservationService.lambdaQuery()
                        .eq(Reservation::getReservationNo, message.getReservationNo())
                        .one();

                if (reservation != null && reservation.getStatus() == 4) {
                    log.info("✅ 预约状态更新成功: {}", message.getReservationNo());
                    return true;
                } else {
                    return false;
                }
            } else {
                log.warn("❓ 未知消息类型: {}", message.getType());
                return false;
            }
        } catch (Exception e) {
            log.error("❌ 处理消息失败: {}", message.getReservationNo(), e);
            return false;
        }
    }
}
