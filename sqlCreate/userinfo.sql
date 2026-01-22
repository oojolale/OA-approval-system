/*
Navicat MySQL Data Transfer

Source Server         : localhost_3306
Source Server Version : 50717
Source Host           : localhost:3306
Source Database       : base

Target Server Type    : MYSQL
Target Server Version : 50717
File Encoding         : 65001

Date: 2026-01-22 22:31:13
*/

SET FOREIGN_KEY_CHECKS=0;

-- ----------------------------
-- Table structure for userinfo
-- ----------------------------
DROP TABLE IF EXISTS `userinfo`;
CREATE TABLE `userinfo` (
  `id` varchar(255) NOT NULL,
  `name` varchar(255) DEFAULT NULL,
  `card` varchar(255) DEFAULT NULL,
  `sex` varchar(255) DEFAULT NULL,
  `creatTime` datetime DEFAULT NULL,
  `creatUser` varchar(255) DEFAULT NULL,
  `updateTime` datetime DEFAULT NULL,
  `updateUser` varchar(255) DEFAULT NULL,
  `create_time` datetime(6) DEFAULT NULL,
  `create_user` varchar(255) DEFAULT NULL,
  `update_time` datetime(6) DEFAULT NULL,
  `update_user` varchar(255) DEFAULT NULL,
  PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8;
