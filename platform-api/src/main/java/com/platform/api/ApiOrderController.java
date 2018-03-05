package com.platform.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.MapUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.OrderGoodsVo;
import com.platform.entity.OrderVo;
import com.platform.entity.UserVo;
import com.platform.service.ApiGoodsService;
import com.platform.service.ApiKdniaoService;
import com.platform.service.ApiOrderGoodsService;
import com.platform.service.ApiOrderService;
import com.platform.util.ApiBaseAction;
import com.platform.util.ApiPageUtils;
import com.platform.utils.Query;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/order")
public class ApiOrderController extends ApiBaseAction {
	@Autowired
	private ApiOrderService orderService;
	@Autowired
	private ApiOrderGoodsService orderGoodsService;
	@Autowired
	private ApiKdniaoService apiKdniaoService;
	@Autowired
	private ApiGoodsService apiGoodsService;

	/**
	 */
	@IgnoreAuth
	@RequestMapping("index")
	public Object index(@LoginUser UserVo loginUser) {
		//
		return toResponsSuccess("");
	}

	/**
	 * 获取订单列表
	 */
	@RequestMapping("list")
	public Object list(@LoginUser UserVo loginUser, @RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "size", defaultValue = "10") Integer size) {
		//
		Map<String, Object> params = new HashMap<String, Object>();
		params.put("user_id", loginUser.getUserId());
		params.put("page", page);
		params.put("limit", size);
		params.put("sidx", "id");
		params.put("order", "asc");
		// 查询列表数据
		Query query = new Query(params);
		List<OrderVo> orderEntityList = orderService.queryList(query);
		int total = orderService.queryTotal(query);
		ApiPageUtils pageUtil = new ApiPageUtils(orderEntityList, total, query.getLimit(), query.getPage());
		//
		for (OrderVo item : orderEntityList) {
			Map<String, Object> orderGoodsParam = new HashMap<String, Object>();
			orderGoodsParam.put("order_id", item.getId());
			// 订单的商品
			List<OrderGoodsVo> goodsList = orderGoodsService.queryList(orderGoodsParam);
			Integer goodsCount = 0;
			for (OrderGoodsVo orderGoodsEntity : goodsList) {
				goodsCount += orderGoodsEntity.getNumber();
				item.setGoodsCount(goodsCount);
			}
		}
		return toResponsSuccess(pageUtil);
	}

	/**
	 * 获取订单详情
	 */
	@RequestMapping("detail")
	public Object detail(@LoginUser UserVo loginUser, Integer orderId) {
		Map<String, Object> resultObj = new HashMap<String, Object>();
		//
		OrderVo orderInfo = orderService.queryObject(orderId);
		if (null == orderInfo) {
			return toResponsObject(400, "订单不存在", "");
		}
		Map<String, Object> orderGoodsParam = new HashMap<String, Object>();
		orderGoodsParam.put("order_id", orderId);
		// 订单的商品
		List<OrderGoodsVo> orderGoods = orderGoodsService.queryList(orderGoodsParam);
		// 订单最后支付时间
		if (orderInfo.getOrder_status() == 0) {
			// if (moment().subtract(60, 'minutes') <
			// moment(orderInfo.add_time)) {
			// orderInfo.final_pay_time = moment("001234",
			// "Hmmss").format("mm:ss")
			// } else {
			// //超过时间不支付，更新订单状态为取消
			// }
		}

		// 订单可操作的选择,删除，支付，收货，评论，退换货
		Map<String, Object> handleOption = orderInfo.getHandleOption();
		//
		resultObj.put("orderInfo", orderInfo);
		resultObj.put("orderGoods", orderGoods);
		resultObj.put("handleOption", handleOption);
		/*
		 * if (!StringUtils.isEmpty(orderInfo.getShipping_code()) &&
		 * !StringUtils.isEmpty(orderInfo.getShipping_no())) { // 快递 List Traces
		 * = apiKdniaoService.getOrderTracesByJson(orderInfo.getShipping_code(),
		 * orderInfo.getShipping_no()); resultObj.put("shippingList", Traces); }
		 */
		return toResponsSuccess(resultObj);
	}

	/**
	 * 获取订单列表
	 */
	@RequestMapping("submit")
	public Object submit(@LoginUser UserVo loginUser) {
		Map resultObj = null;
		try {
			resultObj = orderService.submit(getJsonRequest(), loginUser);
			if (null != resultObj) {
				return toResponsObject(MapUtils.getInteger(resultObj, "errno"), MapUtils.getString(resultObj, "errmsg"),
						resultObj.get("data"));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toResponsFail("提交失败");
	}

	/**
	 * 获取订单列表
	 */
	@RequestMapping("cancelOrder")
	public Object cancelOrder(@LoginUser UserVo loginUser, Integer orderId) {
		Map<String, Object> result = orderService.cancelOrder(loginUser, orderId);
		int errno = MapUtils.getInteger(result, "errno");
		if(errno == 0){
			return toResponsSuccess("取消成功");
		}else{
			return toResponsFail(MapUtils.getString(result, "errmsg"));
		}
	}

	/**
	 * 确认收货
	 */
	@RequestMapping("confirmOrder")
	public Object confirmOrder(@LoginUser UserVo loginUser, Integer orderId) {
		try {
			OrderVo orderVo = orderService.queryObject(orderId);
			orderVo.setOrder_status(301);
			orderVo.setShipping_status(2);
			orderVo.setConfirm_time(new Date());
			orderService.update(orderVo);
			return toResponsSuccess("取消成功");
		} catch (Exception e) {
			e.printStackTrace();
		}
		return toResponsFail("提交失败");
	}
}