package com.platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.platform.dao.ServiceDao;
import com.platform.entity.ServiceEntity;
import com.platform.service.ServiceService;

/**
 * Service实现类
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2018-03-04 20:41:53
 */
@Service("serviceService")
public class ServiceServiceImpl implements ServiceService {
    @Autowired
    private ServiceDao serviceDao;

    @Override
    public ServiceEntity queryObject(Integer id) {
        return serviceDao.queryObject(id);
    }

    @Override
    public List<ServiceEntity> queryList(Map<String, Object> map) {
        return serviceDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return serviceDao.queryTotal(map);
    }

    @Override
    public int save(ServiceEntity service) {
        return serviceDao.save(service);
    }

    @Override
    public int update(ServiceEntity service) {
        return serviceDao.update(service);
    }

    @Override
    public int delete(Integer id) {
        return serviceDao.delete(id);
    }

    @Override
    public int deleteBatch(Integer[]ids) {
        return serviceDao.deleteBatch(ids);
    }
}
