package com.platform.service;

import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.platform.dao.ApiPointTradeMapper;
import com.platform.entity.PointTradeVo;

@Service
public class ApiPointTradeService {
	@Autowired
	private ApiPointTradeMapper apiPointTradeMapper;
	
	public List<PointTradeVo> queryList(Map<String, Object> map){
		return apiPointTradeMapper.queryList(map);
	}
	
	public int queryTotal(Map<String, Object> map) {
        return apiPointTradeMapper.queryTotal(map);
    }
	
}
