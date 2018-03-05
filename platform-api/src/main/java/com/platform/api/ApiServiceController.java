package com.platform.api;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.alibaba.fastjson.JSONObject;
import com.platform.annotation.IgnoreAuth;
import com.platform.annotation.LoginUser;
import com.platform.entity.ServiceLogVo;
import com.platform.entity.ServiceVo;
import com.platform.entity.UserVo;
import com.platform.service.ApiServiceService;
import com.platform.util.ApiBaseAction;
import com.platform.util.ApiPageUtils;
import com.platform.utils.Query;

/**
 * 作者: @author Harmon <br>
 * 时间: 2017-08-11 08:32<br>
 * 描述: ApiIndexController <br>
 */
@RestController
@RequestMapping("/api/service")
public class ApiServiceController extends ApiBaseAction {
    @Autowired
    private ApiServiceService apiServiceServie;

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
		param.put("sidx", "is_top,sortnum,id");
		param.put("order", "desc");
		param.put("user_id", loginUser.getUserId());
		Query query = new Query(param);

		List<ServiceVo> serviceVos = apiServiceServie.queryList(query);
		int total = apiServiceServie.queryTotal(query);
		ApiPageUtils pageUtil = new ApiPageUtils(serviceVos, total, query.getLimit(), query.getPage());
		return toResponsSuccess(pageUtil);
	}

    /**
     * 获取生活服务的详情
     */
    @IgnoreAuth
    @RequestMapping("detail")
    public Object detail(Integer id) {
        ServiceVo entity = apiServiceServie.queryObject(id);
        return toResponsSuccess(entity);
    }

    /**
     * 删除指定的生活服务
     */
    @RequestMapping("visit")
	public Object visit(@LoginUser UserVo loginUser) {
		JSONObject jsonParam = this.getJsonRequest();
		Integer id = jsonParam.getInteger("id");

		ServiceVo serviceVo = apiServiceServie.queryObject(id);
		apiServiceServie.visit(serviceVo, loginUser.getUserId().intValue());
		return toResponsSuccess("");
	}
}