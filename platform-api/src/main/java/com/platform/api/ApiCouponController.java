package com.platform.api;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.LoginUser;
import com.platform.entity.CouponVo;
import com.platform.entity.SmsLogVo;
import com.platform.entity.UserCouponVo;
import com.platform.entity.UserVo;
import com.platform.service.ApiCouponService;
import com.platform.service.ApiUserCouponService;
import com.platform.service.ApiUserService;
import com.platform.util.ApiBaseAction;
import com.platform.utils.CharUtil;
import com.qiniu.util.StringUtils;

/**
 * API优惠券管理
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2017-03-23 15:31
 */
@RestController
@RequestMapping("/api/coupon")
public class ApiCouponController extends ApiBaseAction {
	@Autowired
	private ApiUserService apiUserService;
	@Autowired
	private ApiCouponService apiCouponService;
	@Autowired
	private ApiUserCouponService apiUserCouponService;

	/**
	 * 获取优惠券列表
	 */
	@RequestMapping("list")
	public Object list(@LoginUser UserVo loginUser) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("enabled", 1);
		List<CouponVo> couponVos = apiCouponService.queryUserCoupons(param);
		return toResponsSuccess(couponVos);
	}

	/**
	 * 兑换优惠券
	 */
	@RequestMapping("exchange")
	public Object exchange(@LoginUser UserVo loginUser) {
		// 获取优惠券
		// 判断优惠券是否有效
		// 判断积分是否足够
		// 保存用户优惠券
		JSONObject jsonParam = getJsonRequest();
		String couponId = jsonParam.getString("couponId");
		Map<String, Object> r = apiCouponService.exchange(couponId, loginUser.getUserId().intValue());
		if (r.get("errno").toString().equals("1")) {
			return toResponsFail(r.get("errmsg").toString());
		} else {
			return toResponsSuccess("兑换成功");
		}
	}

	/**
	 * 填写手机号码，领券
	 */
	@RequestMapping("newuser")
	public Object newuser(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = getJsonRequest();
		//
		String phone = jsonParam.getString("phone");
		String smscode = jsonParam.getString("smscode");
		// 校验短信码
		SmsLogVo smsLogVo = apiUserService.querySmsCodeByUserId(loginUser.getUserId());
		if (null != smsLogVo && !smsLogVo.getSms_code().equals(smscode)) {
			return toResponsFail("短信校验失败");
		}
		// 更新手机号码
		if (!StringUtils.isNullOrEmpty(phone)) {
			if (phone.equals(loginUser.getMobile())) {
				loginUser.setMobile(phone);
				apiUserService.update(loginUser);
			}
		}
		// 判断是否是新用户
		if (!StringUtils.isNullOrEmpty(loginUser.getMobile())) {
			return toResponsFail("当前优惠券只能新用户领取");
		}
		// 是否领取过了
		Map params = new HashMap();
		params.put("user_id", loginUser.getUserId());
		params.put("send_type", 4);
		List<CouponVo> couponVos = apiCouponService.queryUserCoupons(params);
		if (null != couponVos && couponVos.size() > 0) {
			return toResponsFail("已经领取过，不能重复领取");
		}
		// 领取
		Map couponParam = new HashMap();
		couponParam.put("send_type", 4);
		CouponVo newCouponConfig = apiCouponService.queryMaxUserEnableCoupon(couponParam);
		if (null != newCouponConfig) {
			UserCouponVo userCouponVo = new UserCouponVo();
			userCouponVo.setAdd_time(new Date());
			userCouponVo.setCoupon_id(newCouponConfig.getId());
			userCouponVo.setCoupon_number(CharUtil.getRandomString(12));
			userCouponVo.setUser_id(loginUser.getUserId());
			apiUserCouponService.save(userCouponVo);
			return toResponsSuccess(userCouponVo);
		} else {
			return toResponsFail("领取失败");
		}
	}

	/**
	 * 转发领取红包
	 */
	@RequestMapping("transActivit")
	public Object transActivit(@LoginUser UserVo loginUser, String sourceKey, Long referrer) {
		JSONObject jsonParam = getJsonRequest();
		// 是否领取过了
		Map params = new HashMap();
		params.put("user_id", loginUser.getUserId());
		params.put("send_type", 2);
		params.put("source_key", sourceKey);
		List<CouponVo> couponVos = apiCouponService.queryUserCoupons(params);
		if (null != couponVos && couponVos.size() > 0) {
			return toResponsObject(2, "已经领取过", couponVos);
		}
		// 领取
		Map couponParam = new HashMap();
		couponParam.put("send_type", 2);
		CouponVo newCouponConfig = apiCouponService.queryMaxUserEnableCoupon(couponParam);
		if (null != newCouponConfig) {
			UserCouponVo userCouponVo = new UserCouponVo();
			userCouponVo.setAdd_time(new Date());
			userCouponVo.setCoupon_id(newCouponConfig.getId());
			userCouponVo.setCoupon_number(CharUtil.getRandomString(12));
			userCouponVo.setUser_id(loginUser.getUserId());
			userCouponVo.setSource_key(sourceKey);
			userCouponVo.setReferrer(referrer);
			apiUserCouponService.save(userCouponVo);
			//
			List<UserCouponVo> userCouponVos = new ArrayList();
			userCouponVos.add(userCouponVo);
			//
			params = new HashMap();
			params.put("user_id", loginUser.getUserId());
			params.put("send_type", 2);
			params.put("source_key", sourceKey);
			couponVos = apiCouponService.queryUserCoupons(params);
			return toResponsSuccess(couponVos);
		} else {
			return toResponsFail("领取失败");
		}
	}
}
