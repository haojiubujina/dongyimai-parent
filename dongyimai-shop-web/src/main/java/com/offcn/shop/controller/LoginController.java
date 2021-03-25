package com.offcn.shop.controller;

import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController()
@RequestMapping("/login")
public class LoginController {

    @RequestMapping("/showName")
    //当前登录用户的信息 key -value
    public Map showName() {

        //获得当前用户的用户名 username, springSecurity需要获得登录的用户名
        String name = SecurityContextHolder.getContext().getAuthentication().getName();

        Map map = new HashMap();
        map.put("showName", name);

        return map;
    }

}
