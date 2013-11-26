package mobitnt.util;

import java.io.IOException;
import java.util.List;  
  
import org.apache.http.HttpResponse;  
import org.apache.http.HttpStatus;  
import org.apache.http.NameValuePair;  
import org.apache.http.client.HttpClient;  
import org.apache.http.client.entity.UrlEncodedFormEntity;  
import org.apache.http.client.methods.HttpGet;  
import org.apache.http.client.methods.HttpPost;  
import org.apache.http.impl.client.DefaultHttpClient;  
import org.apache.http.params.BasicHttpParams;  
import org.apache.http.params.HttpConnectionParams;  
import org.apache.http.params.HttpParams;  

import android.util.Log;
  
 

/*��Ҫ��Ҫ����:

1.    ʹ��POST��ʽʱ�����ݲ�������ʹ��NameValuePair����
2.    ʹ��GET��ʽʱ��ͨ��URL���ݲ�����ע��д��

3.      ͨ��setEntity����������HTTP����

4.      ͨ��DefaultHttpClient �� execute��������ȡHttpResponse
5. ͨ��getEntity()��Response�л�ȡ����*/
  
public class HttpRequestHelper {  
    /** 
     *Post���� 
     */  
    public void doPost(String url , List<NameValuePair> nameValuePairs){  
        //�½�HttpClient����    
        HttpClient httpclient = new DefaultHttpClient();  
        //����POST����  
        HttpPost httppost = new HttpPost(url);  
        try {  
//          //ʹ��PSOT��ʽ��������NameValuePair���鴫�ݲ���  
//          List<NameValuePair> nameValuePairs = new ArrayList<NameValuePair>();  
//          nameValuePairs.add(new BasicNameValuePair("id", "12345"));  
//          nameValuePairs.add(new BasicNameValuePair("stringdata","hps is Cool!"));  
            httppost.setEntity(new UrlEncodedFormEntity(nameValuePairs));  
            HttpResponse response = httpclient.execute(httppost);
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                Log.i("POST", "Bad Request!");  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
    }  
      
    /** 
     *Get���� 
     */  
    public void doGet(String url){  
        HttpParams httpParams = new BasicHttpParams();  
        HttpConnectionParams.setConnectionTimeout(httpParams,30000);    
        HttpConnectionParams.setSoTimeout(httpParams, 30000);    
              
        HttpClient httpClient = new DefaultHttpClient(httpParams);  
        // GET  
        HttpGet httpGet = new HttpGet(url);
        try {  
            HttpResponse response = httpClient.execute(httpGet);
            //String content = EntityUtils.toString(response.getEntity());
            if (response.getStatusLine().getStatusCode() != HttpStatus.SC_OK){
                Log.i("GET", "Bad Request!");  
            }  
        } catch (IOException e) {  
            e.printStackTrace();  
        }  
  
    }  
}  

