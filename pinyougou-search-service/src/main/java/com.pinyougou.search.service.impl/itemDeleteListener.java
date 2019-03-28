package com.pinyougou.search.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.ObjectMessage;
import java.util.Arrays;

@Component //扫描
public class itemDeleteListener implements MessageListener {

    @Autowired
    private ItemSearchServiceImpl itemSearchService;
    @Override
    public void onMessage(Message message) {
        //强转为Object
        ObjectMessage objectMessage = (ObjectMessage) message;
        try {
            //再转回ids
            Long[] ids = (Long[]) objectMessage.getObject();
            System.out.println("监听到消息"+ids);
            //删除的是集合List
            itemSearchService.deleteByGoodsIds(Arrays.asList(ids));
            System.out.println("删除索引库的记录成功");
        } catch (JMSException e) {
            e.printStackTrace();
        }

    }
}
