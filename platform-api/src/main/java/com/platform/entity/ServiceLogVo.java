package com.platform.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 实体
 * 表名 nideshop_service_log
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2018-03-04 20:41:53
 */
public class ServiceLogVo implements Serializable {
    private static final long serialVersionUID = 1L;

    //
    private Integer id;
    //服务id
    private Integer serviceId;
    //工作
    private Integer userId;
    //创建时间
    private Date createTime;

    /**
     * 设置：
     */
    public void setId(Integer id) {
        this.id = id;
    }

    /**
     * 获取：
     */
    public Integer getId() {
        return id;
    }
    /**
     * 设置：服务id
     */
    public void setServiceId(Integer serviceId) {
        this.serviceId = serviceId;
    }

    /**
     * 获取：服务id
     */
    public Integer getServiceId() {
        return serviceId;
    }
    
    public Integer getUserId() {
		return userId;
	}

	public void setUserId(Integer userId) {
		this.userId = userId;
	}

	/**
     * 设置：创建时间
     */
    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    /**
     * 获取：创建时间
     */
    public Date getCreateTime() {
        return createTime;
    }
}
