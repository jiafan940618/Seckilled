package cn.hfbin.seckill.Lock;

import java.util.Map;
import java.util.concurrent.CountDownLatch;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;

//消息接受者
@Component
@RabbitListener(queues = "SeckillRabbitMQ")
public class Receiver {
	
	 @Autowired
	    SeckillGoodsService seckillGoodsService;

	    @Autowired
	    SeckillOrderService seckillOrderService;
	    
	    @Autowired
	    UserService userService;
	
	private CountDownLatch latch = new CountDownLatch(1);

	public void receiveMessage(String message) {
       // System.out.println("发送消息为: <" + message + ">");
        JSONObject itemJSONObj = JSONObject.parseObject(message);
		
	/*	Map<String, Object> itemMap = JSONObject.toJavaObject(itemJSONObj, Map.class);
	    
		long goodsId =   Integer.toUnsignedLong((int)itemMap.get("goodsId"));
		
		String PhoneNumber =(String)itemMap.get("PhoneNumber");
		
		 User user = userService.checkPhone(PhoneNumber);*/
		
		User user =	JSON.parseObject(JSON.parseObject(message).getString("user"), User.class);

		GoodsBo goods =JSON.parseObject(JSON.parseObject(message).getString("goods"), GoodsBo.class);
		
		 
		 seckillOrderService.insert(user, goods);
		/*GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            System.out.println("内存不足！");
          
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
           System.out.println("不能重复秒杀！");
           
        }else{
        
        	OrderInfo orderInfo = seckillOrderService.insert(user, goods);
        	System.out.println("秒杀成功！");
        }*/
        
		
        latch.countDown();
      
    }
	
	
	/*public String receiveMessage(String message) {
        System.out.println("Received <" + message + ">");
        latch.countDown();
		return message;
    }*/

    public CountDownLatch getLatch() {
        return latch;
    }
}