package com.zbj.alg.industry.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.nlpcn.commons.lang.tire.domain.Forest;
import org.nlpcn.commons.lang.tire.domain.Value;
import org.nlpcn.commons.lang.tire.library.Library;

import com.zbj.alg.seg.splitWord.IndexAnalysis;
import com.zbj.alg.seg.splitWord.ToAnalysis;

public class InitialDictionary {
	
	/**
	 * insert indstry dictionary
	 */
	public static Set<String> insertIndDic(Forest dic, String path) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		Set<String> set = new HashSet<String>();
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >=3){
				//insert category words
				String[] ind = seg[1].split("#");
				for(String d:ind){
					if(!set.contains(d)){
						set.add(d);
						Library.insertWord(dic, new Value(d.toLowerCase(), "IND", "10000"));
					}
				}
				
				if(seg[2].equals("机构名称")){
					continue;
				}
				
				for(int i=3;i<seg.length;i++){
					String tag = seg[i].split("/")[0];
					if(!set.contains(tag) 
							&& tag.length()>=2//2
							&& tag.length()<=12){//12
						set.add(tag);
						Library.insertWord(dic, new Value(tag.toLowerCase(), "IND", "10000"));
					}
				}
			}
		}
		br.close();
		return set;
	}

	/**
	 *  load industry tag library
	 */
	public static Map<String, Map<String, Map<String, Integer>>> loadIndLibrary(
			String path) throws IOException {
		Map<String,Map<String, Map<String, Integer>>> indTagMap = new HashMap<String,Map<String, Map<String, Integer>>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >= 4){
				String cat = seg[1];
				String attr = seg[2];
				if(attr.contains("主题")){
					continue;
				}
				for(int i=3;i<seg.length;i++){
					String tag = seg[i].split("/")[0];
					Integer value = Integer.parseInt(seg[i].split("/")[1]);
					if(indTagMap.containsKey(cat)){
						if(indTagMap.get(cat).containsKey(attr)){
							indTagMap.get(cat).get(attr).put(tag, value);
						}else{
							Map<String, Integer> tem = new HashMap<String, Integer>();
							tem.put(tag, value);
							indTagMap.get(cat).put(attr, tem);
						}
					}else{
						Map<String, Integer> tem = new HashMap<String, Integer>();
						tem.put(tag, value);
						Map<String,Map<String, Integer>> map = new HashMap<String,Map<String, Integer>>();
						map.put(attr, tem);
						indTagMap.put(cat, map);
					}
				}
			}
		}
		br.close();
		return indTagMap;
	}

	/**
	 * load service tag library
	 * @throws IOException 
	 * @throws NumberFormatException */
	public static void insertSerDic(Forest serdic, String serTagPath) throws IOException {
		BufferedReader ar = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(serTagPath)), "utf-8"));
		String Line = null;
		while ((Line = ar.readLine()) != null) {
			String[] seg = Line.split("\t");
			if (seg.length >= 4) {
				String[] seg_ser = seg[1].split("##")[0].split("&");
				for(String s:seg_ser){
					if(s.split("/").length ==1){
						Library.insertWord(serdic, new Value(s, "SER", String.valueOf(10000)));
					}
				}
				for(int i=3;i<seg.length;i++){
					if(seg[i].split("/").length ==2){
						Library.insertWord(serdic, new Value(seg[i].split("/")[0], "SER", String.valueOf(10000*Integer.parseInt(seg[i].split("/")[1]))));
					}
				}
			}
		}
		ar.close();		
		System.out.println(IndexAnalysis.parse("insert zbjdic over"));
	}
	
	/**
	 * load zbj tag library
	 * @throws IOException */
	public static void insertZBJDic(Forest zbjdic, String zbjdicPath) throws IOException {
		BufferedReader ar = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(zbjdicPath)), "utf-8"));
		String Line = null;
		while ((Line = ar.readLine()) != null) {
			String[] seg = Line.split("\t");
			if (seg.length == 3) {
				Library.insertWord(zbjdic, new Value(seg[0], seg[1], seg[2]));
			}
		}
		ar.close();		
		System.out.println(IndexAnalysis.parse("insert zbjdic over"));
	}
	
	/**
	 * load industry words which is similar to service 
	 */
	public static Set<String> loadServiceWords(String indWordsSiSer) throws IOException{
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(indWordsSiSer)), "utf-8"));
		String line = "";
		Set<String> set = new HashSet<String>();
		while((line = br.readLine()) != null){
			set.add(line);
		}
		br.close();
		return set;
	}
}
