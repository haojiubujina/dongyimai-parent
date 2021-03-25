package com.offcn.listener;

import com.offcn.page.service.ItemPageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component
public class ItemPageListener implements MessageListener {

    @Autowired
    private ItemPageService itemPageService;

    @Override
    public void onMessage(Message message) {
        System.out.println("接收到消息，将要执行生成商品详情页");

        TextMessage textMessage = (TextMessage) message;
        try {
            String text = textMessage.getText();
            long goodsId = Long.parseLong(text);
            itemPageService.getItemHtml(goodsId);
            System.out.println("生成商品"+goodsId+"成功！");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
