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
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.industry.model.InitialDictionary;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.ToAnalysis;

public class CorpusCrfLable {
	public static void main(String[] arg) throws IOException{
		
		Forest zbjdic = new Forest();
		
		String ModelFilePath = "D:/Users/zbj/git/modelResource/";
		String IndTagLibPath = "IndTagLibrary";
		String ZBJDicPath = "zbjsmall.dic";
		
		InitialDictionary.insertZBJDic(zbjdic,ModelFilePath+ZBJDicPath);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(ModelFilePath+IndTagLibPath)), "utf-8"));
		BufferedReader br1 = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(ModelFilePath+IndTagLibPath)), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(ModelFilePath+"IndLibCrfData"), "utf-8"), true);
		
		String line = null;
		Map<String,String> map = new TreeMap<String,String>();
		Set<String> set = new HashSet<String>();
		while((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(!(seg[2].equals("机构名称")) && !(seg[2].equals("机构类型"))){
				for(int i=3; i<seg.length; i++){
					String cont = seg[i].split("/")[0];
					set.add(cont);
					}
				}
			}
		br.close();
		
		while((line = br1.readLine()) != null){
			String[] seg = line.split("\t");
			for(int i=3; i<seg.length; i++){
				String cont = seg[i].split("/")[0];
				Result Words = ToAnalysis.parse(cont,UserDefineLibrary.FOREST, zbjdic);
				for(Term w:Words){
					if(set.contains(w.getName())){
						pw.println(w.getName() + "\t" + w.getNatureStr() + "\t" + "ID");
						pw.println();
					}else if(w.getName().length() == 1){
						pw.println(w.getName() + "\t" + w.getNatureStr() + "\t" + "OT");
						pw.println();
					}else{
						pw.println(w.getName() + "\t" + w.getNatureStr() + "\t" + "TH");
						pw.println();
					}
					map.put(w.getName(), w.getNatureStr());
				}
			}
		}
		br1.close();
	}
}
