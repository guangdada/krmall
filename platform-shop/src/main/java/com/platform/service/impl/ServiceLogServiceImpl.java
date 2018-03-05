package com.platform.service.impl;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

import com.platform.dao.ServiceLogDao;
import com.platform.entity.ServiceLogEntity;
import com.platform.service.ServiceLogService;

/**
 * Service实现类
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2018-03-04 20:41:53
 */
@Service("serviceLogService")
public class ServiceLogServiceImpl implements ServiceLogService {
    @Autowired
    private ServiceLogDao serviceLogDao;

    @Override
    public ServiceLogEntity queryObject(Integer id) {
        return serviceLogDao.queryObject(id);
    }

    @Override
    public List<ServiceLogEntity> queryList(Map<String, Object> map) {
        return serviceLogDao.queryList(map);
    }

    @Override
    public int queryTotal(Map<String, Object> map) {
        return serviceLogDao.queryTotal(map);
    }

    @Override
    public int save(ServiceLogEntity serviceLog) {
        return serviceLogDao.save(serviceLog);
    }

    @Override
    public int update(ServiceLogEntity serviceLog) {
        return serviceLogDao.update(serviceLog);
    }

    @Override
    public int delete(Integer id) {
        return serviceLogDao.delete(id);
    }

    @Override
    public int deleteBatch(Integer[]ids) {
        return serviceLogDao.deleteBatch(ids);
    }
}
