package com.zbj.alg.industry.corpus;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;

/***
 * Generate modeling corpus
 */
public class IndLibraryCorpus {
	public static void main(String argv[]) throws IOException{
		String path = "E:/tr_project_one/project/IndustryTagLibrary/IndTagLibrary";
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/tr_project_one/project/IndustryTagLibrary/IndTagLibrary.corpus"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >= 4){
				String cat = seg[1];
				String attr = seg[2];
				if(attr.contains("地域")
						|| attr.contains("机构名称")){
					continue;
				}
				for(int i=3;i<seg.length;i++){
					pw.println(cat+"\t"+attr+"####"+seg[i]);
				}
			}
		}
		br.close();
		pw.close();
	}
}
