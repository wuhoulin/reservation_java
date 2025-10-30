/*
 Navicat Premium Data Transfer

 Source Server         : 192.168.88.130_3306
 Source Server Type    : MySQL
 Source Server Version : 80027
 Source Host           : 192.168.88.130:3306
 Source Schema         : lacquer_users

 Target Server Type    : MySQL
 Target Server Version : 80027
 File Encoding         : 65001

 Date: 14/02/2025 12:20:59
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for sys_menu
-- ----------------------------
DROP TABLE IF EXISTS `sys_menu`;
CREATE TABLE `sys_menu`  (
                             `id` int(0) NOT NULL AUTO_INCREMENT,
                             `code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单编码',
                             `p_code` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '菜单父编码',
                             `p_id` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '父菜单ID',
                             `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '名称',
                             `url` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL COMMENT '请求地址',
                             `is_menu` int(0) NULL DEFAULT NULL COMMENT '是否是菜单(1.菜单。2.按钮)',
                             `level` int(0) NULL DEFAULT NULL COMMENT '菜单层级',
                             `sort` int(0) NULL DEFAULT NULL COMMENT '菜单排序',
                             `status` int(0) NULL DEFAULT NULL,
                             `icon` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `create_time` datetime(0) NULL DEFAULT NULL,
                             `update_time` datetime(0) NULL DEFAULT NULL,
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `FK_CODE`(`code`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_menu
-- ----------------------------
INSERT INTO `sys_menu` VALUES (1, 'order', NULL, NULL, '订单管理', NULL, 1, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (2, 'order_list', 'order', '1', '订单列表查询', '/mss-upms/order/list', 2, NULL, NULL, NULL, NULL, NULL, NULL);
INSERT INTO `sys_menu` VALUES (3, 'order_detail', 'order', '1', '订单详情', '/mss-upms/order/detail', 2, NULL, NULL, NULL, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for sys_privilege
-- ----------------------------
DROP TABLE IF EXISTS `sys_privilege`;
CREATE TABLE `sys_privilege`  (
                                  `role_id` int(0) NOT NULL,
                                  `menu_id` int(0) NOT NULL,
                                  `create_time` datetime(0) NULL DEFAULT NULL,
                                  PRIMARY KEY (`role_id`, `menu_id`) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_privilege
-- ----------------------------
INSERT INTO `sys_privilege` VALUES (6, 1, NULL);
INSERT INTO `sys_privilege` VALUES (6, 2, NULL);
INSERT INTO `sys_privilege` VALUES (6, 3, NULL);

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
                             `id` int(0) NOT NULL AUTO_INCREMENT,
                             `name` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `value` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `tips` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `create_time` datetime(0) NULL DEFAULT NULL,
                             `update_time` datetime(0) NULL DEFAULT NULL,
                             `status` int(0) NOT NULL,
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `unique_role_name`(`name`) USING BTREE,
                             UNIQUE INDEX `unique_role_value`(`value`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 18 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (6, '管理员', 'admin', NULL, '2020-10-28 15:07:13', NULL, 1);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
                             `id` int(0) NOT NULL AUTO_INCREMENT,
                             `avatar` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `username` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `password` varchar(96) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `salt` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `name` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `birthday` datetime(0) NULL DEFAULT NULL,
                             `sex` int(0) NULL DEFAULT NULL,
                             `email` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `phone` varchar(45) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                             `status` int(0) NULL DEFAULT NULL,
                             `create_time` datetime(0) NULL DEFAULT NULL,
                             `update_time` datetime(0) NULL DEFAULT NULL,
                             PRIMARY KEY (`id`) USING BTREE,
                             UNIQUE INDEX `unique_user_username`(`username`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 52 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
INSERT INTO `sys_user` VALUES (48, NULL, 'admin', '$2a$10$vmp.zWWnX3LFxSs6I00i0eurlHR7ymcfUQ5HtXw71w9QJ.2JUf8Ua', NULL, '管理员', NULL, 1, NULL, NULL, 1, '2020-10-27 17:31:41', NULL);
INSERT INTO `sys_user` VALUES (50, NULL, 'test1', '$2a$10$vmp.zWWnX3LFxSs6I00i0eurlHR7ymcfUQ5HtXw71w9QJ.2JUf8Ua', NULL, 'test1', NULL, 1, NULL, NULL, 1, '2020-10-27 16:11:15', NULL);
INSERT INTO `sys_user` VALUES (51, NULL, 'test2', '$2a$10$vmp.zWWnX3LFxSs6I00i0eurlHR7ymcfUQ5HtXw71w9QJ.2JUf8Ua', NULL, 'test2', NULL, 1, NULL, NULL, 1, '2020-10-27 17:09:51', NULL);
INSERT INTO `sys_user` VALUES (52, NULL, 'john_doe', '$2a$10$Dc4MIrdvqRYwD4BVPTPh3ujoLE9Cu6NBm/xIBm/RzrCNDRxvrg6yS', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (53, NULL, 'user1', '$2a$10$jgZfe4v4K126lBsVnLMKA.9CsRXXhWQWaZJ5Qas0zZA7fuqwDJbQy', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);
INSERT INTO `sys_user` VALUES (54, NULL, 'user2', '$2a$10$0byl9giuvFjOFwfxr1stdOYWLKtlFnYd6ShW6YaWj0q2y2nY75.xa', NULL, NULL, NULL, NULL, NULL, NULL, 1, NULL, NULL);

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
                                  `id` int(0) NOT NULL AUTO_INCREMENT,
                                  `user_id` int(0) NULL DEFAULT NULL,
                                  `role_id` int(0) NULL DEFAULT NULL,
                                  `create_time` datetime(0) NULL DEFAULT NULL,
                                  `create_by` varchar(255) CHARACTER SET utf8 COLLATE utf8_general_ci NULL DEFAULT NULL,
                                  PRIMARY KEY (`id`) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8 COLLATE = utf8_general_ci ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 48, 6, NULL, NULL);

SET FOREIGN_KEY_CHECKS = 1;
