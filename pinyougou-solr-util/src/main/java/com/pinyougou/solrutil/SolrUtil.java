package com.pinyougou.solrutil;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

//扫描基本包,相当于bean
@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper tbItemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    //导入数据库的数据
    public void importItemData() {

        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria=example.createCriteria();
        criteria.andStatusEqualTo("1");//审核通过的
        List<TbItem> itemList=tbItemMapper.selectByExample(example);

        System.out.println("商品列表");
        for (TbItem item:itemList){
            System.out.println(item.getId()+"-"+item.getTitle()+"--"+item.getPrice());
            Map specMap = JSON.parseObject(item.getSpec());//将查到规格json字符串转化为Map并存储在动态域字段里
           item.setSpecMap(specMap);
        }
        solrTemplate.saveBeans(itemList);   //别少写s 报错找不到Id
        solrTemplate.commit();
        System.out.println("导入结束");
    }

    //要在spring环境下运行.在spring容器加载dao的东西
    public static void  main(String [] args){
        //还要加载dao的spring下的文件得用*
        ApplicationContext context =new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");
        SolrUtil solrUtil= (SolrUtil) context.getBean("solrUtil");
        //再调用上面的方法
        solrUtil.importItemData();
    }
}
