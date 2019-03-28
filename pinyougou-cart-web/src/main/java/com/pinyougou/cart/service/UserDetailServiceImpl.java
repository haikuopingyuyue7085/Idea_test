package com.pinyougou.cart.service;

import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

//认证类:账号密码检验后返回结果username
public class UserDetailServiceImpl implements UserDetailsService {
    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
            //构建角色集合
            System.out.println("经过认证类"+username);
            List<GrantedAuthority> authorities=new ArrayList();
            authorities.add(new SimpleGrantedAuthority("ROLE_USER"));
            return new User(username, ""  , authorities);
        }

    }
