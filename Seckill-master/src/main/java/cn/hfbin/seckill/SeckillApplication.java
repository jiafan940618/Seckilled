package cn.hfbin.seckill;

import cn.hfbin.seckill.Lock.Receiver;
import cn.hfbin.seckill.Lock.RedisUtil;
import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.interceptor.SpringContextUtil;
import cn.hfbin.seckill.service.SeckillGoodsService;
import redis.clients.jedis.Jedis;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.BindingBuilder;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.amqp.rabbit.listener.SimpleMessageListenerContainer;
import org.springframework.amqp.rabbit.listener.adapter.MessageListenerAdapter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.support.SpringBootServletInitializer;
import org.springframework.context.annotation.Bean;


@SpringBootApplication
@MapperScan("cn.hfbin.seckill.dao")
public class SeckillApplication extends SpringBootServletInitializer {
	
	
	
	final static String queueName = "SeckillRabbitMQ";

    @Bean
    Queue queue() {
        return new Queue(queueName, false);
    }

    @Bean
    TopicExchange exchange() {
        return new TopicExchange("Seckill_boot_exchange");
    }

    @Bean
    Binding binding(Queue queue, TopicExchange exchange) {
        return BindingBuilder.bind(queue).to(exchange).with(queueName);
    }

    @Bean
    SimpleMessageListenerContainer container(ConnectionFactory connectionFactory,
            MessageListenerAdapter listenerAdapter) {
        SimpleMessageListenerContainer container = new SimpleMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);
        container.setQueueNames(queueName);
        container.setMessageListener(listenerAdapter);
        return container;
    }

    //消息接收监听器
    @Bean
    MessageListenerAdapter listenerAdapter(Receiver receiver) {
    	// receiver接收方法为receiveMessage
        return new MessageListenerAdapter(receiver, "receiveMessage");
    }
	

	public static void main(String[] args) {
		
		
		 final Jedis jedis = new Jedis("127.0.0.1", 6379,10000);
	        jedis.auth("123456");
	        jedis.set("watchkeys", "20");//设置起始的抢购数
	        
	        RedisUtil redisUtil =	RedisUtil.getInstance();
			
			redisUtil.set("stock_count", "9");
			int prdNum = Integer.parseInt(redisUtil.get("stock_count"));// 商品个数
			
			String key = "stock_count";
			String clientList = "goods_name";// 抢购到商品的顾客列表
			Jedis jedi = RedisUtil.getInstance().getJedis();
	 
			if (jedi.exists(key)) {
				jedi.del(key);
			}
			
			if (jedi.exists(clientList)) {
				jedi.del(clientList);
			}
	 
			jedi.set(key, String.valueOf(prdNum));// 初始化
			RedisUtil.returnResource(jedi);
		
		SpringApplication.run(SeckillApplication.class, args);
	}


}
