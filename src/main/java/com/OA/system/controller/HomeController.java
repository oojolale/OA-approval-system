package com.OA.system.controller;

import com.OA.system.entity.Company;
import com.OA.system.entity.User;
import com.OA.system.repository.CompanyRepository;
import com.OA.system.repository.UserRepository;
import com.OA.system.service.CurrentUserService;
import com.OA.system.service.MailService;
import com.OA.system.util.AjaxResult;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

@Slf4j
@Controller
public class HomeController {
    @Autowired
    MailService mailService;

    @Autowired(required = false)
    private UserRepository userRepository;

    @Autowired(required = false)
    private CompanyRepository companyRepository;

    @Autowired(required = false)
    private CurrentUserService currentUserService;

    private final RestTemplate restTemplate;

    public HomeController(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";
    }

    @GetMapping("/login")
    public String login(){
        return "login";
    }

    @GetMapping("/error")
    public String error() {
        return "error";
    }

    @GetMapping("/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public String adminPanel() {
        return "admin";
    }

    @GetMapping("/user")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public String userPanel() {
        return "user";
    }

    /**
     * 发送邮件接口 - 增强版
     * 可以获取当前邮件的详细信息，并判断是否企业类型，同时获取相关企业信息
     * 
     * @param type 邮件类型（可选，默认为"系统通知"）
     * @param prompt 提示信息（可选，默认为"测试邮件"）
     * @param companyId 企业ID（可选，如果提供则会查询企业信息）
     * @return AjaxResult
     */
    @GetMapping("api/sendMail")
    @ResponseBody
    public AjaxResult sendMail(
            @RequestParam(value = "type", required = false, defaultValue = "系统通知") String type,
            @RequestParam(value = "prompt", required = false, defaultValue = "测试邮件") String prompt,
            @RequestParam(value = "companyId", required = false) Long companyId,
            HttpServletResponse response) {
        try {
            log.info("开始发送邮件 - 类型: {}, 提示: {}, 企业ID: {}", type, prompt, companyId);
            
            // 获取当前用户信息
            User currentUser = null;
            try {
                if (currentUserService != null) {
                    String currentUsername = currentUserService.getCurrentUsername();
                    if (currentUsername != null && userRepository != null) {
                        Optional<User> userOpt = userRepository.findByUsername(currentUsername);
                        if (userOpt.isPresent()) {
                            currentUser = userOpt.get();
                            log.info("当前用户: {}, 邮箱: {}", currentUser.getUsername(), currentUser.getEmail());
                        }
                    }
                }
            } catch (Exception e) {
                log.warn("获取当前用户失败: {}", e.getMessage());
                // 如果没有当前用户，使用默认测试用户
                if (userRepository != null) {
                    Optional<User> adminUser = userRepository.findByUsername("admin");
                    currentUser = adminUser.orElse(null);
                }
            }
            
            // 获取企业信息
            Company company = null;
            if (companyId != null && companyRepository != null) {
                Optional<Company> companyOpt = companyRepository.findById(companyId);
                if (companyOpt.isPresent()) {
                    company = companyOpt.get();
                    log.info("企业信息 - 名称: {}, 类型: {}", company.getCompanyName(), company.getCompanyType());
                    
                    // 判断是否为企业类型
                    boolean isEnterprise = "企业".equals(company.getCompanyType()) || 
                                          "政府机构".equals(company.getCompanyType());
                    log.info("是否企业类型: {}", isEnterprise);
                } else {
                    log.warn("未找到企业信息，ID: {}", companyId);
                }
            }
            
            // 构建邮件详细信息
            StringBuilder detailInfo = new StringBuilder();
            detailInfo.append("邮件详细信息：\n");
            detailInfo.append("类型：").append(type).append("\n");
            detailInfo.append("提示：").append(prompt).append("\n");
            
            if (currentUser != null) {
                detailInfo.append("发送用户：").append(currentUser.getUsername()).append("\n");
                detailInfo.append("用户邮箱：").append(currentUser.getEmail()).append("\n");
            }
            
            if (company != null) {
                detailInfo.append("企业名称：").append(company.getCompanyName()).append("\n");
                detailInfo.append("企业类型：").append(company.getCompanyType()).append("\n");
                detailInfo.append("认证状态：").append(company.getCertificationStatus()).append("\n");
                detailInfo.append("联系邮箱：").append(company.getContactEmail()).append("\n");
            }
            
            // 发送邮件
            mailService.send(type, prompt, detailInfo.toString(), currentUser, company);
            
            log.info("邮件发送请求已提交");
            return AjaxResult.success("邮件发送中，请稍后查收！");
        } catch (Exception e) {
            log.error("发送邮件失败", e);
            return AjaxResult.error("邮件发送失败：" + e.getMessage());
        }
    }

    /**
     * 发送企业认证邮件
     * 
     * @param companyId 企业ID
     * @param certificationEmail 认证人邮箱
     * @return AjaxResult
     */
    @GetMapping("api/sendCertificationMail")
    @ResponseBody
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public AjaxResult sendCertificationMail(
            @RequestParam("companyId") Long companyId,
            @RequestParam("certificationEmail") String certificationEmail) {
        try {
            log.info("发送企业认证邮件 - 企业ID: {}, 认证邮箱: {}", companyId, certificationEmail);
            
            // 获取企业信息
            if (companyRepository == null) {
                return AjaxResult.error("系统未配置企业管理功能");
            }
            
            Optional<Company> companyOpt = companyRepository.findById(companyId);
            if (!companyOpt.isPresent()) {
                return AjaxResult.error("未找到企业信息");
            }
            
            Company company = companyOpt.get();
            
            // 获取当前用户
            User currentUser = null;
            if (currentUserService != null && userRepository != null) {
                String currentUsername = currentUserService.getCurrentUsername();
                if (currentUsername != null) {
                    currentUser = userRepository.findByUsername(currentUsername).orElse(null);
                }
            }
            
            // 发送认证邮件
            mailService.sendCertificationEmail(certificationEmail, company, currentUser);
            
            log.info("企业认证邮件发送请求已提交");
            return AjaxResult.success("认证邮件已发送到：" + certificationEmail);
        } catch (Exception e) {
            log.error("发送认证邮件失败", e);
            return AjaxResult.error("认证邮件发送失败：" + e.getMessage());
        }
    }
}