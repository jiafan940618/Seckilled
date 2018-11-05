package cn.hfbin.seckill.service.ipml;

import cn.hfbin.seckill.Lock.RedisUtil;
import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.dao.SeckillOrderMapper;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.service.OrderService;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import static org.mockito.Matchers.intThat;

import java.util.Date;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/16
 * Time: 16:47
 * Such description:
 */
@Service("seckillOrderService")
public class SeckillOrderServiceImpl implements SeckillOrderService {

    @Autowired
    SeckillOrderMapper seckillOrderMapper;

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    OrderService orderService;

    @Override
    public SeckillOrder getSeckillOrderByUserIdGoodsId(long userId, long goodsId) {
        return seckillOrderMapper.selectByUserIdAndGoodsId(userId , goodsId);
    }

    @Transactional
    @Override
    public OrderInfo insert(User user, GoodsBo goods) {
    	RedisUtil redisUtil =	RedisUtil.getInstance();

        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setCreateDate(new Date());
        orderInfo.setAddrId(0L);
        orderInfo.setGoodsCount(1);
        orderInfo.setGoodsId(goods.getId());
        orderInfo.setGoodsName(goods.getGoodsName());
        orderInfo.setGoodsPrice(goods.getSeckillPrice());
        orderInfo.setOrderChannel(1);
        orderInfo.setStatus(0);
        orderInfo.setUserId((long)user.getId());
        //添加信息进订单
        long orderId = orderService.addOrder(orderInfo);
        if(orderId > 0){
		        OrderInfo  orderInfo01 = orderService.selectByPrimaryKey(orderInfo);
		        
		        SeckillOrder seckillOrder = new SeckillOrder();
		        seckillOrder.setGoodsId(goods.getId());
		        seckillOrder.setOrderId(orderInfo01.getId());
		        seckillOrder.setUserId((long)user.getId());
		        //插入秒杀表
		       int count = seckillOrderMapper.insertSelective(seckillOrder);
		       
		       if(count > 0){
		    	   //秒杀商品库存减一
		    	   seckillGoodsService.reduceStock(goods.getId());
		       }
        }
        return orderInfo;
    }
    
    public OrderInfo newGet(User user, GoodsBo goods) {
    	 OrderInfo orderInfo = new OrderInfo();
         orderInfo.setCreateDate(new Date());
         orderInfo.setAddrId(0L);
         orderInfo.setGoodsCount(1);
         orderInfo.setGoodsId(goods.getId());
         orderInfo.setGoodsName(goods.getGoodsName());
         orderInfo.setGoodsPrice(goods.getSeckillPrice());
         orderInfo.setOrderChannel(1);
         orderInfo.setStatus(0);
         orderInfo.setUserId((long)user.getId());
         
         return orderInfo; 
    	
    }
    
}
