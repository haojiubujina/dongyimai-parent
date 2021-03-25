package com.offcn.listener;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.*;

@Component
public class ItemPageDeleteListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        System.out.println("接收到消息，将要执行删除商品详情页");

        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] goodsIds = (Long[]) objectMessage.getObject();

            for (Long goodsId : goodsIds) {
                System.out.println("删除"+goodsId+".html商品详情页");
            }
            itemPageService.deleteItemHtml(goodsIds);
            System.out.println("删除商品详情页成功！");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
