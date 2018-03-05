package com.platform.entity;

import java.io.Serializable;
import java.util.Date;

import com.fasterxml.jackson.annotation.JsonFormat;

/**
 * 积分记录
 * @author Administrator
 *
 */
public class PointTradeVo implements Serializable {
	private static final long serialVersionUID = 1L;

	/**
	 * 编号
	 */
	private Long id;
	/**
	 * 会员id
	 */
	private Long userId;
	/**
	 * 签到规则id
	 */
	private Long signId;
	
	/**
	 * 兑换的优惠券
	 */
	private Integer couponId;
	
	/**
	 * 0支出/1收入
	 */
	private Boolean inOut;
	/**
	 * 交易积分
	 */
	private Integer point;
	/**
	 * 备注
	 */
	private String tag;
	/**
	 * 创建时间
	 */
	private Date createTime;
	/**
	 * 修改时间
	 */
	private Date updateTime;
	/**
	 * 状态
	 */
	private Integer status;

	private Integer tradeType;

	public Long getUserId() {
		return userId;
	}

	public void setUserId(Long userId) {
		this.userId = userId;
	}

	public Long getSignId() {
		return signId;
	}

	public void setSignId(Long signId) {
		this.signId = signId;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public Boolean isInOut() {
		return inOut;
	}

	public void setInOut(Boolean inOut) {
		this.inOut = inOut;
	}

	public Integer getPoint() {
		return point;
	}

	public void setPoint(Integer point) {
		this.point = point;
	}

	public String getTag() {
		return tag;
	}

	public void setTag(String tag) {
		this.tag = tag;
	}

	@JsonFormat(pattern="yyyy-MM-dd HH:mm:ss",timezone = "GMT+8")
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

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}

	public Integer getTradeType() {
		return tradeType;
	}

	public void setTradeType(Integer tradeType) {
		this.tradeType = tradeType;
	}
	
	public Integer getCouponId() {
		return couponId;
	}

	public void setCouponId(Integer couponId) {
		this.couponId = couponId;
	}

	@Override
	public String toString() {
		return "PointTrade{" + "id=" + id + ", inOut=" + inOut + ", point=" + point + ", tag=" + tag + ", createTime="
				+ createTime + ", updateTime=" + updateTime + ", status=" + status + "}";
	}
}
