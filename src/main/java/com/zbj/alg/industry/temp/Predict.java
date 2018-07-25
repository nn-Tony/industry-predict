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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.industry.model.InitialDictionary;
import com.zbj.alg.industry.model.VectorModel;
import com.zbj.alg.industry.utils.Sort;
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.IndexAnalysis;
import com.zbj.alg.seg.splitWord.ToAnalysis;

public class Predict {
	
	private static Forest serdic = new Forest();
	private static Forest zbjdic = new Forest();
	
	private static VectorModel vm = null;
	private static ModelParse MP = new ModelParse();
	
	private static Map<String, Map<String, Double>> C1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> C1to2ValueMap = null;
	
	private static Map<String,Map<String,Double>> CW1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> CW1to2ValueMap = null;
	
	private static Map<String,Map<String,Map<String,Integer>>> SerLibrary = null;
	
	public static void main(String[] args) throws IOException{
		
		String resourcePath = "E:/OntologyModel/";
		
		String vectorModelPath = resourcePath + "WordVec4GuideTag";
		String CRFmodelRealpath = resourcePath + "modelPitch.txt";
		
		String SerDicPath = resourcePath + "ServiceTagLibrary";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		String corpusModelPath = resourcePath + "corpus2017921.csv.corpus.test";
		String modelPath = resourcePath + "SerLMCatModel";
		
		InitialDictionary.insertSerDic(serdic,SerDicPath);
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		
		vm = VectorModel.loadFromFile(vectorModelPath);
		MP.parse(CRFmodelRealpath);
		
		SerLibrary = InitialDictionary.loadIndLibrary(SerDicPath);
		
		C1ValueMap = InitialModel.loadC1ValueMap(modelPath);
		C1to2ValueMap = InitialModel.loadC1to2ValueMap(modelPath);
		CW1ValueMap = InitialModel.loadCW1ValueMap(modelPath);
		CW1to2ValueMap = InitialModel.loadCW1to2ValueMap(modelPath);
		
		testSerModel(corpusModelPath);
	}

	private static void testSerModel(String corpusModelPath) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(corpusModelPath)), "utf-8"));
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/OntologyModel/ser.corpus.result"), "utf-8"), true);
		PrintWriter pw1 = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/OntologyModel/ser.corpus.errorResult"), "utf-8"), true);
        String line = null;
        int num = 0;
        int numr1 = 0;
        int numr2 = 0;
        int numr3 = 0;
		while ((line = br.readLine()) != null){
			String[] seg = line.split(",");
			if(seg.length ==10 ){
				String corpus = seg[8]+"。"+seg[9];
				if(!seg[1].matches("[0-9]*") && seg[0].substring(0, 2).equals("201")){
					continue;
				}
				System.out.println(num++);
            	Map<String, Double> predictMap = predict(corpus);
            	for(Entry<String, Double> pe:predictMap.entrySet()){
            		if(pe.getKey().split("#").length !=3 ){
            			continue;
            		}
            		String cat1 = pe.getKey().split("#")[0];
            		String cat2 = pe.getKey().split("#")[1];
            		String cat3 = pe.getKey().split("#")[2];
            		if(seg[2].equals(cat1)){
            			numr1++;
            		}else{
            			pw1.println(num+" "+numr1+" "+numr2+" "+numr3+" "+pe.getKey()+" "+pe.getValue()+"\t"+line);
            		}
            		if(seg[4].equals(cat2)){
            			numr2++;
            		}
            		if(seg[6].equals(cat3)){
            			numr3++;
            		}
            		pw.println(num+" "+numr1+" "+numr2+" "+numr3+" "+pe.getKey()+" "+pe.getValue()+"\t"+line);
            	}
            	
        	
			}
        	
        }
		br.close();
		pw.close();
		pw1.close();
	}

	private static Map<String, Double> predict(String title) {
		
		Map<String,Double> resultMap = new HashMap<String,Double>();
		
		List<Entry<String, Double>> sentMap = SentenceRankModel.sentenceRank(title,zbjdic,MP,vm);
		
		double value = 0.0;
		int num = 0;
		List<String> Tag = new ArrayList<String>();
		List<Term> tag = new ArrayList<Term>();
		//System.out.println(title);
		for(Entry<String, Double> s:sentMap){
			num++;
			String sent = s.getKey();
			if(num==1){
				value = s.getValue();
			}
			if((value-s.getValue()) >=1
					|| num >=4){
				break;
			}
			//System.out.println(sent);
			Result words = ToAnalysis.parse(sent,UserDefineLibrary.FOREST,zbjdic);
			Result Words = IndexAnalysis.parse(sent,UserDefineLibrary.FOREST,serdic);
			
			for(int m=0;m<Words.size()-1;m++){
				if(Words.get(m).getNatureStr().equals("SER")){
					Tag.add(Words.get(m).getName());
				}
			}
			
			for(int m=0;m<words.size()-1;m++){
				if((!words.get(m).getNatureStr().contains("n")
						&& !words.get(m).getNatureStr().contains("v")
						&& !words.get(m).getNatureStr().matches("SER"))
						|| words.get(m).toString().split("/").length !=2
						|| words.get(m).getName().length()>=15
						|| words.get(m).getName().length()<=1){
					continue;
				}
				tag.add(words.get(m));
			}
		}
		
		Map<String,Double> predictMap = new HashMap<String,Double>();
		
		for(Entry<String, Map<String, Double>> c : C1ValueMap.entrySet()){
			String indcat = c.getKey();
			String cat1 = c.getKey().split("#")[0];
			String cat2 = c.getKey().split("#")[1];
			String cat3 = c.getKey().split("#")[2];
			double score = 0;
			//抽取标签限制
			if(SerLibrary.containsKey(indcat)){
				for(Entry<String, Map<String, Integer>> attr:SerLibrary.get(indcat).entrySet()){
					for(Entry<String, Integer> t:attr.getValue().entrySet()){
						if(Tag.contains(t.getKey())){
							if(attr.getKey().equals("类型")
									|| attr.getKey().equals("主题")){
								score += 0.05*Math.log(t.getValue());
							}else{
								score += 0.01*Math.log(t.getValue());
							}
						}
						if(tag.contains(cat1)
								|| tag.contains(cat2)
								|| tag.contains(cat3)){
							score += 1;
						}
					}
				}
			}
			//
			for(Entry<String, Double> cc:c.getValue().entrySet()){
				cat1 = cc.getKey();
				if(C1to2ValueMap.containsKey(indcat)
						&& C1to2ValueMap.get(indcat).containsKey(cat1)
						&& C1to2ValueMap.get(indcat).containsKey(cat2)
						&& C1to2ValueMap.get(indcat).get(cat1).containsKey(cat2)
						&& C1to2ValueMap.get(indcat).get(cat2).containsKey(cat3)){
					Double pc1 = Math.exp(cc.getValue());
					Double pc12 =  Math.exp(C1to2ValueMap.get(indcat).get(cat1).get(cat2));
					Double pc23 = Math.exp(C1to2ValueMap.get(indcat).get(cat2).get(cat3));
					score += pc1 + pc12 + pc23;
					if(tag.size()>=1 
							&& CW1ValueMap.containsKey(indcat)
							&& CW1ValueMap.get(indcat).containsKey(tag.get(0).getName())){
						score += Math.exp(CW1ValueMap.get(indcat).get(tag.get(0).getName()));
						if(tag.size()>=2){
							for(int k=0;k<(tag.size()-1);k++){
								if(CW1to2ValueMap.containsKey(indcat) 
										&& CW1to2ValueMap.get(indcat).containsKey(tag.get(k).getName())
										&& CW1to2ValueMap.get(indcat).get(tag.get(k).getName()).containsKey(tag.get(k+1).getName())){
									score += Math.exp(CW1to2ValueMap.get(indcat).get(tag.get(k).getName()).get(tag.get(k+1).getName()));
								}
							}
						}
					}
				}
			}
			predictMap.put(indcat, score);
		}
		//out put
		if (!predictMap.isEmpty()){
			List<Entry<String, Double>> predictMapSort = Sort.sortMap(predictMap);
			int count = 0;
			for(Entry<String, Double> re:predictMapSort){
				if(count++ >= 0){
					resultMap.put(re.getKey(),re.getValue());
					break;
				}
			}
		}
		
		return resultMap;
	}
}
