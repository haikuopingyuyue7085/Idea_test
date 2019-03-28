package com.pinyougou.page.service.impl;

import com.pinyougou.mapper.TbGoodsDescMapper;
import com.pinyougou.mapper.TbGoodsMapper;
import com.pinyougou.mapper.TbItemCatMapper;
import com.pinyougou.mapper.TbItemMapper;
import com.pinyougou.page.service.ItemPageService;
import com.pinyougou.pojo.TbGoods;
import com.pinyougou.pojo.TbGoodsDesc;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.List;


//商品详情页
@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")//拼串用.格式"${}"
    private String pagedir;//pagedir=d:\\item 页面输出的位置,同fastDFS文件服务器

    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper GoodsMapper;    //查商品id

    @Autowired
    private TbGoodsDescMapper goodsDescMapper; //查详情

    @Autowired
    private TbItemCatMapper itemCatMapper;//面包屑

    @Autowired
    private TbItemMapper itemMapper;//读取SKU列表

    @Override
    public boolean genItemHtml(Long goodsId) {

        //创建配置对象,
        Configuration configuration=freeMarkerConfig.getConfiguration();
        //配置文件已经定义好了模板的位置,编码,页面输出的位置
        try {
            //加载模板对象
            Template template= configuration.getTemplate("item.ftl");
            //创建数据模型
            HashMap dataModel=new HashMap<>();
            //查spu商品
            TbGoods goods =GoodsMapper.selectByPrimaryKey(goodsId);
            //存入map
            dataModel.put("goods",goods);
            //查商品sku
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);

            //查询面包屑
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1",itemCat1);
            dataModel.put("itemCat2",itemCat2);
            dataModel.put("itemCat3",itemCat3);

            //读取SKU列表
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();
            criteria.andStatusEqualTo("1");//状态为有效
            criteria.andGoodsIdEqualTo(goodsId);//指定SPU ID
            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认
            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);




            //创建文件流输出到指定拼串好的页面
            FileWriter out= new FileWriter(pagedir+goodsId+".html");
            //执行模板
            template.process(dataModel,out);
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    //删除详情页
    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            for (Long goodsId:goodsIds) {
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
    }




