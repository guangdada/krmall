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

import com.platform.entity.ServiceEntity;
import com.platform.service.ServiceService;
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
@RequestMapping("service")
public class ServiceController {
    @Autowired
    private ServiceService serviceService;

    /**
     * 查看列表
     */
    @RequestMapping("/list")
    //@RequiresPermissions("service:list")
    @ResponseBody
    public R list(@RequestParam Map<String, Object> params) {
        //查询列表数据
        Query query = new Query(params);

        List<ServiceEntity> serviceList = serviceService.queryList(query);
        int total = serviceService.queryTotal(query);

        PageUtils pageUtil = new PageUtils(serviceList, total, query.getLimit(), query.getPage());

        return R.ok().put("page", pageUtil);
    }

    /**
     * 查看信息
     */
    @RequestMapping("/info/{id}")
    //@RequiresPermissions("service:info")
    @ResponseBody
    public R info(@PathVariable("id") Integer id) {
        ServiceEntity service = serviceService.queryObject(id);

        return R.ok().put("service", service);
    }

    /**
     * 保存
     */
    @RequestMapping("/save")
    //@RequiresPermissions("service:save")
    @ResponseBody
    public R save(@RequestBody ServiceEntity service) {
        serviceService.save(service);

        return R.ok();
    }

    /**
     * 修改
     */
    @RequestMapping("/update")
    //@RequiresPermissions("service:update")
    @ResponseBody
    public R update(@RequestBody ServiceEntity service) {
        serviceService.update(service);

        return R.ok();
    }

    /**
     * 删除
     */
    @RequestMapping("/delete")
    //@RequiresPermissions("service:delete")
    @ResponseBody
    public R delete(@RequestBody Integer[]ids) {
        serviceService.deleteBatch(ids);

        return R.ok();
    }

    /**
     * 查看所有列表
     */
    @RequestMapping("/queryAll")
    @ResponseBody
    public R queryAll(@RequestParam Map<String, Object> params) {

        List<ServiceEntity> list = serviceService.queryList(params);

        return R.ok().put("list", list);
    }
}
