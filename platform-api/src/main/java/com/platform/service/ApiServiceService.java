package com.platform.service;

import java.util.Date;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.dao.ApiServiceLogMapper;
import com.platform.dao.ApiServiceMapper;
import com.platform.entity.ServiceLogVo;
import com.platform.entity.ServiceVo;


@Service
public class ApiServiceService {
    @Autowired
    private ApiServiceMapper apiServiceDao;
    @Autowired
    private ApiServiceLogMapper apiServiceLogDao;


    public ServiceVo queryObject(Integer id) {
        return apiServiceDao.queryObject(id);
    }


    public List<ServiceVo> queryList(Map<String, Object> map) {
        return apiServiceDao.queryList(map);
    }


    public int queryTotal(Map<String, Object> map) {
        return apiServiceDao.queryTotal(map);
    }


    public void save(ServiceVo service) {
        apiServiceDao.save(service);
    }
    
    
	public void visit(ServiceVo service, Integer userId) {
		apiServiceDao.updateVisit(service.getId(), userId);
		
		ServiceLogVo vo = new ServiceLogVo();
		vo.setCreateTime(new Date());
		vo.setServiceId(service.getId());
		vo.setUserId(userId);
		apiServiceLogDao.save(vo);
		
	}


    public void update(ServiceVo service) {
        apiServiceDao.update(service);
    }


    public void delete(Integer id) {
        apiServiceDao.delete(id);
    }


    public void deleteBatch(Integer[] ids) {
        apiServiceDao.deleteBatch(ids);
    }

}
