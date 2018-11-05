package cn.hfbin.seckill.dao;

import cn.hfbin.seckill.entity.OrderInfo;

public interface OrdeInfoMapper {
    int deleteByPrimaryKey(Long id);

    int insert(OrderInfo record);

    int insertSelective(OrderInfo record);

    OrderInfo selectByPrimaryKey(OrderInfo record);

    int updateByPrimaryKeySelective(OrderInfo record);

    int updateByPrimaryKey(OrderInfo record);
}