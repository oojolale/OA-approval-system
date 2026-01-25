# 邮件发送功能使用指南

## 功能概述

本系统实现了完整的邮件发送功能，包括：
1. 发送邮件到配置的指定邮箱地址
2. 激活认证人邮件功能
3. 获取当前邮件的详细信息
4. 判断企业类型并获取相关企业信息
5. 使用Thymeleaf构建美观的邮件模板

## 配置说明

### 1. 邮件服务器配置（application.yml）

```yaml
spring:
  mail:
    host: smtp.qq.com              # QQ邮箱SMTP服务器
    port: 465                       # SSL端口
    username: 3942607275@qq.com     # 发件人邮箱
    password: your-authorization-code  # 授权码（不是邮箱密码！）
    protocol: smtp
    default-encoding: UTF-8
    properties:
      mail:
        smtp:
          auth: true
          ssl:
            enable: true
          socketFactory:
            class: javax.net.ssl.SSLSocketFactory
            port: 465
          debug: true               # 开发时可开启，生产环境建议关闭
```

### 2. 收件人配置

```yaml
mail:
  to: 3942607275@qq.com,572233016@qq.com  # 多个收件人用逗号分隔
  replyTo: 572233016@qq.com                # 回复地址
  templatePath: email                       # 模板名称（不含.html）
  subject: "OA审批系统异常警告"             # 邮件主题前缀
```

## 如何开启QQ邮箱SMTP服务

### 步骤1：登录QQ邮箱
访问 https://mail.qq.com 登录您的QQ邮箱

### 步骤2：进入设置
点击顶部"设置" -> "账户"

### 步骤3：开启POP3/SMTP服务
1. 找到"POP3/IMAP/SMTP/Exchange/CardDAV/CalDAV服务"
2. 点击"开启POP3/SMTP服务"或"开启IMAP/SMTP服务"
3. 根据提示发送短信验证（需要绑定的手机号）
4. 验证成功后，会显示**授权码**（16位字符）

### 步骤4：保存授权码
⚠️ **重要**: 将授权码复制并保存到 `application.yml` 中的 `spring.mail.password` 配置项

```yaml
spring:
  mail:
    password: "abcd efgh ijkl mnop"  # 这里填写授权码，不是QQ密码！
```

## API接口说明

### 1. 发送普通邮件

**接口地址**: `GET /api/sendMail`

**请求参数**:
- `type` (可选): 邮件类型，默认"系统通知"
- `prompt` (可选): 提示信息，默认"测试邮件"
- `companyId` (可选): 企业ID，传入后会附带企业信息

**示例**:
```
# 简单测试
GET http://localhost:8080/api/sendMail

# 带参数
GET http://localhost:8080/api/sendMail?type=告警通知&prompt=服务器异常&companyId=1
```

**响应**:
```json
{
  "code": 200,
  "msg": "邮件发送中，请稍后查收！"
}
```

### 2. 发送企业认证邮件

**接口地址**: `GET /api/sendCertificationMail`

**权限要求**: 需要 USER 或 ADMIN 角色

**请求参数**:
- `companyId` (必填): 企业ID
- `certificationEmail` (必填): 认证人邮箱地址

**示例**:
```
GET http://localhost:8080/api/sendCertificationMail?companyId=1&certificationEmail=admin@company.com
```

**响应**:
```json
{
  "code": 200,
  "msg": "认证邮件已发送到：admin@company.com"
}
```

## 数据库表结构

### Company表（企业信息表）

运行 `sqlCreate/company.sql` 创建表和测试数据：

```sql
-- 创建企业信息表
CREATE TABLE `company` (
    `id` BIGINT(20) NOT NULL AUTO_INCREMENT,
    `company_name` VARCHAR(100) NOT NULL COMMENT '企业名称',
    `company_type` VARCHAR(50) NOT NULL COMMENT '企业类型',
    `contact_email` VARCHAR(100),
    `contact_phone` VARCHAR(20),
    `address` VARCHAR(200),
    `description` VARCHAR(500),
    `enabled` TINYINT(1) DEFAULT 1,
    `create_time` DATETIME,
    `update_time` DATETIME,
    `certification_status` VARCHAR(20) DEFAULT '未认证',
    `certification_email` VARCHAR(100),
    PRIMARY KEY (`id`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;
```

### 企业类型说明

系统会自动判断企业类型：
- **企业**: 标准的企业单位
- **政府机构**: 政府部门
- **个人**: 个体工商户或个人

判断逻辑在 `MailService` 中：
```java
boolean isEnterprise = "企业".equals(company.getCompanyType()) || 
                       "政府机构".equals(company.getCompanyType());
```

## 邮件模板

邮件模板位于 `src/main/resources/templates/email.html`

### 模板变量说明

模板支持以下变量：
- `subject`: 邮件主题
- `type`: 邮件类型
- `prompt`: 提示信息
- `username`: 用户名称
- `email`: 用户邮箱
- `companyName`: 企业名称
- `companyType`: 企业类型
- `currentDate`: 当前时间
- `count`: 邮件计数
- `info`: 详细信息

### 自定义模板

您可以根据需要修改 `email.html` 模板，Thymeleaf语法示例：

```html
<!-- 条件显示 -->
<tr th:if="${companyName}">
    <td>企业名称：</td>
    <td th:text="${companyName}">企业</td>
</tr>

<!-- 循环显示 -->
<div th:each="item : ${items}">
    <span th:text="${item}"></span>
</div>
```

## 核心功能说明

### 1. 邮件发送服务（MailService）

#### 发送到指定邮箱
```java
mailService.sendToSpecificEmail(
    new String[]{"user1@qq.com", "user2@qq.com"},  // 收件人列表
    "测试主题",                                      // 主题
    params                                          // 参数Map
);
```

#### 发送企业认证邮件
```java
mailService.sendCertificationEmail(
    "admin@company.com",  // 认证人邮箱
    company,              // 企业对象
    user                  // 用户对象
);
```

#### 发送普通邮件（带企业信息）
```java
mailService.send(
    "系统通知",      // 类型
    "测试消息",      // 提示
    "详细信息...",  // 详情
    user,           // 用户
    company         // 企业
);
```

### 2. 邮件队列机制

系统实现了邮件队列机制：
- 相同的邮件在30分钟内只发送一次
- 重复的邮件会累计计数
- 定时任务每30分钟检查并发送积压邮件

### 3. 线程池配置

邮件发送使用异步线程池（ThreadPoolConfig）：
- 核心线程数: 5
- 最大线程数: 10
- 队列容量: 100
- 拒绝策略: CallerRunsPolicy（调用者执行）

## 测试步骤

### 1. 配置邮箱
1. 修改 `application.yml` 中的邮箱配置
2. 设置正确的授权码
3. 配置收件人地址

### 2. 创建数据库表
```bash
# 在MySQL中执行
mysql -u root -p base < sqlCreate/company.sql
```

### 3. 启动项目
```bash
mvn spring-boot:run
```

### 4. 测试发送
```bash
# 测试简单邮件
curl http://localhost:8080/api/sendMail

# 测试带企业信息的邮件
curl "http://localhost:8080/api/sendMail?type=测试&prompt=测试消息&companyId=1"
```

### 5. 查看日志
日志会显示邮件发送过程：
```
INFO  - 开始发送邮件 - 类型: 测试, 提示: 测试消息, 企业ID: 1
INFO  - 当前用户: admin, 邮箱: admin@test.com
INFO  - 企业信息 - 名称: 测试科技有限公司, 类型: 企业
INFO  - 是否企业类型: true
INFO  - 准备发送邮件
INFO  - 邮件已发送
```

## 常见问题

### 1. 邮件发送失败

**问题**: `535 Authentication failed`

**解决**:
- 检查授权码是否正确
- 确认QQ邮箱已开启SMTP服务
- 使用授权码而不是QQ密码

### 2. SSL连接失败

**问题**: `Could not convert socket to TLS`

**解决**:
- 检查端口配置（465使用SSL，587使用TLS）
- 确认 `ssl.enable: true` 配置正确
- 检查 `socketFactory.class` 配置

### 3. 收不到邮件

**检查项**:
1. 查看日志确认邮件已发送
2. 检查收件人邮箱地址是否正确
3. 查看垃圾邮件箱
4. 确认发件人邮箱余额充足（QQ邮箱免费）

### 4. 模板变量不显示

**问题**: 邮件中变量显示为空

**解决**:
- 确认传入的参数Map包含对应的key
- 检查Thymeleaf语法是否正确
- 查看日志中的参数值

## 参考文档

- Spring Boot Mail官方文档: https://docs.spring.io/spring-boot/docs/current/reference/html/messaging.html#messaging.email
- Thymeleaf官方文档: https://www.thymeleaf.org/
- QQ邮箱帮助中心: https://service.mail.qq.com/

## 维护建议

1. **定期监控**: 关注邮件发送日志，及时发现问题
2. **授权码安全**: 不要将授权码提交到版本控制系统
3. **发送频率控制**: 避免短时间内大量发送邮件
4. **模板优化**: 定期优化邮件模板，提升用户体验
5. **错误处理**: 完善异常捕获和错误提示

## 扩展功能

可以考虑添加以下功能：
1. 附件支持
2. HTML富文本编辑
3. 邮件发送记录表
4. 邮件模板管理界面
5. 发送失败重试机制
6. 邮件统计分析
