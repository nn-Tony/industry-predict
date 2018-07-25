package com.zbj.alg.industry.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.zbj.alg.industry.model.server.IndCat;
import com.zbj.alg.industry.model.server.IndTagGroup;
import com.zbj.alg.industry.model.server.IndTaging;
import com.zbj.alg.industry.model.server.IndTagingEnhancer;
import com.zbj.alg.industry.model.server.IndText;
import com.zbj.alg.industry.model.server.Tag;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;

public class IndTagingDemo {

	/***
	 * 类目预测
	 * @throws IOException
	 */
	public static void main(String argv[]) throws IOException {
		String modelPath = "D:/Users/zbj/git/modelResource/";
		IndTaging text2IndTag = new IndTagingEnhancer(modelPath);
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/tr_project_one/project/IndustryTagLibrary/IndTestCorpus_1"), "utf-8"), true);
        BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File("E:/tr_project_one/project/IndustryTagLibrary/招投标订单_包含雇主名称.csv")), "utf-8"));
        
        String line = null;
        int num = 0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length != 7){
				continue;
			}
			String nickname = seg[4];
			String title = seg[5];
			String temp = seg[6];
			if(seg[6].length() > 100){
				temp = seg[6].substring(0,100);
			}else{
				temp = seg[6];
			}
			String corpus = title+""+temp;
			String service = seg[1]+"#"+seg[2]+"#"+seg[3];
			System.out.println(num++);
			IndText text = new IndText();
			text.setSingleCorpus(corpus);
			text.setType(1);
			List<IndTagGroup> Tags = text2IndTag.getLabels(text, 1, 5, "bayes");
			for(IndTagGroup tag:Tags){
				IndCat cat = tag.getIndcat();
				if(cat.getIndCatOne_name().equals("其他")){
					continue;
				}else{
					pw.println(cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name()+"\t"+service+"\t"+title+"\t"+temp);
				}
			}
        }
        br.close();
        pw.close();
	}
}
 