package com.zbj.alg.tag.model.crf;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Template {
//U07:%x[-1,0]/%x[0,0]/%x[1,0]

	Map<String,List<String>> featureTemplatesplit = new HashMap<String,List<String>>();
	
	void addTemplateSplit(String str){
		String[] seg = str.split(":");
		String str1 = seg[1].replaceFirst("%x", "");
		String[] seg1 = str1.split("/%x");
		List<String> templates = new ArrayList<String>();
		for(int i=0,length=seg1.length; i<length; i++){
			templates.add(seg1[i]);
		}
		
		featureTemplatesplit.put(seg[0], templates);
	}
}
