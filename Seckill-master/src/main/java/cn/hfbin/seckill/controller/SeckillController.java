package cn.hfbin.seckill.controller;

import cn.hfbin.seckill.MyRunnable;
import cn.hfbin.seckill.Lock.Receiver;
import cn.hfbin.seckill.Lock.Runner;
import cn.hfbin.seckill.Lock.SeckillUtil;
import cn.hfbin.seckill.Lock.SeckillUtilAble;
import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.entity.OrderInfo;
import cn.hfbin.seckill.entity.SeckillOrder;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.exception.RepeatKillException;
import cn.hfbin.seckill.exception.SeckillCloseException;
import cn.hfbin.seckill.redis.RedisService;
import cn.hfbin.seckill.redis.UserKey;
import cn.hfbin.seckill.result.CodeMsg;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.SeckillService;
import cn.hfbin.seckill.service.UserService;
import cn.hfbin.seckill.service.ipml.SeckillServiceImpl;
import cn.hfbin.seckill.util.CookieUtil;

import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.alibaba.fastjson.JSONObject;


import static org.hamcrest.CoreMatchers.nullValue;
import static org.mockito.Matchers.intThat;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;

/**
 * Created by: HuangFuBin
 * Date: 2018/7/15
 * Time: 23:55
 * Such description:
 */
@Controller
@RequestMapping("seckill")
public class SeckillController {
	 @Autowired
	SeckillUtil seckillUtil;

    @Autowired
    RedisService redisService;

    @Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillOrderService seckillOrderService;
    
    @Autowired
    UserService userService;
    
    @Autowired
    SeckillServiceImpl seckillService;
    @Autowired
    RabbitTemplate rabbitTemplate;
    @Autowired
    Receiver  receiver;
    
    ExecutorService executor = Executors.newFixedThreadPool(20);  //20个线程池并发数
    
    private CountDownLatch latch = new CountDownLatch(1);
	
	@Resource
    private RedisTemplate<String,Object> redisTemplate;

    @RequestMapping("/seckill")
    public String list(Model model,
                       @RequestParam("goodsId")long goodsId , @RequestParam("PhoneNumber") String PhoneNumber,HttpServletRequest request) {

        System.out.println("进入方法！");
    	
        String loginToken = CookieUtil.readLoginToken(request);
        
        //User user = redisService.get(UserKey.getByName, loginToken, User.class);

        User user = userService.checkPhone(PhoneNumber);
        
        model.addAttribute("user", user);
        if(user == null) {
            return "login";
        }
        //判断库存
        GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(goodsId);
        int stock = goods.getStockCount();
        if(stock <= 0) {
            model.addAttribute("errmsg", CodeMsg.MIAO_SHA_OVER.getMsg());
            return "miaosha_fail";
        }
        //判断是否已经秒杀到了
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        //减库存 下订单 写入秒杀订单
        OrderInfo orderInfo = seckillOrderService.insert(user, goods);
        model.addAttribute("orderInfo", orderInfo);
        model.addAttribute("goods", goods);
        return "order_detail";
    }
    //https://www.cnblogs.com/VitoYi/p/8726070.html
    //http://localhost:8888/seckill/seckill?goodsId=1&PhoneNumber=18077200000
    @RequestMapping("/find")
    public String newlist(Model model,
                       @RequestParam("goodsId")long goodsId, @RequestParam("PhoneNumber") String PhoneNumber,HttpServletRequest request) throws InterruptedException {
    	
    	System.out.println("进入方法！");
    	
        String loginToken = CookieUtil.readLoginToken(request);

        User user = userService.checkPhone(PhoneNumber);
        
        model.addAttribute("user", user);
        
        if(user == null) {
            return "login";
        }
        
        SeckillOrder order = seckillOrderService.getSeckillOrderByUserIdGoodsId(user.getId(), goodsId);
        if(order != null) {
        	System.out.println("不能重复秒杀哦！");
            model.addAttribute("errmsg", CodeMsg.REPEATE_MIAOSHA.getMsg());
            return "miaosha_fail";
        }
        seckillUtil.initClient(user, goodsId); 
        
      
        
        String result =  seckillUtil.NAME;
       
        //减库存 下订单 写入秒杀订单
     
         if(result.equals("order_detail") ){
        	 model.addAttribute("orderInfo", seckillUtil.ORDER_INFO);
             model.addAttribute("goods", seckillUtil.GOODS_BO);
         }
        return result;
    }
    //redis 乐观锁实现团购秒杀
    @RequestMapping("/seckillTest")
    @ResponseBody
    public String dofind(@RequestParam("goodsId")long goodsId,@RequestParam("PhoneNumber") String PhoneNumber){
    	
    	 User user = userService.checkPhone(PhoneNumber);
    	
    	// executor.execute(new SeckillUtilAble(user));
    	new Thread(new SeckillUtilAble(user,goodsId)).start(); 
		return String.valueOf(1);
    	
    }
    
}
