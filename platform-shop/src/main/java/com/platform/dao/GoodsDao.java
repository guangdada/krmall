package com.platform.dao;

import org.apache.ibatis.annotations.Param;

import com.platform.entity.GoodsEntity;

/**
 * Dao
 *
 * @author lipengjun
 * @email 939961241@qq.com
 * @date 2017-08-21 21:19:49
 */
public interface GoodsDao extends BaseDao<GoodsEntity> {
    Integer queryMaxId();
    
    int updateSellVolume(@Param("number") int number,@Param("id") int id);
    
    int updateGoodsNumber(@Param("number") int number,@Param("id") int id);
}
