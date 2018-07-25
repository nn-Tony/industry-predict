package com.zbj.alg.industry.demo;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.csvreader.CsvReader;
import com.zbj.alg.industry.model.server.IndCat;
import com.zbj.alg.industry.model.server.IndTagGroup;
import com.zbj.alg.industry.model.server.IndTaging;
import com.zbj.alg.industry.model.server.IndTagingEnhancer;
import com.zbj.alg.industry.model.server.IndText;
import com.zbj.alg.industry.model.server.Tag;

public class IndTestDemo {

	/***
	 * 类目预测
	 * @throws IOException
	 */
	@SuppressWarnings("null")
	public static void main(String argv[]) throws IOException {
		String modelPath = "E:/tr_project_one/project/IndustryTagLibrary/standardCorpus/";
		IndTaging text2IndTag = new IndTagingEnhancer("E:/tr_project_one/project/IndustryTagLibrary/modelResource/");

		BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(modelPath+"IndTestCorpus500")), "utf-8"));

        String line = null;
        int num = 0;
        double c = 0.0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length < 4){
				continue;
			}
			num++;
//			System.out.println(num);
			if(num>600){
				break;
			}
			String corpus = seg[2]+seg[3];
        	IndText text = new IndText();
    		text.setSingleCorpus(corpus);
    		text.setType(1);
    		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 1, 8, "bayes");
    		if(Tags.isEmpty()){
    			continue;
    		}else{
    			for(IndTagGroup tag:Tags){
					IndCat cat = tag.getIndcat();
					String temp = cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name();
//					String temp = cat.getIndCatOne_name();
					if(temp.equals(seg[0])){
						c++;
					}else{
						StringBuffer sb = new StringBuffer();
						for(Tag t:tag.getTags()){
							if(!(t.getProperty().contains("级行业"))){
								sb.append(t.getName()+"\t");
							}
						}
						System.out.println(num +"\t"+"["+ seg[0] +"__"+ temp +"]"+"\t"+ sb.toString());
					}
    			}
    		}
		}
		br.close();
		double a = c/num;
		System.out.println(a);
	}
	
	public static void cccc(String argv[]) throws IOException {
		String modelPath = "E:/tr_project_one/project/IndustryTagLibrary/standardCorpus/";
		IndTaging text2IndTag = new IndTagingEnhancer("E:/tr_project_one/project/IndustryTagLibrary/modelResource/");

		BufferedReader br = new BufferedReader(new InputStreamReader(
					new FileInputStream(new File(modelPath+"providerTaskAndInd.csv")), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
					new FileOutputStream(modelPath+"providerTaskAndInd.ind"), "utf-8"), true);

        String line = null;
        int num = 0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length < 6){
				continue;
			}
			System.out.println(num++);
			String corpus = seg[4]+seg[5];
        	IndText text = new IndText();
    		text.setSingleCorpus(corpus);
    		text.setType(1);
    		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 1, 5, "bayes");
    		if(Tags.isEmpty()){
    			continue;
    		}else{
    			for(IndTagGroup tag:Tags){
    				StringBuffer sb = new StringBuffer();
    				int count = 1;
					IndCat cat = tag.getIndcat();
					for(Tag tt:tag.getTags()){
						if(count++>2){
							sb.append(tt.getName()+"/");
						}
					}
					pw.println(cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name()
	    						+"\t"+seg[0]+"\t"+seg[1]+"\t"+seg[4]+"\t"+seg[5]+"\t"+sb.toString());
    			}
    		}
		}
		br.close();
		pw.close();
	}
}
 