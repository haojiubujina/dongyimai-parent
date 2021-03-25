package com.offcn.page.service.impl;

import com.offcn.mapper.TbGoodsDescMapper;
import com.offcn.mapper.TbGoodsMapper;
import com.offcn.mapper.TbItemCatMapper;
import com.offcn.mapper.TbItemMapper;
import com.offcn.page.service.ItemPageService;
import com.offcn.pojo.TbGoods;
import com.offcn.pojo.TbGoodsDesc;
import com.offcn.pojo.TbItem;
import com.offcn.pojo.TbItemExample;
import freemarker.template.Configuration;
import freemarker.template.Template;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.servlet.view.freemarker.FreeMarkerConfig;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


@Service
public class ItemPageServiceImpl implements ItemPageService {

    @Value("${pagedir}")
    private String pagedir;//生成静态文件存放位置

    //freemarker配置对象
    @Autowired
    private FreeMarkerConfig freeMarkerConfig;

    @Autowired
    private TbGoodsMapper goodsMapper;

    @Autowired
    private TbGoodsDescMapper goodsDescMapper;

    @Autowired
    private TbItemCatMapper itemCatMapper;

    @Autowired
    private TbItemMapper itemMapper;


    @Override
    public boolean getItemHtml(Long goodsId) {

        try {
            //获得freemarkerConfiguration对象
            Configuration configuration = freeMarkerConfig.getConfiguration();
            //加载模板
            Template template = configuration.getTemplate("item.ftl");

            Map dataModel = new HashMap();
            //查询goods
            TbGoods goods = goodsMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goods",goods);

            //查询goodsdesc
            TbGoodsDesc goodsDesc = goodsDescMapper.selectByPrimaryKey(goodsId);
            dataModel.put("goodsDesc",goodsDesc);

            //根据三级分类的id依次查询商品分类
            String itemCat1 = itemCatMapper.selectByPrimaryKey(goods.getCategory1Id()).getName();
            String itemCat2 = itemCatMapper.selectByPrimaryKey(goods.getCategory2Id()).getName();
            String itemCat3 = itemCatMapper.selectByPrimaryKey(goods.getCategory3Id()).getName();
            dataModel.put("itemCat1", itemCat1);
            dataModel.put("itemCat2", itemCat2);
            dataModel.put("itemCat3", itemCat3);

            //添加sku信息
            TbItemExample example=new TbItemExample();
            TbItemExample.Criteria criteria = example.createCriteria();

            criteria.andStatusEqualTo("1");//状态为有效

            criteria.andGoodsIdEqualTo(goodsId);//指定SPU ID

            example.setOrderByClause("is_default desc");//按照状态降序，保证第一个为默认

            List<TbItem> itemList = itemMapper.selectByExample(example);
            dataModel.put("itemList", itemList);

            //创建writer对象生成指定文件名
            Writer out=new FileWriter(pagedir+goodsId+".html");

            //调用模板对象方法输出文件
            template.process(dataModel, out);

            System.out.println("生成文件成功！");

            //关闭流
            out.close();
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    @Override
    public boolean deleteItemHtml(Long[] goodsIds) {
        try {
            //遍历goodsIds
            for(Long goodsId:goodsIds){
                new File(pagedir+goodsId+".html").delete();
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}
