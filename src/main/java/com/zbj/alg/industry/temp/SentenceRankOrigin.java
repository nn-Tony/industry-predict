package com.zbj.alg.industry.temp;

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

import com.zbj.alg.industry.model.InitialDictionary;
import com.zbj.alg.industry.model.VectorModel;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.ToAnalysis;


public class SentenceRankOrigin {

	private static VectorModel vm = null;
	private static Forest zbjdic = new Forest();
	
	public static void main(String[] args) throws IOException{
		String resourcePath = "E:/OntologyModel/";
		String testCorpusPath = resourcePath+"corpus2017921.csv.corpus.test";
		String vectorModelPath = resourcePath+"WordVec4GuideTag";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		vm = VectorModel.loadFromFile(vectorModelPath);
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testCorpusPath+".ranko"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(testCorpusPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length ==10){
				String corpus = seg[8]+"。"+seg[9];
				List<Entry<String, Double>> sentMap = sentenceRank(corpus);
				pw.println("\n"+line+"\n");
				for(Entry<String, Double> s:sentMap){
					pw.println(s.getKey()+"\t"+s.getValue());
				}
			}
		}
		br.close();
		pw.close();
	}

	private static List<Entry<String, Double>> sentenceRank(String corpus) {
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
					|| w.getNatureStr() == null
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
				for(int i=0;i<vm.getVectorSize();i++){
					corpusvec[i] += vm.getWordVector(w.getName())[i];
					sentenceVec[i] += vm.getWordVector(w.getName())[i];
				}
			}
		}
		sentnceVecMap.put(num+"#"+sentence.toString(), sentenceVec);
		//calculate sentence rank score
		for(Entry<String, float[]> s:sentnceVecMap.entrySet()){
			double distance = 0.0;
			if(s.getKey().split("#").length !=2){
				continue;
			}
			double para = Double.parseDouble(s.getKey().split("#")[0]);
			String sent = s.getKey().split("#")[1];
			for(int i=0;i<vm.getVectorSize();i++){
				distance += (corpusvec[i]/sum) * (s.getValue()[i]/para);
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
        return sentnceMapSort;
	}
}
