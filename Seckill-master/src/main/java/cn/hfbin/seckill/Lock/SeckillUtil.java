package cn.hfbin.seckill.Lock;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.alibaba.fastjson.JSONObject;


import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.Transaction;

@Component
public class SeckillUtil {
	@Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillOrderService seckillOrderService;
    
    @Autowired
    UserService userService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    Receiver  receiver;
    
    
	
    public  int NUM_KUO = 0;
    
    public   String NAME;
    
    public OrderInfo ORDER_INFO;
    
    public GoodsBo GOODS_BO;
    
	/*
	 * 初始化顾客开始抢商品
	 */
    @Transactional
	public  void initClient(User user,Long goodsId) throws InterruptedException {
		ExecutorService cachedThreadPool = Executors.newCachedThreadPool();
		
		//Thread thread =new Thread(new ClientThread(user,goodsId));

		new Thread(new ClientThread(user,goodsId)).start();
		
		cachedThreadPool.shutdown();
		
		while(true){  
	            if(cachedThreadPool.isTerminated()){  
	                System.out.println("所有的线程都结束了！");  
	                break;  
	            }  
	            try {
	            	Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}    
	        }  
		
	}
	
	/**
	 * 初始化商品个数
	 */
	
	


	
	class ClientThread implements Runnable {
		
		Jedis jedis = null;
		String key = "stock_count";// 商品主键
		String clientList = "goods_name";//// 抢购到商品的顾客列表主键
		String clientName;
		Long goodsId;
		User user;
	 
		public ClientThread(User user,Long goodsId) {
			clientName = "编号=" + user.getUserName();
			this.user = user;
			this.goodsId = goodsId;
		}
	 
		public void run() {
			try {
				Thread.sleep((int)(Math.random()*5000));// 随机睡眠一下
			} catch (InterruptedException e1) {
			}
			
			 GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
			
			while (true) {
				System.out.println("顾客:" + clientName + "开始抢商品");
				jedis = RedisUtil.getInstance().getJedis();
				try {
					jedis.watch(key);
					int  prdNum = 0;
					if(null != jedis.get(key)){
						prdNum = Integer.parseInt(jedis.get(key));// 当前商品个数
					}
					
					
					if (prdNum > 0) {
						Transaction transaction = jedis.multi();
						transaction.set(key, String.valueOf(prdNum - 1));
						List<Object> result = transaction.exec();
						if (result == null || result.isEmpty()) {
							System.out.println("悲剧了，顾客:" + clientName + "没有抢到商品");// 可能是watch-key被外部修改，或者是数据操作被驳回
						
							NAME = "miaosha_fail";
						
						} else {
							jedis.sadd(clientList, clientName);// 抢到商品记录一下
							/** 向消息队列传递消息*/
							/*Map<String, Object> map = new HashMap<String, Object>();
							map.put("user", user);
							map.put("goods", goods);
							String message=	JSONObject.toJSONString(map);
							
							Runner runner = new Runner(receiver,rabbitTemplate);
							runner.run(message);
							
							
							OrderInfo orderInfo = seckillOrderService.newGet(user, goods);*/
							
							
							OrderInfo orderInfo =  seckillOrderService.insert(user, goods);
							
							 ORDER_INFO = orderInfo;
							 GOODS_BO = goods;
							System.out.println("好高兴，顾客:" + clientName + "抢到商品");
							NAME ="order_detail";
							break;
						}
					} else {
						System.out.println("悲剧了，库存为0，顾客:" + clientName + "没有抢到商品");
							NAME = "miaosha_fail";
						break;
					}
				} catch (Exception e) {
					e.printStackTrace();
				} finally {
					jedis.unwatch();
					RedisUtil.returnResource(jedis);
				}
	 
			}
		}
	}
	
	
	
}


