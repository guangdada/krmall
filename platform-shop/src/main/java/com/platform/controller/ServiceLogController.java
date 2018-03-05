package com.platform.controller;

import java.util.List;
import java.util.Map;

import org.apache.shiro.authz.annotation.RequiresPermissions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.platform.entity.ServiceLogEntity;
import com.platform.service.ServiceLogService;
import com.platform.utils.PageUtils;
import com.platform.utils.Query;
import com.platform.utils.R;

/**
 * Controller
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2018-03-04 20:41:53
 */
@Controller
@RequestMapping("servicelog")
public class ServiceLogController {
    @Autowired
    private ServiceLogService serviceLogService;

    /**
     * 查看列表
     */
    @RequestMapping("/list")
    @RequiresPermissions("servicelog:list")
    @ResponseBody
    public R list(@RequestParam Map<String, Object> params) {
        //查询列表数据
        Query query = new Query(params);

        List<ServiceLogEntity> serviceLogList = serviceLogService.queryList(query);
        int total = serviceLogService.queryTotal(query);

        PageUtils pageUtil = new PageUtils(serviceLogList, total, query.getLimit(), query.getPage());

        return R.ok().put("page", pageUtil);
    }

    /**
     * 查看信息
     */
    @RequestMapping("/info/{id}")
    @RequiresPermissions("servicelog:info")
    @ResponseBody
    public R info(@PathVariable("id") Integer id) {
        ServiceLogEntity serviceLog = serviceLogService.queryObject(id);

        return R.ok().put("serviceLog", serviceLog);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    @RequiresPermissions("servicelog:save")
    @ResponseBody
    public R save(@RequestBody ServiceLogEntity serviceLog) {
        serviceLogService.save(serviceLog);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    @RequiresPermissions("servicelog:update")
    @ResponseBody
    public R update(@RequestBody ServiceLogEntity serviceLog) {
        serviceLogService.update(serviceLog);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    @RequiresPermissions("servicelog:delete")
    @ResponseBody
    public R delete(@RequestBody Integer[]ids) {
        serviceLogService.deleteBatch(ids);

        return R.ok();
    }

    /**
     * 查看所有列表
     */
    @RequestMapping("/queryAll")
    @ResponseBody
    public R queryAll(@RequestParam Map<String, Object> params) {

        List<ServiceLogEntity> list = serviceLogService.queryList(params);

        return R.ok().put("list", list);
    }
}
