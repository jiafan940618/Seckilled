package cn.hfbin.seckill.service.ipml;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.exception.RepeatKillException;
import cn.hfbin.seckill.exception.SeckillCloseException;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.SeckillService;
import cn.hfbin.seckill.service.UserService;
import cn.hfbin.seckill.util.RedisLock;

@Service("seckillService")
public class SeckillServiceImpl implements SeckillService{

	public OrderInfo orderInfo;
	
	public GoodsBo goods;
	
	@Autowired
	private RedisLock redisLock;
	
	@Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillOrderService seckillOrderService;
    
    @Autowired
    UserService userService;
	
	@Override
	@Transactional
	public String seckill(Long goodsId,User user) throws RuntimeException {
		String result = "";
		
	    //判断库存
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        int stock = goods.getStockCount();
        try {

	        if(order != null) {
	            System.out.println("已经买过了，不能重复购买!");
	           
	            result = "miaosha_fail";
	       
	        }else{
	        //判断是否已经秒杀到了
	        if(stock <= 0) {
			       	 System.out.println("库存没有了!");
			       	
			       	 result = "miaosha_fail";
		        }else{
		        	OrderInfo orderInfo = seckillOrderService.insert(user, goods);
		            this.orderInfo = orderInfo;
		            this.goods = goods;
		          
		            result = "order_detail";
		        }
	        }
        } catch (SeckillCloseException e1) {
			throw e1;
		} catch (RepeatKillException e2) {
			throw e2;
		} catch (Exception e) {
			throw e;
		}
        return result;
        
	}

}
