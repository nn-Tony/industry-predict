package com.zbj.alg.industry.corpus;  
  
import java.io.BufferedReader;  
import java.io.FileOutputStream;
import java.io.FileReader;  
import java.io.IOException;  
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
  
public class EnterpriceJSONData {  
  
    /** 
     * @param args 
     */  
    public static void main(String[] args) {  

        // 读取原始json文件并进行操作和输出  
        try {  
            BufferedReader br = new BufferedReader(new FileReader(  
                    "E:/OntologyModel/EnterpriseCorpus20170609"));// 读取原始json文件  
            PrintWriter pw = new PrintWriter(new OutputStreamWriter(
    				new FileOutputStream("E:/OntologyModel/company.list"), "utf-8"), true);
            String s = null;  
            while ((s = br.readLine()) != null) {  
                // System.out.println(s);  
                try {  
                    JSONObject dataJson = new JSONObject(s);// 创建一个包含原始json串的json对象  
                    Object companyName = dataJson.get("companyName");
                    Object address = dataJson.get("address");
                    Object tel = dataJson.getString("tel");
                    Object email = dataJson.getString("email");
                    JSONObject background = dataJson.getJSONObject("background");
                    JSONObject baseInfo = background.getJSONObject("baseInfo");
                    String registeredCapital = baseInfo.getString("registeredCapital");
                    String registeredTime = baseInfo.getString("registeredTime");
                    String companyType = baseInfo.getString("companyType");
                    String categoryType = baseInfo.getString("categoryType");
                    String businessScope = baseInfo.getString("businessScope");
                    
                    JSONObject businessCondition = dataJson.getJSONObject("businessCondition");
                    JSONArray employments = businessCondition.getJSONArray("employments");
                    StringBuffer sb = new StringBuffer();
                    for (int i = 0; i < employments.length(); i++){
                    	String title = employments.getJSONObject(i).getString("title");
                    	sb.append(title+"##");
                    	//System.out.println(title);
                    }
//                    pw.println(companyName+"\t"+address+"\t"+tel+"\t"+email+"\t"
//                    		+registeredCapital+"\t"+registeredTime+"\t"
//                    		+companyType+"\t"+categoryType+"\t"+businessScope+"\t"
//                    		+sb.toString());  
                    pw.println(categoryType+"#&#"+companyName+"#&#"+businessScope);  
                } catch (JSONException e) {  
                    e.printStackTrace();  
                }  
            }   
            br.close();  
            pw.close();
  
        } catch (IOException e) {   
            e.printStackTrace();  
        }  
  
    }  
  
}  
