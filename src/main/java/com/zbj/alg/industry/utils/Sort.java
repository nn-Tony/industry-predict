package com.zbj.alg.industry.utils;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

public class Sort {

	public static List<Entry<String, Double>> sortMap(Map<String, Double> Map) {
		List<Map.Entry<String, Double>> SortMap = new ArrayList<Map.Entry<String, Double>>();
		if(null != Map && !Map.isEmpty()){
			SortMap = new ArrayList<Map.Entry<String, Double>>(Map.entrySet());
			Collections.sort(SortMap, new Comparator<Map.Entry<String, Double>>(){
				public int compare(Map.Entry<String, Double> m1, Map.Entry<String,Double> m2){
					return m2.getValue().compareTo(m1.getValue());
				}
			});
		}
		return SortMap;
	}
	


	public static List<Entry<String, Integer>> sortIntMap(Map<String, Integer> Map) {
		List<Map.Entry<String, Integer>> SortMap = new ArrayList<Map.Entry<String, Integer>>();
		if(null != Map && !Map.isEmpty()){
			SortMap = new ArrayList<Map.Entry<String, Integer>>(Map.entrySet());
			Collections.sort(SortMap, new Comparator<Map.Entry<String, Integer>>(){
				public int compare(Map.Entry<String, Integer> m1, Map.Entry<String,Integer> m2){
					return m2.getValue().compareTo(m1.getValue());
				}
			});
		}
		return SortMap;
	}
	
	
	public static void main(String argv[]) throws IOException {
		Map<String,Double> map = null;
		List<Entry<String, Double>> result = sortMap(map);
		System.out.println(result);
	}
}
