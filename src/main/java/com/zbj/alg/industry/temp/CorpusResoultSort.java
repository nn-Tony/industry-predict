package com.zbj.alg.industry.temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import com.zbj.alg.industry.utils.Sort;

public class CorpusResoultSort {
	public static void main(String[] arg) throws IOException{
		String corpusPath = "D:/Users/zbj/git/modelResource/";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(corpusPath+"zl-ServiceTitle.ind")), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(corpusPath+"zl-ServiceTitleSort.ind"), "utf-8"), true);
		String line = null;
		Map<String,Map<String,String>> sort = new HashMap<String,Map<String,String>>();
		int i=1;
		while((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			System.out.println(i++);
			System.out.println(seg[4]);
			if(!(seg[0].matches("[0-9]+"))){
				continue;
			}
			if(sort.containsKey(seg[1])){
				if(!(sort.get(seg[1]).containsKey(seg[4].split(",")[2]))){
					sort.get(seg[1]).put(seg[4].split(",")[2], seg[5]);
				}
			}else{
				Map<String,String> sort_value = new HashMap<String,String>();
				sort_value.put(seg[4].split(",")[2], seg[5]);
				sort.put(seg[1], sort_value);
			}
		}
		br.close();
		Map<String,Integer> indNum = new HashMap<String,Integer>();
		for(Entry<String,Map<String,String>> s:sort.entrySet()){
			System.out.print(s.getKey()+":\t");
			Map<String,String> s_value = new HashMap<String,String>();
			s_value = s.getValue();
			int j=0;
			for(Entry<String,String> s_v:s_value.entrySet()){
				j++;
			}
			System.out.println(j);
			indNum.put(s.getKey(), j);
		}
		List<Entry<String, Integer>> indNumSort = null;
		indNumSort = Sort.sortIntMap(indNum);
		for(Entry<String,Integer> ind:indNumSort){
			Map<String,String> s_value1 = sort.get(ind.getKey());
			for(Entry<String,String> s_v1:s_value1.entrySet()){
				pw.println(ind.getValue()+"\t"+ind.getKey()+"\t"+s_v1.getKey()+"\t"+s_v1.getValue());
			}
		}
		pw.close();
	}
}
