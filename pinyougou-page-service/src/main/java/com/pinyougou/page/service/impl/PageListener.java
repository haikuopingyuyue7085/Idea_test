package com.pinyougou.page.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

@Component//扫描
public class PageListener implements MessageListener {

    @Autowired
    private ItemPageServiceImpl itemPageService;

    @Override
    public void onMessage(Message message) {
        TextMessage textMessage = (TextMessage) message;
        try {
            String text= textMessage.getText();
            System.out.println("监听到消息"+text);

            boolean b = itemPageService.genItemHtml(Long.parseLong(text));//Long[] String转long数组
            System.out.println("生成网页"+b);
    } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
