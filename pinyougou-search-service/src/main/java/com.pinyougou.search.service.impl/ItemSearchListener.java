package com.pinyougou.search.service.impl;

import com.alibaba.fastjson.JSON;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;
import java.util.List;

//扫描注解
@Component
public class ItemSearchListener  implements MessageListener {

    @Autowired
    private ItemSearchService itemSearchService;

    @Override
    public void onMessage(Message message) {
        //将消息强转成text类型
        TextMessage textMessage = (TextMessage) message;
        try {
            String text=textMessage.getText();
            System.out.println("监听到消息"+text);
            //再将字符串转回原来的list
            List<TbItem> list= JSON.parseArray(text, TbItem.class);//定义数据属于哪个实体表(做泛型)

            itemSearchService.importList(list);
            System.out.println("导入solr索引库成功!");
        } catch (JMSException e) {
            e.printStackTrace();
        }
    }
}
