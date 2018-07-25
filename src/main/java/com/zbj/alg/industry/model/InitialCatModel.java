package com.zbj.alg.industry.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class InitialCatModel {

	public static Map<String, Double> loadWordMap(String path) throws NumberFormatException, IOException {
    	BufferedReader ar = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String Line = null;
		Map<String,Double> wordMap = new HashMap<String,Double>();
		while ((Line = ar.readLine()) != null){
			String[] seg = Line.split("##");
			if(seg.length != 2 ){
				continue;
			}
			wordMap.put(seg[0], Double.parseDouble(seg[1].replaceAll("#", "")));
		}
		ar.close();
		
		return wordMap;
	}

	public static Map<String, Double> loadCategoryMap(String path) throws NumberFormatException, IOException {
    	BufferedReader ar = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String Line = null;
		Map<String,Double> categoryMap = new HashMap<String,Double>();
		while ((Line = ar.readLine()) != null){
			String[] seg = Line.split("\t");
			if(seg.length == 2){
				categoryMap.put(seg[0], Double.parseDouble(seg[1]));
			}
		}
		ar.close();
		
		return categoryMap;
	}

	public static Map<String, Map<String, Double>> loadCategoryWordMap(
			String path) throws IOException {
    	BufferedReader ar = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String Line = null;
		Map<String,Map<String,Double>> categoryWordMap = new HashMap<String,Map<String,Double>>();
		while ((Line = ar.readLine()) != null){
			String[] seg = Line.split("\t");
			if(seg.length == 3){
				if(categoryWordMap.containsKey(seg[0])){
					categoryWordMap.get(seg[0]).put(seg[1], Double.parseDouble(seg[2]));
				}else{
					Map<String,Double> temp = new HashMap<String,Double>();
					temp.put(seg[1], Double.parseDouble(seg[2]));
					categoryWordMap.put(seg[0], temp);
				}
			}
		}
		ar.close();
		
		return categoryWordMap;
	}

}
