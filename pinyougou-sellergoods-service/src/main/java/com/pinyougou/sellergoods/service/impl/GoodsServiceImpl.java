package com.pinyougou.sellergoods.service.impl;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

import com.alibaba.fastjson.JSON;
import com.pinyougou.mapper.*;
import com.pinyougou.pojo.*;
import com.pinyougou.pojogroup.Goods;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.pinyougou.pojo.TbGoodsExample.Criteria;
import com.pinyougou.sellergoods.service.GoodsService;

import entity.PageResult;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class GoodsServiceImpl implements GoodsService {

	@Autowired
	private TbGoodsMapper goodsMapper;

	@Autowired
	private TbGoodsDescMapper goodsDescMapper;

	@Autowired
	private TbItemMapper itemMapper;

	@Autowired
	private TbBrandMapper brandMapper;

	@Autowired
	private TbItemCatMapper itemCatMapper;

	@Autowired
	private TbSellerMapper sellerMapper;


	/**
	 * 查询全部
	 */
	@Override
	public List<TbGoods> findAll() {
		return goodsMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbGoods> page=   (Page<TbGoods>) goodsMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加
	 */
	@Override
	public void add(Goods goods) {
		goods.getGoods().setAuditStatus("0");//设置未审核状态
		goodsMapper.insert(goods.getGoods());//插入商品基本信息
		/*int i=1/0;*/
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());;//将基本信息表的id赋值给扩展表(SELECT LAST_INSERT_ID() AS id)
		goodsDescMapper.insert(goods.getGoodsDesc());//插入商品扩展信息

		saveItemList(goods);//抽取方法:插入规格

	}
	//抽取方法:插入规格
	private void saveItemList(Goods goods){
		for(TbItem item :goods.getItemList()){
			//标题
			String title= goods.getGoods().getGoodsName();
			Map<String,Object> specMap = JSON.parseObject(item.getSpec());
			for(String key:specMap.keySet()){
				title+=" "+ specMap.get(key);
			}
			item.setTitle(title);
			item.setGoodsId(goods.getGoods().getId());//商品SPU编号
			item.setSellerId(goods.getGoods().getSellerId());//商家编号
			item.setCategoryid(goods.getGoods().getCategory3Id());//商品分类编号（3级）
			item.setCreateTime(new Date());//创建日期
			item.setUpdateTime(new Date());//修改日期
			//品牌名称
			TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
			item.setBrand(brand.getName());
			//分类名称
			TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
			item.setCategory(itemCat.getName());
			//商家名称
			TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
			item.setSeller(seller.getNickName());
			//图片地址（取spu的第一个图片）
			List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class) ;
			if(imageList.size()>0){
				item.setImage ( (String)imageList.get(0).get("url"));
			}
			itemMapper.insert(item);
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//修改基本信息
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//修改扩展信息
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());
		//修改规格信息,先清空整个对象
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());
		itemMapper.deleteByExample(example);

		saveItemList(goods);//插入商品SKU列表数据
	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){
		//查询基本信息
		Goods goods = new Goods();
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
		goods.setGoods(tbGoods);
		//查询扩展信息
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);
		goods.setGoodsDesc(tbGoodsDesc);
		//查询规格信息
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);

		List<TbItem> itemList = itemMapper.selectByExample(example);//根据模板id对象查询
		goods.setItemList(itemList);
		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//逻辑删除
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setIsDelete("1");
			goodsMapper.updateByPrimaryKey(goods);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		//只显示逻辑删除外的数据
		criteria.andIsDeleteIsNull();
		if(goods!=null){
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				criteria.andSellerIdLike("%"+goods.getSellerId()+"%");
			}
			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}
			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}
			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}
			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}
			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}
			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}
			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
			}
	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	//修改审核状态
	@Override
	public void updateStatus(Long[] ids, String status) {
		for (Long id:ids) {
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			goods.setAuditStatus(status);
			goodsMapper.updateByPrimaryKey(goods);
		}
	}
	//修改商品上下架
	@Override
	public void updateMarketable(Long[] ids, String isMarketable) {
		for (Long id : ids) {

			TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);
			//只有审核通过的才能上下架
			if ("1".equals(tbGoods.getAuditStatus())) {
				tbGoods.setIsMarketable(isMarketable);
				goodsMapper.updateByPrimaryKey(tbGoods);
			} else {
				throw new RuntimeException("只有审核通过的才能上下架");
			}
		}

	}
	//更新索引库之根据SPUID查询SKU列表
	@Override
	public List<TbItem> findItemListByGoodsIdandStatus(Long[] goodsIds, String status) {
		TbItemExample example=new TbItemExample();
		com.pinyougou.pojo.TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));//相当于sql的子查询in,数组转集合,自带遍历
		criteria.andStatusEqualTo(status);//有多种审核状态到manager-web再做判断
		return itemMapper.selectByExample(example);

	}



}
