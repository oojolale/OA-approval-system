# OA审批系统邮件功能启动指南

## 问题解决

### 已解决的启动错误

**错误信息**:
```
No qualifying bean of type 'org.springframework.core.task.AsyncTaskExecutor' available: 
expected at least 1 bean which qualifies as autowire candidate. 
Dependency annotations: {@org.springframework.beans.factory.annotation.Qualifier("applicationTaskExecutor")}
```

**原因**: Flowable工作流引擎需要一个名为`applicationTaskExecutor`的线程池bean。

**解决方案**: 在`ThreadPoolConfig`中添加了两个线程池：
1. `tpte` - 专门用于邮件发送的线程池
2. `applicationTaskExecutor` - Flowable等框架需要的默认线程池（标记为@Primary）

## 启动前准备

### 1. 配置邮箱

编辑 `src/main/resources/application.yml`:

```yaml
spring:
  mail:
    host: smtp.qq.com
    port: 465
    username: your-email@qq.com        # 改成你的QQ邮箱
    password: your-authorization-code   # 改成你的QQ邮箱授权码
```

### 2. 创建数据库表

在MySQL中执行：

```bash
mysql -u root -p base < sqlCreate/company.sql
```

或手动执行SQL：
```sql
USE base;
SOURCE E:/OA-approval-system/sqlCreate/company.sql;
```

### 3. 检查数据库连接

确认`application.yml`中的数据库配置正确：

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3306/base?...
    username: root
    password: '%Zheng7219212'  # 改成你的数据库密码
```

## 启动步骤

### 方法1: 使用IDE (推荐)

1. 打开项目到IntelliJ IDEA
2. 找到主类 `OaApprovalSystemApplication.java`
3. 右键点击 -> Run 'OaApprovalSystemApplication'
4. 查看控制台输出，确认启动成功

### 方法2: 使用Maven命令

```bash
# 进入项目目录
cd E:/OA-approval-system

# 清理并编译
mvn clean compile

# 启动项目
mvn spring-boot:run
```

### 方法3: 打包后运行

```bash
# 打包
mvn clean package -DskipTests

# 运行jar包
java -jar target/OA-system-1.0.0.jar
```

## 启动成功标志

看到以下日志说明启动成功：

```
Started OaApprovalSystemApplication in X.XXX seconds
Tomcat started on port(s): 8080 (http)
```

## 测试邮件功能

### 1. 访问测试接口

打开浏览器访问：
```
http://localhost:8080/api/sendMail
```

或使用curl命令：
```bash
curl http://localhost:8080/api/sendMail
```

### 2. 带参数测试

```bash
# 测试带企业信息的邮件（需要先登录）
curl "http://localhost:8080/api/sendMail?type=系统测试&prompt=测试邮件发送功能&companyId=1"
```

### 3. 查看日志

控制台应该显示：
```
INFO  - 开始发送邮件 - 类型: 系统测试, 提示: 测试邮件发送功能
INFO  - 准备发送邮件
INFO  - 邮件已发送
```

### 4. 检查收件箱

登录到配置的收件邮箱（如 3942607275@qq.com），查看是否收到邮件。

## 常见启动问题

### 问题1: 端口被占用

**错误**: `Port 8080 is already in use`

**解决**:
```bash
# Windows - 查找占用8080端口的进程
netstat -ano | findstr :8080

# 杀死进程（PID是上面查到的进程号）
taskkill /PID <PID> /F

# 或修改端口
# 编辑 application.yml
server:
  port: 8081  # 改成其他端口
```

### 问题2: 数据库连接失败

**错误**: `Could not open JDBC Connection`

**检查项**:
1. MySQL服务是否启动
2. 数据库`base`是否存在
3. 用户名密码是否正确
4. 防火墙是否阻止连接

**解决**:
```bash
# 启动MySQL服务
net start mysql

# 创建数据库
mysql -u root -p
CREATE DATABASE base DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_general_ci;
```

### 问题3: 依赖下载失败

**错误**: `Could not resolve dependencies`

**解决**:
```bash
# 清理Maven缓存
mvn clean

# 强制更新依赖
mvn clean install -U

# 或配置国内Maven镜像（阿里云）
# 编辑 ~/.m2/settings.xml 添加mirror配置
```

### 问题4: Lombok注解不生效

**错误**: `Cannot resolve symbol 'log'` 或 `Cannot find symbol getData()`

**解决**:
1. 安装Lombok插件（IntelliJ IDEA）
2. 启用注解处理：Settings -> Build -> Compiler -> Annotation Processors -> Enable annotation processing
3. 重新构建项目

## 项目结构说明

```
OA-approval-system/
├── src/main/
│   ├── java/com/OA/system/
│   │   ├── config/
│   │   │   ├── SecurityConfig.java         # 安全配置
│   │   │   └── ThreadPoolConfig.java       # 线程池配置（新增）
│   │   ├── controller/
│   │   │   └── HomeController.java         # 邮件发送接口（已更新）
│   │   ├── entity/
│   │   │   ├── Company.java                # 企业实体（新增）
│   │   │   └── User.java
│   │   ├── repository/
│   │   │   └── CompanyRepository.java      # 企业仓库（新增）
│   │   └── service/
│   │       └── MailService.java            # 邮件服务（已更新）
│   └── resources/
│       ├── application.yml                  # 主配置（已更新）
│       └── templates/
│           └── email.html                   # 邮件模板（新增）
├── sqlCreate/
│   └── company.sql                          # 企业表SQL（新增）
├── EMAIL_GUIDE.md                           # 邮件功能使用指南
└── STARTUP_GUIDE.md                         # 本文件
```

## 功能验证清单

- [ ] 项目成功启动，无错误日志
- [ ] 访问 http://localhost:8080 能看到主页
- [ ] 访问 http://localhost:8080/api/sendMail 返回成功消息
- [ ] 收件邮箱收到测试邮件
- [ ] 邮件内容格式正确，模板渲染正常
- [ ] 日志显示邮件发送成功

## 下一步操作

1. **配置真实邮箱**: 修改`application.yml`中的邮箱配置
2. **测试企业功能**: 插入企业数据，测试带企业信息的邮件
3. **测试认证邮件**: 调用`/api/sendCertificationMail`接口
4. **优化邮件模板**: 根据需求修改`email.html`
5. **集成到业务**: 在实际业务场景中调用邮件服务

## 生产环境部署建议

1. **安全配置**:
   - 使用环境变量存储敏感信息（邮箱密码、数据库密码）
   - 启用HTTPS
   - 配置防火墙规则

2. **性能优化**:
   - 调整线程池大小
   - 配置邮件发送频率限制
   - 启用数据库连接池

3. **监控告警**:
   - 配置日志收集（ELK、Splunk等）
   - 设置邮件发送失败告警
   - 监控系统资源使用

4. **备份恢复**:
   - 定期备份数据库
   - 保存邮件发送记录
   - 制定灾难恢复计划

## 技术支持

如遇到问题，请查看：
1. 项目日志文件
2. EMAIL_GUIDE.md 详细使用文档
3. Spring Boot官方文档
4. Flowable官方文档

## 更新日志

### 2026-01-25
- ✅ 实现邮件发送功能
- ✅ 创建企业信息管理
- ✅ 添加邮件模板（Thymeleaf）
- ✅ 修复线程池配置问题
- ✅ 完善文档和测试指南
