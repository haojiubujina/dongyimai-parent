package com.offcn.user.service;

import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.pojo.TbSeller;
import com.offcn.sellergoods.service.SellerService;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;

import java.util.ArrayList;
import java.util.List;

/**
 * 认证类
 */
public class UserDetailsServiceImpl implements UserDetailsService {

    private SellerService sellerService;

    public void setSellerService(SellerService sellerService) {
        this.sellerService = sellerService;
    }

    public UserDetails loadUserByUsername(String sellerId) throws UsernameNotFoundException {

        List<GrantedAuthority> grantedAuths = new ArrayList<GrantedAuthority>();
        grantedAuths.add(new SimpleGrantedAuthority("ROLE_SELLER"));
        //利用username去数据库查询用户信息
        TbSeller tbSeller = sellerService.findOne(sellerId);
        if(tbSeller!=null) {//用户存在

            //判断用户是否通过审核的用户
            if(tbSeller.getStatus().equals("1")) {

                //判断密码是否正确
                return new User(sellerId,tbSeller.getPassword(),grantedAuths);
            }
        }
        return null;
    }
}
