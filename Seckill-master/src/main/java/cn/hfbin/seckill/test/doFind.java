package cn.hfbin.seckill.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

public class doFind {
	
	public static void main(String[] args) throws Exception {
		
	
	 String url = "http://localhost:8888/seckill/find?goodsId=1&PhoneNumber=18077200001";
    
         // 利用string url构建URL对象
         URL mURL = new URL(url);
         HttpURLConnection conn = (HttpURLConnection) mURL.openConnection();

         conn.setRequestMethod("GET");
         conn.setReadTimeout(5000);
         conn.setConnectTimeout(10000);

         int responseCode = conn.getResponseCode();
         if (responseCode == 200) {
        	 
        	 InputStream is = conn.getInputStream();
             String state = getStringFromInputStream(is);
        	 
        	 System.out.println("连接成功！返回结果："+state);
         }
	}
	
	 private static String getStringFromInputStream(InputStream is)
	            throws IOException {
	        ByteArrayOutputStream os = new ByteArrayOutputStream();
	        // 模板代码 必须熟练
	        byte[] buffer = new byte[1024];
	        int len = -1;
	        // 一定要写len=is.read(buffer)
	        // 如果while((is.read(buffer))!=-1)则无法将数据写入buffer中
	        while ((len = is.read(buffer)) != -1) {
	            os.write(buffer, 0, len);
	        }
	        is.close();
	        String state = os.toString();// 把流中的数据转换成字符串,采用的编码是utf-8(模拟器默认编码)
	        os.close();
	        return state;
	    }

}
