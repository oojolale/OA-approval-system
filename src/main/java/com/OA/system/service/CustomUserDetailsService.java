package com.OA.system.service;

import com.OA.system.entity.User;
import com.OA.system.repository.UserRepository;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.stream.Collectors;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("尝试加载用户: " + username);

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> {
                    System.out.println("用户未找到: " + username);
                    return new UsernameNotFoundException("用户未找到: " + username);
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

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.isEnabled(),
                true, // accountNonExpired
                true, // credentialsNonExpired
                true, // accountNonLocked
                authorities
        );
    }
}