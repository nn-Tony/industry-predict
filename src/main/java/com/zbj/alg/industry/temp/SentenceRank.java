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
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.nlp.server.CRF;
import com.zbj.alg.nlp.server.CRFEnhancer;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.ToAnalysis;
import com.zbj.alg.tag.model.category.CategoryPredict;

public class SentenceRank {

	private static VectorModel vm = null;
	private static Forest zbjdic = new Forest();
	//CRF模型解析
    private static ModelParse MP = new ModelParse();
    static CRF crf = null;
    
	public static void main(String[] args) throws IOException{
		String resourcePath = "E:/OntologyModel/";
		String testCorpusPath = resourcePath+"corpus2017921.csv.corpus.test";
		String vectorModelPath = resourcePath+"WordVec4GuideTag";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		String CRFmodelRealpath = resourcePath + "modelPitch.txt";
		
		crf = new CRFEnhancer();
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		vm = VectorModel.loadFromFile(vectorModelPath);
		MP.parse(CRFmodelRealpath);
		
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(testCorpusPath+".rank"), "utf-8"), true);
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(testCorpusPath)), "utf-8"));
		String line = null;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length ==10){
				String corpus = seg[8]+"。"+seg[9];
				List<Entry<String, Double>> sentMap = SentenceRankModel.sentenceRank(corpus,zbjdic,MP,vm);
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
        return sentnceMapSort;
	}
}
