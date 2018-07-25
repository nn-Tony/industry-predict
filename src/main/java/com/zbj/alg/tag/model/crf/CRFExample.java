package com.zbj.alg.tag.model.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class CRFExample {
//使用模型进行标记
	public static void main(String[] args){
		String resource ="F:/CRFLabelWord/chinese/";//模型文件
		ModelParse MP = new ModelParse();
		//解析模型
		MP.parse(resource+"model.txt");
		
		String output = "F:/CRFLabelWord/chinese/";//输入文件
		//使用模型
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(
	 				new FileInputStream(new File(output+"crftestData002")),"utf-8"));
			PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(output+"data.seg"),"utf-8"),true);//输出文件
			String line = "";
			List<String> queryNature = new ArrayList<String>();
			List<String> saveline = new ArrayList<String>();
			while((line=br.readLine())!=null){
				String[] seg = line.split("\t");
				if(seg.length==2 && line.length()>1){
					queryNature.add(seg[0]+"&&"+seg[1]);
					saveline.add(line);
				}else{
					//中心词
					String centerWord = MP.useTestViterbi(queryNature);
//					String centerWord = MP.useTestViterbi_V1(queryNature, cateNm);
					if(centerWord==null || centerWord.split("\t").length!=queryNature.size()) {
//						System.out.println(queryNature);
						queryNature = new ArrayList<String>();
						saveline = new ArrayList<String>();
						line = "";
						continue;
					}
					String[] seg1 = centerWord.split("\t");
					for(int i=0,length=saveline.size(); i<length; i++){
						String str = saveline.get(i);
//						pw.println(str);
						pw.print(str);
						pw.println("\t"+seg1[i]);
					}
					pw.println();
					queryNature = new ArrayList<String>();
					saveline = new ArrayList<String>();
				}
				line = "";
			}
			br.close();
			pw.close();
			System.out.println("test process ok");
		}catch(IOException e){
			System.out.println(e.getMessage());
		}
	}
	
}
