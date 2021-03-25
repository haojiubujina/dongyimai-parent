package com.offcn.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import javax.jms.*;

@RestController
public class TestSmsController {
    //注入发送短信JMS
    @Autowired
    private JmsTemplate jmsTemplate;
    //目的地址
    @Autowired
    private Destination queueSmsDestination;

    @RequestMapping("/sendSms")
    public String sendMsg(String mobile,String msg){

        jmsTemplate.send(queueSmsDestination, new MessageCreator() {
            @Override
            public Message createMessage(Session session) throws JMSException {
                MapMessage mapMessage=session.createMapMessage();
                mapMessage.setString("mobile", mobile);
                mapMessage.setString("param", msg);
                return mapMessage;
            }
        });

        return "send ok";

    }
}