/*
 Navicat Premium Dump SQL

 Source Server         : 127.0.0.1
 Source Server Type    : MySQL
 Source Server Version : 80012 (8.0.12)
 Source Host           : localhost:3306
 Source Schema         : cs_member

 Target Server Type    : MySQL
 Target Server Version : 80012 (8.0.12)
 File Encoding         : 65001

 Date: 04/12/2025 15:21:50
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for benefit_template
-- ----------------------------
DROP TABLE IF EXISTS `benefit_template`;
CREATE TABLE `benefit_template`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '模板ID',
  `template_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板名称',
  `template_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '模板编码',
  `benefit_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权益类型：discount-折扣 points-积分 coupon-优惠券 service-服务',
  `benefit_params` json NOT NULL COMMENT '权益参数（JSON格式）',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `applicable_grades` json NULL COMMENT '适用等级（JSON数组）',
  `is_default` tinyint(1) NULL DEFAULT 0 COMMENT '是否默认模板：0-否 1-是',
  `usage_count` int(11) NULL DEFAULT 0 COMMENT '使用次数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_template_code`(`template_code` ASC) USING BTREE,
  INDEX `idx_benefit_type`(`benefit_type` ASC) USING BTREE,
  INDEX `idx_is_default`(`is_default` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '权益模板表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of benefit_template
-- ----------------------------

-- ----------------------------
-- Table structure for consumption_record
-- ----------------------------
DROP TABLE IF EXISTS `consumption_record`;
CREATE TABLE `consumption_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `consumption_no` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消费单号',
  `member_id` bigint(20) NULL DEFAULT NULL COMMENT '会员ID',
  `store_id` bigint(20) NULL DEFAULT NULL COMMENT '门店ID',
  `operator_id` bigint(20) NULL DEFAULT NULL COMMENT '操作员ID',
  `consumption_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '消费类型：purchase-购买 refund-退货 exchange-换货',
  `total_amount` decimal(10, 2) NOT NULL COMMENT '总金额',
  `discount_amount` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '折扣金额',
  `points_earned` int(11) NULL DEFAULT 0 COMMENT '获得积分',
  `points_used` int(11) NULL DEFAULT 0 COMMENT '使用积分',
  `coupon_id` bigint(20) NULL DEFAULT NULL COMMENT '使用的优惠券ID',
  `payment_method` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '支付方式',
  `consumption_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '消费时间',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：completed-已完成 cancelled-已取消 refunded-已退款',
  `cancel_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '取消原因',
  `refund_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '退款原因',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '备注',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_consumption_no`(`consumption_no` ASC) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_store_id`(`store_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_consumption_time`(`consumption_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_store_consumption_time`(`store_id` ASC, `consumption_time` ASC) USING BTREE COMMENT '复合索引：门店+消费时间',
  INDEX `idx_member_consumption_time`(`member_id` ASC, `consumption_time` ASC) USING BTREE COMMENT '复合索引：会员+消费时间',
  INDEX `fk_consumption_coupon_id`(`coupon_id` ASC) USING BTREE,
  CONSTRAINT `fk_consumption_coupon_id` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_consumption_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_consumption_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT,
  CONSTRAINT `fk_consumption_store_id` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE RESTRICT ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '消费记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of consumption_record
-- ----------------------------

-- ----------------------------
-- Table structure for coupon
-- ----------------------------
DROP TABLE IF EXISTS `coupon`;
CREATE TABLE `coupon`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '优惠券ID',
  `coupon_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券名称',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券代码',
  `coupon_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券类型：discount-折扣券 cash-现金券 gift-礼品券',
  `discount_value` decimal(10, 2) NULL DEFAULT NULL COMMENT '折扣价值',
  `min_consumption` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '最低消费金额',
  `max_discount` decimal(10, 2) NULL DEFAULT NULL COMMENT '最大折扣金额',
  `valid_from` datetime NOT NULL COMMENT '有效期开始',
  `valid_until` datetime NOT NULL COMMENT '有效期结束',
  `usage_limit` int(11) NULL DEFAULT NULL COMMENT '使用次数限制',
  `used_count` int(11) NULL DEFAULT 0 COMMENT '已使用次数',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '状态：active-激活 inactive-停用 expired-过期',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_coupon_code`(`coupon_code` ASC) USING BTREE,
  INDEX `idx_coupon_type`(`coupon_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_valid_period`(`valid_from` ASC, `valid_until` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '优惠券表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of coupon
-- ----------------------------

-- ----------------------------
-- Table structure for email_verification
-- ----------------------------
DROP TABLE IF EXISTS `email_verification`;
CREATE TABLE `email_verification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '验证ID',
  `user_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户类型：member-会员 admin-管理员',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '邮箱地址',
  `verification_code` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证码',
  `verification_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '验证类型：register-注册 reset-重置密码 bind-绑定邮箱',
  `is_verified` tinyint(1) NULL DEFAULT 0 COMMENT '是否已验证：0-未验证 1-已验证',
  `send_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `verified_time` datetime NULL DEFAULT NULL COMMENT '验证时间',
  `ip_address` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求IP',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '用户代理信息',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_email`(`email` ASC) USING BTREE,
  INDEX `idx_user_type_phone`(`user_type` ASC) USING BTREE,
  INDEX `idx_verification_type`(`verification_type` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_is_verified`(`is_verified` ASC) USING BTREE,
  INDEX `idx_user_type_email_type`(`user_type` ASC, `email` ASC, `verification_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 62 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '邮箱验证表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of email_verification
-- ----------------------------

-- ----------------------------
-- Table structure for login_log
-- ----------------------------
DROP TABLE IF EXISTS `login_log`;
CREATE TABLE `login_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录IP',
  `login_location` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '登录地点',
  `browser` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '浏览器',
  `os` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作系统',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：1-成功 0-失败',
  `message` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '提示信息',
  `login_result` tinyint(1) NULL DEFAULT 1 COMMENT '登录结果：1-成功 0-失败',
  `failure_reason` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '失败原因',
  `user_agent` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '用户代理信息',
  `session_id` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会话ID',
  `login_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '登录时间',
  `logout_time` datetime NULL DEFAULT NULL COMMENT '登出时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_login_time`(`login_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_login_ip`(`login_ip` ASC) USING BTREE,
  INDEX `idx_login_result`(`login_result` ASC) USING BTREE,
  INDEX `idx_user_login_time`(`user_id` ASC, `login_time` DESC) USING BTREE COMMENT '复合索引：用户+登录时间'
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '登录日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of login_log
-- ----------------------------

-- ----------------------------
-- Table structure for member
-- ----------------------------
DROP TABLE IF EXISTS `member`;
CREATE TABLE `member`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '会员ID',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号码（扩展支持国际号码）',
  `member_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员编号（按时间顺序自动生成）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员姓名',
  `password` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密存储）',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别：1-男，2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '地址',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '会员头像URL',
  `total_consumption` decimal(12, 2) NULL DEFAULT 0.00 COMMENT '累计消费金额',
  `total_points` int(11) NULL DEFAULT 0 COMMENT '累计积分',
  `current_points` int(11) NULL DEFAULT 0 COMMENT '当前积分',
  `balance` decimal(10, 2) NULL DEFAULT 0.00 COMMENT '账户余额',
  `register_store_id` bigint(20) NULL DEFAULT NULL COMMENT '注册门店ID',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-正常 2-冻结',
  `last_consumption_time` datetime NULL DEFAULT NULL COMMENT '最后消费时间',
  `login_count` int(11) NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_member_no`(`member_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_member_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_register_store_id`(`register_store_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `idx_level_status`(`status` ASC) USING BTREE COMMENT '复合索引：等级+状态',
  INDEX `idx_phone_password`(`phone` ASC, `password` ASC) USING BTREE COMMENT '复合索引：手机号+密码',
  CONSTRAINT `fk_member_register_store_id` FOREIGN KEY (`register_store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 43 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member
-- ----------------------------

-- ----------------------------
-- Table structure for member_card
-- ----------------------------
DROP TABLE IF EXISTS `member_card`;
CREATE TABLE `member_card`  (
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `member_no` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '会员编号（按时间顺序自动生成）',
  `card_status` tinyint(1) NULL DEFAULT 1 COMMENT '卡状态：1-正常 2-挂失 3-注销',
  `issue_date` datetime NULL DEFAULT NULL COMMENT '发卡日期',
  `expire_date` datetime NULL DEFAULT NULL COMMENT '到期日期',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`member_id`) USING BTREE,
  UNIQUE INDEX `uk_card_no`(`member_no` ASC) USING BTREE,
  UNIQUE INDEX `uk_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_card_status`(`card_status` ASC) USING BTREE,
  CONSTRAINT `fk_member_card_member` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员卡表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member_card
-- ----------------------------

-- ----------------------------
-- Table structure for member_coupon
-- ----------------------------
DROP TABLE IF EXISTS `member_coupon`;
CREATE TABLE `member_coupon`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `coupon_id` bigint(20) NOT NULL COMMENT '优惠券ID',
  `coupon_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '优惠券码',
  `receive_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '领取时间',
  `use_time` datetime NULL DEFAULT NULL COMMENT '使用时间',
  `expire_time` datetime NOT NULL COMMENT '过期时间',
  `consumption_id` bigint(20) NULL DEFAULT NULL COMMENT '消费记录ID',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_coupon_id`(`coupon_id` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_member_status`(`member_id` ASC) USING BTREE COMMENT '复合索引：会员+状态',
  INDEX `idx_consumption_id`(`consumption_id` ASC) USING BTREE,
  CONSTRAINT `fk_member_coupon_consumption_id` FOREIGN KEY (`consumption_id`) REFERENCES `consumption_record` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_member_coupon_coupon_id` FOREIGN KEY (`coupon_id`) REFERENCES `coupon` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_member_coupon_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 1 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员优惠券关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member_coupon
-- ----------------------------

-- ----------------------------
-- Table structure for member_grade_config
-- ----------------------------
DROP TABLE IF EXISTS `member_grade_config`;
CREATE TABLE `member_grade_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '等级ID',
  `grade_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级编码',
  `grade_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '等级名称',
  `min_points` int(11) NOT NULL COMMENT '最低积分要求',
  `max_points` int(11) NULL DEFAULT NULL COMMENT '最高积分（NULL表示无上限）',
  `points_multiplier` decimal(3, 2) NULL DEFAULT 1.00 COMMENT '积分倍率',
  `discount_rate` decimal(5, 2) NULL DEFAULT 100.00 COMMENT '折扣率（百分比）',
  `color` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '等级颜色',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_grade_code`(`grade_code` ASC) USING BTREE,
  INDEX `idx_points_range`(`min_points` ASC, `max_points` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 14 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '会员等级配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of member_grade_config
-- ----------------------------
INSERT INTO `member_grade_config` VALUES (7, 'VIP1', 'VIP1会员', 0, 999, 1.00, 99.00, '#C0C0C0', 1, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (8, 'VIP2', 'VIP2会员', 1000, 2999, 1.10, 98.00, '#87CEEB', 2, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (9, 'VIP3', 'VIP3会员', 3000, 5999, 1.20, 95.00, '#FFD700', 3, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (10, 'VIP4', 'VIP4会员', 6000, 9999, 1.30, 92.00, '#FF8C00', 4, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (11, 'VIP5', 'VIP5会员', 10000, 19999, 1.50, 88.00, '#FF6347', 5, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (12, 'VIP6', 'VIP6会员', 20000, 49999, 1.80, 85.00, '#9370DB', 6, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');
INSERT INTO `member_grade_config` VALUES (13, 'VIP7', 'VIP7会员', 50000, 999, 2.00, 77.00, '#FF1493', 7, 1, '2025-11-25 16:11:26', '2025-11-25 16:11:26');

-- ----------------------------
-- Table structure for notification
-- ----------------------------
DROP TABLE IF EXISTS `notification`;
CREATE TABLE `notification`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '通知ID',
  `title` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '标题',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '内容',
  `notification_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '通知类型：system-系统 coupon-优惠券 points-积分 activity-活动',
  `target_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '目标类型：all-全体 level-等级 member-会员',
  `target_ids` json NULL COMMENT '目标ID列表（JSON数组）',
  `sender_id` bigint(20) NULL DEFAULT NULL COMMENT '发送者ID',
  `sender_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '发送者姓名',
  `send_time` datetime NULL DEFAULT NULL COMMENT '发送时间',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '过期时间',
  `read_count` int(11) NULL DEFAULT 0 COMMENT '阅读次数',
  `status` tinyint(1) NULL DEFAULT 0 COMMENT '状态：0-草稿 1-已发布 2-已撤回',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_notification_type`(`notification_type` ASC) USING BTREE,
  INDEX `idx_target_type`(`target_type` ASC) USING BTREE,
  INDEX `idx_sender_id`(`sender_id` ASC) USING BTREE,
  INDEX `idx_send_time`(`send_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '通知表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of notification
-- ----------------------------

-- ----------------------------
-- Table structure for operation_log
-- ----------------------------
DROP TABLE IF EXISTS `operation_log`;
CREATE TABLE `operation_log`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '日志ID',
  `user_id` bigint(20) NULL DEFAULT NULL COMMENT '用户ID',
  `username` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '用户名',
  `operation_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作类型',
  `operation_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '操作名称',
  `operation_module` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作模块',
  `business_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '业务类型：member-会员 consumption-消费 coupon-优惠券 system-系统',
  `method` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方法',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求方式：GET POST PUT DELETE',
  `oper_url` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '请求URL',
  `oper_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作IP',
  `oper_location` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作地点',
  `oper_param` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '请求参数',
  `json_result` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '返回参数',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-异常 1-正常',
  `error_msg` varchar(2000) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '错误消息',
  `cost_time` bigint(20) NULL DEFAULT NULL COMMENT '耗时（毫秒）',
  `oper_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_username`(`username` ASC) USING BTREE,
  INDEX `idx_operation_type`(`operation_type` ASC) USING BTREE,
  INDEX `idx_business_type`(`business_type` ASC) USING BTREE,
  INDEX `idx_oper_time`(`oper_time` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_oper_ip`(`oper_ip` ASC) USING BTREE
) ENGINE = InnoDB CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '操作日志表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of operation_log
-- ----------------------------

-- ----------------------------
-- Table structure for points_record
-- ----------------------------
DROP TABLE IF EXISTS `points_record`;
CREATE TABLE `points_record`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '记录ID',
  `member_id` bigint(20) NOT NULL COMMENT '会员ID',
  `change_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '积分变动类型：earn-获得 use-使用 expire-过期 refund-退款调整',
  `points_change` int(11) NOT NULL COMMENT '积分变动数量（正数为获得，负数为使用）',
  `points_before` int(11) NULL DEFAULT 0 COMMENT '变动前积分',
  `points_after` int(11) NULL DEFAULT 0 COMMENT '变动后积分',
  `change_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '变动原因',
  `reference_type` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '关联类型：consumption-消费 coupon-优惠券 manual-手动调整',
  `reference_id` bigint(20) NULL DEFAULT NULL COMMENT '关联ID',
  `store_id` bigint(20) NULL DEFAULT NULL COMMENT '门店ID',
  `operator_id` bigint(20) NULL DEFAULT NULL COMMENT '操作员ID',
  `operator_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '操作员姓名',
  `expire_time` datetime NULL DEFAULT NULL COMMENT '积分过期时间',
  `record_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '记录时间',
  PRIMARY KEY (`id`) USING BTREE,
  INDEX `idx_member_id`(`member_id` ASC) USING BTREE,
  INDEX `idx_change_type`(`change_type` ASC) USING BTREE,
  INDEX `idx_reference_type_id`(`reference_type` ASC, `reference_id` ASC) USING BTREE,
  INDEX `idx_store_id`(`store_id` ASC) USING BTREE,
  INDEX `idx_operator_id`(`operator_id` ASC) USING BTREE,
  INDEX `idx_record_time`(`record_time` ASC) USING BTREE,
  INDEX `idx_expire_time`(`expire_time` ASC) USING BTREE,
  INDEX `idx_member_record_time`(`member_id` ASC, `record_time` DESC) USING BTREE COMMENT '复合索引：会员+记录时间',
  CONSTRAINT `fk_points_record_member_id` FOREIGN KEY (`member_id`) REFERENCES `member` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_points_record_operator_id` FOREIGN KEY (`operator_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT,
  CONSTRAINT `fk_points_record_store_id` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '积分记录表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of points_record
-- ----------------------------

-- ----------------------------
-- Table structure for store
-- ----------------------------
DROP TABLE IF EXISTS `store`;
CREATE TABLE `store`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '门店ID',
  `store_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店编号',
  `store_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '门店名称',
  `store_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'direct' COMMENT '门店类型：direct-直营 franchise-加盟',
  `province` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '省份',
  `city` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '城市',
  `district` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '区县',
  `address` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '详细地址',
  `contact_phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系电话',
  `contact_email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '联系邮箱',
  `business_hours` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '营业时间',
  `manager_id` bigint(20) NULL DEFAULT NULL COMMENT '店长ID',
  `manager_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '店长姓名',
  `area` decimal(8, 2) NULL DEFAULT NULL COMMENT '门店面积（平方米）',
  `employee_count` int(11) NULL DEFAULT 0 COMMENT '员工数量',
  `member_count` int(11) NULL DEFAULT 0 COMMENT '会员数量',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-停业 1-营业 2-装修',
  `open_time` datetime NULL DEFAULT NULL COMMENT '开业时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_store_no`(`store_no` ASC) USING BTREE,
  INDEX `idx_store_type`(`store_type` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_manager_id`(`manager_id` ASC) USING BTREE,
  INDEX `idx_city`(`city` ASC) USING BTREE,
  INDEX `idx_district`(`district` ASC) USING BTREE,
  CONSTRAINT `fk_store_manager_id` FOREIGN KEY (`manager_id`) REFERENCES `sys_user` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '门店表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of store
-- ----------------------------
INSERT INTO `store` VALUES (1, 'STORE001', '总店', 'direct', '北京市', '北京市', '朝阳区', '北京市朝阳区建国路88号', '010-12345678', NULL, NULL, NULL, NULL, NULL, 0, 0, 0, NULL);
INSERT INTO `store` VALUES (2, 'STORE002', '朝阳分店', 'direct', '北京市', '北京市', '朝阳区', '北京市朝阳区朝阳北路101号', '010-87654321', NULL, NULL, NULL, '门店管理员', NULL, 0, 0, 0, NULL);
INSERT INTO `store` VALUES (3, 'STORE003', '海淀分店', 'direct', '北京市', '北京市', '海淀区', '北京市海淀区中关村大街1号', '010-11223344', NULL, NULL, NULL, NULL, NULL, 0, 0, 1, NULL);

-- ----------------------------
-- Table structure for sys_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_permission`;
CREATE TABLE `sys_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '权限ID（主键）',
  `permission_code` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码（包含层级和类型信息，如system:user:add）',
  `permission_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '权限编码（包含层级和类型信息，如system:user:add）',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_permission_code`(`permission_code` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 110 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统权限表（终极版 - 仅保留核心权限识别字段）' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_permission
-- ----------------------------
INSERT INTO `sys_permission` VALUES (2, 'system:dashboard:view', '查看系统仪表盘', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (3, 'system:user:view', '查看用户列表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (4, 'system:user:add', '添加用户', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (5, 'system:user:edit', '编辑用户', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (6, 'system:user:delete', '删除用户', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (7, 'system:user:reset-password', '重置用户密码', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (8, 'system:role:view', '查看角色列表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (9, 'system:role:add', '添加角色', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (10, 'system:role:edit', '编辑角色', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (11, 'system:role:delete', '删除角色', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (12, 'system:permission:view', '查看权限列表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (13, 'system:permission:assign', '分配权限', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (14, 'system:store:view', '查看门店列表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (15, 'system:store:add', '添加门店', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (16, 'system:store:edit', '编辑门店', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (17, 'system:store:delete', '删除门店', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (18, 'system:config:view', '查看系统配置', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (19, 'system:config:edit', '编辑系统配置', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (20, 'system:log:view', '查看系统日志', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (21, 'system:backup:create', '创建系统备份', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (22, 'system:backup:restore', '恢复系统备份', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (23, 'store:dashboard:view', '查看门店仪表盘', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (24, 'store:member:view', '查看门店会员', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (25, 'store:member:add', '添加会员', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (26, 'store:member:edit', '编辑会员', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (27, 'store:member:level:manage', '管理会员等级', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (28, 'store:consumption:view', '查看消费记录', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (29, 'store:consumption:add', '录入消费记录', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (30, 'store:consumption:edit', '编辑消费记录', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (31, 'store:consumption:refund', '处理退款', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (32, 'store:coupon:view', '查看优惠券', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (33, 'store:coupon:issue', '发放优惠券', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (34, 'store:coupon:manage', '管理优惠券', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (35, 'store:points:manage', '管理积分', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (36, 'store:employee:view', '查看门店员工', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (37, 'store:employee:add', '添加门店员工', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (38, 'store:employee:edit', '编辑门店员工', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (39, 'store:report:view', '查看门店报表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (40, 'store:report:export', '导出门店报表', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (41, 'basic:profile:view', '查看个人资料', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (42, 'basic:profile:edit', '编辑个人资料', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (43, 'basic:password:change', '修改密码', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (44, 'basic:member:search', '查询会员信息', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (45, 'basic:consumption:create', '创建消费记录', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (46, 'basic:points:view', '查看积分信息', '2025-11-18 16:01:51', '2025-11-18 16:01:51');
INSERT INTO `sys_permission` VALUES (47, 'basic:coupon:verify', '验证优惠券', '2025-11-18 16:01:51', '2025-11-18 16:01:51');

-- ----------------------------
-- Table structure for sys_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_role`;
CREATE TABLE `sys_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '角色ID',
  `role_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色名称',
  `role_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色编码',
  `role_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '角色类型',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '描述',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE,
  INDEX `idx_role_type`(`role_type` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role
-- ----------------------------
INSERT INTO `sys_role` VALUES (1, '系统管理员', 'SYSTEM_ADMIN', 'system', '系统管理员，拥有所有权限', 1, NOW(), NOW());
INSERT INTO `sys_role` VALUES (2, '店长', 'STORE_ADMIN', 'store', '门店管理员，管理门店相关业务', 1, NOW(), NOW());
INSERT INTO `sys_role` VALUES (3, '店员', 'CLERK', 'clerk', '店员，处理日常业务操作', 1, NOW(), NOW());

-- ----------------------------
-- Table structure for sys_role_permission
-- ----------------------------
DROP TABLE IF EXISTS `sys_role_permission`;
CREATE TABLE `sys_role_permission`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  `permission_id` bigint(20) NOT NULL COMMENT '权限ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_role_permission`(`role_id` ASC, `permission_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  INDEX `idx_permission_id`(`permission_id` ASC) USING BTREE,
  CONSTRAINT `fk_rp_permission_id` FOREIGN KEY (`permission_id`) REFERENCES `sys_permission` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_rp_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 809 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '角色权限关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_role_permission
-- ----------------------------
INSERT INTO `sys_role_permission` VALUES (737, 1, 2);
INSERT INTO `sys_role_permission` VALUES (753, 1, 3);
INSERT INTO `sys_role_permission` VALUES (749, 1, 4);
INSERT INTO `sys_role_permission` VALUES (751, 1, 5);
INSERT INTO `sys_role_permission` VALUES (750, 1, 6);
INSERT INTO `sys_role_permission` VALUES (752, 1, 7);
INSERT INTO `sys_role_permission` VALUES (744, 1, 8);
INSERT INTO `sys_role_permission` VALUES (741, 1, 9);
INSERT INTO `sys_role_permission` VALUES (743, 1, 10);
INSERT INTO `sys_role_permission` VALUES (742, 1, 11);
INSERT INTO `sys_role_permission` VALUES (740, 1, 12);
INSERT INTO `sys_role_permission` VALUES (739, 1, 13);
INSERT INTO `sys_role_permission` VALUES (748, 1, 14);
INSERT INTO `sys_role_permission` VALUES (745, 1, 15);
INSERT INTO `sys_role_permission` VALUES (747, 1, 16);
INSERT INTO `sys_role_permission` VALUES (746, 1, 17);
INSERT INTO `sys_role_permission` VALUES (736, 1, 18);
INSERT INTO `sys_role_permission` VALUES (735, 1, 19);
INSERT INTO `sys_role_permission` VALUES (738, 1, 20);
INSERT INTO `sys_role_permission` VALUES (733, 1, 21);
INSERT INTO `sys_role_permission` VALUES (734, 1, 22);
INSERT INTO `sys_role_permission` VALUES (722, 1, 23);
INSERT INTO `sys_role_permission` VALUES (729, 1, 24);
INSERT INTO `sys_role_permission` VALUES (726, 1, 25);
INSERT INTO `sys_role_permission` VALUES (727, 1, 26);
INSERT INTO `sys_role_permission` VALUES (728, 1, 27);
INSERT INTO `sys_role_permission` VALUES (718, 1, 28);
INSERT INTO `sys_role_permission` VALUES (715, 1, 29);
INSERT INTO `sys_role_permission` VALUES (716, 1, 30);
INSERT INTO `sys_role_permission` VALUES (717, 1, 31);
INSERT INTO `sys_role_permission` VALUES (721, 1, 32);
INSERT INTO `sys_role_permission` VALUES (719, 1, 33);
INSERT INTO `sys_role_permission` VALUES (720, 1, 34);
INSERT INTO `sys_role_permission` VALUES (730, 1, 35);
INSERT INTO `sys_role_permission` VALUES (725, 1, 36);
INSERT INTO `sys_role_permission` VALUES (723, 1, 37);
INSERT INTO `sys_role_permission` VALUES (724, 1, 38);
INSERT INTO `sys_role_permission` VALUES (732, 1, 39);
INSERT INTO `sys_role_permission` VALUES (731, 1, 40);
INSERT INTO `sys_role_permission` VALUES (714, 1, 41);
INSERT INTO `sys_role_permission` VALUES (713, 1, 42);
INSERT INTO `sys_role_permission` VALUES (711, 1, 43);
INSERT INTO `sys_role_permission` VALUES (710, 1, 44);
INSERT INTO `sys_role_permission` VALUES (708, 1, 45);
INSERT INTO `sys_role_permission` VALUES (712, 1, 46);
INSERT INTO `sys_role_permission` VALUES (709, 1, 47);
INSERT INTO `sys_role_permission` VALUES (785, 2, 23);
INSERT INTO `sys_role_permission` VALUES (792, 2, 24);
INSERT INTO `sys_role_permission` VALUES (789, 2, 25);
INSERT INTO `sys_role_permission` VALUES (790, 2, 26);
INSERT INTO `sys_role_permission` VALUES (791, 2, 27);
INSERT INTO `sys_role_permission` VALUES (781, 2, 28);
INSERT INTO `sys_role_permission` VALUES (778, 2, 29);
INSERT INTO `sys_role_permission` VALUES (779, 2, 30);
INSERT INTO `sys_role_permission` VALUES (780, 2, 31);
INSERT INTO `sys_role_permission` VALUES (784, 2, 32);
INSERT INTO `sys_role_permission` VALUES (782, 2, 33);
INSERT INTO `sys_role_permission` VALUES (783, 2, 34);
INSERT INTO `sys_role_permission` VALUES (793, 2, 35);
INSERT INTO `sys_role_permission` VALUES (788, 2, 36);
INSERT INTO `sys_role_permission` VALUES (786, 2, 37);
INSERT INTO `sys_role_permission` VALUES (787, 2, 38);
INSERT INTO `sys_role_permission` VALUES (795, 2, 39);
INSERT INTO `sys_role_permission` VALUES (794, 2, 40);
INSERT INTO `sys_role_permission` VALUES (777, 2, 41);
INSERT INTO `sys_role_permission` VALUES (776, 2, 42);
INSERT INTO `sys_role_permission` VALUES (774, 2, 43);
INSERT INTO `sys_role_permission` VALUES (773, 2, 44);
INSERT INTO `sys_role_permission` VALUES (771, 2, 45);
INSERT INTO `sys_role_permission` VALUES (775, 2, 46);
INSERT INTO `sys_role_permission` VALUES (772, 2, 47);
INSERT INTO `sys_role_permission` VALUES (808, 3, 41);
INSERT INTO `sys_role_permission` VALUES (807, 3, 42);
INSERT INTO `sys_role_permission` VALUES (805, 3, 43);
INSERT INTO `sys_role_permission` VALUES (804, 3, 44);
INSERT INTO `sys_role_permission` VALUES (802, 3, 45);
INSERT INTO `sys_role_permission` VALUES (806, 3, 46);
INSERT INTO `sys_role_permission` VALUES (803, 3, 47);

-- ----------------------------
-- Table structure for sys_user
-- ----------------------------
DROP TABLE IF EXISTS `sys_user`;
CREATE TABLE `sys_user`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '用户ID',
  `phone` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '手机号码（扩展支持国际号码）',
  `password` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '密码（BCrypt加密）',
  `name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '姓名',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '邮箱',
  `avatar` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '头像URL',
  `gender` tinyint(1) NULL DEFAULT NULL COMMENT '性别：1-男，2-女',
  `birthday` date NULL DEFAULT NULL COMMENT '生日',
  `store_id` bigint(20) NULL DEFAULT NULL COMMENT '所属门店ID',
  `employee_no` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '员工编号',
  `status` tinyint(1) NULL DEFAULT 1 COMMENT '状态：0-禁用 1-启用 2-锁定',
  `login_count` int(11) NULL DEFAULT 0 COMMENT '登录次数',
  `last_login_time` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `last_login_ip` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '最后登录IP',
  `password_error_count` int(11) NULL DEFAULT 0 COMMENT '密码错误次数',
  `password_error_time` datetime NULL DEFAULT NULL COMMENT '密码错误时间',
  `password_update_time` datetime NULL DEFAULT NULL COMMENT '密码更新时间',
  `is_first_login` tinyint(1) NULL DEFAULT 1 COMMENT '是否首次登录：0-否 1-是',
  `created_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE,
  INDEX `idx_store_id`(`store_id` ASC) USING BTREE,
  INDEX `idx_status`(`status` ASC) USING BTREE,
  INDEX `idx_created_time`(`created_time` ASC) USING BTREE,
  INDEX `idx_last_login_time`(`last_login_time` ASC) USING BTREE,
  INDEX `idx_employee_no`(`employee_no` ASC) USING BTREE,
  CONSTRAINT `fk_sys_user_store_id` FOREIGN KEY (`store_id`) REFERENCES `store` (`id`) ON DELETE SET NULL ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 17 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user
-- ----------------------------
-- 默认系统管理员: 账号 13800000000 密码 admin123
INSERT INTO `sys_user` VALUES (1, '13800000000', '$2a$10$0tjZHvANFoatZRa4xptfM.RGECXJaOowfyILekJT6LLltGp8tpJmG', '系统管理员', NULL, NULL, 1, NULL, 2, 'ADM0000000001', 1, 0, NULL, NULL, 0, NULL, NULL, 1, NOW(), NOW());

-- ----------------------------
-- Table structure for sys_user_role
-- ----------------------------
DROP TABLE IF EXISTS `sys_user_role`;
CREATE TABLE `sys_user_role`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '主键ID',
  `user_id` bigint(20) NOT NULL COMMENT '用户ID',
  `role_id` bigint(20) NOT NULL COMMENT '角色ID',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_user_role`(`user_id` ASC, `role_id` ASC) USING BTREE,
  INDEX `idx_user_id`(`user_id` ASC) USING BTREE,
  INDEX `idx_role_id`(`role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_role_role_id` FOREIGN KEY (`role_id`) REFERENCES `sys_role` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT,
  CONSTRAINT `fk_user_role_user_id` FOREIGN KEY (`user_id`) REFERENCES `sys_user` (`id`) ON DELETE CASCADE ON UPDATE RESTRICT
) ENGINE = InnoDB AUTO_INCREMENT = 16 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '用户角色关联表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of sys_user_role
-- ----------------------------
INSERT INTO `sys_user_role` VALUES (1, 1, 1);

-- ----------------------------
-- Table structure for system_config
-- ----------------------------
DROP TABLE IF EXISTS `system_config`;
CREATE TABLE `system_config`  (
  `id` bigint(20) NOT NULL AUTO_INCREMENT COMMENT '配置ID',
  `config_key` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NOT NULL COMMENT '配置键',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL COMMENT '配置值',
  `config_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'system' COMMENT '配置类型：system-系统 business-业务 security-安全 notification-通知',
  `config_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT NULL COMMENT '配置描述',
  `data_type` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci NULL DEFAULT 'string' COMMENT '数据类型：string-字符串 number-数字 boolean-布尔 json-JSON decimal-小数',
  `sort_order` int(11) NULL DEFAULT 0 COMMENT '排序',
  `create_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE,
  INDEX `idx_config_type`(`config_type` ASC) USING BTREE,
  INDEX `idx_sort_order`(`sort_order` ASC) USING BTREE,
  INDEX `idx_config_type_key`(`config_type` ASC, `config_key` ASC) USING BTREE,
  INDEX `idx_update_time`(`update_time` ASC) USING BTREE
) ENGINE = InnoDB AUTO_INCREMENT = 46 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_unicode_ci COMMENT = '系统配置表' ROW_FORMAT = DYNAMIC;

-- ----------------------------
-- Records of system_config
-- ----------------------------
INSERT INTO `system_config` VALUES (1, 'site.name', '便利店会员管理系统', 'system', '网站名称', '系统显示的网站名称', 'string', 1, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (2, 'site.logo', '/uploads/0619629d-014c-4a16-8091-410c6cbc4826.jpg', 'system', '网站Logo', '系统Logo文件路径', 'string', 2, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (3, 'site.company_name', '便利店会员管理系统', 'system', '公司名称', '系统所属公司名称', 'string', 3, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (4, 'site.contact_phone', '400-000-0000', 'system', '联系电话', '公司客服联系电话', 'string', 4, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (5, 'site.contact_email', 'contact@example.com', 'system', '联系邮箱', '公司客服联系邮箱', 'string', 5, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (6, 'site.address', '北京市朝阳区建国路88号', 'system', '公司地址', '公司详细地址', 'string', 6, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (7, 'site.icp_number', '京ICP备12345678号', 'system', 'ICP备案号', '网站备案许可证号', 'string', 7, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (8, 'site.copyright', '© 2025 便利店会员科技有限公司', 'system', '版权信息', '网站版权信息', 'string', 8, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (9, 'member.register_default_points', '111', 'business', '注册默认积分', '新会员注册时获得的默认积分', 'number', 9, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (10, 'member.register_bonus_points', '100', 'business', '注册奖励积分', '新会员注册奖励积分', 'number', 10, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (11, 'member.birthday_bonus_points', '500', 'business', '生日奖励积分', '会员生日当天奖励积分', 'number', 11, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (12, 'points.conversion_rate', '100', 'business', '积分兑换比例', '100积分=1元', 'number', 12, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (13, 'points.expire_days', '365', 'business', '积分有效期', '积分过期天数', 'number', 13, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (14, 'points.earn_rate', '1', 'business', '积分获取比例', '消费1元获得的积分数', 'number', 14, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (15, 'points.deduction_rate', '100', 'business', '积分抵扣比例', '多少积分可抵扣1元', 'number', 15, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (16, 'coupon.register_enabled', 'true', 'business', '注册送券开关', '新会员注册是否赠送优惠券', 'boolean', 16, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (17, 'coupon.register_coupon_type', 'discount', 'business', '注册券类型', '注册赠送优惠券类型：discount-折扣券 cash-现金券', 'string', 17, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (18, 'coupon.register_coupon_value', '10', 'business', '注册券面值', '注册赠送优惠券面值', 'number', 18, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (19, 'coupon.register_coupon_valid_days', '30', 'business', '注册券有效期', '注册赠送优惠券有效天数', 'number', 19, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (20, 'coupon.birthday_enabled', 'true', 'business', '生日送券开关', '会员生日是否赠送优惠券', 'boolean', 20, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (21, 'security.password_min_length', '10', 'security', '密码最小长度', '用户密码最小长度要求', 'number', 21, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (22, 'security.login_max_attempts', '5', 'security', '登录最大尝试次数', '登录失败最大尝试次数', 'number', 22, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (23, 'security.login_lockout_duration', '30', 'security', '登录锁定时长', '登录锁定时长（分钟）', 'number', 23, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (25, 'security.session_timeout', '30', 'security', '会话超时时间', '用户会话超时时间（分钟）', 'number', 25, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (26, 'security.token_expire_hours', '2', 'security', '令牌有效期', 'JWT令牌有效期（小时）', 'number', 26, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (27, 'security.refresh_token_expire_days', '7', 'security', '刷新令牌有效期', '刷新令牌有效期（天）', 'number', 27, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (28, 'notification.email_enabled', 'true', 'notification', '邮件通知开关', '是否启用邮件通知功能', 'boolean', 28, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (29, 'notification.sms_enabled', 'true', 'notification', '短信通知开关', '是否启用短信通知功能', 'boolean', 29, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (30, 'notification.site_message_enabled', 'true', 'notification', '站内消息开关', '是否启用站内消息功能', 'boolean', 30, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (31, 'notification.register_welcome', 'true', 'notification', '注册欢迎邮件', '新注册会员是否发送欢迎邮件', 'boolean', 31, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (32, 'notification.birthday_reminder', 'true', 'notification', '生日提醒', '会员生日是否发送祝福消息', 'boolean', 32, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (33, 'notification.points_expire_reminder', 'true', 'notification', '积分过期提醒', '积分过期前是否发送提醒消息', 'boolean', 33, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (34, 'notification.expire_reminder_days', '7', 'notification', '过期提醒天数', '积分过期前多少天开始提醒', 'number', 34, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (35, 'ui.page_size', '20', 'system', '默认分页大小', '列表默认分页条数', 'number', 35, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (36, 'ui.date_format', 'YYYY-MM-DD', 'system', '日期格式', '系统默认日期显示格式', 'string', 36, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (37, 'ui.time_format', '24', 'system', '时间格式', '时间显示格式：12-12小时制 24-24小时制', 'string', 37, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (38, 'data.backup_enabled', 'true', 'system', '数据备份开关', '是否启用自动数据备份', 'boolean', 38, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (39, 'data.backup_frequency', 'daily', 'system', '备份频率', '数据备份频率：daily-每日 weekly-每周 monthly-每月', 'string', 39, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (40, 'data.backup_retention_days', '30', 'system', '备份保留天数', '数据备份文件保留天数', 'number', 40, '2025-11-25 10:27:16', '2025-11-25 10:27:16');
INSERT INTO `system_config` VALUES (41, 'data.log_retention_days', '90', 'system', '日志保留天数', '系统日志保留天数', 'number', 41, '2025-11-25 10:27:16', '2025-11-25 10:27:16');

SET FOREIGN_KEY_CHECKS = 1;
