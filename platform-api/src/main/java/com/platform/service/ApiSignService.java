package com.platform.service;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;
import com.platform.dao.ApiPointTradeMapper;
import com.platform.dao.ApiSignLogMapper;
import com.platform.dao.ApiSignMapper;
import com.platform.dao.ApiUserMapper;
import com.platform.entity.PointTradeVo;
import com.platform.entity.SignLogVo;
import com.platform.entity.SignVo;
import com.platform.entity.UserVo;
import com.platform.utils.DateUtil;

@Service
public class ApiSignService {
	@Autowired
	private ApiSignMapper apiSignMapper;
	@Autowired
	private ApiSignLogMapper apiSignLogMapper;
	@Autowired
	private ApiPointTradeMapper apiPointTradeMapper;
	@Autowired
	private ApiUserMapper apiUserMapper;
	
	
	

	/**
	 * 保存签到
	 * @param loginUser
	 * @return
	 * @throws Exception
	 */
	@Transactional
	public Map<String, Object> submit(UserVo loginUser) throws Exception {
		Map<String, Object> resultObj = new HashMap<String, Object>();
		if (null == loginUser) {
			resultObj.put("errno", 1);
			resultObj.put("errmsg", "用户没有登录");
			return resultObj;
		}

		Long userId = loginUser.getUserId();
		// 当前时间
		Date nowDate = new Date();
		Date yesDate = DateUtil.getAfterDayDate(-1);
		String nowDateStr = DateUtil.getDay(new Date());
		String yesDateStr = DateUtil.getDay(yesDate);
		synchronized (userId.toString().intern()) {

			// 最后签到日期
			Date signDate = loginUser.getSignDate();
			// 获得会员最后签到日期
			// 如果最后签到日期为null，表示历史第一次签到，修改连续签到天数=1,修改最后签到日期为今天
			if (signDate == null) {
				loginUser.setSignDays(1);
			}
			String signDateStr = signDate == null ? "" : DateUtil.getDay(signDate);
			// 如果最后签到日期是今天，提示今天已经签到过， 不做后续处理
			if (nowDateStr.equals(signDateStr)) {
				resultObj.put("errno", 1);
				resultObj.put("errmsg", "今天已经签到过了");
				return resultObj;
			} else {
				// 如果最后签到日期不是今天，保存今天的签到记录
				SignLogVo signLog = new SignLogVo();
				signLog.setUserId(userId);
				apiSignLogMapper.save(signLog);

				// 如果最后签到日期是昨天，修改连续签到天数+1
				if (signDateStr.equals(yesDateStr)) {
					loginUser.setSignDays(loginUser.getSignDays() + 1);
				} else {// 如果最后签到日期不是昨天，修改连续签到天数=1
					loginUser.setSignDays(1);
				}

				// 修改签到信息
				loginUser.setSignDate(nowDate);
				apiUserMapper.update(loginUser);

				// 连续签到天数
				Integer signDays = loginUser.getSignDays();
				// 获得签到规则
				boolean getPoint = false;

				List<SignVo> signs = apiSignMapper.queryList(new HashMap<String, Object>());
				for (SignVo signVo : signs) {
					if (!signVo.isOnce()) {
						Integer times = signVo.getTimes();
						Integer score = signVo.getScore();
						// times 等于1表示不需要连续， 每天签都能得积分
						if (signDays % times == 0) {
							PointTradeVo pointTradeVo = new PointTradeVo();
							pointTradeVo.setUserId(userId);
							pointTradeVo.setSignId(signVo.getId());
							pointTradeVo.setInOut(true);
							pointTradeVo.setTag("签到");
							pointTradeVo.setPoint(score);
							int result = apiPointTradeMapper.save(pointTradeVo);
							if (result <= 0) {
								throw new Exception();
							}
							// 连续签到为1天的情况， 可以同其他规则一起生效，不管是不是设置了“仅领一次”
							if (times != 1) {
								getPoint = true;// 领取过后 ， “仅领一次”的规则就不处理了
							}
						}
					}
				}

				// 处理“仅领一次”的规则
				if (!getPoint) {
					for (SignVo signVo : signs) {
						if (signVo.isOnce()) {
							Integer times = signVo.getTimes();
							Integer score = signVo.getScore();
							if (signDays % times == 0) {
								PointTradeVo pointTradeVo = new PointTradeVo();
								pointTradeVo.setUserId(userId);
								pointTradeVo.setSignId(signVo.getId());
								pointTradeVo.setInOut(true);
								pointTradeVo.setTag("签到");
								pointTradeVo.setPoint(score);
								int result = apiPointTradeMapper.save(pointTradeVo);
								if (result <= 0) {
									throw new Exception();
								}
								break; // 获取一次便退出
							}
						}
					}
				}
			}

		}

		resultObj.put("errno", 0);
		resultObj.put("errmsg", "签到成功");
		return resultObj;
	}
}
