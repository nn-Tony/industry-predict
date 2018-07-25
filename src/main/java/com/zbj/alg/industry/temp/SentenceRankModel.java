package com.zbj.alg.industry.temp;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.industry.model.VectorModel;
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.nlp.server.CRF;
import com.zbj.alg.nlp.server.CRFEnhancer;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.ToAnalysis;
import com.zbj.alg.tag.model.category.CategoryPredict;

public class SentenceRankModel {

	public static List<Entry<String, Double>> sentenceRank(String corpus,
			Forest zbjdic, ModelParse MP, VectorModel vm) {
		CRF crf = new CRFEnhancer();
		float[] corpusvec = new float[vm.getVectorSize()];
		Map<String,Double> sentnceMap = new HashMap<String,Double>();
		Map<String,float[]> sentnceVecMap = new HashMap<String,float[]>();
		Result words = ToAnalysis.parse(corpus,UserDefineLibrary.FOREST,zbjdic);
		StringBuffer sentence = new StringBuffer();
		float[] sentenceVec = new float[vm.getVectorSize()];
		double sum = 1;
		double num = 1;
		for(Term w:words){
			if(w.getNatureStr().equals("w")
					|| !w.toString().contains("/")
					|| num == 10){
				sentnceVecMap.put(num+"#-"+sentence.toString(), sentenceVec);
				num =1;
				sentence = new StringBuffer();
				sentenceVec = new float[vm.getVectorSize()];
			}
			if(vm.getWordMap().containsKey(w.getName())){
				num++;
				sum++;
				sentence.append(w.getName());
			}
		}
		sentnceVecMap.put(num+"#"+sentence.toString(), sentenceVec);
		for(Entry<String, float[]> s:sentnceVecMap.entrySet()){
			if(s.getKey().split("#").length !=2){
				continue;
			}
			double distance = 0.0;
			double number = Double.parseDouble(s.getKey().split("#")[0]);
			String sent = s.getKey().split("#")[1];
			Map<Term, String> centerWord = crf.getWordLabel(sent,zbjdic,MP);
			for(Entry<Term, String> w:centerWord.entrySet()){
				double para = 1.0;
				if(vm.getWordMap().containsKey(w.getKey().getName())){
					if(w.getValue().equals("TH")){
						//System.out.println(w.getKey());
						para = 5;
					}
					for(int i=0;i<vm.getVectorSize();i++){
						s.getValue()[i] += para*vm.getWordVector(w.getKey().getName())[i]/number;
						corpusvec[i] += para*vm.getWordVector(w.getKey().getName())[i]/sum;
						distance += corpusvec[i] * s.getValue()[i];
					}
				}
			}
			sentnceMap.put(sent, distance);
		}

		//rank sentence
		List<Map.Entry<String, Double>> sentnceMapSort = null;
        if (!sentnceMap.isEmpty()) {
        	sentnceMapSort = new ArrayList<Map.Entry<String, Double>>(sentnceMap.entrySet());
			Collections.sort(sentnceMapSort,new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> map1,
										Map.Entry<String, Double> map2) {
									return map2.getValue().compareTo(map1.getValue());
								}
							});
        }
        if(sentnceMapSort == null){
        	sentnceMapSort = new ArrayList<Map.Entry<String, Double>>();
        	Map<String,Double> map = new HashMap<String,Double>();
        	map.put(corpus, 1.0);
        	for(Entry<String, Double> m:map.entrySet()){
        		sentnceMapSort.add(m);
        	}
        }
        return sentnceMapSort;
	}

}
