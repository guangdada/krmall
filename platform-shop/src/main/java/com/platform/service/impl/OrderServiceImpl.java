package com.platform.service.impl;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platform.dao.GoodsDao;
import com.platform.dao.OrderDao;
import com.platform.dao.OrderGoodsDao;
import com.platform.dao.ShippingDao;
import com.platform.entity.GoodsEntity;
import com.platform.entity.OrderEntity;
import com.platform.entity.OrderGoodsEntity;
import com.platform.service.OrderService;
import com.platform.utils.RRException;
import com.platform.utils.wechat.WechatRefundApiResult;
import com.platform.utils.wechat.WechatUtil;

@Service("orderService")
public class OrderServiceImpl implements OrderService {
	@Autowired
	private OrderDao orderDao;
	@Autowired
	private ShippingDao shippingDao;
	@Autowired
	private OrderGoodsDao orderGoodsDao;
	@Autowired
	private GoodsDao goodsDao;

	@Override
	public OrderEntity queryObject(Integer id) {
		return orderDao.queryObject(id);
	}

	@Override
	public List<OrderEntity> queryList(Map<String, Object> map) {
		return orderDao.queryList(map);
	}

	@Override
	public int queryTotal(Map<String, Object> map) {
		return orderDao.queryTotal(map);
	}

	@Override
	public int save(OrderEntity order) {
		return orderDao.save(order);
	}

	@Override
	public int update(OrderEntity order) {
		return orderDao.update(order);
	}

	@Override
	public int delete(Integer id) {
		return orderDao.delete(id);
	}

	@Override
	public int deleteBatch(Integer[] ids) {
		return orderDao.deleteBatch(ids);
	}

	@Override
	public int confirm(Integer id) {
		OrderEntity orderEntity = queryObject(id);
		Integer shippingStatus = orderEntity.getShippingStatus();// 发货状态
		Integer payStatus = orderEntity.getPayStatus();// 付款状态
		if (2 != payStatus) {
			throw new RRException("此订单未付款，不能确认收货！");
		}
		if (4 == shippingStatus) {
			throw new RRException("此订单处于退货状态，不能确认收货！");
		}
		if (0 == shippingStatus) {
			throw new RRException("此订单未配货，不能确认收货！");
		}

		// 如果是团购订单
		if (orderEntity.getGroupGoodsId() != null && orderEntity.getGroupGoodsId().intValue() != 0) {
			GoodsEntity goodsEntiy = goodsDao.queryObject(orderEntity.getGroupGoodsId());
			if (goodsEntiy.getIsGroup().intValue() == 1 && goodsEntiy.getSellVolume() < goodsEntiy.getGroupNum()) {
				throw new RRException("“" + goodsEntiy.getName() + "”当前销量为" + goodsEntiy.getSellVolume() + "，没有达到拼团数量"
						+ goodsEntiy.getGroupNum() + "，不能确认收货！");
			}
		}
		orderEntity.setShippingStatus(2);
		return 0;
	}

	@Override
	public int sendGoods(OrderEntity order) {
		Integer payStatus = order.getPayStatus();// 付款状态
		Integer orderStatus = order.getOrderStatus(); // 订单状态
		if (2 != payStatus || 201 != orderStatus) {
			throw new RRException("此订单未付款！");
		}

		/*
		 * ShippingEntity shippingEntity =
		 * shippingDao.queryObject(order.getShippingId()); if (null !=
		 * shippingEntity) { order.setShippingName(shippingEntity.getName()); }
		 */
		order.setOrderStatus(300);// 订单已配货
		order.setShippingStatus(1);// 已发货
		return orderDao.update(order);
	}

	@Transactional
	public void cancelOrder(Integer orderId) {
		synchronized (orderId.toString().intern()) {
			OrderEntity order = queryObject(orderId);
			if (order.getOrderStatus() == 301) {
				throw new RRException("此订单已取货！");
			}

			Map<String, Object> orderGoodsParam = new HashMap<String, Object>();
			orderGoodsParam.put("orderId", order.getId());
			List<OrderGoodsEntity> orderGoods = orderGoodsDao.queryList(orderGoodsParam);
			// 已支付需要退款
			if (order.getPayStatus() == 2) {
				WechatRefundApiResult result = WechatUtil.wxRefund(orderId.toString(),
						order.getActualPrice().doubleValue(), order.getActualPrice().doubleValue());
				if (result.getResult_code().equals("SUCCESS")) {
					order.setOrderStatus(401);
					order.setPayStatus(4);
					orderDao.update(order);
					// 回退销量
					if (CollectionUtils.isNotEmpty(orderGoods)) {
						for (OrderGoodsEntity goods : orderGoods) {
							goodsDao.updateSellVolume(goods.getId(), -goods.getNumber());
							goodsDao.updateGoodsNumber(goods.getId(), goods.getNumber());
						}
					}
					throw new RRException("取消成功");
				} else {
					throw new RRException("取消失败");
				}
			} else {
				order.setOrderStatus(101);
				orderDao.update(order);
				// 回退库存
				if (CollectionUtils.isNotEmpty(orderGoods)) {
					for (OrderGoodsEntity goods : orderGoods) {
						goodsDao.updateGoodsNumber(goods.getId(), goods.getNumber());
					}
				}
				throw new RRException("取消成功");
			}
		}
	}
}
