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
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import com.zbj.alg.industry.utils.Sort;

/***
 * output structured
 */
public class IndLibraryReconstrcuct {

	public static void main(String argv[]) throws IOException{
		String path = "E:/tr_project_one/project/IndustryTagLibrary/IndTagLibrary";
		
		Map<String,Map<String,Map<String,Integer>>> indMap = new TreeMap<String,Map<String,Map<String,Integer>>>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(path+".re"), "utf-8"), true);
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(path+"行业类目"), "utf-8"), true);
		
		String line = null;
		Set<String> set = new HashSet<String>();
		int num = 0;
		int num1 = 0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >= 4){
				
//				System.out.println(num++);
				String cat = seg[1];
				String attr = seg[2];
				for(int i=3;i<seg.length;i++){
					if(seg[1].split("#")[0].equals("餐饮")){
						num++;
					}
					if(!(set.contains(seg[i].split("/")[0]))){
						set.add(seg[i].split("/")[0]);
						num1++;
					}
					Integer value = 0;
					String tag = seg[i].split("/")[0];
					value = Integer.parseInt(seg[i].split("/")[1]);
					System.out.println(cat+"\t"+tag+"\t"+seg[i].split("/")[1]);
					
					if(indMap.containsKey(cat)){
						if(indMap.get(cat).containsKey(attr)){
							if(indMap.get(cat).get(attr).containsKey(tag)){
								indMap.get(cat).get(attr).put(tag, indMap.get(cat).get(attr).get(tag)+value);
							}else{
								indMap.get(cat).get(attr).put(tag, value);
							}
						}else{
							Map<String,Integer> map = new HashMap<String,Integer>();
							map.put(tag, value);
							indMap.get(cat).put(attr, map);
						}
					}else{
						Map<String,Integer> map = new HashMap<String,Integer>();
						map.put(tag, value);
						Map<String,Map<String,Integer>> map2 = new HashMap<String,Map<String,Integer>>();
						map2.put(attr, map);
						indMap.put(cat, map2);
					}
				}
			}
		}
		br.close();
		System.out.println(num1);
		System.out.println(num);
		Set<String> ind_one = new TreeSet<String>();
		int count = 0;
		for(Entry<String, Map<String, Map<String, Integer>>> cat:indMap.entrySet()){
			count++;
			if(!(ind_one.contains(cat.getKey().split("#")[0]))){
				ind_one.add(cat.getKey().split("#")[0]);
			}
			pw1.println(cat.getKey());
			//1
			if(cat.getValue().containsKey("机构名称")){
				Map<String, Integer> attrMap = cat.getValue().get("机构名称");
				pw.print(count+"\t"+cat.getKey()+"\t"+"机构名称"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %4 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"机构名称"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
			//2
			if(cat.getValue().containsKey("机构类型")){
				Map<String, Integer> attrMap = cat.getValue().get("机构类型");
				pw.print(count+"\t"+cat.getKey()+"\t"+"机构类型"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %5 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"机构类型"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
			//3
			if(cat.getValue().containsKey("行业")){
				Map<String, Integer> attrMap = cat.getValue().get("行业");
				pw.print(count+"\t"+cat.getKey()+"\t"+"行业"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %10 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"行业"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
			//4
			if(cat.getValue().containsKey("主营")){
				Map<String, Integer> attrMap = cat.getValue().get("主营");
				pw.print(count+"\t"+cat.getKey()+"\t"+"主营"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %10 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"主营"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");

				}
				pw.print("\n");
			}
			//5
			if(cat.getValue().containsKey("产品")){
				Map<String, Integer> attrMap = cat.getValue().get("产品");
				pw.print(count+"\t"+cat.getKey()+"\t"+"产品"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %10 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"产品"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
			//5
			if(cat.getValue().containsKey("职位")){
				Map<String, Integer> attrMap = cat.getValue().get("职位");
				pw.print(count+"\t"+cat.getKey()+"\t"+"职位"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %10 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"职位"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
			//6
			if(cat.getValue().containsKey("热门")){
				Map<String, Integer> attrMap = cat.getValue().get("热门");
				pw.print(count+"\t"+cat.getKey()+"\t"+"热门"+"\t");
				List<Entry<String, Integer>> tagMap = Sort.sortIntMap(attrMap);
				int number = 1;
				for(Entry<String, Integer> tag:tagMap){
					if(number++ %6 == 0){
						pw.print("\n");
						pw.print(count+"\t"+cat.getKey()+"\t"+"热门"+"\t");
					}
					pw.print(tag.getKey()+"/"+tag.getValue()+"\t");
				}
				pw.print("\n");
			}
		}
		pw1.println();
		for(String s:ind_one){
			pw1.println(s);
		}
		pw.close();
		pw1.close();
	}

	private static int filterTag(String tag, String attr, String value) {
		int flag = 0;
		
		if(attr.equals("热门") 
				&& Double.parseDouble(value) == 0.0
				&& tag.length() <=2){
			flag = 1;
		}
		
		if(tag.equals("公司")||tag.equals("企业")||tag.equals("上传")
				|| tag.equals("稿件")||tag.equals("包装")||tag.equals("商标")
				|| tag.equals("http")||tag.equals("外观")||tag.equals("房")
				|| tag.equals("群内")||tag.equals("发送")||tag.equals("短信")
				|| tag.equals("解绑")||tag.equals("项目")||tag.equals("vi设计")
				|| tag.equals("议室")||tag.equals("集团")||tag.equals("类目")
				|| tag.equals("人员")||tag.equals("开发")||tag.equals("技术")
				|| tag.equals("效果")||tag.equals("国际")||tag.equals("方案")
				|| tag.equals("编辑")||tag.equals("平台")||tag.equals("产品")
				
				|| tag.equals("可以")||tag.equals("拟订")||tag.equals("恭喜")
				|| tag.equals("区域")||tag.equals("网站")||tag.equals("信息")
				|| tag.equals("行业")||tag.equals("手机")||tag.equals("商品")){
			flag = 1;
		}
		
		return flag;
	}

}
