package com.offcn.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;
import java.io.Serializable;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

@Component
public class ItemDeleteSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        System.out.println("监听器接收到消息...将从索引库中删除商品信息。");

        try {
            //接收数据
            ObjectMessage objectMessage = (ObjectMessage) message;
            Long[] goodsIds = (Long[]) objectMessage.getObject();

            for (Long goodsId : goodsIds) {

                System.out.println("ItemDeleteListener监听接收的消息..."+Arrays.toString(goodsIds));

            }

            //执行删除索引库中的信息
            itemSearchService.deleteByGoodsIds(Arrays.asList(goodsIds));

            System.out.println("成功删除索引库中的记录!");

        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
