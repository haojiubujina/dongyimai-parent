package com.offcn.search.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.github.promeg.pinyinhelper.Pinyin;
import com.offcn.pojo.TbItem;
import com.offcn.search.service.ItemSearchService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.solr.core.SolrTemplate;
import org.springframework.data.solr.core.query.*;
import org.springframework.data.solr.core.query.result.*;

import java.util.*;

@Service
public class ItemSearchServiceImpl implements ItemSearchService {

    @Autowired
    private SolrTemplate solrTemplate;

    @Autowired
    private RedisTemplate redisTemplate;

    @Override
    public Map<String, Object> search(Map searchMap) {

        //创建map集合
        Map<String, Object> map = new HashMap<String, Object>();
        //查询列表
        map.putAll(searchList(searchMap));

        //2.根据关键字查询商品分类
        List categoryList = searchCategoryList(searchMap);
        map.put("categoryList",categoryList);

        //3.查询品牌和规格列表
        //读取分类名称
        String categoryName=(String) searchMap.get("category");
        if(!"".equals(categoryName)) {
            //按照分类名称重新读取对应品牌、规格
            map.putAll(searchBrandAndSpecList(categoryName));
        }else {
            if (categoryList.size() > 0) {
                map.putAll(searchBrandAndSpecList(categoryName));
            }
        }

        return map;
    }


    //根据分类名称查询品牌和分类列表
    private Map searchBrandAndSpecList(String category) {

        Map map = new HashMap();
        //获取模板id
        Long typeId = (Long) redisTemplate.boundHashOps("itemCat").get(category);

        //判断分类的Id是否为空
        if(typeId!=null) {

            //根据模板ID查询品牌列表
            List brandList =(List) redisTemplate.boundHashOps("brandList").get(typeId);
            //返回值添加品牌列表
            map.put("brandList",brandList);

            //根据模板ID查询规格列表
            List specList =(List) redisTemplate.boundHashOps("specList").get(typeId);
            map.put("specList",specList);

        }
        return map;
    }

    //根据关键字查询，对查询的结果进行高亮
    private Map searchList(Map searchMap){

        Map map=new HashMap();

        //1、创建一个支持高亮查询器对象
        SimpleHighlightQuery query = new SimpleHighlightQuery();
        //2、设定需要高亮处理字段
        HighlightOptions highlightOptions = new HighlightOptions();
        highlightOptions.addField("item_title");
        //3、设置高亮前缀
        highlightOptions.setSimplePrefix("<em style='color:red'>");
        //4、设置高亮后缀
        highlightOptions.setSimplePostfix("</em>");
        //5、关联高亮选项到高亮查询器对象
        query.setHighlightOptions(highlightOptions);

        //关键字空格处理
        if(searchMap.get("keywords")!=null){
            int index= searchMap.get("keywords").toString().indexOf(" ");
            if(index>=0){//空格存在
                String keywords=  searchMap.get("keywords").toString().replace(" ","");
                searchMap.put("keywords",keywords);
            }

        }

        //6、设定查询条件 根据关键字查询
        //1.1创建查询条件对象
        Criteria criteria = new Criteria("item_keywords").is(searchMap.get("keywords"));
        //关联查询条件到查询器对象
        query.addCriteria(criteria);

        //1.2按分类筛选
        if(!"".equals(searchMap.get("category"))){
            Criteria filterCriteria=new Criteria("item_category").is(searchMap.get("category"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.3按品牌筛选
        if(!"".equals(searchMap.get("brand"))){
            Criteria filterCriteria=new Criteria("item_brand").is(searchMap.get("brand"));
            FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
            query.addFilterQuery(filterQuery);
        }

        //1.4过滤规格
        if(searchMap.get("spec")!=null){
            Map<String,String> specMap= (Map) searchMap.get("spec");
            for(String key:specMap.keySet() ){
                Criteria filterCriteria=new Criteria("item_spec_"+ Pinyin.toPinyin(key,"").toLowerCase()).is(specMap.get(key));
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);
                query.addFilterQuery(filterQuery);
            }
        }/* System.out.println("price:-------"+searchMap.get("price"));*/

        //1.5按照价格过滤
        if(!"".equals(searchMap.get("price"))) {

            String[] prices = ((String) searchMap.get("price")).split("-");
         /*   System.out.println("price:-------"+ Arrays.toString(prices));
            System.out.println("price:-------"+ Arrays.toString(prices));*/

            //取出的第一个元素是否包含0,就是0-500
            if(!"0".equals(prices[0])) {//区间开始不等于0

                //创建查询封装条件
                Criteria filterCriteria=new Criteria("item_price").greaterThanEqual(prices[0]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);

                //将查询条件添加到查询对象
                query.addFilterQuery(filterQuery);

            }

            if(!"*".equals(prices[1])) {//区间结束不等于1
                //创建查询封装条件                                item_price
                Criteria filterCriteria=new Criteria("item_price").lessThan(prices[1]);
                FilterQuery filterQuery=new SimpleFilterQuery(filterCriteria);

                //将查询条件添加到查询对象
                query.addFilterQuery(filterQuery);

            }
        }

        //1.6 分页查询
        Integer pageNo= (Integer) searchMap.get("pageNo");//提取页码
        if(pageNo==null){
            pageNo=1;//默认第一页
        }
        Integer pageSize=(Integer) searchMap.get("pageSize");//每页记录数
        if(pageSize==null){
            pageSize=20;//默认20
        }
        query.setOffset((pageNo-1)*pageSize);//从第几条记录查询
        query.setRows(pageSize);

        //1.7排序
        //取出排序的字段和方式
        String sortValue= (String) searchMap.get("sort");//ASC  DESC

        String sortField= (String) searchMap.get("sortField");//排序字段

        if(sortValue!=null && !sortValue.equals("")){

            //判断是升序还是降序
            if(sortValue.equals("asc")){//升序

                Sort sort=new Sort(Sort.Direction.ASC, "item_"+sortField);

                query.addSort(sort);
            }
            if(sortValue.equals("desc")){//降序

                Sort sort=new Sort(Sort.Direction.DESC, "item_"+sortField);

                query.addSort(sort);
            }
        }

        //7、发出带高亮数据查询请求
        HighlightPage<TbItem> page = solrTemplate.queryForHighlightPage(query, TbItem.class);
        //8、获取高亮集合入口
        List<HighlightEntry<TbItem>> highlightEntryList = page.getHighlighted();
        //9、遍历高亮集合
        for(HighlightEntry<TbItem> highlightEntry:highlightEntryList){
            //获取基本数据对象
            TbItem tbItem = highlightEntry.getEntity();
            if(highlightEntry.getHighlights().size()>0&&highlightEntry.getHighlights().get(0).getSnipplets().size()>0) {
                List<HighlightEntry.Highlight> highlightList = highlightEntry.getHighlights();
                //高亮结果集合
                List<String> snipplets = highlightList.get(0).getSnipplets();
                //获取第一个高亮字段对应的高亮结果，设置到商品标题
                tbItem.setTitle(snipplets.get(0));
            }
        }

        //把带高亮数据集合存放map
        map.put("rows",page.getContent());
        map.put("totalPages", page.getTotalPages());//返回总页数
        map.put("total", page.getTotalElements());//返回总记录数
        return map;
    }

    /**
     * 查询分类列表
     * @param searchMap
     * @return
     */
    private  List searchCategoryList(Map searchMap){

        //创建list集合存入查询的数据
        List<String> list=new ArrayList();
        //创建一个查询对象
        Query query=new SimpleQuery();
        //按照关键字查询
        Criteria criteria=new Criteria("item_keywords").is(searchMap.get("keywords"));
        query.addCriteria(criteria);

        //设置分组选项
        GroupOptions groupOptions=new GroupOptions().addGroupByField("item_category");
        query.setGroupOptions(groupOptions);
        //得到分组页
        GroupPage<TbItem> page = solrTemplate.queryForGroupPage(query, TbItem.class);
        //根据列得到分组结果集
        GroupResult<TbItem> groupResult = page.getGroupResult("item_category");
        //得到分组结果入口页
        Page<GroupEntry<TbItem>> groupEntries = groupResult.getGroupEntries();
        //得到分组入口集合
        List<GroupEntry<TbItem>> content = groupEntries.getContent();
        for(GroupEntry<TbItem> entry:content){
            list.add(entry.getGroupValue());//将分组结果的名称封装到返回值中
        }
        return list;
    }


    @Override
    public void importList(List<TbItem> itemList) {

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

    @Override
    public void deleteByGoodsIds(List goodsIdList) {

        System.out.println("删除商品ID"+goodsIdList);
        //构造条件
        Query query=new SimpleQuery();
        Criteria criteria=new Criteria("item_goodsid").in(goodsIdList);
        query.addCriteria(criteria);

        //开始删除
        solrTemplate.delete(query);

        solrTemplate.commit();

    }

}
