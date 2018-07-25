package com.zbj.alg.tag.model.crf;

import java.util.List;

public class Tables {

	String[][] v ;
	int attribute = 2;
	final String HEAD = "_B";
	
	public Tables(){}
		
	public Tables(List<String>words){
		v = new String[words.size()][attribute];
		
		for(int i=0,length=words.size(); i<length; i++){
			String word = words.get(i);
			if(word.split("&&").length<2){
				v[i][0] = word;
				v[i][1] = null;
			}else{
				String[] seg = word.split("&&");
				v[i][0] = seg[0];
				v[i][1] = seg[1];
			}
		}
	}
	
	String getTag(int x, int y){
		 if (x < 0) return HEAD + x;
	        if (x >= v.length) return HEAD + "+" + (x - v.length + 1);

	        return v[x][y];
	}
}
