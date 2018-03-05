package com.platform.dao;

import org.apache.ibatis.annotations.Param;

import com.platform.entity.ServiceVo;

/**
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2017-08-11 09:14:25
 */
public interface ApiServiceMapper extends BaseDao<ServiceVo> {
	int updateVisit(@Param("id") int id,@Param("userId") int userId);
}
