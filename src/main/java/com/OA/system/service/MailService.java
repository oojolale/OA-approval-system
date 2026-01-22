package com.OA.system.service;


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

import javax.mail.MessagingException;
import javax.mail.internet.InternetAddress;
//import javax.mail.internet.MimeMessage;
//import javax.mail.internet.MimeMessage.RecipientType;
import jakarta.mail.internet.MimeMessage;
import jakarta.mail.internet.MimeMessage.RecipientType;

import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
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
    private final static Map<String, MailInfo> mailQueue = new ConcurrentHashMap<String, MailInfo>();
    //邮件发送间隔
    private final static long MAIL_SEND_INTERVAL = 1000 * 30 * 60;
    //不处理白名单
    private final static String[] EXCLUDE_ERROR = new String[]{"该流水正在处理中", "记录不存在", "bankCode is not defined"};

    /**
     * @throws Exception
     */
    private void send(String key, String tPath) {
        tpte.execute(() -> {
            try {
                log.info("准备发送邮件");
                MimeMessage mimeMessage = mailSender.createMimeMessage();
                if (StringUtils.isNotBlank(replyTo)) {
                    InternetAddress[] internetAddressCC = InternetAddress.parse(replyTo);
                    mimeMessage.setRecipients(RecipientType.CC, Arrays.toString(internetAddressCC));
                }

                MimeMessageHelper mimeMessageHelper = new MimeMessageHelper(mimeMessage);
                mimeMessageHelper.setTo(mailTo);
                mimeMessageHelper.setFrom(mailFrom);
                mimeMessageHelper.setSubject(subject + DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD));
                //设置发件时间
                mimeMessageHelper.setSentDate(new Date());
                // 利用 Thymeleaf 模板构建 html 文本
                Context ctx = new Context();
                // 给模板的参数的上下文
                MailInfo info = mailQueue.get(key);
                Map<String, Object> param = info.getParam();
                ctx.setVariable("count", info.getCount());
                ctx.setVariable("currentDate", info.getCurrentDate());
                ctx.setVariables(param);
                String emailText = templateEngine.process(tPath, ctx);
                // log.error(emailText);
                mimeMessageHelper.setText(emailText, true);

                // 发送邮件
                mailSender.send(mimeMessage);

                info.clear();
                log.info("邮件已发送");
            }
            catch (MessagingException e) {
                log.error("邮件发送失败");
                e.printStackTrace();
            } catch (jakarta.mail.MessagingException e) {
                throw new RuntimeException(e);
            }
        });
    }


    public void send(String type, String prompt, String th) {
        MailInfo mailinfo = null;
        long currentTimeMillis = System.currentTimeMillis();
        long lastSendDate = 0;

        String key = prompt;
        mailinfo = mailQueue.get(key);

        //屏蔽该类型错误
        if (StringUtils.equalsAny(key, EXCLUDE_ERROR)) {
            return;
        }

        if (mailinfo == null) {
            Map<String, Object> param = new HashMap<>();
            param.put("subject", subject);
            param.put("currentDate", DateUtils.dateTimeNow(DateUtils.YYYY_MM_DD_HH_MM_SS));
            param.put("type", type);
            param.put("prompt", th);
            param.put("info", key);

            mailinfo = new MailInfo();
            mailinfo.setParam(param);
            mailinfo.setCount(1);
            mailinfo.setLastSendDate(currentTimeMillis);
            mailinfo.setKey(key);

            mailQueue.put(key, mailinfo);

            send(key, templatePath);
        }
        else {
            lastSendDate = mailinfo.getLastSendDate();
            if (currentTimeMillis - lastSendDate > MAIL_SEND_INTERVAL) {
                mailinfo.setLastSendDate(currentTimeMillis);
                send(mailinfo.getKey(), templatePath);
            }
            else {
                mailinfo.add();
            }
        }
    }


    public void send(String type, String prompt, Throwable e) {
        send(type, prompt, ExceptionUtils.getStackTrace(e));
    }


    /**
     * 五分钟一次
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
