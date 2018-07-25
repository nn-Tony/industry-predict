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
import java.util.Set;

public class Corpus {
	public static void main(String argv[]) throws IOException{
		String path = "E:/OntologyModel/corpus2017921.csv";
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(path+".corpus"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length ==10){
				pw.print("\n"+line+" ");
			}else{
				pw.print(line+" ");
			}
		}
		br.close();
		pw.close();
	}
}
