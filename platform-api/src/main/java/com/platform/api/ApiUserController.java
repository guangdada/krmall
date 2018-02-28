package com.platform.api;

import java.util.HashMap;
import java.util.Map;
import java.util.Random;

import org.apache.commons.lang.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.SmsLogVo;
import com.platform.entity.UserVo;
import com.platform.service.ApiUserService;
import com.platform.util.ApiBaseAction;
import com.platform.utils.CharUtil;
import com.platform.utils.cache.CacheUtil;
import com.platform.utils.sms.MsgUtils;
import com.platform.utils.sms.SmsAlidayu;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/user")
public class ApiUserController extends ApiBaseAction {
	@Autowired
	private ApiUserService userService;

	/**
	 */
	@IgnoreAuth
	@RequestMapping("info")
	public Object info(@LoginUser UserVo loginUser, String mobile) {
		Map param = new HashMap();
		param.put("mobile", mobile);
		UserVo user = userService.queryByMobile(mobile);
		user.setPassword("");
		return user;
	}

	/**
	 * 保存用户头像
	 */
	@RequestMapping("saveAvatar")
	public Object saveAvatar(@LoginUser UserVo loginUser, String avatar) {
		return null;
	}

	/**
	 * 发送短信
	 */
	@RequestMapping("smscode")
	public Object smscode(@LoginUser UserVo loginUser) {
		JSONObject jsonParams = getJsonRequest();
		String phone = jsonParams.getString("phone");
		// 一分钟之内不能重复发送短信
		SmsLogVo smsLogVo = userService.querySmsCodeByUserId(loginUser.getUserId());
		if (null != smsLogVo && (System.currentTimeMillis() / 1000 - smsLogVo.getLog_date()) < 1 * 60) {
			return toResponsFail("短信已发送");
		}
		// 生成验证码
		String sms_code = CharUtil.getRandomNum(4);
		String smsTemplateCode = "SMS_94340007";
		String msgContent = "您的验证码是：" + sms_code + "，请在页面中提交验证码完成验证。";
		String param = "{\"code\":\"" + sms_code + "\"}";
		// 发送短信
		// String rpt = "0";
		String rpt = SmsAlidayu.sendTplShortMessage(param, phone, smsTemplateCode);
		if (rpt == null || rpt.equals("0") == false) {
			return toResponsFail("短信发送失败");
		} else {
			smsLogVo = new SmsLogVo();
			smsLogVo.setLog_date(System.currentTimeMillis() / 1000);
			smsLogVo.setUser_id(loginUser.getUserId());
			smsLogVo.setPhone(phone);
			smsLogVo.setSms_code(sms_code);
			smsLogVo.setSms_text(msgContent);
			userService.saveSmsCodeLog(smsLogVo);
			return toResponsSuccess("短信发送成功");
		}
	}

	/**
	 * 获取当前会员等级
	 *
	 * @param loginUser
	 * @return
	 */
	@RequestMapping("getUserLevel")
	public Object getUserLevel(@LoginUser UserVo loginUser) {
		String userLevel = userService.getUserLevel(loginUser);
		return toResponsSuccess(userLevel);
	}

	/**
	 * 判断验证码是否正确
	 * 
	 * @param loginUser
	 * @param mobile
	 * @param randomCode
	 * @return
	 */
	@RequestMapping(value = { "validtecode" }, method = { RequestMethod.POST })
	public Object validtecode(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = this.getJsonRequest();
		String mobile = jsonParam.getString("mobile");
		String randomCode = jsonParam.getString("randomCode");
		if (StringUtils.isBlank(mobile)) {
			return toResponsFail("请输入手机号码");
		}
		if (StringUtils.isBlank(randomCode)) {
			return toResponsFail("请输入验证码");
		}
		String imgcode = CacheUtil.get(CacheUtil.registerImgCode, loginUser.getWeixin_openid());
		if (imgcode == null || !imgcode.equals(randomCode)) {
			return toResponsFail("验证码输入错误");
		}

		// 一分钟之内不能重复发送短信
		SmsLogVo smsLogVo = userService.querySmsCodeByUserId(loginUser.getUserId());
		if (null != smsLogVo && (System.currentTimeMillis() / 1000 - smsLogVo.getLog_date()) < 1 * 60) {
			return toResponsFail("短信已发送");
		}
				
		// 发送短信
		int max = 999999;
		int min = 100000;
		Random random = new Random();
		String msgcode = random.nextInt(max) % (max - min + 1) + min + "";
		String msgContent = "【小区驿站】您的验证码是：" + msgcode;
		// 调用发送短信接口
		MsgUtils.request(mobile, msgContent);
		
		smsLogVo = new SmsLogVo();
		smsLogVo.setLog_date(System.currentTimeMillis() / 1000);
		smsLogVo.setUser_id(loginUser.getUserId());
		smsLogVo.setPhone(mobile);
		smsLogVo.setSms_code(msgcode);
		smsLogVo.setSms_text(msgContent);
		userService.saveSmsCodeLog(smsLogVo);

		//CacheUtil.put(CacheUtil.registerMsgCode, loginUser.getWeixin_openid(), msgcode);
		// 移除验证码
		CacheUtil.remove(CacheUtil.registerImgCode, loginUser.getWeixin_openid());
		return toResponsSuccess("短信发送成功");
	}

	/**
	 * 绑定手机号
	 * 
	 * @param loginUser
	 * @param mobile
	 *            手机号
	 * @param mobileCode
	 *            短信码
	 * @param referralCode
	 *            邀请码
	 * @return
	 */
	@RequestMapping(value = { "bindphone" }, method = { RequestMethod.POST })
	public Object bindphone(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = this.getJsonRequest();
		String mobile = jsonParam.getString("mobile");
		String mobileCode = jsonParam.getString("mobileCode");
		String referralCode = jsonParam.getString("referralCode");
		if (StringUtils.isBlank(mobile)) {
			return toResponsFail("请输入手机号");
		}
		if (StringUtils.isBlank(mobileCode)) {
			return toResponsFail("请输入短信码");
		}
		if (StringUtils.isBlank(referralCode)) {
			return toResponsFail("请输入邀请码");
		}

		SmsLogVo smsLogVo = userService.querySmsCodeByUserId(loginUser.getUserId());
		if (null == smsLogVo || !smsLogVo.getPhone().equals(mobile) || !smsLogVo.getSms_code().equals(mobileCode)) {
			return toResponsFail("短信码输入错误");
		}
		/*String msgcode = CacheUtil.get(CacheUtil.registerMsgCode, loginUser.getWeixin_openid());
		if (msgcode == null || !msgcode.equals(mobileCode)) {
			return toResponsFail("短信码输入错误");
		}*/
		
		// 判断手机是否已经被绑定过
		UserVo user = userService.queryByMobile(mobile);
		if(user != null){
			return toResponsFail("该手机已经被占用了！");
		}else{
			// 修改手机号
			loginUser.setMobile(mobile);
			userService.update(loginUser);
		}
		
		// 移除短信码
		//CacheUtil.remove(CacheUtil.registerMsgCode, loginUser.getWeixin_openid());
		
		return toResponsSuccess("绑定成功！");
	}

}