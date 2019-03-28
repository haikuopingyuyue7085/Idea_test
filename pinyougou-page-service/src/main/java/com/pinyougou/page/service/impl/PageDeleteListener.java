package com.pinyougou.page.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;

@Component//扫描
public class PageDeleteListener implements MessageListener {


    @Autowired
    private ItemPageServiceImpl itemPageService;

    @Override
    public void onMessage(Message message) {

        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            Long[] ids= (Long[]) objectMessage.getObject();
            System.out.println("监听到消息"+ids);

            boolean b = itemPageService.deleteItemHtml(ids);
            System.out.println("删除网页"+b);
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
