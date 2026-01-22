企业OA系统架构设计与审批流程处理思路
一、系统整体架构图
┌─────────────────────────────────────────────────────────────────────────────┐
│                              前端展示层 (Vue/React)                           │
│  ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐ ┌──────────┐           │
│  │ 流程设计器 │ │ 表单设计器 │ │  待办中心  │ │  流程监控  │ │  统计报表  │           │
│  └──────────┘ └──────────┘ └──────────┘ └──────────┘ └──────────┘           │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              API网关层 (Gateway)                             │
│           认证鉴权 │ 限流熔断 │ 路由转发 │ 日志记录 │ 统一异常处理              │
└─────────────────────────────────────────────────────────────────────────────┘
                               │
                               ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              业务服务层                                       │
│                                                                             │
│  ┌─────────────────────────────────────────────────────────────────────┐   │
│  │                     ncjr-flowable (流程引擎模块)                      │   │
│  │  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                 │   │
│  │  │FlowDefinition│ │ FlowInstance │ │  FlowTask    │                 │   │
│  │  │   Controller │ │  Controller  │ │  Controller  │                 │   │
│  │  └──────┬───────┘ └──────┬───────┘ └──────┬───────┘                 │   │
│  │         │                │                │                          │   │
│  │  ┌──────▼───────┐ ┌──────▼───────┐ ┌──────▼───────┐                 │   │
│  │  │  Definition  │ │   Instance   │ │    Task      │                 │   │
│  │  │   Service    │ │   Service    │ │   Service    │                 │   │
│  │  └──────────────┘ └──────────────┘ └──────────────┘                 │   │
│  └─────────────────────────────────────────────────────────────────────┘   │
│                                                                             │
│  ┌───────────────┐ ┌───────────────┐ ┌───────────────┐ ┌───────────────┐   │
│  │  ncjr-system  │ │ ncjr-framework│ │  ncjr-common  │ │  业务模块      │   │
│  │  (系统管理)    │ │  (基础框架)    │ │  (公共组件)   │ │ (请假/报销等) │   │
│  └───────────────┘ └───────────────┘ └───────────────┘ └───────────────┘   │
└─────────────────────────────────────────────────────────────────────────────┘
                                │
                                ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                         Flowable 流程引擎核心                                 │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐       │
│  │ Repository   │ │   Runtime    │ │    Task      │ │   History    │       │
│  │   Service    │ │   Service    │ │   Service    │ │   Service    │       │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐                        │
│  │   Identity   │ │    Form      │ │  Management  │                        │
│  │   Service    │ │   Service    │ │   Service    │                        │
│  └──────────────┘ └──────────────┘ └──────────────┘                        │
└─────────────────────────────────────────────────────────────────────────────┘
                                 │
                                 ▼
┌─────────────────────────────────────────────────────────────────────────────┐
│                              数据持久层                                       │
│  ┌──────────────┐ ┌──────────────┐ ┌──────────────┐ ┌──────────────┐       │
│  │    MySQL     │ │    Redis     │ │ ElasticSearch│ │     MinIO    │       │
│  │  (业务数据)   │ │   (缓存)     │ │  (全文检索)   │ │  (文件存储)   │       │
│  └──────────────┘ └──────────────┘ └──────────────┘ └──────────────┘       │
└─────────────────────────────────────────────────────────────────────────────┘

二、核心模块设计
1. 流程定义模块架构
   ┌─────────────────────────────────────────────────────────────────┐
   │                      流程定义管理                                  │
   ├─────────────────────────────────────────────────────────────────┤
   │                                                                 │
   │  ┌─────────────┐    ┌─────────────┐    ┌─────────────┐          │
   │  │  流程设计器   │───▶│  BPMN解析    │───▶│  流程部署     │          │
   │  │  (Bpmn.js)  │    │             │    │             │          │
   │  └─────────────┘    └─────────────┘    └──────┬──────┘          │
   │                                               │                 │
   │                     ┌─────────────────────────┼─────────────┐   │
   │                     ▼                         ▼             ▼   │
   │              ┌─────────────┐          ┌─────────────┐           │
   │              │  表单绑定     │          │  版本管理    │           │
   │              │             │          │             │           │
   │              └─────────────┘          └─────────────┘           │
   │                                                                 │
   │  核心表结构:                                                      │
   │  ├── ACT_RE_DEPLOYMENT  (部署信息)                                │
   │  ├── ACT_RE_PROCDEF     (流程定义)                                │
   │  ├── ACT_GE_BYTEARRAY   (资源文件)                                │
   │  └── sys_deploy_form    (表单关联)                                │
   └─────────────────────────────────────────────────────────────────┘

2. 审批流程处理核心架构
   ┌──────────────────────────────────────────────────────────────────────────┐
   │                           审批流程处理核心                                 │
   ├──────────────────────────────────────────────────────────────────────────┤
   │                                                                          │
   │   ┌──────────────────────────────────────────────────────────────────┐   │
   │   │                        流程实例生命周期                             │   │
   │   │                                                                  │   │
   │   │  发起申请 ──▶ 流程运行 ──▶ 任务处理 ──▶ 流程结束 ──▶ 归档存储            │   │
   │   │     │           │           │           │           │            │   │
   │   │     ▼           ▼           ▼           ▼           ▼            │   │
   │   │  创建实例      生成任务     审批/驳回      更新状态    历史记录          │   │
   │   └──────────────────────────────────────────────────────────────────┘   │
   │                                                                          │
   │   ┌─────────────────────┐  ┌─────────────────────┐                      │
   │   │    任务处理方式       │  │    流程控制操作        │                      │
   │   │                     │  │                     │                      │
   │   │  • 审批通过          │  │  • 撤回 (revokeProcess)│                   │
   │   │  • 驳回 (reject)    │  │  • 取消 (stopProcess) │                    │
   │   │  • 退回 (return)    │  │  • 挂起/激活         │                      │
   │   │  • 委派 (delegate)  │  │  • 跳转指定节点      │                      │
   │   │  • 转办 (assign)    │  │  • 加签/减签         │                      │
   │   │  • 认领 (claim)     │  │                     │                      │
   │   └─────────────────────┘  └─────────────────────┘                      │
   │                                                                          │
   └──────────────────────────────────────────────────────────────────────────┘

三、审批流程处理的设计思路
1. 审批节点处理器设计模式
   /**
* 审批处理策略接口 - 策略模式
  */
  public interface ApprovalStrategy {
  /**
    * 执行审批操作
      */
      void execute(FlowTaskVo taskVo);

  /**
    * 获取策略类型
      */
      String getType();
      }

/**
* 审批通过策略
  */
  @Component
  public class ApproveStrategy implements ApprovalStrategy {
  @Autowired
  private TaskService taskService;

  @Override
  public void execute(FlowTaskVo taskVo) {
  // 1. 设置审批意见
  taskService.addComment(taskVo.getTaskId(),
  taskVo.getInstanceId(),
  FlowComment.NORMAL.getType(),
  taskVo.getComment());

       // 2. 设置流程变量
       if (taskVo.getVariables() != null) {
           taskService.setVariables(taskVo.getTaskId(), taskVo.getVariables());
       }
       
       // 3. 完成任务
       taskService.complete(taskVo.getTaskId());
       
       // 4. 发送通知
       notifyNextApprover(taskVo);
  }

  @Override
  public String getType() {
  return "approve";
  }
  }

/**
* 驳回策略
  */
  @Component
  public class RejectStrategy implements ApprovalStrategy {
  @Override
  public void execute(FlowTaskVo taskVo) {
  // 驳回到发起人
  // 1. 获取发起节点
  // 2. 流程跳转
  // 3. 记录驳回原因
  }

  @Override
  public String getType() {
  return "reject";
  }
  }

/**
* 策略上下文 - 统一调度
  */
  @Component
  public class ApprovalContext {
  private final Map<String, ApprovalStrategy> strategyMap;

  @Autowired
  public ApprovalContext(List<ApprovalStrategy> strategies) {
  strategyMap = strategies.stream()
  .collect(Collectors.toMap(ApprovalStrategy::getType, s -> s));
  }

  public void execute(String type, FlowTaskVo taskVo) {
  ApprovalStrategy strategy = strategyMap.get(type);
  if (strategy == null) {
  throw new BusinessException("不支持的审批类型: " + type);
  }
  strategy.execute(taskVo);
  }
  }
2. 任务分配策略设计
   ┌─────────────────────────────────────────────────────────────────┐
   │                       任务分配策略                               │
   ├─────────────────────────────────────────────────────────────────┤
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐   │
   │  │                  分配方式                                │   │
   │  │                                                         │   │
   │  │  1. 指定人员 (assignee)                                 │   │
   │  │     └── 直接指定用户ID                                  │   │
   │  │                                                         │   │
   │  │  2. 候选人 (candidateUsers)                             │   │
   │  │     └── 多人可认领,认领后独占处理                        │   │
   │  │                                                         │   │
   │  │  3. 候选组 (candidateGroups)                            │   │
   │  │     └── 角色/部门内人员可认领                           │   │
   │  │                                                         │   │
   │  │  4. 表达式动态分配                                       │   │
   │  │     └── ${leaderService.getLeader(execution)}           │   │
   │  │                                                         │   │
   │  │  5. 监听器分配                                          │   │
   │  │     └── TaskListener 动态设置                           │   │
   │  └─────────────────────────────────────────────────────────┘   │
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐   │
   │  │               多实例(会签/或签)                          │   │
   │  │                                                         │   │
   │  │  会签: 所有人都需审批通过                                │   │
   │  │  或签: 任一人审批通过即可                                │   │
   │  │  比例签: 达到指定比例通过即可                            │   │
   │  │                                                         │   │
   │  │  配置示例:                                              │   │
   │  │  <multiInstanceLoopCharacteristics               │   │
   │  │      isSequential="false"                              │   │
   │  │      collection="${assigneeList}"                       │   │
   │  │      elementVariable="assignee">                        │   │
   │  │    <completionCondition>                                │   │
   │  │      ${nrOfCompletedInstances/nrOfInstances >= 0.5}    │   │
   │  │    </completionCondition>                               │   │
   │  │  </multiInstanceLoopCharacteristics>                    │   │
   │  └─────────────────────────────────────────────────────────┘   │
   │                                                                 │
   └─────────────────────────────────────────────────────────────────┘
3. 完整的审批流程时序图
   ┌──────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
   │ 申请人 │     │Controller│     │ Service  │     │ Flowable │     │ Database │
   └──┬───┘     └────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
   │               │               │               │               │
   │ 1.发起申请    │               │               │               │
   │──────────────▶│               │               │               │
   │               │ 2.startProcess│               │               │
   │               │──────────────▶│               │               │
   │               │               │ 3.创建实例    │               │
   │               │               │──────────────▶│               │
   │               │               │               │ 4.保存数据    │
   │               │               │               │──────────────▶│
   │               │               │               │               │
   │               │               │ 5.生成首个任务│               │
   │               │               │◀──────────────│               │
   │               │               │               │               │
   │               │               │ 6.发送通知    │               │
   │               │               │──────────────▶│ (WebSocket/消息)
   │               │               │               │               │
   │ 7.返回结果    │               │               │               │
   │◀──────────────│               │               │               │
   │               │               │               │               │

   ... 审批人处理 ...

┌──────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐     ┌──────────┐
│ 审批人 │     │Controller│     │ Service  │     │ Flowable │     │ Database │
└──┬───┘     └────┬─────┘     └────┬─────┘     └────┬─────┘     └────┬─────┘
│               │               │               │               │
│ 1.获取待办    │               │               │               │
│──────────────▶│               │               │               │
│               │ 2.todoList    │               │               │
│               │──────────────▶│               │               │
│               │               │ 3.查询任务    │               │
│               │               │──────────────▶│               │
│               │               │               │──────────────▶│
│               │               │               │◀──────────────│
│               │               │◀──────────────│               │
│ 4.返回待办列表   │               │               │               │
│◀──────────────│               │               │               │
│               │               │               │               │
│ 5.审批通过      │               │               │               │
│──────────────▶│               │               │               │
│               │ 6.complete    │               │               │
│               │──────────────▶│               │               │
│               │               │ 7.添加审批意见│               │
│               │               │──────────────▶│               │
│               │               │ 8.完成任务    │               │
│               │               │──────────────▶│               │
│               │               │               │ 9.更新状态    │
│               │               │               │──────────────▶│
│               │               │               │               │
│               │               │ 10.触发下一节点│              │
│               │               │◀──────────────│               │
│               │               │               │               │
│               │               │ 11.通知下一审批人             │
│               │               │──────────────▶│               │
│ 12.返回结果     │               │               │               │
│◀──────────────│               │               │               │

四、建议的代码重构与优化
1. 服务层抽象优化
   /**
* 流程业务基础服务 - 模板方法模式
  */
  public abstract class AbstractFlowService<T extends FlowBusinessEntity> {

  @Autowired
  protected RuntimeService runtimeService;
  @Autowired
  protected TaskService taskService;
  @Autowired
  protected HistoryService historyService;

  /**
    * 提交流程 - 模板方法
      */
      @Transactional(rollbackFor = Exception.class)
      public final String submitProcess(T entity) {
      // 1. 业务数据验证
      validateBusiness(entity);

      // 2. 保存业务数据
      saveBusiness(entity);

      // 3. 启动流程
      String processInstanceId = startProcess(entity);

      // 4. 关联业务与流程
      bindBusinessProcess(entity, processInstanceId);

      // 5. 后置处理
      afterSubmit(entity, processInstanceId);

      return processInstanceId;
      }

  protected abstract void validateBusiness(T entity);
  protected abstract void saveBusiness(T entity);
  protected abstract String getProcessDefinitionKey();
  protected abstract Map<String, Object> getProcessVariables(T entity);

  protected String startProcess(T entity) {
  Map<String, Object> variables = getProcessVariables(entity);
  ProcessInstance instance = runtimeService.startProcessInstanceByKey(
  getProcessDefinitionKey(),
  entity.getBusinessKey(),
  variables
  );
  return instance.getId();
  }

  protected void bindBusinessProcess(T entity, String processInstanceId) {
  entity.setProcessInstanceId(processInstanceId);
  entity.setStatus(FlowStatus.PROCESSING);
  updateBusiness(entity);
  }

  protected void afterSubmit(T entity, String processInstanceId) {
  // 子类可覆盖实现
  }

  protected abstract void updateBusiness(T entity);
  }

/**
* 请假申请服务实现
  */
  @Service
  public class LeaveApplyService extends AbstractFlowService<LeaveApply> {

  private static final String PROCESS_KEY = "leave_apply";

  @Autowired
  private LeaveApplyMapper leaveApplyMapper;

  @Override
  protected void validateBusiness(LeaveApply entity) {
  if (entity.getStartTime().after(entity.getEndTime())) {
  throw new BusinessException("开始时间不能晚于结束时间");
  }
  // 检查是否有重复请假
  // 检查假期余额等
  }

  @Override
  protected void saveBusiness(LeaveApply entity) {
  entity.setCreateTime(new Date());
  entity.setStatus(FlowStatus.DRAFT);
  leaveApplyMapper.insert(entity);
  }

  @Override
  protected String getProcessDefinitionKey() {
  return PROCESS_KEY;
  }

  @Override
  protected Map<String, Object> getProcessVariables(LeaveApply entity) {
  Map<String, Object> variables = new HashMap<>();
  variables.put("applicant", entity.getApplicantId());
  variables.put("leaveDays", entity.getLeaveDays());
  variables.put("leaveType", entity.getLeaveType());
  // 根据请假天数决定审批层级
  if (entity.getLeaveDays() <= 3) {
  variables.put("approverLevel", 1); // 直接主管
  } else if (entity.getLeaveDays() <= 7) {
  variables.put("approverLevel", 2); // 部门经理
  } else {
  variables.put("approverLevel", 3); // 总经理
  }
  return variables;
  }

  @Override
  protected void updateBusiness(LeaveApply entity) {
  leaveApplyMapper.updateById(entity);
  }
  }
2. 事件驱动架构
   /**
* 流程事件定义
  */
  public class FlowEvents {
  public static final String PROCESS_STARTED = "flow.process.started";
  public static final String TASK_CREATED = "flow.task.created";
  public static final String TASK_COMPLETED = "flow.task.completed";
  public static final String PROCESS_COMPLETED = "flow.process.completed";
  public static final String PROCESS_REJECTED = "flow.process.rejected";
  }

/**
* 流程事件对象
  */
  @Data
  public class FlowEvent {
  private String eventType;
  private String processInstanceId;
  private String taskId;
  private String businessKey;
  private String operator;
  private Map<String, Object> variables;
  private Date eventTime;
  }

/**
* 全局流程监听器
  */
  @Component
  public class GlobalFlowEventListener implements TaskListener, ExecutionListener {

  @Autowired
  private ApplicationEventPublisher eventPublisher;

  @Override
  public void notify(DelegateTask delegateTask) {
  FlowEvent event = buildEvent(delegateTask);
  eventPublisher.publishEvent(event);
  }

  @Override
  public void notify(DelegateExecution execution) {
  FlowEvent event = buildEvent(execution);
  eventPublisher.publishEvent(event);
  }
  }

/**
* 消息通知处理器
  */
  @Component
  public class NotificationHandler {

  @Autowired
  private WebSocketService webSocketService;
  @Autowired
  private EmailService emailService;
  @Autowired
  private SmsService smsService;

  @EventListener
  @Async
  public void handleTaskCreated(FlowEvent event) {
  if (!FlowEvents.TASK_CREATED.equals(event.getEventType())) {
  return;
  }

       // 获取任务处理人
       List<String> assignees = getTaskAssignees(event.getTaskId());
       
       for (String assignee : assignees) {
           // WebSocket实时推送
           webSocketService.sendToUser(assignee, new TodoNotification(event));
           
           // 邮件通知
           emailService.sendTodoNotification(assignee, event);
           
           // 根据配置决定是否发送短信
           if (needSmsNotify(assignee)) {
               smsService.sendTodoNotification(assignee, event);
           }
       }
  }
  }
3. 流程节点权限控制
   ┌─────────────────────────────────────────────────────────────────┐
   │                     节点权限控制体系                             │
   ├─────────────────────────────────────────────────────────────────┤
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐   │
   │  │                   审批人确定规则                         │   │
   │  │                                                         │   │
   │  │  优先级: 表达式 > 监听器 > 候选组 > 候选人 > 指定人     │   │
   │  │                                                         │   │
   │  │  1. 组织架构规则:                                       │   │
   │  │     - 直接主管: ${orgService.getDirectLeader(applicant)}│   │
   │  │     - 部门负责人: ${orgService.getDeptManager(deptId)}  │   │
   │  │     - 分管领导: ${orgService.getChargeLeader(deptId)}   │   │
   │  │                                                         │   │
   │  │  2. 业务规则:                                           │   │
   │  │     - 金额范围: amount < 10000 ? 'manager' : 'director'│   │
   │  │     - 申请类型: ${typeService.getApprover(applyType)}   │   │
   │  │                                                         │   │
   │  │  3. 动态配置:                                           │   │
   │  │     - 从数据库读取配置                                  │   │
   │  │     - 支持运行时修改                                    │   │
   │  └─────────────────────────────────────────────────────────┘   │
   │                                                                 │
   │  ┌─────────────────────────────────────────────────────────┐   │
   │  │                   操作权限控制                           │   │
   │  │                                                         │   │
   │  │  任务操作    │ 任务办理人 │ 流程发起人 │ 管理员         │   │
   │  │  ──────────┼────────────┼────────────┼────────────     │   │
   │  │  审批通过   │     ✓      │            │                 │   │
   │  │  驳回       │     ✓      │            │                 │   │
   │  │  退回上一步 │     ✓      │            │                 │   │
   │  │  委派       │     ✓      │            │                 │   │
   │  │  转办       │     ✓      │            │     ✓          │   │
   │  │  撤回申请   │            │     ✓      │                 │   │
   │  │  取消流程   │            │     ✓      │     ✓          │   │
   │  │  强制结束   │            │            │     ✓          │   │
   │  └─────────────────────────────────────────────────────────┘   │
   │                                                                 │
   └─────────────────────────────────────────────────────────────────┘

五、数据库表设计建议
-- ========================================
-- 业务表与流程关联表设计
-- ========================================

-- 流程表单配置表(已有 sys_deploy_form)
-- 建议增加节点级别的表单配置
CREATE TABLE sys_node_form (
id              BIGINT PRIMARY KEY AUTO_INCREMENT,
deploy_id       VARCHAR(64) NOT NULL COMMENT '流程部署ID',
node_id         VARCHAR(64) NOT NULL COMMENT '节点ID',
form_id         BIGINT NOT NULL COMMENT '表单ID',
form_type       VARCHAR(20) DEFAULT 'view' COMMENT '表单类型:edit/view/approve',
create_time     DATETIME DEFAULT CURRENT_TIMESTAMP,
UNIQUE KEY uk_deploy_node (deploy_id, node_id)
) COMMENT '节点表单配置';

-- 流程业务关联表
CREATE TABLE flow_business (
id                  BIGINT PRIMARY KEY AUTO_INCREMENT,
process_instance_id VARCHAR(64) NOT NULL COMMENT '流程实例ID',
business_key        VARCHAR(100) NOT NULL COMMENT '业务主键',
business_type       VARCHAR(50) NOT NULL COMMENT '业务类型',
business_table      
