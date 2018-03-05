package com.platform.entity;

import java.io.Serializable;
import java.util.Date;


/**
 * 签到规则
 * @author Administrator
 *
 */
public class SignVo implements Serializable {
	
	private static final long serialVersionUID = 1L;

    /**
     * id
     */
	private Long id;
    /**
     * 连续签到次数，连续签到的第15天，同时符合3和15的这2个规则，所以粉丝可获得1+5=6个积分
     */
	private Integer times;
    /**
     * 奖励积分
     */
	private Integer score;
    /**
     * 勾选“一人仅领一次”选项，将会排斥前两个原则，即：不会再按照整数倍触发奖励，无法同时触发多次奖励
     */
	private Boolean once;
    /**
     * 创建时间
     */
	private Date createTime;
    /**
     * 修改时间
     */
	private Date updateTime;
    /**
     * 删除状态
     */
	private Integer status;


	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Integer getTimes() {
		return times;
	}

	public void setTimes(Integer times) {
		this.times = times;
	}

	public Integer getScore() {
		return score;
	}

	public void setScore(Integer score) {
		this.score = score;
	}

	public Boolean isOnce() {
		return once;
	}

	public void setOnce(Boolean once) {
		this.once = once;
	}

	public Date getCreateTime() {
		return createTime;
	}

	public void setCreateTime(Date createTime) {
		this.createTime = createTime;
	}

	public Date getUpdateTime() {
		return updateTime;
	}

	public void setUpdateTime(Date updateTime) {
		this.updateTime = updateTime;
	}

	public Integer isStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	@Override
	public String toString() {
		return "Sign{" +
			"id=" + id +
			", times=" + times +
			", score=" + score +
			", once=" + once +
			", createTime=" + createTime +
			", updateTime=" + updateTime +
			", status=" + status +
			"}";
	}
}
