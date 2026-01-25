package com.OA.system.service;

import cn.hutool.core.util.ObjectUtil;
import com.OA.system.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import com.OA.system.entity.User;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.web.authentication.WebAuthenticationDetails;
import org.springframework.stereotype.Service;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class CurrentUserService {
    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public Map<String, Object> getUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if( ObjectUtil.isNotEmpty(authentication) && authentication.isAuthenticated()) {
//            if (authentication.getPrincipal() instanceof org.springframework.security.core.userdetails.User principal){
//                if(authentication.getDetails() instanceof WebAuthenticationDetails details){
//
//                }
//            }
            WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
            String sessionId = details.getSessionId();
            if(ObjectUtil.isNotEmpty(sessionId) && ObjectUtil.isNotEmpty(SecurityContextHolder.getContext())){
                //通过UserDetailsService 加载用户加载用户
                org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
                System.out.println("尝试加载用户: " + principal.getUsername());
                User user = userRepository.findByUsername(principal.getUsername())
                        .orElseThrow(() -> {
                            System.out.println("用户未找到: " + principal.getUsername());
                            return new UsernameNotFoundException("用户未找到: " + principal.getUsername());
                        });
                System.out.println("找到用户: " + user.getUsername());
                System.out.println("用户密码哈希: " + user.getPassword());
                System.out.println("用户是否启用: " + user.isEnabled());
                Collection<GrantedAuthority> authorities = user.getRoles().stream()
                        .map(role -> {
                            System.out.println("用户角色: " + role.getName());
                            return new SimpleGrantedAuthority(role.getName());
                        })
                        .collect(Collectors.toList());
                Map<String, Object> map = new HashMap<>();
                user.setRoles(null);
                map.put("user",user);
                map.put("authorities",authorities);
                return map;
            }
        }
        return null;
    }

    public String getCurrentUsername() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if( ObjectUtil.isNotEmpty(authentication) && authentication.isAuthenticated()) {
            WebAuthenticationDetails details = (WebAuthenticationDetails) authentication.getDetails();
            String sessionId = details.getSessionId();
            if(ObjectUtil.isNotEmpty(sessionId) && ObjectUtil.isNotEmpty(SecurityContextHolder.getContext())){
                //通过UserDetailsService 加载用户加载用户
                org.springframework.security.core.userdetails.User principal = (org.springframework.security.core.userdetails.User) authentication.getPrincipal();
                System.out.println("尝试加载用户: " + principal.getUsername());
                return principal.getUsername();
            }
        }
        return null;
    }
}
