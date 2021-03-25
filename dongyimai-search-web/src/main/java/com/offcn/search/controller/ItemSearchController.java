package com.offcn.search.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.offcn.search.service.ItemSearchService;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;

import java.util.Map;

@RestController
@RequestMapping("/itemSearch")
public class ItemSearchController {

    //引入服务层
    @Reference
    private ItemSearchService itemSearchService;

    @RequestMapping("/search")
    public Map<String,Object> search(@RequestBody Map searchMap) {

        return itemSearchService.search(searchMap);

    }

}
