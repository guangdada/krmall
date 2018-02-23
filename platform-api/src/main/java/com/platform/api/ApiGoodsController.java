package com.platform.api;

import com.qiniu.util.StringUtils;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.*;
import com.platform.service.*;
import com.platform.util.ApiBaseAction;
import com.platform.util.ApiPageUtils;
import com.platform.utils.Base64;
import com.platform.utils.CharUtil;
import com.platform.utils.DateUtils;
import com.platform.utils.Query;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.*;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/goods")
public class ApiGoodsController extends ApiBaseAction {
	@Autowired
	private ApiGoodsService goodsService;
	@Autowired
	private ApiGoodsSpecificationService goodsSpecificationService;
	@Autowired
	private ApiProductService productService;
	@Autowired
	private ApiGoodsGalleryService goodsGalleryService;
	@Autowired
	private ApiGoodsIssueService goodsIssueService;
	@Autowired
	private ApiAttributeService attributeService;
	@Autowired
	private ApiBrandService brandService;
	@Autowired
	private ApiCommentService commentService;
	@Autowired
	private ApiUserService userService;
	@Autowired
	private ApiCommentPictureService commentPictureService;
	@Autowired
	private ApiCollectService collectService;
	@Autowired
	private ApiFootprintService footprintService;
	@Autowired
	private ApiCategoryService categoryService;
	@Autowired
	private ApiSearchHistoryService searchHistoryService;
	@Autowired
	private ApiRelatedGoodsService relatedGoodsService;
	@Autowired
	private ApiCouponService apiCouponService;
	@Autowired
	private ApiUserCouponService apiUserCouponService;
	@Autowired
	private ApiCartService cartService;

	/**
	 */
	@IgnoreAuth
	@RequestMapping("index")
	public Object index(@LoginUser UserVo loginUser) {
		//
		Map param = new HashMap();
		List<GoodsVo> goodsList = goodsService.queryList(param);
		//
		return toResponsSuccess(goodsList);
	}

	/**
	 * 获取sku信息，用于购物车编辑时选择规格
	 */
	@IgnoreAuth
	@RequestMapping("sku")
	public Object sku(@LoginUser UserVo loginUser, Integer id) {
		Map<String, Object> resultObj = new HashMap();
		//
		Map param = new HashMap();
		param.put("goods_id", id);
		List<GoodsSpecificationVo> goodsSpecificationEntityList = goodsSpecificationService.queryList(param);
		//
		List<ProductVo> productEntityList = productService.queryList(param);
		//
		resultObj.put("specificationList", goodsSpecificationEntityList);
		resultObj.put("productList", productEntityList);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 商品详情页数据
	 */
	@IgnoreAuth
	@RequestMapping("detail")
	public Object detail(Integer id, Long referrer) {
		Map<String, Object> resultObj = new HashMap<String, Object>();
		GoodsVo info = goodsService.queryObject(id);
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("goods_id", id);
		//查询商品规格
		Map<String, Object> specificationParam = new HashMap<String, Object>();
		specificationParam.put("fields", "gs.*, s.name");
		specificationParam.put("goods_id", id);
		specificationParam.put("specification", true);
		specificationParam.put("sidx", "s.sort_order");
		specificationParam.put("order", "asc");
		List<GoodsSpecificationVo> goodsSpecificationEntityList = goodsSpecificationService
				.queryList(specificationParam);

		List<Map> specificationList = new ArrayList();
		// 按规格名称分组
		for (int i = 0; i < goodsSpecificationEntityList.size(); i++) {
			GoodsSpecificationVo specItem = goodsSpecificationEntityList.get(i);
			//
			List<GoodsSpecificationVo> tempList = null;
			for (int j = 0; j < specificationList.size(); j++) {
				if (specificationList.get(j).get("specification_id").equals(specItem.getSpecification_id())) {
					tempList = (List<GoodsSpecificationVo>) specificationList.get(j).get("valueList");
					break;
				}
			}
			//
			if (null == tempList) {
				Map<String, Object> temp = new HashMap<String, Object>();
				temp.put("specification_id", specItem.getSpecification_id());
				temp.put("name", specItem.getName());
				tempList = new ArrayList();
				tempList.add(specItem);
				temp.put("valueList", tempList);
				specificationList.add(temp);
			} else {
				for (int j = 0; j < specificationList.size(); j++) {
					if (specificationList.get(j).get("specification_id").equals(specItem.getSpecification_id())) {
						tempList = (List<GoodsSpecificationVo>) specificationList.get(j).get("valueList");
						tempList.add(specItem);
						break;
					}
				}
			}
		}
		// 查询产品信息
		List<ProductVo> productEntityList = productService.queryList(param);
		// 查询录播图
		List<GoodsGalleryVo> gallery = goodsGalleryService.queryList(param);
		// 商品属性
		Map<String, Object> ngaParam = new HashMap<String, Object>();
		ngaParam.put("fields", "nga.value, na.name");
		ngaParam.put("sidx", "nga.id");
		ngaParam.put("order", "asc");
		ngaParam.put("goods_id", id);
		List<AttributeVo> attribute = attributeService.queryList(ngaParam);

		//
		resultObj.put("info", info);
		resultObj.put("gallery", gallery);
		resultObj.put("attribute", attribute);
		resultObj.put("specificationList", specificationList);
		resultObj.put("productList", productEntityList);
		// resultObj.put("userHasCollect", userHasCollect);
		// resultObj.put("issue", issue);
		// resultObj.put("comment", comment);
		// resultObj.put("brand", brand);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 获取分类下的商品
	 */
	@IgnoreAuth
	@RequestMapping("category")
	public Object category(@LoginUser UserVo loginUser, Integer id) {
		Map<String, Object> resultObj = new HashMap();
		//
		CategoryVo currentCategory = categoryService.queryObject(id);
		//
		CategoryVo parentCategory = categoryService.queryObject(currentCategory.getParent_id());
		Map params = new HashMap();
		params.put("parent_id", currentCategory.getParent_id());
		params.put("isShow", 1);
		List<CategoryVo> brotherCategory = categoryService.queryList(params);
		//
		resultObj.put("currentCategory", currentCategory);
		resultObj.put("parentCategory", parentCategory);
		resultObj.put("brotherCategory", brotherCategory);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 获取商品列表
	 */
	@IgnoreAuth
	@RequestMapping("list")
	public Object list(@LoginUser UserVo loginUser, Integer categoryId, Integer brandId, String keyword, Integer isNew,
			Integer isHot, @RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "size", defaultValue = "10") Integer size, String sort, String order) {
		Map params = new HashMap();
		params.put("brand_id", brandId);
		params.put("keyword", keyword);
		params.put("is_new", isNew);
		params.put("is_hot", isHot);
		params.put("page", page);
		params.put("limit", size);
		params.put("order", sort);
		params.put("sidx", order);
		//
		if (null != sort && sort.equals("price")) {
			params.put("sidx", "retail_price");
			params.put("order", order);
		} else {
			params.put("sidx", "id");
			params.put("order", "desc");
		}
		// 添加到搜索历史
		if (!StringUtils.isNullOrEmpty(keyword)) {
			SearchHistoryVo searchHistoryVo = new SearchHistoryVo();
			searchHistoryVo.setAdd_time(System.currentTimeMillis() / 1000);
			searchHistoryVo.setKeyword(keyword);
			searchHistoryVo.setUser_id(null != loginUser ? loginUser.getUserId().toString() : "");
			searchHistoryVo.setFrom("");
			searchHistoryService.save(searchHistoryVo);

		}
		// 筛选的分类
		List<CategoryVo> filterCategory = new ArrayList();
		CategoryVo rootCategory = new CategoryVo();
		rootCategory.setId(0);
		rootCategory.setName("全部");
		rootCategory.setChecked(false);
		filterCategory.add(rootCategory);
		//
		params.put("fields", "category_id");
		List<GoodsVo> categoryEntityList = goodsService.queryList(params);
		params.remove("fields");
		if (null != categoryEntityList && categoryEntityList.size() > 0) {
			List<Integer> categoryIds = new ArrayList();
			for (GoodsVo goodsVo : categoryEntityList) {
				categoryIds.add(goodsVo.getCategory_id());
			}
			// 查找二级分类的parent_id
			Map categoryParam = new HashMap();
			categoryParam.put("ids", categoryIds);
			categoryParam.put("fields", "parent_id");
			List<CategoryVo> parentCategoryList = categoryService.queryList(categoryParam);
			//
			List<Integer> parentIds = new ArrayList();
			for (CategoryVo categoryEntity : parentCategoryList) {
				parentIds.add(categoryEntity.getParent_id());
			}
			// 一级分类
			categoryParam = new HashMap();
			categoryParam.put("fields", "id,name");
			categoryParam.put("order", "asc");
			categoryParam.put("sidx", "sort_order");
			categoryParam.put("ids", parentIds);
			List<CategoryVo> parentCategory = categoryService.queryList(categoryParam);
			if (null != parentCategory) {
				filterCategory.addAll(parentCategory);
			}
		}
		// 加入分类条件
		if (null != categoryId && categoryId > 0) {
			List<Integer> categoryIds = new ArrayList();
			Map categoryParam = new HashMap();
			categoryParam.put("parent_id", categoryId);
			categoryParam.put("fields", "id");
			List<CategoryVo> childIds = categoryService.queryList(categoryParam);
			for (CategoryVo categoryEntity : childIds) {
				categoryIds.add(categoryEntity.getId());
			}
			categoryIds.add(categoryId);
			params.put("categoryIds", categoryIds);
		}
		// 查询列表数据
		params.put("fields", "id, name, list_pic_url, market_price, retail_price, goods_brief");
		Query query = new Query(params);
		List<GoodsVo> goodsList = goodsService.queryList(query);
		int total = goodsService.queryTotal(query);
		ApiPageUtils goodsData = new ApiPageUtils(goodsList, total, query.getLimit(), query.getPage());
		// 搜索到的商品
		for (CategoryVo categoryEntity : filterCategory) {
			if (null != categoryId && categoryEntity.getId() == 0 || categoryEntity.getId() == categoryId) {
				categoryEntity.setChecked(true);
			} else {
				categoryEntity.setChecked(false);
			}
		}
		goodsData.setFilterCategory(filterCategory);
		goodsData.setGoodsList(goodsData.getData());
		return toResponsSuccess(goodsData);
	}

	/**
	 * 商品列表筛选的分类列表
	 */
	@IgnoreAuth
	@RequestMapping("filter")
	public Object filter(@LoginUser UserVo loginUser, Integer categoryId, String keyword, Integer isNew,
			Integer isHot) {
		Map params = new HashMap();
		params.put("categoryId", categoryId);
		params.put("keyword", keyword);
		params.put("isNew", isNew);
		params.put("isHot", isHot);
		if (null != categoryId) {
			Map categoryParams = new HashMap();
			categoryParams.put("categoryId", categoryId);
			List<CategoryVo> categoryEntityList = categoryService.queryList(categoryParams);
			List<Integer> category_ids = new ArrayList();
			for (CategoryVo categoryEntity : categoryEntityList) {
				category_ids.add(categoryEntity.getId());
			}
			params.put("category_id", category_ids);
		}
		// 筛选的分类
		List<CategoryVo> filterCategory = new ArrayList();
		CategoryVo rootCategory = new CategoryVo();
		rootCategory.setId(0);
		rootCategory.setName("全部");
		// 二级分类id
		List<GoodsVo> goodsEntityList = goodsService.queryList(params);
		if (null != goodsEntityList && goodsEntityList.size() > 0) {
			List<Integer> categoryIds = new ArrayList();
			for (GoodsVo goodsEntity : goodsEntityList) {
				categoryIds.add(goodsEntity.getCategory_id());
			}
			// 查找二级分类的parent_id
			Map categoryParam = new HashMap();
			categoryParam.put("categoryIds", categoryIds);
			List<CategoryVo> parentCategoryList = categoryService.queryList(categoryParam);
			//
			List<Integer> parentIds = new ArrayList();
			for (CategoryVo categoryEntity : parentCategoryList) {
				parentIds.add(categoryEntity.getId());
			}
			// 一级分类
			categoryParam.put("categoryIds", parentIds);
			List<CategoryVo> parentCategory = categoryService.queryList(categoryParam);
			if (null != parentCategory) {
				filterCategory.addAll(parentCategory);
			}
		}
		return toResponsSuccess(filterCategory);
	}

	/**
	 * 新品首发
	 */
	@IgnoreAuth
	@RequestMapping("new")
	public Object newAction(@LoginUser UserVo loginUser) {
		Map<String, Object> resultObj = new HashMap();
		Map bannerInfo = new HashMap();
		bannerInfo.put("url", "");
		bannerInfo.put("name", "坚持初心，为你寻觅世间好物");
		bannerInfo.put("img_url", "http://yanxuan.nosdn.127.net/8976116db321744084774643a933c5ce.png");
		resultObj.put("bannerInfo", bannerInfo);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 人气推荐
	 */
	@IgnoreAuth
	@RequestMapping("hot")
	public Object hot(@LoginUser UserVo loginUser) {
		Map<String, Object> resultObj = new HashMap();
		Map bannerInfo = new HashMap();
		bannerInfo.put("url", "");
		bannerInfo.put("name", "大家都在买的严选好物");
		bannerInfo.put("img_url", "http://yanxuan.nosdn.127.net/8976116db321744084774643a933c5ce.png");
		resultObj.put("bannerInfo", bannerInfo);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 商品详情页的大家都在看的商品
	 */
	@IgnoreAuth
	@RequestMapping("related")
	public Object related(@LoginUser UserVo loginUser, Integer id) {
		Map<String, Object> resultObj = new HashMap();
		Map param = new HashMap();
		param.put("goods_id", id);
		param.put("fields", "related_goods_id");
		List<RelatedGoodsVo> relatedGoodsEntityList = relatedGoodsService.queryList(param);

		List<Integer> relatedGoodsIds = new ArrayList();
		for (RelatedGoodsVo relatedGoodsEntity : relatedGoodsEntityList) {
			relatedGoodsIds.add(relatedGoodsEntity.getRelated_goods_id());
		}
		List<GoodsVo> relatedGoods = new ArrayList<>();
		if (null == relatedGoodsIds || relatedGoods.size() < 1) {
			// 查找同分类下的商品
			GoodsVo goodsCategory = goodsService.queryObject(id);
			Map paramRelated = new HashMap();
			paramRelated.put("fields", "id, name, list_pic_url, retail_price");
			paramRelated.put("category_id", goodsCategory.getCategory_id());
			relatedGoods = goodsService.queryList(paramRelated);
		} else {
			Map paramRelated = new HashMap();
			paramRelated.put("goods_ids", relatedGoodsIds);
			paramRelated.put("fields", "id, name, list_pic_url, retail_price");
			relatedGoods = goodsService.queryList(paramRelated);
		}
		resultObj.put("goodsList", relatedGoods);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 在售的商品总数
	 */
	@IgnoreAuth
	@RequestMapping("count")
	public Object count(@LoginUser UserVo loginUser) {
		Map<String, Object> resultObj = new HashMap();
		Map param = new HashMap();
		param.put("is_delete", 0);
		param.put("is_on_sale", 1);
		Integer goodsCount = goodsService.queryTotal(param);
		resultObj.put("goodsCount", goodsCount);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 获取商品列表
	 */
	@IgnoreAuth
	@RequestMapping("productlist")
	public Object productlist(@LoginUser UserVo loginUser, Integer categoryId, Integer isNew, Integer discount,
			@RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "size", defaultValue = "10") Integer size, String sort, String order) {
		Map params = new HashMap();
		params.put("is_new", isNew);
		params.put("page", page);
		params.put("limit", size);
		params.put("order", sort);
		params.put("sidx", order);
		//
		if (null != sort && sort.equals("price")) {
			params.put("sidx", "retail_price");
			params.put("order", order);
		} else if (null != sort && sort.equals("sell")) {
			params.put("sidx", "orderNum");
			params.put("order", order);
		} else {
			params.put("sidx", "id");
			params.put("order", "desc");
		}
		// 0不限 1特价 2团购
		if (null != discount && discount == 1) {
			params.put("is_hot", 1);
		} else if (null != discount && discount == 2) {
			params.put("is_group", true);
		}
		// 加入分类条件
		if (null != categoryId && categoryId > 0) {
			List<Integer> categoryIds = new ArrayList();
			Map categoryParam = new HashMap();
			categoryParam.put("parent_id", categoryId);
			categoryParam.put("fields", "id");
			List<CategoryVo> childIds = categoryService.queryList(categoryParam);
			for (CategoryVo categoryEntity : childIds) {
				categoryIds.add(categoryEntity.getId());
			}
			categoryIds.add(categoryId);
			params.put("categoryIds", categoryIds);
		}
		// 查询列表数据
		Query query = new Query(params);
		List<GoodsVo> goodsList = goodsService.queryCatalogProductList(query);
		int total = goodsService.queryTotal(query);

		// 当前购物车中
		List<CartVo> cartList = new ArrayList();
		if (null != getUserId()) {
			// 查询列表数据
			Map cartParam = new HashMap();
			cartParam.put("user_id", getUserId());
			cartList = cartService.queryList(cartParam);
		}
		if (null != cartList && cartList.size() > 0 && null != goodsList && goodsList.size() > 0) {
			for (GoodsVo goodsVo : goodsList) {
				for (CartVo cartVo : cartList) {
					if (goodsVo.getId().equals(cartVo.getGoods_id())) {
						goodsVo.setCart_num(cartVo.getNumber());
					}
				}
			}
		}
		ApiPageUtils goodsData = new ApiPageUtils(goodsList, total, query.getLimit(), query.getPage());
		goodsData.setGoodsList(goodsData.getData());
		return toResponsSuccess(goodsData);
	}
}