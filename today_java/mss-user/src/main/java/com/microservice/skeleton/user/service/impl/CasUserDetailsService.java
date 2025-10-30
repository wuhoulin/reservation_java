package com.microservice.skeleton.user.service.impl;

import org.jasig.cas.client.validation.Assertion;
import org.springframework.security.cas.userdetails.AbstractCasAssertionUserDetailsService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class CasUserDetailsService extends AbstractCasAssertionUserDetailsService {

    @Override
    protected UserDetails loadUserDetails(Assertion assertion) {
        // 从CAS获取用户信息（根据文档中的属性名称）
        String username = assertion.getPrincipal().getName();
        String cn = (String) assertion.getPrincipal().getAttributes().get("cn");

        // 这里可以添加角色/权限逻辑
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority("ROLE_USER"));

        // 返回用户详情
        return new User(username, "", true, true, true, true, authorities);
    }
}
