/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : base

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-01-22 22:30:35
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for location
-- ----------------------------
DROP TABLE IF EXISTS `location`;
CREATE TABLE `location` (
  `id` int(32) NOT NULL AUTO_INCREMENT COMMENT '主键',
  `type` char(10) DEFAULT NULL COMMENT '位置类型',
  `location_data` text,
  `create_time` datetime DEFAULT NULL,
  `create_by` char(32) DEFAULT NULL,
  `update_time` datetime DEFAULT NULL,
  `update_by` char(32) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=20 DEFAULT CHARSET=utf8 COMMENT='登录者地理位置信息';
