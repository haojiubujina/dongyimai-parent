package com.offcn.solrutil;


import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.mapper.TbItemMapper;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.Query;
import org.springframework.data.solr.core.query.SimpleQuery;
import org.springframework.stereotype.Component;

import java.net.SocketTimeoutException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class SolrUtil {

    @Autowired
    private TbItemMapper itemMapper;

    @Autowired
    private SolrTemplate solrTemplate;

    /**
     * 完成将数据库中的数据查询出来，导入索引库中
     */
    public void importItemData() {
        //查询出所有的通过商品审核的商品数据
        TbItemExample example = new TbItemExample();
        TbItemExample.Criteria criteria = example.createCriteria();
        //设置差查询条件为审核通过的
        criteria.andStatusEqualTo("1");
        List<TbItem> itemList = itemMapper.selectByExample(example);
        System.out.println("=========商品列表========");
        for (TbItem item : itemList) {
            System.out.println(item.getTitle());
            //读取规格数据，字符串，转换成json对象
            Map<String,String> specMap = JSON.parseObject(item.getSpec(),Map.class);
            //创建一个新map集合存储拼音
            Map<String,String> mapPinyin=new HashMap<String,String>();
            //遍历map，替换key从汉字变为拼音
            for(String key :specMap.keySet()){
                mapPinyin.put(Pinyin.toPinyin(key,"").toLowerCase(),specMap.get(key));
            }
            item.setSpecMap(mapPinyin);
        }

        //将数据导入索引库
        solrTemplate.saveBeans(itemList);
        solrTemplate.commit();

        System.out.println("====结束==导入索引库正常==");
    }

    //删除数据
    public void deleteAll(){
        Query query=new SimpleQuery("*:*");
        solrTemplate.delete(query);
        solrTemplate.commit();
    }

    public static void main(String[] args) {
        ApplicationContext context=new ClassPathXmlApplicationContext("classpath*:spring/applicationContext*.xml");

        SolrUtil solrUtil = (SolrUtil) context.getBean("solrUtil");
        //导入数据
        solrUtil.importItemData();
        //删除数据
        //solrUtil.deleteAll();
    }



}
