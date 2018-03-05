package com.platform.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.platform.annotation.LoginUser;
import com.platform.entity.UserVo;
import com.platform.service.ApiUserCouponService;
import com.platform.util.ApiBaseAction;
import com.platform.util.ApiPageUtils;
import com.platform.utils.Query;

/**
 * API优惠券管理
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2017-03-23 15:31
 */
@RestController
@RequestMapping("/api/userCoupon")
public class ApiUserCouponController extends ApiBaseAction {
	@Autowired
	private ApiUserCouponService apiUserCouponService;

	/**
	 * 获取优惠券列表
	 */
	@RequestMapping("list")
	public Object list(@LoginUser UserVo loginUser, @RequestParam(value = "page", defaultValue = "1") Integer page,
			@RequestParam(value = "size", defaultValue = "10") Integer size) {
		Map<String, Object> param = new HashMap<String, Object>();
		param.put("enabled", 1);
		param.put("page", page);
		param.put("limit", size);
		param.put("sidx", "order_id");
		param.put("order", "asc");
		param.put("user_id", loginUser.getUserId());
		Query query = new Query(param);

		List<Map<String,Object>> couponVos = apiUserCouponService.queryListMap(query);
		int total = apiUserCouponService.queryTotal(query);
		ApiPageUtils pageUtil = new ApiPageUtils(couponVos, total, query.getLimit(), query.getPage());
		return toResponsSuccess(pageUtil);
	}
}
