package com.platform.service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.platform.dao.ApiAddressMapper;
import com.platform.dao.ApiCartMapper;
import com.platform.dao.ApiCouponMapper;
import com.platform.dao.ApiGoodsMapper;
import com.platform.dao.ApiOrderGoodsMapper;
import com.platform.dao.ApiOrderMapper;
import com.platform.dao.ApiUserCouponMapper;
import com.platform.entity.CartVo;
import com.platform.entity.CouponVo;
import com.platform.entity.GoodsVo;
import com.platform.entity.OrderGoodsVo;
import com.platform.entity.OrderVo;
import com.platform.entity.UserCouponVo;
import com.platform.entity.UserVo;
import com.platform.util.CommonUtil;
import com.platform.utils.wechat.WechatRefundApiResult;
import com.platform.utils.wechat.WechatUtil;

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
	private ApiGoodsMapper apiGoodsMapper;

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
		resultObj.put("errno", 1);
		Integer productId = jsonParam.getInteger("productId");
		// 使用的优惠券
		String couponNumber = jsonParam.getString("couponNumber");
		String postscript = jsonParam.getString("postscript");

		// 获取要购买的商品
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("user_id", loginUser.getUserId());
		if (productId != null) {
			param.put("product_id", productId);
		}
		param.put("session_id", 1);
		param.put("checked", 1);
		List<CartVo> checkedGoodsList = apiCartMapper.queryList(param);
		if (null == checkedGoodsList) {
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
			if (userCouponVo == null || userCouponVo.getOrder_id() != 0) {
				resultObj.put("errmsg", "优惠券已使用");
				return resultObj;
			}
			couponVo = apiCouponMapper.queryObject(userCouponVo.getCoupon_id());
			if (null != couponVo && null != couponVo.getType_money()) {
				couponPrice = couponVo.getType_money();
			}
		}

		GoodsVo groupGoods = null;
		GoodsVo limitGoods = null;
		boolean isOther = false;
		// 验证商品信息
		for (CartVo goodsItem : checkedGoodsList) {
			GoodsVo goods = apiGoodsMapper.queryObject(goodsItem.getId());
			if (goods == null) {
				resultObj.put("errmsg", "商品不存在");
				return resultObj;
			}
			if (goods.getIs_group().intValue() == 1) {
				groupGoods = goods;
			}else if(goods.getIs_limitTime().intValue() == 1){
				limitGoods = goods;
			} else {
				isOther = true;
			}
			if (goods.getIs_on_sale().intValue() == 0) {
				resultObj.put("errmsg", "“" + goods.getName() + "”已下架");
				return resultObj;
			}
			if (goods.getIs_delete().intValue() == 1) {
				resultObj.put("errmsg", "“" + goods.getName() + "”已删除");
				return resultObj;
			}
			// 校验库存
			if (goods.getGoods_number() == null || goods.getGoods_number() < goodsItem.getNumber()) {
				resultObj.put("errmsg", "“" + goods.getName() + "”库存不足");
				return resultObj;
			}

			// 限时购商品判断时间
			if (goods.getIs_limitTime().intValue() == 1 && goods.getLimitTime() != null
					&& goods.getLimitTime().before(new Date())) {
				resultObj.put("errmsg", "“" + goods.getName() + "”购买时间已经截止");
				return resultObj;
			}
		}

		// 限时购商品、拼团商品不能和其他商品一块下单
		if (isOther && groupGoods != null) {
			resultObj.put("errmsg", "“" + groupGoods.getName() + "”是拼团商品，请单独下单");
			return resultObj;
		}else if(isOther && limitGoods != null){
			resultObj.put("errmsg", "“" + limitGoods.getName() + "”是限时购商品，请单独下单");
			return resultObj;
		}else if(groupGoods != null && limitGoods != null){
			resultObj.put("errmsg", "“" + groupGoods.getName() + "”是拼团商品，请单独下单");
			return resultObj;
		}

		BigDecimal orderTotalPrice = goodsTotalPrice.add(new BigDecimal(0.00)); // 订单的总价

		// 减去其它支付的金额后，要实际支付的金额
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
		// 订单类型
		if (groupGoods != null) {
			orderInfo.setOrder_type(2);
			orderInfo.setGoods_id(groupGoods.getId());
		} else if (limitGoods != null) {
			orderInfo.setOrder_type(3);
			orderInfo.setGoods_id(limitGoods.getId());
		} else {
			orderInfo.setOrder_type(1);
		}

		// 开启事务，插入订单信息和订单商品
		apiOrderMapper.save(orderInfo);
		if (null == orderInfo.getId()) {
			resultObj.put("errno", 1);
			resultObj.put("errmsg", "订单提交失败");
			return resultObj;
		}
		// 统计商品总价
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
			// 扣减库存
			apiGoodsMapper.updateGoodsNumber(goodsItem.getGoods_id(), -goodsItem.getNumber());
			apiOrderGoodsMapper.save(orderGoodsVo);
		}

		// 优惠券标记已用
		if (null != userCouponVo && 0 == userCouponVo.getOrder_id().intValue()) {
			userCouponVo.setUsed_time(new Date());
			userCouponVo.setOrder_id(orderInfo.getId());
			apiUserCouponMapper.update(userCouponVo);
		}

		// 清空已购买的商品
		apiCartMapper.deleteByCart(loginUser.getUserId(), 1, 1, productId);
		resultObj.put("errno", 0);
		resultObj.put("errmsg", "订单提交成功");
		Map<String, Object> orderInfoMap = new HashMap<String, Object>();
		orderInfoMap.put("orderInfo", orderInfo);
		resultObj.put("data", orderInfoMap);
		return resultObj;
	}

	@Transactional
	public Map<String, Object> cancelOrder(UserVo loginUser, Integer orderId) {
		Map<String, Object> resultObj = new HashMap<String, Object>();
		synchronized (orderId.toString().intern()) {
			resultObj.put("errno", 1);
			OrderVo orderVo = apiOrderMapper.queryObject(orderId);
			if (orderVo.getOrder_status() == 300) {
				resultObj.put("errmsg", "已配货，不能取消");
				return resultObj;
			} else if (orderVo.getOrder_status() == 301) {
				resultObj.put("errmsg", "已收货，不能取消");
				return resultObj;
			}
			if (orderVo.getUser_id().intValue() != loginUser.getUserId().intValue()) {
				resultObj.put("errmsg", "您没有权限取消该订单");
				return resultObj;
			}

			Map<String, Object> orderGoodsParam = new HashMap<String, Object>();
			orderGoodsParam.put("order_id", orderVo.getId());
			List<OrderGoodsVo> orderGoods = apiOrderGoodsMapper.queryList(orderGoodsParam);
			// 已支付需要退款
			if (orderVo.getPay_status() == 2) {
				WechatRefundApiResult result = WechatUtil.wxRefund(orderVo.getId().toString(), 0.01, 0.01);
				if (result.getResult_code().equals("SUCCESS")) {
					orderVo.setOrder_status(401);
					orderVo.setPay_status(4);
					apiOrderMapper.update(orderVo);

					// 回退销量
					if (CollectionUtils.isNotEmpty(orderGoods)) {
						for (OrderGoodsVo goods : orderGoods) {
							apiGoodsMapper.updateSellVolume(goods.getGoods_id(), -goods.getNumber());
							apiGoodsMapper.updateGoodsNumber(goods.getId(), goods.getNumber());
						}
					}
					resultObj.put("errmsg", "取消成功");
				} else {
					resultObj.put("errmsg", "取消失败");
					return resultObj;
				}
			} else {
				orderVo.setOrder_status(101);
				apiOrderMapper.update(orderVo);
				// 回退库存
				if (CollectionUtils.isNotEmpty(orderGoods)) {
					for (OrderGoodsVo goods : orderGoods) {
						apiGoodsMapper.updateGoodsNumber(goods.getId(), goods.getNumber());
					}
				}
				resultObj.put("errmsg", "取消成功");
			}
		}
		return resultObj;
	}
}
