package com.offcn.sellergoods.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.offcn.entity.PageResult;
import com.offcn.entity.Result;
import com.offcn.group.Goods;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbItem;
import com.offcn.sellergoods.service.GoodsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;
import java.util.Arrays;
import java.util.List;

/**
 * controller
 * @author Administrator
 *
 */
@RestController
@RequestMapping("/goods")
public class GoodsController {

	@Reference
	private GoodsService goodsService;

	@Autowired
	private Destination queueSolrAddDestination;//用于发送solr导入的消息

	@Autowired
	private Destination queueSolrDeleteDestination;//用户在索引库中删除记录

	@Autowired
	private JmsTemplate jmsTemplate;

	@Autowired
	private Destination topicPageAddDestination;//生成商品详情页的目标地址

	@Autowired
	private Destination topicPageDeleteDestination;//删除商品详情页的目标地址

	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findAll")
	public List<TbGoods> findAll(){
		return goodsService.findAll();
	}
	
	
	/**
	 * 返回全部列表
	 * @return
	 */
	@RequestMapping("/findPage")
	public PageResult findPage(int page, int rows){
		return goodsService.findPage(page, rows);
	}
	
	/**
	 * 增加
	 * @param goods
	 * @return
	 */
	@RequestMapping("/add")
	public Result add(@RequestBody Goods goods){
		try {
			goodsService.add(goods);
			return new Result(true, "增加成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "增加失败");
		}
	}
	
	/**
	 * 修改
	 * @param goods
	 * @return
	 */
	@RequestMapping("/update")
	public Result update(@RequestBody Goods goods){
		try {
			goodsService.update(goods);
			return new Result(true, "修改成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "修改失败");
		}
	}	
	
	/**
	 * 获取实体
	 * @param id
	 * @return
	 */
	@RequestMapping("/findOne")
	public Goods findOne(Long id){
		return goodsService.findOne(id);		
	}
	
	/**
	 * 批量删除
	 * @param ids
	 * @return
	 */
	@RequestMapping("/delete")
	public Result delete(final Long [] ids){
		try {
			goodsService.delete(ids);

			//删除索引库中的信息
			//发送删除的消息
			jmsTemplate.send(queueSolrDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			//发送消息删除详情页面
			jmsTemplate.send(topicPageDeleteDestination, new MessageCreator() {
				public Message createMessage(Session session) throws JMSException {
					return session.createObjectMessage(ids);
				}
			});

			return new Result(true, "删除成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "删除失败");
		}
	}
	
		/**
	 * 查询+分页
	 * @param goods
	 * @param page
	 * @param rows
	 * @return
	 */
	@RequestMapping("/search")
	public PageResult search(@RequestBody TbGoods goods, int page, int rows  ){
		return goodsService.findPage(goods, page, rows);		
	}

	/**
	 * 修改状态
	 * @param ids
	 * @param status
	 * @return
	 */
	@RequestMapping("/updateStatus")
	public Result updateStatus(Long[] ids, String status){
		try {
			goodsService.updateStatus(ids, status);
			//判断是否是审核通过，还是删除  审核通过状态值为1
			if(status.equals("1")) {//审核通过，添加到索引库中
				//1.将对应的数据item查询出来
				List<TbItem> itemList = goodsService.findItemListByGoodsIdAndStatus(ids,status);

				//导入索引库,判断是否数据
				if(itemList.size()>0) {

					//发布消息添加商品到索引库
					//将itemList转换为JSON字符串
					final String jsonString = JSON.toJSONString(itemList);

					jmsTemplate.send(queueSolrAddDestination, new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							System.out.println("=====发布消息给消费者，添加商品到索引库中=====");
							return session.createTextMessage(jsonString);
						}
					});

				}else {

					System.out.println("没有需要导入的数据");
				}

				//商品详情页静态页生成
				for(final Long goodsId:ids){
					//itemPageService.getItemHtml(goodsId);
					jmsTemplate.send(topicPageAddDestination, new MessageCreator() {
						public Message createMessage(Session session) throws JMSException {
							System.out.println("发送消息给页面服务，生成"+goodsId+"商品详情页");
							return session.createTextMessage(String.valueOf(goodsId));
						}
					});
				}

			}
			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}
	}

	@RequestMapping("/updateMarketable")
	public Result updateMarketable(Long[] ids, String marketable) {

		try {
			goodsService.updateMarketable(ids, marketable);
			return new Result(true, "操作成功");
		} catch (Exception e) {
			e.printStackTrace();
			return new Result(false, "操作失败");
		}

	}

/*	//生成商品详情页的测试
	@RequestMapping("/genHtml")
	public void genHtml(Long goodsId){
		itemPageService.getItemHtml(goodsId);
	}*/
	
}
