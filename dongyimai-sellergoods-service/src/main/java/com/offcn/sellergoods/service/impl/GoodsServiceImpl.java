package com.offcn.sellergoods.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.pagehelper.Page;
import com.github.pagehelper.PageHelper;
import com.offcn.entity.PageResult;
import com.offcn.group.Goods;
import com.offcn.mapper.*;
import com.offcn.pojo.*;
import com.offcn.pojo.TbGoodsExample.Criteria;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Map;

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
		//设置待审核状态
		goods.getGoods().setAuditStatus("0");

		goodsMapper.insert(goods.getGoods());

		//模拟一个事务
		//int x=1/0;

		//设置id
		goods.getGoodsDesc().setGoodsId(goods.getGoods().getId());
		//插入商品扩展数据
		goodsDescMapper.insert(goods.getGoodsDesc());

		//保存sku列表数据
		saveItemList(goods);

	}

	//保存sku列表数据
	private void saveItemList(Goods goods) {

		//判断是否启用规格
		if("1".equals(goods.getGoods().getIsEnableSpec())) {

			//保存sku列表数据
			for (TbItem item : goods.getItemList()) {
				//标题
				String title = goods.getGoods().getGoodsName();
				Map<String,Object> specMap = JSON.parseObject(item.getSpec(), Map.class);
				for (String key : specMap.keySet()) {

					title+=" "+ specMap.get(key);
				}
				item.setTitle(title);

				setItemValus(goods,item);

				itemMapper.insert(item);
			}
		}else {//不启用规格 spu-->sku，但是只有一个
			//提供一个sku
			TbItem item = new TbItem();
			item.setTitle(goods.getGoods().getGoodsName());//商品SPU+规格描述串作为SKU名称
			item.setPrice( goods.getGoods().getPrice() );//价格
			item.setStatus("1");//状态
			item.setIsDefault("1");//是否默认
			item.setNum(100);//库存数量
			item.setSpec("{}");//设置规格和规格选项
			setItemValus(goods,item);
			itemMapper.insert(item);
		}
	}

	private void setItemValus(Goods goods, TbItem item) {

		//商品SPU编号
		item.setGoodsId(goods.getGoods().getId());
		//商家编号
		item.setSellerId(goods.getGoods().getSellerId());
		//商品3级分类编号
		item.setCategoryid(goods.getGoods().getCategory3Id());
		//创建日期
		item.setCreateTime(new Date());
		//修改日期
		item.setUpdateTime(new Date());
		//品牌名称
		TbBrand brand = brandMapper.selectByPrimaryKey(goods.getGoods().getBrandId());
		item.setBrand(brand.getName());
		//分类名称
		TbItemCat itemCat = itemCatMapper.selectByPrimaryKey(goods.getGoods().getCategory3Id());
		item.setCategory(itemCat.getName());
		//商家名称
		TbSeller seller = sellerMapper.selectByPrimaryKey(goods.getGoods().getSellerId());
		item.setSeller(seller.getNickName());
		//图片地址，取出spu的第一个图片
		List<Map> imageList = JSON.parseArray(goods.getGoodsDesc().getItemImages(), Map.class);
		if(imageList.size()>0) {

			item.setImage((String)imageList.get(0).get("url"));
		}
	}


	/**
	 * 修改
	 */
	@Override
	public void update(Goods goods){
		//刚从表单获取的商品信息是未审核，所以要设置审核的状态还是未审核
		goods.getGoods().setAuditStatus("0");
		//修改商品
		goodsMapper.updateByPrimaryKey(goods.getGoods());
		//插入数据库
		goodsDescMapper.updateByPrimaryKey(goods.getGoodsDesc());

		//根据  goods的id删除对应的 sku
		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(goods.getGoods().getId());

		itemMapper.deleteByExample(example);

		//添加新的sku信息
		saveItemList(goods);

	}	
	
	/**
	 * 根据ID获取实体
	 * @param id
	 * @return
	 */
	@Override
	public Goods findOne(Long id){

		//创建一个包装类
		Goods goods = new Goods();
		//查询 spu表
		TbGoods tbGoods = goodsMapper.selectByPrimaryKey(id);

		goods.setGoods(tbGoods);

		//查询goodDesc表
		TbGoodsDesc tbGoodsDesc = goodsDescMapper.selectByPrimaryKey(id);

		goods.setGoodsDesc(tbGoodsDesc);
		//查询sku列表数据
		TbItemExample example=new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();
		criteria.andGoodsIdEqualTo(id);//查询条件：商品ID
		List<TbItem> itemList = itemMapper.selectByExample(example);
		goods.setItemList(itemList);

		return goods;
	}

	/**
	 * 批量删除
	 */
	@Override
	public void delete(Long[] ids) {
		for(Long id:ids){
			//根据id查询商品数据
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改字段值为1,表示删除
			goods.setIsDelete("1");
			//通过数据库中
			goodsMapper.updateByPrimaryKey(goods);
		}
		//修改商品sdu状态为禁用
		List<TbItem> listitem = findItemListByGoodsIdAndStatus(ids,"1");
		for (TbItem tbItem : listitem) {
			tbItem.setStatus("0");
			itemMapper.updateByPrimaryKey(tbItem);
		}
	}
	
	
		@Override
	public PageResult findPage(TbGoods goods, int pageNum, int pageSize) {
		PageHelper.startPage(pageNum, pageSize);
		
		TbGoodsExample example=new TbGoodsExample();
		Criteria criteria = example.createCriteria();
		
		if(goods!=null){			
						if(goods.getSellerId()!=null && goods.getSellerId().length()>0){
				//criteria.andSellerIdLike("%"+goods.getSellerId()+"%");代码生成器，使用的是模糊查询，所以需要修改为精确查询
							criteria.andSellerIdEqualTo(goods.getSellerId());
			}			if(goods.getGoodsName()!=null && goods.getGoodsName().length()>0){
				criteria.andGoodsNameLike("%"+goods.getGoodsName()+"%");
			}			if(goods.getAuditStatus()!=null && goods.getAuditStatus().length()>0){
				criteria.andAuditStatusLike("%"+goods.getAuditStatus()+"%");
			}			if(goods.getIsMarketable()!=null && goods.getIsMarketable().length()>0){
				criteria.andIsMarketableLike("%"+goods.getIsMarketable()+"%");
			}			if(goods.getCaption()!=null && goods.getCaption().length()>0){
				criteria.andCaptionLike("%"+goods.getCaption()+"%");
			}			if(goods.getSmallPic()!=null && goods.getSmallPic().length()>0){
				criteria.andSmallPicLike("%"+goods.getSmallPic()+"%");
			}			if(goods.getIsEnableSpec()!=null && goods.getIsEnableSpec().length()>0){
				criteria.andIsEnableSpecLike("%"+goods.getIsEnableSpec()+"%");
			}			if(goods.getIsDelete()!=null && goods.getIsDelete().length()>0){
				//criteria.andIsDeleteLike("%"+goods.getIsDelete()+"%");
				criteria.andIsDeleteIsNull();//非删除状态
			}	
		}
		
		Page<TbGoods> page= (Page<TbGoods>)goodsMapper.selectByExample(example);
		return new PageResult(page.getTotal(), page.getResult());
	}

	//修改审核状态
	@Override
	public void updateStatus(Long[] ids, String status) {
		//遍历id
		for(Long id:ids){
			//根据id查询
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setAuditStatus(status);
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);
			//修改sku的状态
			TbItemExample example = new TbItemExample();
			TbItemExample.Criteria criteria = example.createCriteria();
			criteria.andGoodsIdEqualTo(id);
			List<TbItem> itemList = itemMapper.selectByExample(example);
			//遍历sku集合
			for(TbItem item:itemList){
				//修改状态
				item.setStatus("1");
				itemMapper.updateByPrimaryKey(item);
			}
		}
	}

	//修改商品上下架
	@Override
	public void updateMarketable(Long[] ids, String marketable) {

		//遍历id
		for(Long id:ids){
			//根据id查询
			TbGoods goods = goodsMapper.selectByPrimaryKey(id);
			//修改商品状态
			goods.setIsMarketable("1");
			//更新商品信息到数据库
			goodsMapper.updateByPrimaryKey(goods);
		}
	}

	@Override
	public List<TbItem> findItemListByGoodsIdAndStatus(Long[] goodsIds, String status) {

		TbItemExample example = new TbItemExample();
		TbItemExample.Criteria criteria = example.createCriteria();

		//添加查询条件
		//1.goodsIds
		criteria.andGoodsIdIn(Arrays.asList(goodsIds));
		//2.状态
		criteria.andStatusEqualTo(status);

		return itemMapper.selectByExample(example);
	}


}
