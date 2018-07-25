package com.zbj.alg.industry.temp;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.zbj.alg.industry.model.InitialDictionary;

public class CorpusFilter {
	
	private static Map<String,Map<String,Map<String,Integer>>> SerLibrary = null;
	
	public static void main(String argv[]) throws IOException{
		String path = "E:/OntologyModel/corpus2017921.csv.corpus";
		
		SerLibrary = InitialDictionary.loadIndLibrary("E:/OntologyModel/ServiceTagLibrary");
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(path+".train"), "utf-8"), true);
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(path+".test"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		int num = 0;
		Set<String> set = new HashSet<String>();
		Set<String> set2 = new HashSet<String>();
 		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length ==10
					&& seg[1].matches("[0-9]*")
					&& !set.contains(seg[1])
					&& seg[8].length() >= 5
					&& seg[9].length() >= 5
					&& seg[9].length() >= seg[8].length()
					&& !seg[9].contains(seg[8])){
				String cat = seg[2]+"#"+seg[4]+"#"+seg[6];
				if(SerLibrary.containsKey(cat)
						&& !set2.contains(seg[8])){
					System.out.println(num++);
					set.add(seg[1]);
					set.add(seg[8]);
					if(num<=800000){
						pw.println(line);
					}else{
						pw1.println(line);
					}
				}
			}
		}
		br.close();
		pw.close();
	}
}
