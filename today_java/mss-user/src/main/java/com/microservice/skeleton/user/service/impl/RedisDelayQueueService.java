package com.microservice.skeleton.user.service.impl;

import com.microservice.skeleton.user.domain.entity.DelayQueueMessage;
import com.microservice.skeleton.user.service.ReservationService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.ZoneId;
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

    /**
     * 添加延时任务
     */
    public void addDelayTask(DelayQueueMessage message) {
        // 计算延迟时间（毫秒）
        long delayTime = message.getExecuteTime().atZone(ZoneId.systemDefault())
                               .toInstant().toEpochMilli() - System.currentTimeMillis();

        if (delayTime <= 0) {
            log.warn("延迟时间已过，立即执行: {}", message.getReservationNo());
            processMessage(message);
            return;
        }

        // 使用Redis的ZSET实现延时队列
        redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, message,
                                     (double) System.currentTimeMillis() + delayTime);
        log.info("添加延时任务成功: {}, 执行时间: {}", message.getReservationNo(), message.getExecuteTime());
    }

    /**
     * 移除延时任务
     */
    public void removeDelayTask(DelayQueueMessage message) {
        redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, message);
        log.info("移除延时任务: {}", message.getReservationNo());
    }

    /**
     * 处理延时队列
     */
    // 修正后的processDelayQueue方法
    @Scheduled(fixedRate = 5000)
    public void processDelayQueue() {
        try {
            long now = System.currentTimeMillis();
            // 获取到期的任务
            Set<Object> messages = redisTemplate.opsForZSet().rangeByScore(DELAY_QUEUE_KEY, 0, now);

            if (messages != null && !messages.isEmpty()) {
                log.info("发现 {} 个到期的延时任务", messages.size());
                for (Object messageObj : messages) {
                    try {
                        // 这里需要确保类型转换安全
                        if (messageObj instanceof DelayQueueMessage) {
                            DelayQueueMessage message = (DelayQueueMessage) messageObj;
                            processMessage(message);
                            // 处理成功后从队列中移除
                            redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, message);
                            log.info("延时任务处理完成: {}", message.getReservationNo());
                        } else {
                            log.warn("消息类型不匹配: {}", messageObj.getClass());
                            // 移除无效消息
                            redisTemplate.opsForZSet().remove(DELAY_QUEUE_KEY, messageObj);
                        }
                    } catch (Exception e) {
                        log.error("处理延时任务失败: {}", messageObj, e);
                    }
                }
            }
        } catch (Exception e) {
            log.error("处理延时队列异常", e);
        }
    }

    /**
     * 处理消息
     */
    private void processMessage(DelayQueueMessage message) {
        if ("RESERVATION_COMPLETE".equals(message.getType())) {
            reservationService.completeReservation(message.getReservationNo());
        }
    }
}
