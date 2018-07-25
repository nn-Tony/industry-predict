package com.zbj.alg.industry.temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

public class InitialModel {
	public static Map<String, Map<String, Double>> loadC1ValueMap(
			String modelPath) throws IOException {
		Map<String, Map<String, Double>> C1ValueMap = new HashMap<String, Map<String, Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg[0].equals("c1")){
				if(C1ValueMap.containsKey(seg[1])){
					C1ValueMap.get(seg[1]).put(seg[2], Double.parseDouble(seg[3]));
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(seg[2], Double.parseDouble(seg[3]));
					C1ValueMap.put(seg[1], map);
				}
			}
		}
		br.close();
		return C1ValueMap;
	}

	public static Map<String, Map<String, Map<String, Double>>> loadC1to2ValueMap(
			String modelPath) throws IOException {
		Map<String,Map<String,Map<String,Double>>> C1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length != 5){
				continue;
			}
			if(seg[0].equals("c12")){
				if(C1to2ValueMap.containsKey(seg[1])){
					if(C1to2ValueMap.get(seg[1]).containsKey(seg[2])){
						C1to2ValueMap.get(seg[1]).get(seg[2]).put(seg[3], Double.parseDouble(seg[4]));
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(seg[3], Double.parseDouble(seg[4]));
						C1to2ValueMap.get(seg[1]).put(seg[2], map);
					}
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(seg[3], Double.parseDouble(seg[4]));
					Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
					map2.put(seg[2], map);
					C1to2ValueMap.put(seg[1], map2);
				}
			}
		}
		br.close();
		return C1to2ValueMap;
	}

	public static Map<String, Map<String, Double>> loadCW1ValueMap(
			String modelPath) throws IOException {
		Map<String,Map<String,Double>> CW1ValueMap = new HashMap<String,Map<String,Double>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length != 4){
				continue;
			}
			if(seg[0].equals("w1")){
				if(CW1ValueMap.containsKey(seg[1])){
					CW1ValueMap.get(seg[1]).put(seg[2], Double.parseDouble(seg[3]));
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(seg[2], Double.parseDouble(seg[3]));
					CW1ValueMap.put(seg[1], map);
				}
			}
		}
		br.close();
		return CW1ValueMap;
	}

	public static Map<String, Map<String, Map<String, Double>>> loadCW1to2ValueMap(
			String modelPath) throws IOException {
		Map<String,Map<String,Map<String,Double>>> CW1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length != 5){
				continue;
			}
			if(seg[0].equals("w12")){
				if(CW1to2ValueMap.containsKey(seg[1])){
					if(CW1to2ValueMap.get(seg[1]).containsKey(seg[2])){
						CW1to2ValueMap.get(seg[1]).get(seg[2]).put(seg[3], Double.parseDouble(seg[4]));
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(seg[3], Double.parseDouble(seg[4]));
						CW1to2ValueMap.get(seg[1]).put(seg[2], map);
					}
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(seg[3], Double.parseDouble(seg[4]));
					Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
					map2.put(seg[2], map);
					CW1to2ValueMap.put(seg[1], map2);
				}
			}
		}
		br.close();
		return CW1to2ValueMap;
	}

}
