package com.zbj.alg.industry.corpus;

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

/***
 * format detection
 */
public class IndLibraryDetection {

	public static void main(String argv[]) throws IOException{
		
		String modelPath = "E:/OntologyModel/";
		IndTaging text2IndTag = new IndTagingEnhancer(modelPath);
		
		String path = modelPath+"IndTagLibrary";
		
		PrintWriter rw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndTagLibrary.right"), "utf-8"), true);
		PrintWriter rw2 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(modelPath+"IndTagLibrary.cat"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(path)), "utf-8"));
		String line = null;
		Set<String> set = new HashSet<String>();
 		while ((line = br.readLine()) != null){
			String[] seg = line.split("\t");
			if(seg.length >= 4){
				String cat = seg[1];
				String attr = seg[2];
				set.add(cat);
				StringBuffer rt = new StringBuffer();
				rt.append(seg[0]+"\t"+cat+"\t"+attr+"\t");
				for(int i=3;i<seg.length;i++){
					String tag = seg[i].split("/")[0];
					int flag = tagAnomalousDetection(text2IndTag,tag,cat,attr);
					rt.append(tag+"/"+flag+"\t");
				}
				if(rt.toString().split("\t").length >=4){
					rw.println(rt.toString());
				}
			}
		}
 		System.out.println(set.size());
 		for(String cat:set){
 			rw2.println(cat);
 		}
		br.close();
		rw.close();
	}

	/***
	 * detect error tag
	 * @param text2IndTag
	 * @param tag
	 * @param cat
	 * @param attr
	 * @return
	 */
	private static int tagAnomalousDetection(IndTaging text2IndTag, String tag, String cat, String attr) {
		int flag = 0;
		
		IndText text = new IndText();
		text.setSingleCorpus(tag);
		text.setType(1);
		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 240, 5, "bayes");
		int count = 0;
		for(IndTagGroup t:Tags){
			count++;
			IndCat c = t.getIndcat();
			String preCat = c.getIndCatOne_name()+"#"+c.getIndCatTwo_name();
			if(preCat .equals(cat)){
				flag = 240 - count;
			}
		}
		
		
		return flag;
	}
}
