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

public class IndCatDemo {

	/***
	 * 类目预测
	 * @throws IOException
	 */
	public static void main(String argv[]) throws IOException {
		String modelPath = "E:/OntologyModel/";
		IndTaging text2IndTag = new IndTagingEnhancer(modelPath);
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndCorpusEnterprice"), "utf-8"), true);
        BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(modelPath+"company.corpus")), "utf-8"));
        String line = null;
        int num = 0;
		Set<String> set = new HashSet<String>();
		while ((line = br.readLine()) != null){
			String[] seg = line.split("#&#");
			if(seg.length == 3 && !set.contains(seg[1])){
				System.out.println(num++);
				String companyName = seg[1];
				set.add(companyName);
				String categoryType =seg[0];
				String businessScope = seg[2];
				
				int length = 30;
	        	if(businessScope.length() < 30){
	        		length = businessScope.length();
	        	}
	        	
	        	String corpus = categoryType+" "+categoryType+" "+categoryType
	        			+"\t"+companyName
	        			+"\t"+businessScope.substring(0, length);
	        	IndText text = new IndText();
	    		text.setSingleCorpus(corpus);
	    		text.setType(1);
	    		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 1, 5, "bayes");
				for(IndTagGroup tag:Tags){
					IndCat cat = tag.getIndcat();
					StringBuffer st = new StringBuffer();
					for(Tag tt:tag.getTags()){
						if(tt.getProperty().contains("类目")){
							continue;
						}
						st.append(tt.getName()+"/"+tt.getProperty()+" ");
					}
					//pw.println(cat.getIndCatOne_name()+"\t"+cat.getIndCatTwo_name()+"\t"+String.valueOf(cat.getPredictScore())+"\t"+categoryType+"\t"+companyName+"\t"+businessScope);
					pw.println(cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name()+"\t"+categoryType+"&"+companyName+"&"+businessScope);
	    		}
			}
		}
		br.close();
		pw.close();
	}
}
 