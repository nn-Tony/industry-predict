package com.zbj.alg.industry.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

/***
 * compute industry tags frequency`
 */
public class IndLibraryFrequency {
	public static void main(String argv[]) throws IOException{
		
		String indDicPath = "E:/OntologyModel/IndTagLibraryV1";
		String corpusPath = "E:/OntologyModel/IndCorpusEnterprice";
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/OntologyModel/IndTagLibraryV1.fre"), "utf-8"), true);
		
		Map<String, Map<String, Map<String, Integer>>> indMap = loadInitialMap(indDicPath);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(corpusPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length == 2){
				String cat =seg[0];
				String cont = seg[1];
				if(!indMap.containsKey(cat)){
					System.out.println(cat);
					continue;
				}
				for(Entry<String, Map<String, Integer>> attr:indMap.get(cat).entrySet()){
					for(Entry<String, Integer> mm:attr.getValue().entrySet()){
						if(cont.contains(mm.getKey())){
							String tag = mm.getKey();
							indMap.get(cat).get(attr.getKey()).put(tag, indMap.get(cat).get(attr.getKey()).get(tag)+1);
						}
					}
				}
			}
		}
		br.close();
		
		//output industry library
		int count = 0;
		for(Entry<String, Map<String, Map<String, Integer>>> cat:indMap.entrySet()){
			count++;
			for(Entry<String, Map<String, Integer>> attr:cat.getValue().entrySet()){
				pw.print(count+"\t"+cat.getKey()+"\t"+attr.getKey()+"\t");
				Map<String, Integer> wordMap = attr.getValue();
				List<Map.Entry<String, Integer>> wordMapSort = null;
		        if (!wordMap.isEmpty()) {
		        	wordMapSort = new ArrayList<Map.Entry<String, Integer>>(wordMap.entrySet());
					Collections.sort(wordMapSort,new Comparator<Map.Entry<String, Integer>>() {
										public int compare(Map.Entry<String, Integer> map1,
												Map.Entry<String, Integer> map2) {
											return map2.getValue().compareTo(map1.getValue());
										}
									});
		        }
		        
		        for(Entry<String, Integer> w:wordMapSort){
		        	pw.print(w.getKey()+"/"+w.getValue()+"\t");
		        }
		        pw.print("\n");
			}
		}
		pw.close();
	}

	static Map<String, Map<String, Map<String, Integer>>> loadInitialMap(
			String path) throws IOException {
		
		Map<String,Map<String,Map<String,Integer>>> indMap = new HashMap<String,Map<String,Map<String,Integer>>>();
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >= 4){
				String cat = seg[1];
				String attr = seg[2];
				for(int i=3;i<seg.length;i++){
					String tag = seg[i].split("/")[0];
					if(indMap.containsKey(cat)){
						if(indMap.get(cat).containsKey(attr)){
							if(indMap.get(cat).get(attr).containsKey(tag)){
								indMap.get(cat).get(attr).put(tag, indMap.get(cat).get(attr).get(tag)+1);
							}else{
								indMap.get(cat).get(attr).put(tag, 1);
							}
						}else{
							Map<String,Integer> map = new HashMap<String,Integer>();
							map.put(tag, 1);
							indMap.get(cat).put(attr, map);
						}
					}else{
						Map<String,Integer> map = new HashMap<String,Integer>();
						map.put(tag, 1);
						Map<String,Map<String,Integer>> map2 = new HashMap<String,Map<String,Integer>>();
						map2.put(attr, map);
						indMap.put(cat, map2);
					}
				}
			}
		}
		br.close();
		
		return indMap;
	}
}
