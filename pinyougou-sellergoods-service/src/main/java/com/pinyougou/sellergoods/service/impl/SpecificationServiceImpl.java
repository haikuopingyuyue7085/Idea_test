package com.pinyougou.sellergoods.service.impl;
import java.util.List;
import java.util.Map;

import com.github.pagehelper.Page;
import com.pinyougou.mapper.TbSpecificationOptionMapper;
import com.pinyougou.pojo.TbSpecificationOption;
import com.pinyougou.pojo.TbSpecificationOptionExample;
import com.pinyougou.pojogroup.Specification;
import org.springframework.beans.factory.annotation.Autowired;
import com.alibaba.dubbo.config.annotation.Service;
import com.github.pagehelper.PageHelper;
import com.pinyougou.mapper.TbSpecificationMapper;
import com.pinyougou.pojo.TbSpecification;
import com.pinyougou.pojo.TbSpecificationExample;
import com.pinyougou.sellergoods.service.SpecificationService;

import entity.PageResult;
import org.springframework.transaction.annotation.Transactional;

/**
 * 服务实现层
 * @author Administrator
 *
 */
@Service
@Transactional
public class SpecificationServiceImpl implements SpecificationService {
	//规格
	@Autowired
	private TbSpecificationMapper specificationMapper;
	//规格行
	@Autowired
	private TbSpecificationOptionMapper specificationOptionMapper;
	/**
	 * 查询全部
	 */
	@Override
	public List<TbSpecification> findAll() {
		return specificationMapper.selectByExample(null);
	}

	/**
	 * 按分页查询
	 */
	@Override
	public PageResult findPage(int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);		
		Page<TbSpecification> page=(Page<TbSpecification>) specificationMapper.selectByExample(null);
		return new PageResult(page.getTotal(), page.getResult());
	}

	/**
	 * 增加(组合表,获得dao层sql的返回规格表的id再遍历插入)
	 */
	@Override
	public void add(Specification specification) {
		//获取规格实体
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.insert(tbSpecification);

		//根据对应的规格id循环插入规格选项行
		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption option :specificationOptionList) {
			//设置规格行的id==规格表的id
			option.setSpecId(tbSpecification.getId());
			//插入每一个规格行
			specificationOptionMapper.insert(option);
		}
	}

	
	/**
	 * 修改
	 */
	@Override
	public void update(Specification specification){
		//获取规格实体
		TbSpecification tbSpecification = specification.getSpecification();
		specificationMapper.updateByPrimaryKey(tbSpecification);

		//根据对应的规格id循环插入规格选项行
		//修改不知是新增行还是删除行,所以先删除行
		TbSpecificationOptionExample example = new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria = example.createCriteria();
		criteria.andIdEqualTo(tbSpecification.getId());
		specificationOptionMapper.deleteByExample(example);

		List<TbSpecificationOption> specificationOptionList = specification.getSpecificationOptionList();
		for (TbSpecificationOption option :specificationOptionList) {
			//设置规格行的id==规格表的id
			option.setSpecId(tbSpecification.getId());
			//插入每一个规格行
			specificationOptionMapper.insert(option);
		}

	}
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Specification findOne(Long id){
		//根据传来的id去查询规格表并返回结果集
		Specification specification =new Specification();
		TbSpecification tbSpecification = specificationMapper.selectByPrimaryKey(id);
		specification.setSpecification(tbSpecification);
		//获取规格表id查询规格行并返回结果集
		TbSpecificationOptionExample example=new TbSpecificationOptionExample();
		TbSpecificationOptionExample.Criteria criteria= example.createCriteria();//新建规格表的标准
		criteria.andIdEqualTo(id);//匹配上规格表的id
		List<TbSpecificationOption> specificationOptionList =specificationOptionMapper.selectByExample(example);
		specification.setSpecificationOptionList(specificationOptionList);
		return specification;//返回组合实体
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//先获得规格表的id
			specificationMapper.deleteByPrimaryKey(id);
			//根据规格表的id(规格行从表的外键)
			TbSpecificationOptionExample example = new TbSpecificationOptionExample();
			TbSpecificationOptionExample.Criteria criteria =example.createCriteria();
			criteria.andIdEqualTo(id);
			specificationOptionMapper.deleteByExample(example);
		}		
	}
	
	
		@Override
	public PageResult findPage(TbSpecification specification, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbSpecificationExample example=new TbSpecificationExample();
		TbSpecificationExample.Criteria criteria = example.createCriteria();
		
		if(specification!=null){			
						if(specification.getSpecName()!=null && specification.getSpecName().length()>0){
				criteria.andSpecNameLike("%"+specification.getSpecName()+"%");
			}
	
		}
		
		Page<TbSpecification> page= (Page<TbSpecification>)specificationMapper.selectByExample(example);		
		return new PageResult(page.getTotal(), page.getResult());
	}

	/*规格下拉列表*/
	@Override
	public List<Map> selectOptionList() {
		return specificationMapper.selectOptionList();
	}

}
