package com.platform.service;

import com.alibaba.fastjson.JSONObject;
import com.platform.dao.*;
import com.platform.entity.*;
import com.platform.util.CommonUtil;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.*;

@Service
public class ApiOrderService {
	@Autowired
	private ApiOrderMapper orderDao;
	@Autowired
	private ApiAddressMapper apiAddressMapper;
	@Autowired
	private ApiCartMapper apiCartMapper;
	@Autowired
	private ApiCouponMapper apiCouponMapper;
	@Autowired
	private ApiOrderMapper apiOrderMapper;
	@Autowired
	private ApiOrderGoodsMapper apiOrderGoodsMapper;
	@Autowired
	private ApiUserCouponMapper apiUserCouponMapper;
	@Autowired
	private ApiCouponService apiCouponService;

	public OrderVo queryObject(Integer id) {
		return orderDao.queryObject(id);
	}

	public List<OrderVo> queryList(Map<String, Object> map) {
		return orderDao.queryList(map);
	}

	public int queryTotal(Map<String, Object> map) {
		return orderDao.queryTotal(map);
	}

	public void save(OrderVo order) {
		orderDao.save(order);
	}

	public void update(OrderVo order) {
		orderDao.update(order);
	}

	public void delete(Integer id) {
		orderDao.delete(id);
	}

	public void deleteBatch(Integer[] ids) {
		orderDao.deleteBatch(ids);
	}

	@Transactional
	public Map<String, Object> submit(JSONObject jsonParam, UserVo loginUser) {
		// 
		Map<String, Object> resultObj = new HashMap<String, Object>();
		Integer productId = jsonParam.getInteger("productId");
		// 使用的优惠券
		String couponNumber = jsonParam.getString("couponNumber");
		String postscript = jsonParam.getString("postscript");
		
		// 获取要购买的商品
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("user_id", loginUser.getUserId());
		if(productId != null){
			param.put("product_id", productId);
		}
		param.put("session_id", 1);
		param.put("checked", 1);
		List<CartVo> checkedGoodsList = apiCartMapper.queryList(param);
		if (null == checkedGoodsList) {
			resultObj.put("errno", 400);
			resultObj.put("errmsg", "请选择商品");
			return resultObj;
		}
		// 统计商品总价
		BigDecimal goodsTotalPrice = new BigDecimal(0.00);
		for (CartVo cartItem : checkedGoodsList) {
			goodsTotalPrice = goodsTotalPrice
					.add(cartItem.getRetail_price().multiply(new BigDecimal(cartItem.getNumber())));
		}

		// 获取订单使用的优惠券
		BigDecimal couponPrice = new BigDecimal(0.00);
		CouponVo couponVo = null;
		UserCouponVo userCouponVo = null;
		if (!StringUtils.isEmpty(couponNumber)) {
			userCouponVo = apiUserCouponMapper.queryByCouponNumber(couponNumber);
			if(userCouponVo == null || userCouponVo.getOrder_id() != null){
				resultObj.put("errno", 1);
				resultObj.put("errmsg", "优惠券已使用");
				return resultObj;
			}
			couponVo = apiCouponService.queryObject(userCouponVo.getCoupon_id());
			if (null != couponVo && null != couponVo.getType_money()) {
				couponPrice = couponVo.getType_money();
			}
		}
		
		
		BigDecimal orderTotalPrice = goodsTotalPrice.add(new BigDecimal(0.00)); // 订单的总价

		//减去其它支付的金额后，要实际支付的金额
		BigDecimal actualPrice = orderTotalPrice.subtract(couponPrice); // 减去其它支付的金额后，要实际支付的金额
		// 保存订单信息
		OrderVo orderInfo = new OrderVo();
		orderInfo.setOrder_sn(CommonUtil.generateOrderNumber());
		orderInfo.setUser_id(loginUser.getUserId());
		// 留言
		orderInfo.setPostscript(postscript);
		// 使用的优惠券
		// orderInfo.setFull_cut_price(fullCutCouponDec);
		orderInfo.setCoupon_id(couponVo == null ? null : couponVo.getId());
		orderInfo.setCoupon_price(couponPrice);
		orderInfo.setAdd_time(new Date());
		orderInfo.setGoods_price(goodsTotalPrice);
		orderInfo.setOrder_price(orderTotalPrice);
		orderInfo.setActual_price(actualPrice);
		// 待付款
		orderInfo.setOrder_status(0);
		orderInfo.setShipping_status(0);
		orderInfo.setPay_status(0);
		orderInfo.setShipping_id(0);
		orderInfo.setShipping_fee(new BigDecimal(0));
		orderInfo.setIntegral(0);
		orderInfo.setIntegral_money(new BigDecimal(0));

		// 开启事务，插入订单信息和订单商品
		apiOrderMapper.save(orderInfo);
		if (null == orderInfo.getId()) {
			resultObj.put("errno", 1);
			resultObj.put("errmsg", "订单提交失败");
			return resultObj;
		}
		// 统计商品总价
		List<OrderGoodsVo> orderGoodsData = new ArrayList<OrderGoodsVo>();
		for (CartVo goodsItem : checkedGoodsList) {
			OrderGoodsVo orderGoodsVo = new OrderGoodsVo();
			orderGoodsVo.setOrder_id(orderInfo.getId());
			orderGoodsVo.setGoods_id(goodsItem.getGoods_id());
			orderGoodsVo.setGoods_sn(goodsItem.getGoods_sn());
			orderGoodsVo.setProduct_id(goodsItem.getProduct_id());
			orderGoodsVo.setGoods_name(goodsItem.getGoods_name());
			orderGoodsVo.setList_pic_url(goodsItem.getList_pic_url());
			orderGoodsVo.setMarket_price(goodsItem.getMarket_price());
			orderGoodsVo.setRetail_price(goodsItem.getRetail_price());
			orderGoodsVo.setNumber(goodsItem.getNumber());
			orderGoodsVo.setGoods_specifition_name_value(goodsItem.getGoods_specifition_name_value());
			orderGoodsVo.setGoods_specifition_ids(goodsItem.getGoods_specifition_ids());
			orderGoodsData.add(orderGoodsVo);
			apiOrderGoodsMapper.save(orderGoodsVo);
		}
		
		// 优惠券标记已用
		if (null != userCouponVo && null == userCouponVo.getOrder_id()) {
			userCouponVo.setUsed_time(new Date());
			userCouponVo.setOrder_id(orderInfo.getId());
			apiUserCouponMapper.update(userCouponVo);
		}

		// 清空已购买的商品
		apiCartMapper.deleteByCart(loginUser.getUserId(), 1, 1,productId);
		resultObj.put("errno", 0);
		resultObj.put("errmsg", "订单提交成功");
		Map<String, Object> orderInfoMap = new HashMap<String, Object>();
		orderInfoMap.put("orderInfo", orderInfo);
		resultObj.put("data", orderInfoMap);
		return resultObj;
	}

}
