package cn.hfbin.seckill.Lock;

import java.util.concurrent.TimeUnit;

import org.springframework.amqp.rabbit.connection.CachingConnectionFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.boot.CommandLineRunner;


public class Runner implements CommandLineRunner {

	
	private final RabbitTemplate rabbitTemplate;
    private final Receiver receiver;
   
    
  
	
    public Runner(Receiver receiver, RabbitTemplate rabbitTemplate) {
        this.receiver = receiver;
       
        
        CachingConnectionFactory connectionFactory = new CachingConnectionFactory("127.0.0.1",5672);
        
        connectionFactory.setUsername("jiafan");
        connectionFactory.setPassword("123456");
        connectionFactory.setVirtualHost("/");
        connectionFactory.setPublisherConfirms(true);
        this.rabbitTemplate =  new RabbitTemplate(connectionFactory);
    }

	
	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		String message = "";
		
		for (int i = 0; i < args.length; i++) {
			 message = args[0];
		}
		
        rabbitTemplate.convertAndSend("Seckill_boot_exchange","SeckillRabbitMQ", message);
        receiver.getLatch().await(10000,TimeUnit.MILLISECONDS);

	}

}
