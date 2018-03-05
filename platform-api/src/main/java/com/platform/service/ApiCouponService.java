package com.platform.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.platform.dao.ApiCouponMapper;
import com.platform.dao.ApiUserCouponMapper;
import com.platform.dao.ApiUserMapper;
import com.platform.entity.CouponVo;
import com.platform.entity.UserCouponVo;
import com.platform.entity.UserVo;
import com.platform.utils.CharUtil;
import com.qiniu.util.StringUtils;

@Service
public class ApiCouponService {
	@Autowired
	private ApiCouponMapper apiCouponMapper;

	@Autowired
	private ApiUserCouponMapper apiUserCouponMapper;

	@Autowired
	private ApiUserMapper apiUserMapper;

	public CouponVo queryObject(Integer couponId) {
		return apiCouponMapper.queryObject(couponId);
	}

	public List<CouponVo> queryList(Map<String, Object> map) {
		return apiCouponMapper.queryList(map);
	}

	public int queryTotal(Map<String, Object> map) {
		return apiCouponMapper.queryTotal(map);
	}

	public void save(CouponVo userVo) {
		apiCouponMapper.save(userVo);
	}

	public void update(CouponVo user) {
		apiCouponMapper.update(user);
	}

	public void delete(Long userId) {
		apiCouponMapper.delete(userId);
	}

	public void deleteBatch(Long[] userIds) {
		apiCouponMapper.deleteBatch(userIds);
	}

	public List<CouponVo> queryUserCoupons(Map<String, Object> map) {
		return apiCouponMapper.queryUserCoupons(map);
	}

	public CouponVo queryMaxUserEnableCoupon(Map<String, Object> map) {
		return apiCouponMapper.queryMaxUserEnableCoupon(map);
	}

	public List<CouponVo> queryUserCouponList(Map<String, Object> map) {
		return apiCouponMapper.queryUserCouponList(map);
	}

	@Transactional
	public Map<String, Object> exchange(String couponId, Integer userId) {
		Map<String, Object> obj = new HashMap<String, Object>();
		obj.put("errno", 0);
		obj.put("errmsg", "兑换成功");
		synchronized (userId.toString().intern()) {
			if (StringUtils.isNullOrEmpty(couponId)) {
				obj.put("errno", 1);
				obj.put("errmsg", "当前优惠券无效");
				return obj;
			}
			CouponVo couponVo = apiCouponMapper.queryObject(Integer.parseInt(couponId));
			if (couponVo.getPoint() == null) {
				obj.put("errno", 1);
				obj.put("errmsg", "当前优惠券没有设置积分");
				return obj;
			}
			if (null == couponVo || null == couponVo.getUse_end_date()
					|| couponVo.getUse_end_date().before(new Date())) {
				obj.put("errno", 1);
				obj.put("errmsg", "当前优惠券已经过期");
				return obj;
			}
			UserVo userVo = apiUserMapper.queryObject(userId);
			if (userVo == null || couponVo.getPoint() > userVo.getPoints()) {
				obj.put("errno", 1);
				obj.put("errmsg", "您的积分不足");
				return obj;
			}
			UserCouponVo userCouponVo = new UserCouponVo();
			userCouponVo.setAdd_time(new Date());
			userCouponVo.setCoupon_id(Integer.parseInt(couponId));
			userCouponVo.setCoupon_number(CharUtil.getRandomString(12));
			userCouponVo.setUser_id(userVo.getUserId());
			apiUserCouponMapper.save(userCouponVo);
			userVo.setPoints(userVo.getPoints() - couponVo.getPoint());
			apiUserMapper.update(userVo);
		}
		return obj;
	}
}
