package com.pinyougou.service;

import com.pinyougou.pojo.TbSeller;
import com.pinyougou.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

//安全框架会经过该认证类(设定角色和账号密码)
public class UserDetailsServiceImpl implements UserDetailsService {
    //注入服务(数据库查用户表)
    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        System.out.println("经过了UserDetailsServiceImpl");
        System.out.println(username);
        //构建角色列表
        List<GrantedAuthority> grantedAuths=new ArrayList<>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //得到商家登录对象
        TbSeller seller = sellerService.findOne(username);
        //判断表中是否有该用户名
        if(seller!=null){
            //并且通过了审核
            if(seller.getStatus().equals("1")){
                return new User(username,seller.getPassword(),grantedAuths);
            }else{
                return null;
            }
        }else{
            return null;
        }

    }
}
