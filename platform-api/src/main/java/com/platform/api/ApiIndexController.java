package com.platform.api;

import com.platform.annotation.IgnoreAuth;
import com.platform.entity.*;
import com.platform.service.*;
import com.platform.util.ApiBaseAction;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/index")
public class ApiIndexController extends ApiBaseAction {
    @Autowired
    private ApiAdService adService;
    @Autowired
    private ApiChannelService channelService;
    @Autowired
    private ApiGoodsService goodsService;
    @Autowired
    private ApiBrandService brandService;
    @Autowired
    private ApiTopicService topicService;
    @Autowired
    private ApiCategoryService categoryService;
    @Autowired
    private ApiGoodsGroupService goodsGroupService;
    @Autowired
    private ApiCartService cartService;

    /**
     * app首页
     */
    @IgnoreAuth
    @RequestMapping("index")
    public Object index() {
        Map<String, Object> resultObj = new HashMap();
        // 商品分类
        Map params = new HashMap();
        params.put("page", 1);
        params.put("limit", 4);
        params.put("sidx", "show_index");
        params.put("order", "asc");
        params.put("parent_id", 0);
        params.put("isShow", 1);
        //查询列表数据
        List<CategoryVo> data = categoryService.queryList(params);
        resultObj.put("categoryList", data);
        
        // 轮播图
        params = new HashMap();
        params.put("ad_position_id", 1);
        List<AdVo> banner = adService.queryList(params);
        resultObj.put("banner", banner);
        
        // 限时团购广告
        params = new HashMap();
        params.put("ad_position_id", 2);
        List<AdVo> ad2 = adService.queryList(params);
        resultObj.put("groupAd", ad2);
        
        //显示团购商品
        params = new HashMap();
        params.put("offset", 0);
        params.put("limit", 10);
        List<GoodsGroupVo> goodsGroupVos = goodsGroupService.queryList(params);
        resultObj.put("groupGoodsList", goodsGroupVos);
        
        // 精品秒杀商品（暂时用热卖商品代替）
        params = new HashMap();
        params.put("is_hot", "1");
        params.put("offset", 0);
        params.put("limit", 3);
        params.put("is_delete", 0);
        params.put("is_on_sale", 1);
        List<GoodsVo> hotGoods = goodsService.queryHotGoodsList(params);
        resultObj.put("hotGoodsList", hotGoods);
        
        
        //频道
        params = new HashMap();
        params.put("sidx", "sort_order ");
        params.put("order", "asc ");
        List<ChannelVo> channel = channelService.queryList(params);
        resultObj.put("channel", channel);
        //最新商品
        /*param = new HashMap();
        param.put("is_new", 1);
        param.put("offset", 0);
        param.put("limit", 4);
        param.put("is_delete", 0);
        param.put("fields", "id, name, list_pic_url, retail_price");
        List<GoodsVo> newGoods = goodsService.queryList(param);
        resultObj.put("newGoodsList", newGoods);*/
        
        //最热商品
        /*param = new HashMap();
        param.put("is_hot", "1");
        param.put("offset", 0);
        param.put("limit", 3);
        param.put("is_delete", 0);
        List<GoodsVo> hotGoods = goodsService.queryHotGoodsList(param);
        resultObj.put("hotGoodsList", hotGoods);
        // 当前购物车中
        List<CartVo> cartList = new ArrayList();
        if (null != getUserId()) {
            //查询列表数据
            Map cartParam = new HashMap();
            cartParam.put("user_id", getUserId());
            cartList = cartService.queryList(cartParam);
        }
        if (null != cartList && cartList.size() > 0 && null != hotGoods && hotGoods.size() > 0) {
            for (GoodsVo goodsVo : hotGoods) {
                for (CartVo cartVo : cartList) {
                    if (goodsVo.getId().equals(cartVo.getGoods_id())) {
                        goodsVo.setCart_num(cartVo.getNumber());
                    }
                }
            }
        }*/
        //
//        param = new HashMap();
//        param.put("is_new", 1);
//        param.put("sidx", "new_sort_order ");
//        param.put("order", "asc ");
//        param.put("offset", 0);
//        param.put("limit", 4);
//        List<BrandVo> brandList = brandService.queryList(param);
//        resultObj.put("brandList", brandList);
        //
//        param = new HashMap();
//        param.put("offset", 0);
//        param.put("limit", 3);
//        List<TopicVo> topicList = topicService.queryList(param);
//        resultObj.put("topicList", topicList);
        // 团购
//        param = new HashMap();
//        param.put("offset", 0);
//        param.put("limit", 3);
//        List<GoodsGroupVo> goodsGroupVos = goodsGroupService.queryList(param);
//        resultObj.put("topicList", goodsGroupVos);
        // 砍价
//        param = new HashMap();
//        param.put("offset", 0);
//        param.put("limit", 3);
//        List<GoodsGroupVo> goodsGroupVos = goodsGroupService.queryList(param);
//        resultObj.put("topicList", goodsGroupVos);
        //
//        param = new HashMap();
//        param.put("parent_id", 0);
//        param.put("notName", "推荐");//<>
//        List<CategoryVo> categoryList = categoryService.queryList(param);
//        List<Map> newCategoryList = new ArrayList<>();
//
//        for (CategoryVo categoryItem : categoryList) {
//            param.remove("fields");
//            param.put("parent_id", categoryItem.getId());
//            List<CategoryVo> categoryEntityList = categoryService.queryList(param);
//            List<Integer> childCategoryIds = new ArrayList<>();
//            for (CategoryVo categoryEntity : categoryEntityList) {
//                childCategoryIds.add(categoryEntity.getId());
//            }
//            //
//            param = new HashMap();
//            param.put("categoryIds", childCategoryIds);
//            param.put("fields", "id as id, name as name, list_pic_url as list_pic_url, retail_price as retail_price");
//            List<GoodsVo> categoryGoods = goodsService.queryList(param);
//            Map newCategory = new HashMap();
//            newCategory.put("id", categoryItem.getId());
//            newCategory.put("name", categoryItem.getName());
//            newCategory.put("goodsList", categoryGoods);
//            newCategoryList.add(newCategory);
//        }
//        resultObj.put("categoryList", newCategoryList);
        return toResponsSuccess(resultObj);
    }
}