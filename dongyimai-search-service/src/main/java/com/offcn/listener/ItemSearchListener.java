package com.offcn.listener;

import com.alibaba.fastjson.JSON;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;
import java.util.Map;

@Component
public class ItemSearchListener implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {

        System.out.println("监听器接收到消息...将商品添加到索引库。");

        try {
            //接收数据
            TextMessage textMessage = (TextMessage) message;
            String text = textMessage.getText();
            //将json字符串转换为数组对象
            List<TbItem> itemList = JSON.parseArray(text, TbItem.class);

            for(TbItem item:itemList){

                System.out.println(item.getId()+" "+item.getTitle());

                Map specMap= JSON.parseObject(item.getSpec());//将spec字段中的json字符串转换为map
                item.setSpecMap(specMap);//给带注解的字段赋值
            }

            //执行导入索引库
            itemSearchService.importList(itemList);

            System.out.println("数据导入索引库成功!");

        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
