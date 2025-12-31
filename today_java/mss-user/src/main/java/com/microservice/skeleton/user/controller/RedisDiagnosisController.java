package com.microservice.skeleton.user.controller;

import com.microservice.skeleton.user.domain.Request.ReservationRequest;
import com.microservice.skeleton.user.domain.Response.ReservationResponse;
import com.microservice.skeleton.user.domain.entity.DelayQueueMessage;
import com.microservice.skeleton.user.domain.entity.Reservation;
import com.microservice.skeleton.user.domain.vo.ReservationVO;
import com.microservice.skeleton.user.service.ReservationService;
import com.microservice.skeleton.user.service.impl.RedisDelayQueueService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ZSetOperations;
import org.springframework.web.bind.annotation.*;

import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.*;

// RedisDiagnosisController.java
@RestController
@RequestMapping("/api/diagnosis")
@Slf4j
public class RedisDiagnosisController {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private ReservationService reservationService;

    @Autowired
    private RedisDelayQueueService redisDelayQueueService;

    @GetMapping("/delay-queue/status")
    public Map<String, Object> getDelayQueueStatus() {
        Map<String, Object> result = new HashMap<>();

        try {
            long now = System.currentTimeMillis();
            log.info("检查延时队列，当前时间: {} ({})", now, new Date(now));

            // 获取所有任务
            Set<ZSetOperations.TypedTuple<Object>> allTasks =
                    redisTemplate.opsForZSet().rangeWithScores("reservation:delay:queue", 0, -1);

            log.info("从Redis获取到 {} 个任务", allTasks != null ? allTasks.size() : 0);

            // 获取到期任务
            Set<Object> expiredTasks = redisTemplate.opsForZSet().rangeByScore("reservation:delay:queue", 0, now);
            log.info("到期任务数量: {}", expiredTasks != null ? expiredTasks.size() : 0);

            result.put("success", true);
            result.put("currentTime", now);
            result.put("totalTasks", allTasks != null ? allTasks.size() : 0);
            result.put("expiredTasks", expiredTasks != null ? expiredTasks.size() : 0);

            if (allTasks != null) {
                List<Map<String, Object>> taskList = new ArrayList<>();
                int delayQueueMessageCount = 0;
                int otherTypeCount = 0;

                for (ZSetOperations.TypedTuple<Object> tuple : allTasks) {
                    Object value = tuple.getValue();
                    log.info("任务值类型: {}, 值: {}",
                            value != null ? value.getClass().getName() : "null",
                            value);

                    if (value instanceof DelayQueueMessage) {
                        DelayQueueMessage message = (DelayQueueMessage) value;
                        Map<String, Object> taskInfo = new HashMap<>();
                        taskInfo.put("reservationNo", message.getReservationNo());
                        taskInfo.put("executeTime", message.getExecuteTime());
                        taskInfo.put("score", tuple.getScore());
                        taskInfo.put("isExpired", tuple.getScore() <= now);
                        taskList.add(taskInfo);
                        delayQueueMessageCount++;
                    } else {
                        // 记录非DelayQueueMessage类型的任务
                        Map<String, Object> otherTaskInfo = new HashMap<>();
                        otherTaskInfo.put("type", value != null ? value.getClass().getName() : "null");
                        otherTaskInfo.put("score", tuple.getScore());
                        otherTaskInfo.put("value", value != null ? value.toString() : "null");
                        otherTaskInfo.put("isExpired", tuple.getScore() <= now);
                        taskList.add(otherTaskInfo);
                        otherTypeCount++;
                    }
                }

                result.put("tasks", taskList);
                result.put("delayQueueMessageCount", delayQueueMessageCount);
                result.put("otherTypeCount", otherTypeCount);
                log.info("解析结果: DelayQueueMessage={}, 其他类型={}",
                        delayQueueMessageCount, otherTypeCount);
            }

        } catch (Exception e) {
            log.error("获取延时队列状态失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }




    @PostMapping("/reservation/complete/{reservationNo}")
    public Map<String, Object> manualCompleteReservation(@PathVariable String reservationNo) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("手动完成预约: {}", reservationNo);
            reservationService.completeReservation(reservationNo);
            result.put("success", true);
            result.put("message", "手动完成预约操作已执行");
        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    // 在 RedisDiagnosisController 中添加以下方法

    @PostMapping("/create-test-reservation")
    public Map<String, Object> createTestReservation(@RequestParam(defaultValue = "1") int minutes) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 创建测试预约，{}分钟后自动完成", minutes);

            // 创建测试预约请求
            ReservationRequest request = new ReservationRequest();
            request.setRoomId(Long.valueOf(1));
            request.setReservationDate(LocalDate.now());
            request.setTimePointIds(Arrays.asList(16, 17)); // 使用现有的时间点
            request.setActivityName("测试预约-" + minutes + "分钟");
            request.setUserName("测试用户");
            request.setContact("13800138000");
            request.setAttendees(3);
            request.setDepartment("测试部");
            request.setCollege("测试学院");
            request.setMajor("测试专业");

            // 创建预约
            ReservationResponse response = reservationService.createReservation(request);

            result.put("success", true);
            result.put("reservationNo", response.getReservationNo());
            result.put("message", "测试预约创建成功，" + minutes + "分钟后自动完成");


        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("创建测试预约失败", e);
        }

        return result;
    }





    @PostMapping("/force-status-update/{reservationNo}")
    public Map<String, Object> forceStatusUpdate(@PathVariable String reservationNo,
                                                 @RequestParam Integer newStatus) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🔧 强制更新预约状态: {} -> {}", reservationNo, newStatus);

            // 查询预约记录
            Reservation reservation = reservationService.lambdaQuery()
                    .eq(Reservation::getReservationNo, reservationNo)
                    .one();

            if (reservation == null) {
                result.put("success", false);
                result.put("message", "预约不存在");
                return result;
            }

            // 直接更新状态
            boolean success = reservationService.lambdaUpdate()
                    .eq(Reservation::getId, reservation.getId())
                    .set(Reservation::getStatus, newStatus)
                    .set(Reservation::getUpdatedAt, LocalDateTime.now())
                    .update();

            if (success) {
                result.put("success", true);
                result.put("message", "状态强制更新成功");
                result.put("reservationNo", reservationNo);
                result.put("newStatus", newStatus);
            } else {
                result.put("success", false);
                result.put("message", "状态更新失败");
            }

        } catch (Exception e) {
            result.put("success", false);
            result.put("error", e.getMessage());
            log.error("强制更新状态失败", e);
        }

        return result;
    }

    @PostMapping("/test-delay-queue-direct")
    public Map<String, Object> testDelayQueueDirect(
            @RequestParam(defaultValue = "2") int minutes,
            @RequestParam(defaultValue = "1") Integer roomId) {

        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 直接测试延时队列，{}分钟后触发", minutes);

            // 1. 生成测试预约编号
            String timeStr = new SimpleDateFormat("yyyyMMddHHmmss").format(new Date());
            int randomNum = new Random().nextInt(900) + 100;
            String reservationNo = "TEST-" + timeStr + randomNum;

            log.info("生成测试预约编号: {}", reservationNo);

            // 2. 在数据库中创建一条直接为"已通过"状态的预约记录
            Reservation reservation = new Reservation();
            reservation.setReservationNo(reservationNo);
            reservation.setRoomId(Long.valueOf(roomId));
            reservation.setReservationDate(LocalDate.now());
            reservation.setStartTimeId(1);  // 随便设置一个
            reservation.setEndTimeId(2);    // 随便设置一个
            reservation.setActivityName("直接测试-" + minutes + "分钟");
            reservation.setDepartment("测试部");
            reservation.setUserName("测试用户");
            reservation.setCollege("测试学院");
            reservation.setMajor("测试专业");
            reservation.setContact("13800138000");
            reservation.setUserId("oAnc9vgK495dktuO_F43WR3fkrzg");
            reservation.setAttendees(3);
            reservation.setStatus(1); // 直接设置为"已通过"状态
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());

            // 保存到数据库
            boolean saved = reservationService.save(reservation);
            log.info("创建测试预约记录: {}, 保存结果: {}", reservationNo, saved);

            // 3. 直接创建延时任务
            DelayQueueMessage message = new DelayQueueMessage();
            message.setReservationNo(reservationNo);
            message.setReservationId(reservation.getId());
            message.setUserId(reservation.getUserId());
            message.setExecuteTime(LocalDateTime.now().plusMinutes(minutes));
            message.setType("RESERVATION_COMPLETE");

            log.info("🕒 创建延时任务，执行时间: {}", message.getExecuteTime());

            // 添加延时任务
            redisDelayQueueService.addDelayTask(message);

            // 4. 立即查看任务状态
            Map<String, Object> queueStatus = getDelayQueueStatus();

            result.put("success", true);
            result.put("reservationNo", reservationNo);
            result.put("reservationId", reservation.getId());
            result.put("executeTime", message.getExecuteTime());
            result.put("currentTime", LocalDateTime.now());
            result.put("delayMinutes", minutes);
            result.put("queueStatus", queueStatus);
            result.put("message", "✅ 测试任务创建成功，" + minutes + "分钟后自动完成");

            log.info("✅ 直接测试任务创建成功: {}", reservationNo);

        } catch (Exception e) {
            log.error("直接测试延时队列失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    @PostMapping("/test-delay-direct")
    public Map<String, Object> testDelayDirect(@RequestParam(defaultValue = "2") int minutes) {
        Map<String, Object> result = new HashMap<>();

        try {
            log.info("🧪 直接测试延时队列，{}分钟后执行", minutes);

            // 1. 生成符合长度的预约编号（20字符以内）
            // 格式：TEST + 年月日时分秒 + 随机数
            String timeStr = new SimpleDateFormat("yyMMddHHmm").format(new Date()); // 10位
            int randomNum = new Random().nextInt(90) + 10; // 2位随机数
            String reservationNo = "T" + timeStr + randomNum; // 总共13位

            log.info("生成的预约编号: {} (长度: {})", reservationNo, reservationNo.length());

            // 2. 在数据库中创建一条预约记录
            Reservation reservation = new Reservation();
            reservation.setReservationNo(reservationNo);
            reservation.setRoomId(1L);
            reservation.setReservationDate(LocalDate.now());
            reservation.setStartTimeId(1);
            reservation.setEndTimeId(2);
            reservation.setActivityName("延时测试-" + minutes + "分钟");
            reservation.setDepartment("测试部");
            reservation.setUserName("测试用户");
            reservation.setCollege("测试学院");
            reservation.setMajor("测试专业");
            reservation.setContact("13800138000");
            reservation.setUserId("oAnc9vgK495dktuO_F43WR3fkrzg");
            reservation.setAttendees(3);
            reservation.setStatus(1); // 已通过
            reservation.setCreatedAt(LocalDateTime.now());
            reservation.setUpdatedAt(LocalDateTime.now());

            // 3. 保存到数据库
            boolean saved = reservationService.save(reservation);
            log.info("📝 创建数据库记录: {}, 成功: {}", reservationNo, saved);

            if (!saved) {
                throw new RuntimeException("数据库保存失败");
            }

            // 4. 直接创建延时任务
            LocalDateTime executeTime = LocalDateTime.now().plusMinutes(minutes);

            DelayQueueMessage message = new DelayQueueMessage();
            message.setReservationNo(reservationNo);
            message.setReservationId(reservation.getId());
            message.setUserId(reservation.getUserId());
            message.setExecuteTime(executeTime);
            message.setType("RESERVATION_COMPLETE");

            log.info("🕒 创建延时任务: 执行时间={}", executeTime);

            // 5. 直接添加到Redis
            addDelayTaskDirect(message);

            result.put("success", true);
            result.put("reservationNo", reservationNo);
            result.put("reservationId", reservation.getId());
            result.put("executeTime", executeTime);
            result.put("currentTime", LocalDateTime.now());
            result.put("message", "✅ 测试任务创建成功，" + minutes + "分钟后自动完成");

        } catch (Exception e) {
            log.error("❌ 测试失败", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }

    /**
     * 直接添加延时任务到Redis
     */
    private void addDelayTaskDirect(DelayQueueMessage message) {
        try {
            String DELAY_QUEUE_KEY = "reservation:delay:queue";

            // 计算延迟时间（毫秒）
            long delayTime = message.getExecuteTime().atZone(ZoneId.systemDefault())
                    .toInstant().toEpochMilli() - System.currentTimeMillis();

            log.info("⏱️ 延迟时间计算: {} 毫秒", delayTime);

            if (delayTime <= 0) {
                log.warn("⚠️ 延迟时间已过，立即执行");
                // 立即处理
                reservationService.completeReservation(message.getReservationNo());
                return;
            }

            // 添加到Redis ZSET
            double score = System.currentTimeMillis() + delayTime;
            Boolean added = redisTemplate.opsForZSet().add(DELAY_QUEUE_KEY, message, score);

            log.info("📊 Redis操作: key={}, score={}, 添加结果={}",
                    DELAY_QUEUE_KEY, score, added);

            if (Boolean.TRUE.equals(added)) {
                log.info("✅ 延时任务添加成功");
            } else {
                log.error("❌ 延时任务添加失败");
            }

        } catch (Exception e) {
            log.error("💥 添加延时任务失败", e);
        }
    }
    @GetMapping("/query-delay-queue")
    public Map<String, Object> queryDelayQueue() {
        Map<String, Object> result = new HashMap<>();

        try {
            String DELAY_QUEUE_KEY = "reservation:delay:queue";
            long now = System.currentTimeMillis();
            LocalDateTime nowDateTime = LocalDateTime.now();

            log.info("🔍【查询延时队列】开始查询，当前时间: {} ({})", nowDateTime, now);

            // 1. 检查key是否存在
            Boolean exists = redisTemplate.hasKey(DELAY_QUEUE_KEY);
            result.put("queueExists", exists);

            if (!Boolean.TRUE.equals(exists)) {
                result.put("success", true);
                result.put("message", "延时队列不存在或为空");
                return result;
            }

            // 2. 获取队列统计信息
            Long totalTasks = redisTemplate.opsForZSet().zCard(DELAY_QUEUE_KEY);
            Long expiredTasks = redisTemplate.opsForZSet().count(DELAY_QUEUE_KEY, 0, now);
            Long upcomingTasks = redisTemplate.opsForZSet().count(DELAY_QUEUE_KEY, now, Double.MAX_VALUE);

            result.put("totalTasks", totalTasks);
            result.put("expiredTasks", expiredTasks);
            result.put("upcomingTasks", upcomingTasks);

            log.info("📊【队列统计】总数: {}, 已过期: {}, 待执行: {}",
                    totalTasks, expiredTasks, upcomingTasks);

            // 3. 获取所有任务详情
            List<Map<String, Object>> taskDetails = new ArrayList<>();
            Set<ZSetOperations.TypedTuple<Object>> allTasks = redisTemplate.opsForZSet()
                    .rangeWithScores(DELAY_QUEUE_KEY, 0, -1);

            if (allTasks != null && !allTasks.isEmpty()) {
                log.info("📋【任务详情】开始解析 {} 个任务", allTasks.size());

                for (ZSetOperations.TypedTuple<Object> tuple : allTasks) {
                    Map<String, Object> taskInfo = new HashMap<>();
                    double score = tuple.getScore();
                    long executeTimestamp = (long) score;
                    long remainingMs = executeTimestamp - now;

                    // 任务基本信息
                    taskInfo.put("score", score);
                    taskInfo.put("executeTimestamp", executeTimestamp);
                    taskInfo.put("executeTime", new Date(executeTimestamp));
                    taskInfo.put("remainingSeconds", remainingMs / 1000);
                    taskInfo.put("remainingMinutes", String.format("%.1f", remainingMs / 60000.0));
                    taskInfo.put("isExpired", remainingMs <= 0);
                    taskInfo.put("overdueBySeconds", remainingMs <= 0 ? Math.abs(remainingMs) / 1000 : 0);

                    // 解析任务内容
                    Object value = tuple.getValue();
                    taskInfo.put("valueType", value.getClass().getName());

                    if (value instanceof DelayQueueMessage) {
                        DelayQueueMessage msg = (DelayQueueMessage) value;
                        taskInfo.put("reservationNo", msg.getReservationNo());
                        taskInfo.put("reservationId", msg.getReservationId());
                        taskInfo.put("userId", msg.getUserId());
                        taskInfo.put("executeTimeInMessage", msg.getExecuteTime());
                        taskInfo.put("type", msg.getType());

                        // 检查时间是否一致
                        boolean timeMatch = msg.getExecuteTime() != null &&
                                Math.abs(executeTimestamp -
                                        msg.getExecuteTime().atZone(ZoneId.systemDefault())
                                                .toInstant().toEpochMilli()) < 1000;
                        taskInfo.put("timeConsistent", timeMatch);

                    } else if (value instanceof LinkedHashMap) {
                        Map<?, ?> map = (LinkedHashMap<?, ?>) value;
                        taskInfo.put("reservationNo", map.get("reservationNo"));
                        taskInfo.put("reservationId", map.get("reservationId"));
                        taskInfo.put("userId", map.get("userId"));
                        taskInfo.put("executeTimeInMessage", map.get("executeTime"));
                        taskInfo.put("type", map.get("type"));
                        taskInfo.put("rawData", map);

                        log.warn("⚠️【序列化问题】任务被序列化为LinkedHashMap");

                    } else {
                        taskInfo.put("rawValue", value.toString());
                        log.warn("⚠️【未知类型】任务类型: {}", value.getClass().getName());
                    }

                    // 根据状态添加颜色标识
                    String status;
                    if (remainingMs <= -60000) { // 过期超过1分钟
                        status = "🔴严重过期";
                    } else if (remainingMs <= 0) { // 刚刚过期
                        status = "🟡已过期";
                    } else if (remainingMs <= 30000) { // 30秒内执行
                        status = "🟢即将执行";
                    } else if (remainingMs <= 300000) { // 5分钟内执行
                        status = "🟢待执行";
                    } else {
                        status = "🔵等待中";
                    }
                    taskInfo.put("status", status);

                    taskDetails.add(taskInfo);

                    // 打印重要日志
                    if (remainingMs <= 0) {
                        log.warn("❌【过期任务】预约号: {}, 执行时间: {}, 已过期: {}秒",
                                taskInfo.get("reservationNo"),
                                taskInfo.get("executeTime"),
                                Math.abs(remainingMs) / 1000);
                    }
                }

                // 按执行时间排序
                taskDetails.sort((a, b) -> {
                    long timeA = ((Number) a.get("score")).longValue();
                    long timeB = ((Number) b.get("score")).longValue();
                    return Long.compare(timeA, timeB);
                });
            }

            result.put("success", true);
            result.put("queryTime", nowDateTime);
            result.put("taskDetails", taskDetails);

            // 4. 添加分析结果
            if (expiredTasks > 0) {
                result.put("analysis", "⚠️ 发现 " + expiredTasks + " 个过期任务未处理，请检查定时任务是否正常运行");
            } else if (totalTasks == 0) {
                result.put("analysis", "✅ 队列为空，一切正常");
            } else {
                result.put("analysis", "✅ 队列正常，所有任务都在未来执行");
            }

            log.info("✅【查询完成】延时队列查询成功");

        } catch (Exception e) {
            log.error("❌【查询失败】查询延时队列异常", e);
            result.put("success", false);
            result.put("error", e.getMessage());
        }

        return result;
    }
}
