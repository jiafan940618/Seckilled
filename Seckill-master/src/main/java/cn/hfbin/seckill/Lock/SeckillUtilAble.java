package cn.hfbin.seckill.Lock;

import static org.mockito.Matchers.intThat;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.context.ApplicationContext;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.stereotype.Component;

import com.alibaba.fastjson.JSONObject;

import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.interceptor.SpringContextUtil;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.result.Result;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;
@Component
public class SeckillUtilAble implements Runnable{
  
    public String ERRMSG;
    public OrderInfo orderInfo;
    public GoodsBo goods;
    
    public String result;
    
    public int num= 0;
    

	
	String watchkeys = "watchkeys";// 监视keys
    Jedis jedis = new Jedis("127.0.0.1", 6379,10000);
   
    User user;
    long goodsId;
    public SeckillUtilAble() { }
    
    public SeckillUtilAble(User user,long goodsId) {
        this.user=user;
        this.goodsId=goodsId;
        jedis.auth("123456");
    }
    @Override
    public void run() {
        try {
            jedis.watch(watchkeys);// watchkeys
 
            String val = jedis.get(watchkeys);
            int valint = Integer.valueOf(val);
            
           
            
            if (valint>=1) {
            
                 Transaction tx = jedis.multi();// 开启事务
               // tx.incr("watchkeys");
                tx.incrBy("watchkeys", -1);
 
                List<Object> list = tx.exec();// 提交事务，如果此时watchkeys被改动了，则返回null
                 
                if (list == null ||list.size()==0) {
 
                    String failuserifo = "fail"+user.getPhone();
                    String failinfo="用户：" + failuserifo + "商品争抢失败，抢购失败,此时的key为："+val;
                    System.out.println(failinfo);
                    /* 抢购失败业务逻辑 */
                    jedis.setnx(failuserifo, failinfo);
                } else {
                    for(Object succ : list){
                    	System.out.println("输出的用户id:"+user.getId()+" goodsId:"+goodsId);
            
                    	SeckillOrderService seckillOrderService =(SeckillOrderService)	SpringContextUtil.getBean("seckillOrderService");
                    	SeckillGoodsService seckillGoodsService =(SeckillGoodsService)	SpringContextUtil.getBean("seckillGoodsService");
                    	GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
                    	SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
                        if(order != null) {
                        	ERRMSG= CodeMsg.REPEATE_MIAOSHA.getMsg();
                        	result = "miaosha_fail";
                        	String succuserifo ="succ"+succ.toString() +user.getPhone();
                        	String succinfo="用户：" + succuserifo + "已经购买过了";
                        	 System.out.println(succinfo);
                        	 jedis.setnx(succuserifo, succinfo);
                        }else{
                        //减库存 下订单 写入秒杀订单
                        	SpringContextUtil.getBean("seckillOrderService");
	                      //  OrderInfo orderInfo = seckillOrderService.insert(user, goods);
                        	
                        	Map<String, Object> map = new HashMap<String, Object>();
							map.put("user", user);
							map.put("goods", goods);
							String message=	JSONObject.toJSONString(map);
							
							Receiver  receiver = new Receiver();
							RabbitTemplate rabbitTemplate = new RabbitTemplate();
							
							Runner runner = new Runner(receiver,rabbitTemplate);
							runner.run(message);
							
							
							OrderInfo orderInfo = seckillOrderService.newGet(user, goods);
                        	
	                        this.orderInfo = orderInfo;
	                        this.goods= goods;
	                        result= "order_detail";
                    	
                         String succuserifo ="succ"+succ.toString() +user.getPhone();
                         String succinfo="用户：" + succuserifo + "抢购成功，当前抢购成功人数:"
                                 + (num+1);
                         System.out.println(succinfo);
                         /* 抢购成功业务逻辑 */
                         jedis.setnx(succuserifo, succinfo);
                         num++;
                    }
                   }   
                }
 
            } else {
                String failuserifo ="kcfail" +  user.getPhone();
                String failinfo1="用户：" + failuserifo + "商品被抢购完毕，抢购失败";
                System.out.println(failinfo1);
                jedis.setnx(failuserifo, failinfo1);
                // Thread.sleep(500);
                return;
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
            jedis.close();
        }
 
    }


}
