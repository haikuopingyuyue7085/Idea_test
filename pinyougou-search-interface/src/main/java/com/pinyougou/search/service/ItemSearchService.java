package com.pinyougou.search.service;

import com.pinyougou.pojo.TbItem;

import java.util.List;
import java.util.Map;

public interface ItemSearchService {
    //搜索
    public Map search(Map searchMap);
    //导入数据
    public void importList(List<TbItem> list);
    //逻辑删除商品,同步更新索引库
    public void deleteByGoodsIds(List goodsIdList);
}
