package com.zbj.alg.industry.model;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nlpcn.commons.lang.tire.domain.Forest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.ToAnalysis;

/***
 * Generate industry predict model
 */
public class IndCategoryModelTrain {
	private static final Logger logger = LoggerFactory.getLogger(IndCategoryModelTrain.class);
	
	private static VectorModel vm = null;
	private static Forest inddic = new Forest();
	private static Forest serdic = new Forest();
	private static Forest zbjdic = new Forest();
	
	private static Map<String,Map<String,Double>> categoryWordMap = null;
	private static Map<String,Double> categoryMap = null;
	private static Map<String,Double> wordMap = null;
	
	public static void main(String[] args) throws IOException{

		String resourcePath = "E:/tr_project_one/project/IndustryTagLibrary/";
		String vectorModelPath = resourcePath + "WordVec4GuideTag";
		String corpusModelPath = resourcePath + "IndTagLibrary.corpus";
		
		String IndDicPath = resourcePath+"IndTagLibrary";
		String SerDicPath = resourcePath + "ServiceTagLibrary";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		
		//initial dictionary and model
		ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertIndDic(inddic,IndDicPath);
		InitialDictionary.insertSerDic(serdic,SerDicPath);
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		
		vm = VectorModel.loadFromFile(vectorModelPath);
		
		//construct industry classify model
		constructIndModel(corpusModelPath);
		logger.info("construct industry classify model over");
		//output IndModel
		outputIndModel(resourcePath);
		logger.info("output IndModel over");
		
	}

	/**output IndModel*/
	private static void outputIndModel(String resourcePath) throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(resourcePath+"IndCategoryModel"), "utf-8"), true);
        //sort1
        List<Map.Entry<String, Double>> wordMapSort = null;
        if (!wordMap.isEmpty()) {
        	wordMapSort = new ArrayList<Map.Entry<String, Double>>(wordMap.entrySet());
			Collections.sort(wordMapSort,new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> map1,
										Map.Entry<String, Double> map2) {
									return map2.getValue().compareTo(map1.getValue());
								}
							});
        }
        pw.println("Count(Word)");
        for(Entry<String, Double> w:wordMapSort){
        	pw.println(w.getKey()+"##"+w.getValue());
        }
        //sort2
        List<Map.Entry<String, Double>> categoryMapSort = null;
        if (!categoryMap.isEmpty()) {
        	categoryMapSort = new ArrayList<Map.Entry<String, Double>>(categoryMap.entrySet());
			Collections.sort(categoryMapSort,new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> map1,
										Map.Entry<String, Double> map2) {
									return map2.getValue().compareTo(map1.getValue());
								}
							});
        }
        Double sum = 0.0;
        for(Entry<String, Double> w:categoryMapSort){
        	sum +=w.getValue();
        }
        pw.println("P(C)");
        for(Entry<String, Double> w:categoryMapSort){
        	pw.println(w.getKey()+"\t"+Math.log(w.getValue()/sum));
        }
        //sort3
        pw.println("P(q(k)|C)");
        for(Entry<String, Map<String, Double>> w:categoryWordMap.entrySet()){
        	List<Map.Entry<String, Double>> wordMapSort1 = null;
            if (!w.getValue().isEmpty()) {
            	wordMapSort1 = new ArrayList<Map.Entry<String, Double>>(w.getValue().entrySet());
    			Collections.sort(wordMapSort1,new Comparator<Map.Entry<String, Double>>() {
    								public int compare(Map.Entry<String, Double> map1,
    										Map.Entry<String, Double> map2) {
    									return map2.getValue().compareTo(map1.getValue());
    								}
    							});
            }
        	double num = 0;
        	for(Entry<String, Double> ww:wordMapSort1){
        		num += ww.getValue();
        	}
        	for(Entry<String, Double> ww:wordMapSort1){
        		double value = Math.log(ww.getValue()/num);
        		if(Math.log(ww.getValue()/num) < -20){
        			value = -20.0;
        		}
        		pw.println(w.getKey()+"\t"+ww.getKey()+"\t"+value);
        	}
        }
        pw.close();
		
	}

	/***
	 * construct industry classify model
	 * @param corpusModelPath
	 * @throws IOException
	 */
	private static void constructIndModel(String corpusModelPath) throws IOException {
		
		categoryWordMap = new HashMap<String,Map<String,Double>>();
	    categoryMap = new HashMap<String,Double>();
	    wordMap = new HashMap<String,Double>();
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(corpusModelPath)),"utf-8"));
        String line = null;
        int num = 0;
        while((line = br.readLine()) != null){
        	String[] seg = line.split("\t");
			if(seg.length ==2 ){
				String ind = seg[0];
				String[] inds = ind.split("#");
				String attr = null;
				String corpus = null;
				Double score = 1.0;
				
				//标签库语料特殊处理
				if(seg[1].split("####").length==2 && seg[1].split("####")[1].split("/").length ==2){
					attr = seg[1].split("####")[0];
					if(attr.equals("地域")){
						continue;
					}
					corpus = seg[1].split("####")[1].split("/")[0];
					score = Double.parseDouble(seg[1].split("####")[1].split("/")[1]);
					if(score < 0.01){
						score = 0.01;
					}
				}
				if(corpus == null){
					corpus = seg[1]; 
				}
				
				System.out.println(num++);
				// categoryMap construct
				categoryMap.put(ind, 100.0);
				String t = corpus;
				score = Math.log(score+1.0)*calculateScore(inds,t,attr);

				//wordMap construct
				if(wordMap.containsKey(t)){
					if(wordMap.get(t) >=10000){
						wordMap.put(t, Math.log(wordMap.get(t))+10000);
					}
					wordMap.put(t, wordMap.get(t)+score);
				}else{
					wordMap.put(t, score);
				}

				//2
				if(categoryWordMap.containsKey(ind)){
					if(categoryWordMap.get(ind).containsKey(t)){
						if(categoryWordMap.get(ind).get(t)>=10000){
							categoryWordMap.get(ind).put(t,Math.log(categoryWordMap.get(ind).get(t)) + 10000);
						}
						categoryWordMap.get(ind).put(t,categoryWordMap.get(ind).get(t) + score);
					}else{
						categoryWordMap.get(ind).put(t, score);
					}
				}else{
					Map<String,Double> temp = new HashMap<String,Double>();
					temp.put(t, score);
					categoryWordMap.put(ind, temp);
				}
			}
        }
		br.close();
		
	}

	/***
	 * compute base score
	 * @param inds
	 * @param t corpus
	 * @param attr
	 * @return
	 */
	private static double calculateScore(String[] inds, String t, String attr) {
		double score = 0.0;
		
		double P1 = 1.0;
		
		if(attr.equals("机构名称")){
			P1 += 20;
		}else if(attr.equals("机构类型")){
			P1 += 10;
		}else if(attr.equals("行业")){
			P1 += 20;
		}else if(attr.equals("主营")){
			P1 += 6;
		}else if(attr.equals("产品")){
			P1 += 3;
		}else if(attr.equals("职位")){
			P1 += 2;
		}else if(attr.equals("热门")){
			P1 += 1;
		}
		
		double P2 = 2*t.length();
		
		for(int k=0;k<inds.length;k++){
			double distance = 0.0;
			
			float[] indvec = new float[vm.getVectorSize()];
			float[] tagvec = new float[vm.getVectorSize()];
			
			if(null != vm.getWordVector(inds[k])){
				indvec = vm.getWordVector(inds[k]);
			}else{
				Result words = ToAnalysis.parse(inds[k]);
				for(Term w:words){
					if(!vm.getWordMap().containsKey(w.getName())){
						continue;
					}
					for(int i=0;i<vm.getVectorSize();i++){
						indvec[i] += vm.getWordVector(w.getName())[i];
					}
				}
			}
			
			if(null != vm.getWordVector(t)){
				tagvec = vm.getWordVector(t);
			}else{
				Result words = ToAnalysis.parse(t);
				for(Term w:words){
					if(!vm.getWordMap().containsKey(w.getName())){
						continue;
					}
					for(int i=0;i<vm.getVectorSize();i++){
						tagvec[i] += vm.getWordVector(w.getName())[i];
					}
				}
			}
			
			for(int i=0;i<vm.getVectorSize();i++){
				//词向量距离计算方法，越大越接近
				distance += indvec[i] * tagvec[i];
			}
			
			if(inds[k].equals(t)){
				distance +=100.0;
			}else if(inds[k].contains(t)){
				distance +=5.0;
			}
			
			score += P1*P2*(k+1)*(10*distance+0.5);
		}
		
		return score*0.01;
	}

}
