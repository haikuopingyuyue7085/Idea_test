package com.pinyougou.search.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import com.alibaba.dubbo.config.annotation.Service;
import com.pinyougou.pojo.TbItem;
import com.pinyougou.search.service.ItemSearchService;
@Service(timeout=5000)//允许超时时间
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;
    //搜索,不用List而用Map.
    @Override
    //抽取成方法的好处,所有方法返回的结果为同一个,只需在前端拿值还好维护省代码
    public Map search(Map searchMap) {
        Map map=new HashMap();

        //关键词中间加空格处理
        String keywords = (String) searchMap.get("keywords");
        searchMap.put("keywords",keywords.replace(" ",""));
        //1,按关键词查询(高亮显示)
        map.putAll(searchList(searchMap));
        //2,关键词分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);
        //3.根据分类名称从缓存中拿到品牌和规格列表数据
        String category = (String) searchMap.get("category");
        if(!category.equals("")){
            map.putAll(searchBrandAndSpecList(category));
        }else {
            if(categoryList.size()>0){
                map.putAll(searchBrandAndSpecList((String) categoryList.get(0)));
            }
        }
        return map;

    }



    //1.查询列表,按关键词查询(高亮显示)
    private Map searchList(Map searchMap){

        Map map=new HashMap();

        HighlightQuery query=new SimpleHighlightQuery();//查询的子接口
        HighlightOptions highlightOptions=new HighlightOptions().addField("item_title");//设置高亮的域
        highlightOptions.setSimplePrefix("<em style='color:red'>");//高亮前缀
        highlightOptions.setSimplePostfix("</em>");//高亮后缀
        query.setHighlightOptions(highlightOptions);//设置高亮选项
        //1.1按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);//条件查询
        //1.2按照商品分类过滤
        if(!"".equals(searchMap.get("category"))){
            FilterQuery filterQuery=new SimpleFilterQuery();//查询的子接口
            Criteria filterCriteria = new Criteria("item_category").is(searchMap.get("category"));//过滤条件变量(搜索对象)
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);//过滤查询
        }
        //1.3按照商品品牌过滤
        if(!"".equals(searchMap.get("brand"))){
            FilterQuery filterQuery=new SimpleFilterQuery();//查询的子接口
            Criteria filterCriteria = new Criteria("item_brand").is(searchMap.get("brand"));//过滤条件变量(搜索对象)
            filterQuery.addCriteria(filterCriteria);
            query.addFilterQuery(filterQuery);//过滤查询
        }
        //1.4按照商品规格过滤,规格对象有多个Map值得遍历
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){//此处也可以用entrySet更好!
                //上面有simpleFilterQuery子接口了此处不必new,否则会出现两对象
                Criteria filterCriteria=new Criteria("item_spec_"+key).is( specMap.get(key) );//内存..网络...
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);//过滤条件
                query.addFilterQuery(filterQuery);//过滤查询

            }
        }
        //1.5按照价格区间
        if(!"".equals(searchMap.get("price"))){//有选价格
            //"0-500"转[0,500],0<X<500
            String[] price = ((String) searchMap.get("price")).split("-");
            if(!price[0].equals("0")){//如果最低价不等于0
                FilterQuery filterQuery=new SimpleFilterQuery();//查询的子接口
                Criteria filterCriteria = new Criteria("item_price").greaterThanEqual(price[0]);//大于
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);//过滤查询
            }
            if(!price[1].equals("*")){//如果最高价不等于*
                FilterQuery filterQuery=new SimpleFilterQuery();//查询的子接口
                Criteria filterCriteria = new Criteria("item_price").lessThanEqual(price[1]);//小于
                filterQuery.addCriteria(filterCriteria);
                query.addFilterQuery(filterQuery);//过滤查询
            }
        }
        //1.6按照分页查询
        Integer pageNo= (Integer) searchMap.get("pageNo");
        if(pageNo==null){
            pageNo=1;//当前页
        }
        Integer pageSize= (Integer) searchMap.get("pageSize");
        if(pageSize==null){
            pageSize=20;//每页显示数
        }
        query.setOffset((pageNo-1)*pageSize);//起始索引
        query.setRows(pageSize);//solr封装好了方法设值即可
        //1.7各个字段的升序降序,前端定义变量,灵活
        String sortValue= (String) searchMap.get("sort");
        String sortField = (String) searchMap.get("sortField");
        if(sortValue!=null&&!sortField.equals("")){
            //降序
            if(sortValue.equals("ASC")){
                    Sort sort=new Sort(Sort.Direction.ASC,"item_"+sortField);//升序,哪个字段
                    query.addSort(sort);
            }
            //升序
           if(sortValue.equals("DESC")){
                   Sort sort=new Sort(Sort.Direction.DESC,"item_"+sortField);//降序,哪个字段
                   query.addSort(sort);
           }

        }

        //-----------------------------查完设置高亮---------------------------
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        for(HighlightEntry<TbItem> h: page.getHighlighted()){//循环高亮入口集合
            TbItem item = h.getEntity();//获取原实体类
            if(h.getHighlights().size()>0 && h.getHighlights().get(0).getSnipplets().size()>0){
                item.setTitle(h.getHighlights().get(0).getSnipplets().get(0));//设置高亮的结果
            }
        }
        map.put("rows",page.getContent());
        map.put("totalPages",page.getTotalPages());//返回总页数
        map.put("total",page.getTotalElements());//返回总记录数
        return map;
    }

    //2.根据关键词分类,手机,电脑...
    private List searchCategoryList(Map searchMap){

        ArrayList<String> list = new ArrayList<>();

        Query query = new SimpleQuery("*:*");
        //按照关键字查询,where
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项,group by
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);

        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //获得分组结果对象
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //获得分组入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //获得分组入口集合
        List<GroupEntry<TbItem>> entryList = groupEntries.getContent();

        for (GroupEntry<TbItem> entry:entryList){
            list.add(entry.getGroupValue());
        }
        return list;
    }

    @Autowired
    private RedisTemplate redisTemplate;
    //3.根据分类名称从缓存中拿到品牌和规格列表数据
    private Map searchBrandAndSpecList(String category){

        HashMap map = new HashMap();
        if(category!=null){
            //以分类名称为key取到分类名称,拿到模板id
            Long templateId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

            //以模板id为key,拿到品牌值
            List brandList = (List) redisTemplate.boundHashOps("brandList").get(templateId);
            map.put("brandList",brandList);
            //以模板id为key,拿到规格值
            List specList = (List) redisTemplate.boundHashOps("specList").get(templateId);
            map.put("specList",specList);
        }
        return map;
    }

    //查询SKU列表后导入数据,到manager-web修改商品审核状态为1时执行该方法,更新索引库
    @Override
    public void importList(List list) {
        solrTemplate.saveBeans(list);
        solrTemplate.commit();
    }


    //逻辑删除商品,同步更新索引库,manage-web后台删除的同时删除索引库
    @Override
    public void deleteByGoodsIds(List goodsIdList) {
        SolrDataQuery query=new SimpleQuery("*:*");
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);//子查询省了遍历,选中的多个商品id
        query.addCriteria(criteria);
        solrTemplate.delete(query);
        solrTemplate.commit();
    }
}
