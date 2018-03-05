package com.platform.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 实体
 * 表名 nideshop_service
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2018-03-04 20:41:53
 */
public class ServiceEntity implements Serializable {
    private static final long serialVersionUID = 1L;

    //
    private Integer id;
    //名称
    private String name;
    //工作
    private String job;
    //电话
    private String phone;
    //备注
    private String remark;
    //头像
    private String imageUrl;
    //访问人数
    private Integer visits;
    //创建时间
    private Date createTime;
    //排序
    private Integer sortnum;
    //删除
    private Integer enabled;
    //置顶
    private Integer isTop;

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
     * 设置：名称
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * 获取：名称
     */
    public String getName() {
        return name;
    }
    /**
     * 设置：工作
     */
    public void setJob(String job) {
        this.job = job;
    }

    /**
     * 获取：工作
     */
    public String getJob() {
        return job;
    }
    /**
     * 设置：电话
     */
    public void setPhone(String phone) {
        this.phone = phone;
    }

    /**
     * 获取：电话
     */
    public String getPhone() {
        return phone;
    }
    /**
     * 设置：备注
     */
    public void setRemark(String remark) {
        this.remark = remark;
    }

    /**
     * 获取：备注
     */
    public String getRemark() {
        return remark;
    }
    /**
     * 设置：头像
     */
    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    /**
     * 获取：头像
     */
    public String getImageUrl() {
        return imageUrl;
    }
    /**
     * 设置：访问人数
     */
    public void setVisits(Integer visits) {
        this.visits = visits;
    }

    /**
     * 获取：访问人数
     */
    public Integer getVisits() {
        return visits;
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
    /**
     * 设置：排序
     */
    public void setSortnum(Integer sortnum) {
        this.sortnum = sortnum;
    }

    /**
     * 获取：排序
     */
    public Integer getSortnum() {
        return sortnum;
    }
    /**
     * 设置：删除
     */
    public void setEnabled(Integer enabled) {
        this.enabled = enabled;
    }

    /**
     * 获取：删除
     */
    public Integer getEnabled() {
        return enabled;
    }
    /**
     * 设置：置顶
     */
    public void setIsTop(Integer isTop) {
        this.isTop = isTop;
    }

    /**
     * 获取：置顶
     */
    public Integer getIsTop() {
        return isTop;
    }
}
