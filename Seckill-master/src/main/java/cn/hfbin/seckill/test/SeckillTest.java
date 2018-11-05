package cn.hfbin.seckill.test;

import static org.mockito.Matchers.intThat;

import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Semaphore;

import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

import cn.hfbin.seckill.service.HTTPService;
import redis.clients.jedis.Jedis;

public class SeckillTest {
	
	
	
	// 请求总数
    public static int clientTotal = 300;

    // 同时并发执行的线程数
    public static int threadTotal = 20;
    
    public static String str = "";
    
    
    public static void main(String[] args) throws URISyntaxException {
    	  final String watchkeys = "watchkeys";
    	  final String watchkey01 = "find";
    	  
    	System.out.println("执行开始!");
    	
    	ExecutorService executorService = Executors.newCachedThreadPool();
    	
    	
        //信号量，此处用于控制并发的线程数
       // final Semaphore semaphore = new Semaphore(threadTotal);
        //闭锁，可实现计数器递减
        final CountDownLatch countDownLatch = new CountDownLatch(clientTotal);
        for (int i = 0; i < clientTotal ; i++) {
        	
            executorService.execute(() -> {
                try {
                	//执行此方法用于获取执行许可，当总计未释放的许可数不超过200时，
                	//允许通行，否则线程阻塞等待，直到获取到许可。
                    //semaphore.acquire();
                    String goodsId = 1+"";
                 	DecimalFormat df=new DecimalFormat("0000");
                 	int num =	(int) (1+Math.random()*(clientTotal));
                 		String str2=df.format(Integer.parseInt(num+""));
                 	
                 		String PhoneNumber = "1807720"+str2;
                 	
                 		String result = HTTPService.loginOfGet(goodsId,PhoneNumber);
                  //  String result = HTTPService.loginOf();
                    //释放许可
                 //   semaphore.release();
                } catch (Exception e) {
                   
                    e.printStackTrace();
                }
                //闭锁减一
               countDownLatch.countDown();
            });
        }
       
        executorService.shutdown();
    	
	}
	
	

}
