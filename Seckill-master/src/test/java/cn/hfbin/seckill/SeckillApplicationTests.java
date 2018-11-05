package cn.hfbin.seckill;

import cn.hfbin.seckill.bo.GoodsBo;
import cn.hfbin.seckill.dao.GoodsMapper;
import cn.hfbin.seckill.entity.Goods;
import cn.hfbin.seckill.entity.User;
import cn.hfbin.seckill.service.SeckillGoodsService;
import cn.hfbin.seckill.service.SeckillOrderService;
import cn.hfbin.seckill.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Slf4j
@RunWith(SpringRunner.class)
@SpringBootTest
public class SeckillApplicationTests {

	@Autowired
	DataSource dataSource;

	@Autowired
	GoodsMapper goodsMapper;
	
	@Autowired
    SeckillGoodsService seckillGoodsService;

    @Autowired
    SeckillOrderService seckillOrderService;
    
    @Autowired
    UserService userService;

	//@Test
	public void contextLoads() throws SQLException {
		//org.apache.tomcat.jdbc.pool.DataSource
		System.out.println(dataSource.getClass());
		Connection connection = dataSource.getConnection();
		System.out.println(connection);
		connection.close();

	}

	@Test
	public void test01(){
		
		User user = userService.checkPhone("18077200000");
		
		GoodsBo goods = seckillGoodsService.getseckillGoodsBoByGoodsId(1l);
		
		Map<String, Object> map = new HashMap<String, Object>();
		 
		map.put("user", user);
		map.put("goods", goods);
		 
		String message=	JSONObject.toJSONString(map);
		
		JSONObject itemJSONObj = JSONObject.parseObject(message);
		
		Map<String, Object> itemMap = JSONObject.toJavaObject(itemJSONObj, Map.class);
		
		User user01 =	JSON.parseObject(JSON.parseObject(message).getString("user"), User.class);

		GoodsBo goods01 =JSON.parseObject(JSON.parseObject(message).getString("goods"), GoodsBo.class);
		
		System.out.println("y用户名："+user.getUserName()+"  用户电话:"+user.getPhone());
		
	}
}
