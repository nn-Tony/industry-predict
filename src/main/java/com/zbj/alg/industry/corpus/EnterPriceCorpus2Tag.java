package com.zbj.alg.industry.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.splitWord.ToAnalysis;


public class EnterPriceCorpus2Tag {

	public static void main(String argv[]) throws IOException{
		String path = "E:/OntologyModel/IndCorpusEnterprice";
		
		Map<String, Map<String, Map<String, Integer>>> indMap = IndLibraryFrequency.loadInitialMap("E:/OntologyModel/IndTagLibraryV1");
		
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/OntologyModel/IndCorpusEnterprice.pre"), "utf-8"), true);
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/OntologyModel/IndCorpusEnterprice.word"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		Map<String,Map<String,Set<String>>> dataMap = new HashMap<String,Map<String,Set<String>>>();
		Map<String,Map<String,Set<String>>> tagMap = new HashMap<String,Map<String,Set<String>>>();
		Set<String> tag = new HashSet<String>();
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length == 2){
				String cat = seg[0];
				String cont = seg[1];
				String[] terms = cont.split("&");
				String name = terms[1];
				if(!name.contains("公司")){
					continue;
				}
				String corpus = terms[2];
				int flag =0;
				if(!indMap.containsKey(cat)){
					continue;
				}
				for(Entry<String, Map<String, Integer>> at:indMap.get(cat).entrySet()){
					if(at.getValue().containsKey(name)){
						flag=1;
					}
				}
				if(flag == 0){
					if(dataMap.containsKey(cat)){
						if(dataMap.get(cat).containsKey("机构名称")){
							dataMap.get(cat).get("机构名称").add(name);
						}
					}else{
						Set<String> set = new HashSet<String>();
						set.add(name);
						Map<String,Set<String>> map = new HashMap<String,Set<String>>();
						map.put("机构名称", set);
						dataMap.put(cat, map);
					}
				}
				
				//
				Result words = ToAnalysis.parse(cat+"\n"+corpus);
				StringBuffer sb = new StringBuffer();
				for(Term word:words){
					if((!word.getNatureStr().equals("n") && !word.getNatureStr().equals("v"))
							||word.getName().length()==1
							||!word.getNatureStr().matches("[a-z]*")){
						sb.append("\n");
					}else{
						sb.append(word.getName());
					}
				}
				for(String t:sb.toString().split("\n")){
					if(t.contains("许可") || t.contains("承担") || t.contains("暂定") 
							|| t.contains("方面") || t.contains("相关") || t.contains("包括") 
							|| t.contains("从事") || t.contains("委托") || t.contains("审批") 
							|| t.contains("附件") || t.contains("承接") || t.contains("进行") 
							|| t.contains("为主") || t.contains("业务") || t.contains("开展") 
							|| t.contains("领域") || t.contains("提供") || t.contains("领域") 
							|| t.contains("项目") || t.contains("认定") || t.contains("代表") 
							|| t.contains("期限") || t.contains("承办") || t.contains("国家") 
							|| t.contains("引进") || t.contains("介绍") || t.contains("允许") 
							|| t.contains("专业") || t.contains("用于") || t.contains("兼营") 
							|| t.contains("单位") || t.contains("参加") || t.contains("接受") 
							|| t.contains("范围") || t.contains("建议") || t.contains("承揽") 
							|| t.contains("专业") || t.contains("功效") || t.contains("除外") 
							|| t.contains("联系") || t.contains("公司") || t.contains("规格") 
							|| t.contains("联系") || t.contains("取得") || t.contains("全资") 
							|| t.contains("承包") || t.contains("企业") || t.contains("利用") ){
						continue;
					}
					if(t.length() > 8){
						System.out.println(t);
						continue;
					}
					int flag1 =0;
					if(!indMap.containsKey(cat)){
						continue;
					}
					for(Entry<String, Map<String, Integer>> at:indMap.get(cat).entrySet()){
						if(at.getValue().containsKey(t)){
							flag1=1;
						}
					}
					if(flag1==0){
						if(t.length()<2){
							continue;
						}
						String attr = "主营";
						if(t.length()==2){
							attr = "热门";
						}
						if(tagMap.containsKey(cat)){
							if(tagMap.get(cat).containsKey(attr)){
								tagMap.get(cat).get(attr).add(t);
							}else{
								Set<String> set = new HashSet<String>();
								set.add(t);
								tagMap.get(cat).put(attr, set);
							}
						}else{
							Set<String> set = new HashSet<String>();
							set.add(t);
							Map<String,Set<String>> map = new HashMap<String,Set<String>>();
							map.put(attr, set);
							tagMap.put(cat, map);
						}
					}
				}
			}
		}
		br.close();
		
		for(Entry<String, Map<String, Set<String>>> d:dataMap.entrySet()){
			for(Entry<String, Set<String>> dd:d.getValue().entrySet()){
				for(String ddd:dd.getValue()){
					pw.println(1+"\t"+d.getKey()+"\t"+dd.getKey()+"\t"+ddd+"/"+1);
				}
			}
		}
		for(Entry<String, Map<String, Set<String>>> d:tagMap.entrySet()){
			for(Entry<String, Set<String>> dd:d.getValue().entrySet()){
				for(String ddd:dd.getValue()){
					pw1.println(1+"\t"+d.getKey()+"\t"+dd.getKey()+"\t"+ddd+"/"+1);
				}
			}
		}
		pw.close();
		pw1.close();
	}
}
