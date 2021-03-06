package com.platform.api;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.LoginUser;
import com.platform.entity.CartVo;
import com.platform.entity.CouponVo;
import com.platform.entity.GoodsSpecificationVo;
import com.platform.entity.GoodsVo;
import com.platform.entity.ProductVo;
import com.platform.entity.UserCouponVo;
import com.platform.entity.UserVo;
import com.platform.service.ApiAddressService;
import com.platform.service.ApiCartService;
import com.platform.service.ApiCouponService;
import com.platform.service.ApiGoodsCrashService;
import com.platform.service.ApiGoodsService;
import com.platform.service.ApiGoodsSpecificationService;
import com.platform.service.ApiProductService;
import com.platform.service.ApiUserCouponService;
import com.platform.util.ApiBaseAction;
import com.qiniu.util.StringUtils;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/cart")
public class ApiCartController extends ApiBaseAction {
	@Autowired
	private ApiCartService cartService;
	@Autowired
	private ApiGoodsService goodsService;
	@Autowired
	private ApiProductService productService;
	@Autowired
	private ApiGoodsSpecificationService goodsSpecificationService;
	@Autowired
	private ApiAddressService addressService;
	@Autowired
	private ApiCouponService apiCouponService;
	@Autowired
	private ApiGoodsCrashService apiGoodsCrashService;
	@Autowired
	private ApiUserCouponService apiUserCouponService;

	/**
	 * 获取购物车中的数据
	 */
	@RequestMapping("getCart")
	public Object getCart(@LoginUser UserVo loginUser) {
		Map<String, Object> resultObj = initCartGoods(loginUser, null);
		return resultObj;
	}

	/**
	 * 初始化购物车商品信息
	 * 
	 * @param loginUser
	 * @param productId
	 * @return
	 */
	private Map<String, Object> initCartGoods(UserVo loginUser, Integer productId) {
		Map<String, Object> resultObj = new HashMap();
		// 查询列表数据
		Map param = new HashMap();
		param.put("user_id", loginUser.getUserId());
		if (productId != null && productId.intValue() != 0) {
			param.put("product_id", productId);
		}
		List<CartVo> cartList = cartService.queryList(param);
		// 获取购物车统计信息
		Integer goodsCount = 0;
		BigDecimal goodsAmount = new BigDecimal(0.00);
		Integer checkedGoodsCount = 0;
		BigDecimal checkedGoodsAmount = new BigDecimal(0.00);
		for (CartVo cartItem : cartList) {
			goodsCount += cartItem.getNumber();
			goodsAmount = goodsAmount.add(cartItem.getRetail_price().multiply(new BigDecimal(cartItem.getNumber())));
			if (null != cartItem.getChecked() && 1 == cartItem.getChecked()) {
				checkedGoodsCount += cartItem.getNumber();
				checkedGoodsAmount = checkedGoodsAmount
						.add(cartItem.getRetail_price().multiply(new BigDecimal(cartItem.getNumber())));
			}
		}
		// 获取优惠信息提示
		Map couponParam = new HashMap();
		couponParam.put("enabled", true);
		Integer[] send_types = new Integer[] { 0, 7 };
		couponParam.put("send_types", send_types);

		resultObj.put("cartList", cartList);
		//
		Map<String, Object> cartTotal = new HashMap();
		cartTotal.put("goodsCount", goodsCount);
		cartTotal.put("goodsAmount", goodsAmount);
		cartTotal.put("checkedGoodsCount", checkedGoodsCount);
		cartTotal.put("checkedGoodsAmount", checkedGoodsAmount);
		//
		resultObj.put("cartTotal", cartTotal);
		return resultObj;
	}

	/**
	 * 获取购物车信息，所有对购物车的增删改操作，都要重新返回购物车的信息
	 */
	@RequestMapping("index")
	public Object index(@LoginUser UserVo loginUser) {
		return toResponsSuccess(getCart(loginUser));
	}

	/**
	 * 添加商品到购物车
	 */
	@RequestMapping("add")
	public Object add(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = getJsonRequest();
		Integer goodsId = jsonParam.getInteger("goodsId");
		Integer productId = jsonParam.getInteger("productId");
		Integer number = jsonParam.getInteger("number");
		// 判断商品是否可以购买
		GoodsVo goodsInfo = goodsService.queryObject(goodsId);
		if (null == goodsInfo || goodsInfo.getIs_delete() == 1) {
			return this.toResponsObject(400, "商品已下架", "");
		}
		// 取得规格的信息,判断规格库存
		ProductVo productInfo = productService.queryObject(productId);
		if (null == productInfo || productInfo.getGoods_number() < number) {
			return this.toResponsObject(400, "库存不足", "");
		}

		// 判断购物车中是否存在此规格商品
		Map cartParam = new HashMap();
		cartParam.put("goods_id", goodsId);
		cartParam.put("product_id", productId);
		cartParam.put("user_id", loginUser.getUserId());
		List<CartVo> cartInfoList = cartService.queryList(cartParam);
		CartVo cartInfo = null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
		if (null == cartInfo) {
			// 添加操作
			// 添加规格名和值
			String[] goodsSepcifitionValue = null;
			if (null != productInfo.getGoods_specification_ids()
					&& productInfo.getGoods_specification_ids().length() > 0) {
				Map specificationParam = new HashMap();
				specificationParam.put("ids", productInfo.getGoods_specification_ids());
				specificationParam.put("goodsId", goodsId);
				List<GoodsSpecificationVo> specificationEntities = goodsSpecificationService
						.queryList(specificationParam);
				goodsSepcifitionValue = new String[specificationEntities.size()];
				for (int i = 0; i < specificationEntities.size(); i++) {
					goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
				}
			}
			cartInfo = new CartVo();

			cartInfo.setGoods_id(goodsId);
			cartInfo.setProduct_id(productId);
			cartInfo.setGoods_sn(productInfo.getGoods_sn());
			cartInfo.setGoods_name(goodsInfo.getName());
			cartInfo.setList_pic_url(goodsInfo.getList_pic_url());
			cartInfo.setNumber(number);
			cartInfo.setSession_id("1");
			cartInfo.setUser_id(loginUser.getUserId());
			cartInfo.setRetail_price(productInfo.getRetail_price());
			cartInfo.setMarket_price(productInfo.getMarket_price());
			if (null != goodsSepcifitionValue) {
				cartInfo.setGoods_specifition_name_value(StringUtils.join(goodsSepcifitionValue, ";"));
			}
			cartInfo.setGoods_specifition_ids(productInfo.getGoods_specification_ids());
			cartInfo.setChecked(1);
			cartService.save(cartInfo);
		} else {
			// 如果已经存在购物车中，则数量增加
			if (productInfo.getGoods_number() < (number + cartInfo.getNumber())) {
				return this.toResponsObject(400, "库存不足", "");
			}
			cartInfo.setNumber(cartInfo.getNumber() + number);
			cartService.update(cartInfo);
		}
		return toResponsSuccess(getCart(loginUser));
	}

	/**
	 * 减少商品到购物车
	 */
	@RequestMapping("minus")
	public Object minus(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = getJsonRequest();
		Integer goodsId = jsonParam.getInteger("goodsId");
		Integer productId = jsonParam.getInteger("productId");
		Integer number = jsonParam.getInteger("number");
		// 判断购物车中是否存在此规格商品
		Map cartParam = new HashMap();
		cartParam.put("goods_id", goodsId);
		cartParam.put("product_id", productId);
		cartParam.put("user_id", loginUser.getUserId());
		List<CartVo> cartInfoList = cartService.queryList(cartParam);
		CartVo cartInfo = null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
		int cart_num = 0;
		if (null != cartInfo) {
			if (cartInfo.getNumber() > number) {
				cartInfo.setNumber(cartInfo.getNumber() - number);
				cartService.update(cartInfo);
				cart_num = cartInfo.getNumber();
			} else if (cartInfo.getNumber() == 1) {
				cartService.delete(cartInfo.getId());
				cart_num = 0;
			}
		}
		return toResponsSuccess(cart_num);
	}

	/**
	 * 更新指定的购物车信息
	 */
	@RequestMapping("update")
	public Object update(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = getJsonRequest();
		Integer goodsId = jsonParam.getInteger("goodsId");
		Integer productId = jsonParam.getInteger("productId");
		Integer number = jsonParam.getInteger("number");
		Integer id = jsonParam.getInteger("id");
		// 取得规格的信息,判断规格库存
		ProductVo productInfo = productService.queryObject(productId);
		if (null == productInfo || productInfo.getGoods_number() < number) {
			return this.toResponsObject(400, "库存不足", "");
		}
		// 判断是否已经存在product_id购物车商品
		CartVo cartInfo = cartService.queryObject(id);
		// 只是更新number
		if (cartInfo.getProduct_id().equals(productId)) {
			cartInfo.setNumber(number);
			cartService.update(cartInfo);
			return toResponsSuccess(getCart(loginUser));
		}

		Map cartParam = new HashMap();
		cartParam.put("goodsId", goodsId);
		cartParam.put("productId", productId);
		List<CartVo> cartInfoList = cartService.queryList(cartParam);
		CartVo newcartInfo = null != cartInfoList && cartInfoList.size() > 0 ? cartInfoList.get(0) : null;
		if (null == newcartInfo) {
			// 添加操作
			// 添加规格名和值
			String[] goodsSepcifitionValue = null;
			if (null != productInfo.getGoods_specification_ids()) {
				Map specificationParam = new HashMap();
				specificationParam.put("ids", productInfo.getGoods_specification_ids());
				specificationParam.put("goodsId", goodsId);
				List<GoodsSpecificationVo> specificationEntities = goodsSpecificationService
						.queryList(specificationParam);
				goodsSepcifitionValue = new String[specificationEntities.size()];
				for (int i = 0; i < specificationEntities.size(); i++) {
					goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
				}
			}
			cartInfo.setProduct_id(productId);
			cartInfo.setGoods_sn(productInfo.getGoods_sn());
			cartInfo.setNumber(number);
			cartInfo.setRetail_price(productInfo.getRetail_price());
			cartInfo.setMarket_price(productInfo.getRetail_price());
			if (null != goodsSepcifitionValue) {
				cartInfo.setGoods_specifition_name_value(StringUtils.join(goodsSepcifitionValue, ";"));
			}
			cartInfo.setGoods_specifition_ids(productInfo.getGoods_specification_ids());
			cartService.update(cartInfo);
		} else {
			// 合并购物车已有的product信息，删除已有的数据
			Integer newNumber = number + newcartInfo.getNumber();
			if (null == productInfo || productInfo.getGoods_number() < newNumber) {
				return this.toResponsObject(400, "库存不足", "");
			}
			cartService.delete(newcartInfo.getId());
			// 添加规格名和值
			String[] goodsSepcifitionValue = null;
			if (null != productInfo.getGoods_specification_ids()) {
				Map specificationParam = new HashMap();
				specificationParam.put("ids", productInfo.getGoods_specification_ids());
				specificationParam.put("goodsId", goodsId);
				List<GoodsSpecificationVo> specificationEntities = goodsSpecificationService
						.queryList(specificationParam);
				goodsSepcifitionValue = new String[specificationEntities.size()];
				for (int i = 0; i < specificationEntities.size(); i++) {
					goodsSepcifitionValue[i] = specificationEntities.get(i).getValue();
				}
			}
			cartInfo.setProduct_id(productId);
			cartInfo.setGoods_sn(productInfo.getGoods_sn());
			cartInfo.setNumber(number);
			cartInfo.setRetail_price(productInfo.getRetail_price());
			cartInfo.setMarket_price(productInfo.getRetail_price());
			if (null != goodsSepcifitionValue) {
				cartInfo.setGoods_specifition_name_value(StringUtils.join(goodsSepcifitionValue, ";"));
			}
			cartInfo.setGoods_specifition_ids(productInfo.getGoods_specification_ids());
			cartService.update(cartInfo);
		}
		return toResponsSuccess(getCart(loginUser));
	}

	/**
	 * 是否选择商品，如果已经选择，则取消选择，批量操作
	 */
	@RequestMapping("checked")
	public Object checked(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = getJsonRequest();
		String productIds = jsonParam.getString("productIds");
		Integer isChecked = jsonParam.getInteger("isChecked");
		if (StringUtils.isNullOrEmpty(productIds)) {
			return this.toResponsFail("删除出错");
		}
		String[] productIdArray = productIds.split(",");
		cartService.updateCheck(productIdArray, isChecked, loginUser.getUserId());
		return toResponsSuccess(getCart(loginUser));
	}

	// 删除选中的购物车商品，批量删除
	@RequestMapping("delete")
	public Object delete(@LoginUser UserVo loginUser) {
		Long userId = loginUser.getUserId();

		JSONObject jsonObject = getJsonRequest();
		String productIds = jsonObject.getString("productIds");

		if (StringUtils.isNullOrEmpty(productIds)) {
			return toResponsFail("删除出错");
		}
		String[] productIdsArray = productIds.split(",");
		cartService.deleteByUserAndProductIds(userId, productIdsArray);

		return toResponsSuccess(getCart(loginUser));
	}

	// 获取购物车商品的总件件数
	@RequestMapping("goodscount")
	public Object goodscount(@LoginUser UserVo loginUser) {
		if (null == loginUser || null == loginUser.getUserId()) {
			return toResponsFail("未登录");
		}
		Map<String, Object> resultObj = new HashMap();
		// 查询列表数据
		Map param = new HashMap();
		param.put("user_id", loginUser.getUserId());
		List<CartVo> cartList = cartService.queryList(param);
		// 获取购物车统计信息
		Integer goodsCount = 0;
		for (CartVo cartItem : cartList) {
			goodsCount += cartItem.getNumber();
		}
		resultObj.put("cartList", cartList);
		//
		Map<String, Object> cartTotal = new HashMap();
		cartTotal.put("goodsCount", goodsCount);
		//
		resultObj.put("cartTotal", cartTotal);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 订单提交前的检验和填写相关订单信息
	 */
	@RequestMapping("checkout")
	public Object checkout(@LoginUser UserVo loginUser, String couponNumber, Integer addressId, Integer productId) {
		Map<String, Object> resultObj = new HashMap<String, Object>();
		// 获取要购买的商品
		Map<String, Object> cartData = (Map<String, Object>) this.initCartGoods(loginUser, productId);

		List<CartVo> checkedGoodsList = new ArrayList<CartVo>();
		for (CartVo cartEntity : (List<CartVo>) cartData.get("cartList")) {
			if (cartEntity.getChecked() == 1) {
				checkedGoodsList.add(cartEntity);
			}
		}
		// 计算订单的费用
		// 商品总价
		BigDecimal goodsTotalPrice = (BigDecimal) ((HashMap) cartData.get("cartTotal")).get("checkedGoodsAmount");

		
		Map<String, Object> map = new HashMap<String, Object>();
		map.put("user_id", loginUser.getUserId());
		map.put("order_id", 0); // 未使用状态
		int coupons = apiUserCouponService.queryTotal(map);
		
		// 使用优惠券减免的金额
		BigDecimal couponPrice = new BigDecimal(0.00);
		if (!StringUtils.isNullOrEmpty(couponNumber)) {
			UserCouponVo userCouponVo = apiUserCouponService.queryByCouponNumber(couponNumber);
			CouponVo checkedCoupon = apiCouponService.queryObject(userCouponVo.getCoupon_id());
			couponPrice = checkedCoupon.getType_money();
			resultObj.put("couponPrice", couponPrice);
			resultObj.put("checkedCoupon", checkedCoupon);
		}
		// 获取优惠信息提示
		// 根据收货地址计算运费
		BigDecimal freightPrice = new BigDecimal(0.00);

		// 订单的总价
		BigDecimal orderTotalPrice = goodsTotalPrice.add(freightPrice);

		// 减去其它支付的金额后，要实际支付的金额
		BigDecimal actualPrice = orderTotalPrice.subtract(couponPrice);

		resultObj.put("freightPrice", freightPrice);
		resultObj.put("checkedGoodsList", checkedGoodsList);
		resultObj.put("goodsTotalPrice", goodsTotalPrice);
		resultObj.put("orderTotalPrice", orderTotalPrice);
		resultObj.put("actualPrice", actualPrice);
		resultObj.put("coupons", coupons);
		return toResponsSuccess(resultObj);
	}

	/**
	 * 选择优惠券列表
	 */
	@RequestMapping("checkedCouponList")
	public Object checkedCouponList(@LoginUser UserVo loginUser) {
		//
		Map param = new HashMap();
		param.put("user_id", loginUser.getUserId());
		List<CouponVo> couponVos = apiCouponService.queryUserCouponList(param);
		if (null != couponVos && couponVos.size() > 0) {
			// 获取要购买的商品
			Map<String, Object> cartData = (Map<String, Object>) this.getCart(loginUser);
			List<CartVo> checkedGoodsList = new ArrayList();
			List<Integer> checkedGoodsIds = new ArrayList();
			for (CartVo cartEntity : (List<CartVo>) cartData.get("cartList")) {
				if (cartEntity.getChecked() == 1) {
					checkedGoodsList.add(cartEntity);
					checkedGoodsIds.add(cartEntity.getId());
				}
			}
			// 计算订单的费用
			BigDecimal goodsTotalPrice = (BigDecimal) ((HashMap) cartData.get("cartTotal")).get("checkedGoodsAmount"); // 商品总价
			// 如果没有用户优惠券直接返回新用户优惠券
			for (CouponVo couponVo : couponVos) {
				if (couponVo.getMin_amount().compareTo(goodsTotalPrice) <= 0) {
					couponVo.setEnabled(1);
				}
			}
		}
		return toResponsSuccess(couponVos);
	}
}
