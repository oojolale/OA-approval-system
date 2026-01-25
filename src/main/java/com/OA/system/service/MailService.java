package com.OA.system.service;


import com.OA.system.entity.Company;
import com.OA.system.entity.User;
import com.OA.system.repository.UserRepository;
import com.OA.system.util.DateUtils;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSenderImpl;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;
import org.springframework.stereotype.Service;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;
import jakarta.mail.internet.InternetAddress;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Data
@Service
public class MailService {

    @Value("${mail.to}")
    private String[] mailTo;
    @Value("${mail.replyTo}")
    private String replyTo;
    @Value("${mail.templatePath}")
    private String templatePath;
    @Value("${mail.subject}")
    private String subject;
    @Value("${spring.mail.username}")
    private String mailFrom;
    @Autowired
    private TemplateEngine templateEngine;
    @Autowired
    private JavaMailSenderImpl mailSender;
    @Autowired
    private ThreadPoolTaskExecutor tpte;
    @Autowired(required = false)
    private UserRepository userRepository;
    
    private final static Map<String, MailInfo> mailQueue = new ConcurrentHashMap<String, MailInfo>();
    //邮件发送间隔
    private final static long MAIL_SEND_INTERVAL = 1000 * 30 * 60;
    //不处理白名单
    private final static String[] EXCLUDE_ERROR = new String[]{"该流水正在处理中", "记录不存在", "bankCode is not defined"};

    /**
     * 发送邮件到指定邮箱地址（可以覆盖配置的邮箱）
     */
    public void sendToSpecificEmail(String[] toEmails, String emailSubject, Map<String, Object> params) {
        tpte.execute(() -> {
            try {
                log.info("准备发送邮件到指定邮箱");
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                helper.setTo(toEmails);
                helper.setFrom(mailFrom);
                helper.setSubject(emailSubject + " - " + DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD));
                helper.setSentDate(new Date());
                
                if (StringUtils.isNotBlank(replyTo)) {
                    helper.setReplyTo(replyTo);
                }
                
                // 构建邮件内容
                Context ctx = new Context();
                ctx.setVariables(params);
                String emailText = templateEngine.process(templatePath, ctx);
                helper.setText(emailText, true);
                
                // 发送邮件
                mailSender.send(mimeMessage);
                log.info("邮件发送成功到: {}", Arrays.toString(toEmails));
            } catch (Exception e) {
                log.error("邮件发送失败: {}", e.getMessage());
                e.printStackTrace();
            }
        });
    }

    /**
     * 激活认证邮件功能 - 发送认证邮件到企业认证人
     */
    public void sendCertificationEmail(String certificationEmail, Company company, User user) {
        try {
            log.info("发送企业认证邮件到: {}", certificationEmail);
            
            Map<String, Object> params = new HashMap<>();
            params.put("subject", "企业认证通知");
            params.put("currentDate", DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
            params.put("type", "企业认证");
            params.put("prompt", "您有一个新的企业认证请求需要处理");
            params.put("username", user != null ? user.getUsername() : "系统管理员");
            params.put("email", user != null ? user.getEmail() : "");
            
            if (company != null) {
                params.put("companyName", company.getCompanyName());
                params.put("companyType", company.getCompanyType());
                params.put("info", "企业认证详细信息：\n" +
                        "企业名称：" + company.getCompanyName() + "\n" +
                        "企业类型：" + company.getCompanyType() + "\n" +
                        "联系邮箱：" + company.getContactEmail() + "\n" +
                        "联系电话：" + company.getContactPhone());
            }
            
            sendToSpecificEmail(new String[]{certificationEmail}, "企业认证通知", params);
        } catch (Exception e) {
            log.error("发送认证邮件失败", e);
        }
    }

    /**
     * 发送邮件 - 带详细信息，可判断企业类型
     */
    private void send(String key, String tPath) {
        tpte.execute(() -> {
            try {
                log.info("准备发送邮件");
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage, true, "UTF-8");
                
                mimeMessageHelper.setTo(mailTo);
                mimeMessageHelper.setFrom(mailFrom);
                mimeMessageHelper.setSubject(subject + " - " + DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD));
                mimeMessageHelper.setSentDate(new Date());
                
                if (StringUtils.isNotBlank(replyTo)) {
                    mimeMessageHelper.setReplyTo(replyTo);
                }
                
                // 利用 Thymeleaf 模板构建 html 文本
                Context ctx = new Context();
                MailInfo info = mailQueue.get(key);
                if (info != null) {
                    Map<String, Object> param = info.getParam();
                    ctx.setVariable("count", info.getCount());
                    ctx.setVariable("currentDate", info.getCurrentDate());
                    ctx.setVariables(param);
                    
                    String emailText = templateEngine.process(tPath, ctx);
                    mimeMessageHelper.setText(emailText, true);
                    
                    // 发送邮件
                    mailSender.send(mimeMessage);
                    
                    info.clear();
                    log.info("邮件已发送");
                } else {
                    log.warn("邮件信息为空，key: {}", key);
                }
            } catch (Exception e) {
                log.error("邮件发送失败: {}", e.getMessage());
                e.printStackTrace();
            }
        });
    }


    public void send(String type, String prompt, String th) {
        send(type, prompt, th, null, null);
    }

    /**
     * 增强版发送方法 - 支持用户和企业信息
     */
    public void send(String type, String prompt, String th, User user, Company company) {
        long currentTimeMillis = System.currentTimeMillis();
        String key = prompt;

        //屏蔽该类型错误
        if (StringUtils.equalsAny(key, EXCLUDE_ERROR)) {
            return;
        }

        MailInfo mailinfo = mailQueue.get(key);

        if (mailinfo == null) {
            Map<String, Object> param = new HashMap<>();
            param.put("subject", subject);
            param.put("currentDate", DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
            param.put("type", type);
            param.put("prompt", prompt);
            param.put("info", th);
            
            // 添加用户信息
            if (user != null) {
                param.put("username", user.getUsername());
                param.put("email", user.getEmail());
            }
            
            // 添加企业信息并判断企业类型
            if (company != null) {
                param.put("companyName", company.getCompanyName());
                param.put("companyType", company.getCompanyType());
                
                // 判断是否为企业类型
                boolean isEnterprise = "企业".equals(company.getCompanyType()) || 
                                      "政府机构".equals(company.getCompanyType());
                param.put("isEnterprise", isEnterprise);
                
                log.info("企业信息 - 名称: {}, 类型: {}, 是否企业: {}", 
                        company.getCompanyName(), company.getCompanyType(), isEnterprise);
            }

            mailinfo = new MailInfo();
            mailinfo.setParam(param);
            mailinfo.setCount(1);
            mailinfo.setLastSendDate(currentTimeMillis);
            mailinfo.setKey(key);
            mailinfo.setCurrentDate(DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD_HH_MM_SS));

            mailQueue.put(key, mailinfo);

            send(key, templatePath);
        } else {
            long lastSendDate = mailinfo.getLastSendDate();
            if (currentTimeMillis - lastSendDate > MAIL_SEND_INTERVAL) {
                mailinfo.setLastSendDate(currentTimeMillis);
                send(mailinfo.getKey(), templatePath);
            } else {
                mailinfo.add();
            }
        }
    }


    public void send(String type, String prompt, Throwable e) {
        send(type, prompt, ExceptionUtils.getStackTrace(e));
    }


    /**
     * 定时发送积压的邮件 - 每30分钟一次
     */
    @Scheduled(fixedDelay = MAIL_SEND_INTERVAL)
    public void delaySend() {
        for (Map.Entry<String, MailInfo> entry : mailQueue.entrySet()) {
            MailInfo info = entry.getValue();
            if (info.getCount() > 0) {
                send(info.getKey(), templatePath);
            }
        }
    }

    @Data
    class MailInfo {
        private Map<String, Object> param;
        private String key;
        private int count;
        private long lastSendDate;
        private String currentDate;

        public void add() {
            count++;
        }

        public void clear() {
            count = 0;
        }
    }
}
