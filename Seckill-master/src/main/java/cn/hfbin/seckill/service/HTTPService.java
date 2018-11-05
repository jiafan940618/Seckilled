package cn.hfbin.seckill.service;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class HTTPService {
	
	
	public static String loginOfGet(String goodsId, String PhoneNumber) {
        HttpURLConnection conn = null;
 //http://localhost:8080/seckill/seckill/execution?userPhone=13613027433
      //  String data = "userPhone="+ PhoneNumber;
        String data = "goodsId=1&PhoneNumber="+ PhoneNumber;
       // String url = "http://localhost:8080/seckill/seckill/execution?"+ data;
        String url = "http://localhost:8888/seckill/seckillTest?"+ data;
      //  String url = "http://localhost:8888/seckill/find?"+ data;
        try {
            // 利用string url构建URL对象
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();
 
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
 
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
 
                InputStream is = conn.getInputStream();
                String state = getStringFromInputStream(is);
                return state;
            } else {
//                Log.i(TAG, "访问失败" + responseCode);
                System.out.print("访问失败");
 
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
 
            if (conn != null) {
                conn.disconnect();
            }
        }
 
        return null;
    }
	
	
	public static String loginOf() {
        HttpURLConnection conn = null;
 
      
        String url = "http://localhost:8081/test";
        try {
            // 利用string url构建URL对象
            URL mURL = new URL(url);
            conn = (HttpURLConnection) mURL.openConnection();
 
            conn.setRequestMethod("GET");
            conn.setReadTimeout(5000);
            conn.setConnectTimeout(10000);
 
            int responseCode = conn.getResponseCode();
            if (responseCode == 200) {
 
                InputStream is = conn.getInputStream();
                String state = getStringFromInputStream(is);
                return state;
            } else {
//                Log.i(TAG, "访问失败" + responseCode);
                System.out.print("访问失败");
 
            }
 
        } catch (Exception e) {
            e.printStackTrace();
        } finally {
 
            if (conn != null) {
                conn.disconnect();
            }
        }
 
        return null;
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
