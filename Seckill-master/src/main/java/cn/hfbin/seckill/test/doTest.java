package cn.hfbin.seckill.test;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.List;

import org.apache.http.client.utils.URIUtils;
import org.apache.http.client.utils.URLEncodedUtils;
import org.apache.http.message.BasicNameValuePair;

public class doTest {
	
	// 请求总数
    public static int clientTotal = 1000;

    // 同时并发执行的线程数
    public static int threadTotal = 50;
    
    public static void main(String[] args) throws URISyntaxException {
		
    	for (int i = 0; i < clientTotal; i++) {
			
    		 List params = new ArrayList(); 
    		 String goodsId = 1+"";
         	DecimalFormat df=new DecimalFormat("0000");
         	//int num =	(int) (1+Math.random()*(clientTotal));
         		String str2=df.format(Integer.parseInt(i+""));
         	
         		String PhoneNumber = "1807720"+str2;
         	
         		String result = loginOfGet(goodsId,PhoneNumber);
    		
         		System.out.println("输出的结果为:"+result);
		}
	}
    
    public static String loginOfGet(String goodsId, String PhoneNumber) {
        HttpURLConnection conn = null;
 
        String data = "goodsId=" + goodsId + "&PhoneNumber="+ PhoneNumber;
        String url = "http://localhost:8888/seckill/find?"+ data;
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
