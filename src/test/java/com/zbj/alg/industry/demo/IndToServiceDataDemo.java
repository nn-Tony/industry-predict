package com.zbj.alg.industry.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.sql.Date;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.Map.Entry;

import com.zbj.alg.industry.model.server.IndCat;
import com.zbj.alg.industry.model.server.IndTagGroup;
import com.zbj.alg.industry.model.server.IndTaging;
import com.zbj.alg.industry.model.server.IndTagingEnhancer;
import com.zbj.alg.industry.model.server.IndText;
import com.zbj.alg.industry.utils.Sort;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;

public class IndToServiceDataDemo {
	public static void main(String argv[]) throws IOException {
		String modelPath = "D:/Users/zbj/git/modelResource/";
		IndTaging text2IndTag = new IndTagingEnhancer(modelPath);

		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath+"task.csv")), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndOneAndOrder_2017_5月~8月.ind"), "utf-8"), true);
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndTwoAndOrder_2017_5月~8月.ind"), "utf-8"), true);
		PrintWriter pw2 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndTwoAndMonthOrder_2017_5月~8月.ind"), "utf-8"), true);
		@SuppressWarnings("resource")
		PrintWriter pw3 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"Industry_name_new.ind"), "utf-8"), true);
		
		Map<String,Integer> ind1Count = new HashMap<String,Integer>();
		Map<String,Integer> ind2Count = new HashMap<String,Integer>();
		Map<String,Map<String,Integer>> ind1AndService = new TreeMap<String,Map<String,Integer>>();
		Map<String,Map<String,Integer>> ind2AndService = new TreeMap<String,Map<String,Integer>>();
		Map<String,Map<Integer,Map<String,Integer>>> ind2MonthService = new TreeMap<String,Map<Integer,Map<String,Integer>>>();
        String line = null;
        int num = 0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length < 10){
				continue;
			}
			String dateTime = seg[1];
			if(dateTime.length() != 10 || seg[0].length() >=10 || !seg[0].matches("[0-9]+")
					|| !seg[3].matches("[0-9]+") || !seg[5].matches("[0-9]+")){
				continue;
			}
			if(getYear(dateTime) != 2017){
				continue;
			}
			int month = getMonth(dateTime);
			if(month>12){
				continue;
			}
			String service = seg[2]+"#"+seg[4]+"#"+seg[6];
			System.out.println(num++);
			String corpus = seg[8];
			if(corpus.equals("")
					||corpus.equals("null")
					||corpus == null){
				continue;
			}
        	IndText text = new IndText();
    		text.setSingleCorpus(corpus);
    		text.setType(1);
    		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 1, 5, "bayes");
    		if(Tags.isEmpty()){
    			continue;
    		}else{
    			for(IndTagGroup tag:Tags){
					IndCat cat = tag.getIndcat();
					String indCat1 = cat.getIndCatOne_name();
					String indCat2 = cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name();
					
					//写入indSeasonService信息
					if(ind1Count.containsKey(indCat1)){
						int indNum1 = ind1Count.get(indCat1);
						ind1Count.put(indCat1, ++indNum1);
					}else{
						ind1Count.put(indCat1, 1);
					}
					
					if(ind2Count.containsKey(indCat2)){
						int indNum2 = ind2Count.get(indCat2);
						ind2Count.put(indCat2, ++indNum2);
					}else{
						ind2Count.put(indCat2, 1);
					}
					
					
					//生成ind1AndService
					if(ind1AndService.containsKey(indCat1)){
						if(ind1AndService.get(indCat1).containsKey(service)){
							int count = ind1AndService.get(indCat1).get(service);
							ind1AndService.get(indCat1).put(service, ++count);
						}else{
							ind1AndService.get(indCat1).put(service, 1);
						}
					}else{
						Map<String,Integer> serviceCount = new TreeMap<String,Integer>();
						serviceCount.put(service, 1);
						ind1AndService.put(indCat1, serviceCount);
					}
					
					//生成ind2AndService
					if(ind2AndService.containsKey(indCat2)){
						if(ind2AndService.get(indCat2).containsKey(service)){
							int count = ind2AndService.get(indCat2).get(service);
							ind2AndService.get(indCat2).put(service, ++count);
						}else{
							ind2AndService.get(indCat2).put(service, 1);
						}
					}else{
						Map<String,Integer> serviceCount = new TreeMap<String,Integer>();
						serviceCount.put(service, 1);
						ind2AndService.put(indCat2, serviceCount);
					}
					
					//生成ind2MonthService
					if(ind2MonthService.containsKey(indCat2)){
						if(ind2MonthService.get(indCat2).containsKey(month)){
							if(ind2MonthService.get(indCat2).get(month).containsKey(service)){
								int count = ind2MonthService.get(indCat2).get(month).get(service);
								ind2MonthService.get(indCat2).get(month).put(service, ++count);
							}else{
								ind2MonthService.get(indCat2).get(month).put(service, 1);
							}
						}else{
							Map<String,Integer> serviceCount = new TreeMap<String,Integer>();
							serviceCount.put(service, 1);
							ind2MonthService.get(indCat2).put(month, serviceCount);
						}
					}else{
						Map<String,Integer> serviceCount = new TreeMap<String,Integer>();
						Map<Integer,Map<String,Integer>> monthService = new TreeMap<Integer,Map<String,Integer>>();
						serviceCount.put(service, 1);
						monthService.put(month, serviceCount);
						ind2MonthService.put(indCat2, monthService);
					}
    			}
    		}
		}
		br.close();
		
		//ind2MonthService排序打印输出
		List<Entry<String, Integer>> ind1CountSort = Sort.sortIntMap(ind1Count);
		pw3.println("一级行业名");
		for(Entry<String,Integer> i1s:ind1CountSort){
			pw3.println(i1s.getKey());
		}
		pw3.println();
		List<Entry<String, Integer>> ind2CountSort = Sort.sortIntMap(ind2Count);
		pw3.println("一级#二级行业名");
		for(Entry<String,Integer> i2s:ind2CountSort){
			pw3.println(i2s.getKey());
		}
		pw3.println();

		for(Entry<String,Integer> i2c:ind2CountSort){
			if(ind2AndService.containsKey(i2c.getKey())){
				pw1.println("行业："+i2c.getKey()+"\t订单数量："+i2c.getValue());
				List<Entry<String, Integer>> iSort = Sort.sortIntMap(ind2AndService.get(i2c.getKey()));
				for(Entry<String,Integer> ii:iSort){
					pw1.print(ii.getKey()+"("+ii.getValue()+")"+"\t");
				}
				pw1.println();
			}
			
			if(ind2MonthService.containsKey(i2c.getKey())){
				pw2.println("行业："+i2c.getKey()+"\t订单数量："+i2c.getValue());
				
				for(Entry<Integer,Map<String,Integer>> ss:ind2MonthService.get(i2c.getKey()).entrySet()){
					Map<String,Integer> sTemp = ind2MonthService.get(i2c.getKey()).get(ss.getKey());
					List<Entry<String, Integer>> sSort = Sort.sortIntMap(sTemp);
					int num2 = 0;
					for(Entry<String,Integer> s:sSort){
						num2 += s.getValue();
						pw2.print(s.getKey()+"("+s.getValue()+")"+"\t");
					}
					pw2.println();
					pw2.println("***"+ss.getKey()+"_Month***"+"\t"+"***"+num2+"***");
				}
				pw2.println();
			}	
		}
		pw1.close();
		pw2.close();
		
		//ind1AndService排序打印输出
		for(Entry<String,Integer> i1c:ind1CountSort){
			if(ind1AndService.containsKey(i1c.getKey())){
				pw.println("行业："+i1c.getKey()+"\t订单数量："+i1c.getValue());
				List<Entry<String, Integer>> iSort = Sort.sortIntMap(ind1AndService.get(i1c.getKey()));
				for(Entry<String,Integer> ii:iSort){
					pw.print(ii.getKey()+"("+ii.getValue()+")"+"\t");
				}
				pw.println();
			}
		}
		pw.close();
	}
	
	//取得当前季度
	public static String getThisSeasonTime(int month){
		String quarter = "";
		if(month>=1&&month<=3){     
            quarter="Spring";     
        }     
        if(month>=4&&month<=6){     
            quarter="Summer";       
        }     
        if(month>=7&&month<=9){     
            quarter = "Autumn";     
        }     
        if(month>=10&&month<=12){     
            quarter = "Winter";     
        }
		return quarter;
	}
	
	//取得当前时间
	public static Date getDateTime(String dateTime){
		Date strDate = java.sql.Date.valueOf(dateTime);
		return strDate;
	}
	
	//取得当前月份
	public static int getMonth(String dateTime){
       Calendar c=Calendar.getInstance();
       c.setTime(getDateTime(dateTime));
       @SuppressWarnings("static-access")
       int month=c.get(c.MONTH)+1;
       return month;
    }
	
	//取得当前年份
	public static int getYear(String dateTime)
    {
       Calendar c=Calendar.getInstance();
       c.setTime(getDateTime(dateTime));
       @SuppressWarnings("static-access")
       int year=c.get(c.YEAR);
       return year;
    }
}
