-- 创建企业信息表
CREATE TABLE IF NOT EXISTS `company` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT COMMENT '企业ID',
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `company_type` VARCHAR(50) NOT NULL COMMENT '企业类型（个人、企业、政府机构等）',
    `contact_email` VARCHAR(100) DEFAULT NULL COMMENT '联系邮箱',
    `contact_phone` VARCHAR(20) DEFAULT NULL COMMENT '联系电话',
    `address` VARCHAR(200) DEFAULT NULL COMMENT '企业地址',
    `description` VARCHAR(500) DEFAULT NULL COMMENT '企业描述',
    `enabled` TINYINT(1) DEFAULT 1 COMMENT '是否启用（1:启用, 0:禁用）',
    `create_time` DATETIME DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `update_time` DATETIME DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `certification_status` VARCHAR(20) DEFAULT '未认证' COMMENT '认证状态（未认证、认证中、已认证）',
    `certification_email` VARCHAR(100) DEFAULT NULL COMMENT '认证人邮箱地址',
    PRIMARY KEY (`id`),
    UNIQUE KEY `uk_company_name` (`company_name`),
    KEY `idx_company_type` (`company_type`),
    KEY `idx_certification_status` (`certification_status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='企业信息表';

-- 插入测试数据
INSERT INTO `company` (`company_name`, `company_type`, `contact_email`, `contact_phone`, `address`, `description`, `certification_status`, `certification_email`) 
VALUES 
('测试科技有限公司', '企业', 'test@company.com', '13800138000', '北京市朝阳区', '一家专注于软件开发的科技公司', '已认证', '3942607275@qq.com'),
('个体工商户张三', '个人', 'zhangsan@qq.com', '13900139000', '上海市浦东新区', '个体经营', '未认证', NULL),
('某市政府办公室', '政府机构', 'gov@city.gov.cn', '010-12345678', '某市中心区', '政府机构', '已认证', '572233016@qq.com');
