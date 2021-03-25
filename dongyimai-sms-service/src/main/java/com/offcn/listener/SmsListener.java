package com.offcn.listener;

import com.offcn.utils.SmsUtil;
import org.apache.http.HttpResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.MapMessage;
import javax.jms.Message;
import javax.jms.MessageListener;

@Component
public class SmsListener implements MessageListener {

    @Autowired
    private SmsUtil smsUtil;

    @Override
    public void onMessage(Message message) {
        //手机号，验证码
        MapMessage mapMessage = (MapMessage) message;

        try {
            String mobile = mapMessage.getString("mobile");
            String param = mapMessage.getString("param");
            System.out.println("收到发送短信请求---手机号"+mobile+"要发送的验证码："+param);

            //发送验证码
            HttpResponse response = smsUtil.sendSms(mobile, param);
            System.out.println("data:"+response.getStatusLine());
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
}
