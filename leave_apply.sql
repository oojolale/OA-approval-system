/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : base

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-01-22 22:30:01
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for leave_apply
-- ----------------------------
DROP TABLE IF EXISTS `leave_apply`;
CREATE TABLE `leave_apply` (
  `id` bigint(20) NOT NULL AUTO_INCREMENT,
  `days` int(32) DEFAULT NULL,
  `reason` longtext,
  `status` varchar(64) DEFAULT NULL,
  `applicant_id` bigint(20) DEFAULT NULL,
  `business_key` varchar(64) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `process_instance_id` varchar(64) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB AUTO_INCREMENT=11 DEFAULT CHARSET=utf8;
