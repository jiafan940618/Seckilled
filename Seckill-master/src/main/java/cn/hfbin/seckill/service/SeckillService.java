package cn.hfbin.seckill.service;

import cn.hfbin.seckill.entity.User;

public interface SeckillService {

	public String seckill(Long goodsId,User user)throws RuntimeException;
	

}
