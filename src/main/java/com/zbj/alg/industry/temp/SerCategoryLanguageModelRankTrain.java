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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbj.alg.industry.model.InitialDictionary;
import com.zbj.alg.industry.model.VectorModel;
import com.zbj.alg.industry.utils.Sort;
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.IndexAnalysis;
import com.zbj.alg.seg.splitWord.ToAnalysis;


public class SerCategoryLanguageModelRankTrain {
	private static final Logger logger = LoggerFactory.getLogger(SerCategoryLanguageModelRankTrain.class);
	
	private static VectorModel vm = null;
	private static ModelParse MP = new ModelParse();
	private static Forest inddic = new Forest();
	private static Forest serdic = new Forest();
	private static Forest zbjdic = new Forest();
	
	private static Map<String,Double> C1Map = null;
	private static Map<String, Map<String, Double>> C1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> C1to2Map = null;
	private static Map<String,Map<String,Map<String,Double>>> C1to2ValueMap = null;
	
	private static Map<String,Map<String,Double>> CW1Map = null;
	private static Map<String,Map<String,Double>> CW1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> CW1to2Map = null;
	private static Map<String,Map<String,Map<String,Double>>> CW1to2ValueMap = null;
		
	private static Map<String,Map<String,Map<String,Integer>>> SerLibrary = null;
	
	public static void main(String[] args) throws IOException{
		String resourcePath = "E:/OntologyModel/";
		
		String vectorModelPath = resourcePath + "WordVec4GuideTag";
		String CRFmodelRealpath = resourcePath + "modelPitch.txt";
		
		String corpusModelPath = resourcePath + "corpus2017921.csv.corpus.train";
		String corpusTestModelPath = resourcePath + "corpus2017921.csv.corpus.test";
		
		String IndDicPath = resourcePath+"IndTagLibrary";
		String SerDicPath = resourcePath + "ServiceTagLibrary";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		
		//initial dictionary and model
		InitialDictionary.insertIndDic(inddic,IndDicPath);
		InitialDictionary.insertSerDic(serdic,SerDicPath);
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		
		vm = VectorModel.loadFromFile(vectorModelPath);
		MP.parse(CRFmodelRealpath);
		
		//download konwledge library
		SerLibrary = InitialDictionary.loadIndLibrary(SerDicPath);
		
		//construct industry classify model
		constructSerModel(corpusModelPath);
		logger.info("construct corpus classify model over");
		//calculate SerModel
		calculateSerModel(resourcePath);
		logger.info("calculate SerModel over");
		//output SerModel
		outputSerModel(resourcePath);
		logger.info("output SerModel over");
		//test SerModel
		testSerModel(corpusTestModelPath);
		logger.info("test SerModel over");
	}

	static void testSerModel(String resourcePath) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(resourcePath)), "utf-8"));
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

	private static Map<String, Double> predict(String title) throws IOException {
		
		Map<String,Double> resultMap = new HashMap<String,Double>();
		
		List<Entry<String, Double>> sentMap = SentenceRankModel.sentenceRank(title,zbjdic,MP,vm);
		
		double value = 0.0;
		int num = 0;
		List<String> Tag = new ArrayList<String>();
		List<Term> tag = new ArrayList<Term>();
		for(Entry<String, Double> s:sentMap){
			num++;
			String sent = s.getKey();
			if(num==1){
				value = s.getValue();
			}
			if((value-s.getValue())/value>0.2
					|| num >=4){
				break;
			}
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
			/*//抽取标签限制
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
			}*/
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

	private static void outputSerModel(String resourcePath) throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(resourcePath+"SerLMCatModel"), "utf-8"), true);
		Map<String,Double> C1Map = new HashMap<String,Double>();
		for(Entry<String, Map<String, Double>> c:C1ValueMap.entrySet()){
			for(Entry<String, Double> cc:c.getValue().entrySet()){
				C1Map.put(c.getKey()+"\t"+cc.getKey(), cc.getValue());
			}
		}
		//output cat layer1
		List<Map.Entry<String, Double>> C1MapSort = null;
        if (!C1Map.isEmpty()) {
        	C1MapSort = new ArrayList<Map.Entry<String, Double>>(C1Map.entrySet());
			Collections.sort(C1MapSort,new Comparator<Map.Entry<String, Double>>() {
								public int compare(Map.Entry<String, Double> map1,
										Map.Entry<String, Double> map2) {
									return map2.getValue().compareTo(map1.getValue());
								}
							});
        }
		for(Entry<String, Double> cc:C1MapSort){
			pw.println("c1"+"\t"+cc.getKey()+"\t"+cc.getValue());
		}
		//output cat layer2
		Map<String,Map<String,Double>> C2Map = new HashMap<String,Map<String,Double>>();
		for(Entry<String, Map<String, Map<String, Double>>> c:C1to2ValueMap.entrySet()){
			for(Entry<String, Map<String, Double>> cc : c.getValue().entrySet()){
				for(Entry<String, Double> ccc:cc.getValue().entrySet()){
					if(C2Map.containsKey(c.getKey())){
						C2Map.get(c.getKey()).put(cc.getKey()+"\t"+ccc.getKey(), ccc.getValue());
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(cc.getKey()+"\t"+ccc.getKey(), ccc.getValue());
						C2Map.put(c.getKey(), map);
					}
				}
			}
		}
		for(Entry<String, Map<String, Double>> c:C2Map.entrySet()){
			List<Map.Entry<String, Double>> C2MapSort = null;
	        if (!c.getValue().isEmpty()) {
	        	C2MapSort = new ArrayList<Map.Entry<String, Double>>(c.getValue().entrySet());
				Collections.sort(C2MapSort,new Comparator<Map.Entry<String, Double>>() {
									public int compare(Map.Entry<String, Double> map1,
											Map.Entry<String, Double> map2) {
										return map2.getValue().compareTo(map1.getValue());
									}
								});
	        }
			for(Entry<String, Double> cc:C2MapSort){
				pw.println("c12"+"\t"+c.getKey()+"\t"+cc.getKey()+"\t"+cc.getValue());
			}
		}

		//output word layer1
		for(Entry<String, Map<String, Double>> c:CW1ValueMap.entrySet()){
			List<Map.Entry<String, Double>> W1MapSort = null;
	        if (!c.getValue().isEmpty()) {
	        	W1MapSort = new ArrayList<Map.Entry<String, Double>>(c.getValue().entrySet());
				Collections.sort(W1MapSort,new Comparator<Map.Entry<String, Double>>() {
									public int compare(Map.Entry<String, Double> map1,
											Map.Entry<String, Double> map2) {
										return map2.getValue().compareTo(map1.getValue());
									}
								});
	        }
			for(Entry<String, Double> w:W1MapSort){
				pw.println("w1"+"\t"+c.getKey()+"\t"+w.getKey()+"\t"+w.getValue());
			}
		}
		
		//output word layer2
		for(Entry<String, Map<String, Map<String, Double>>> c:CW1to2ValueMap.entrySet()){
			for(Entry<String, Map<String, Double>> w:c.getValue().entrySet()){
				List<Map.Entry<String, Double>> W2MapSort = null;
		        if (!w.getValue().isEmpty()) {
		        	W2MapSort = new ArrayList<Map.Entry<String, Double>>(w.getValue().entrySet());
					Collections.sort(W2MapSort,new Comparator<Map.Entry<String, Double>>() {
										public int compare(Map.Entry<String, Double> map1,
												Map.Entry<String, Double> map2) {
											return map2.getValue().compareTo(map1.getValue());
										}
									});
		        }
				for(Entry<String, Double> ww:W2MapSort){
					pw.println("w12"+"\t"+c.getKey()+"\t"+w.getKey()+"\t"+ww.getKey()+"\t"+ww.getValue());
				}
			}
		}
		
		pw.close();
	}

	private static void calculateSerModel(String resourcePath)  {
		
		//First
		C1ValueMap = new HashMap<String,Map<String,Double>>();
		C1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		
		CW1ValueMap = new HashMap<String,Map<String,Double>>();
		CW1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		
		double sum = 0;
		for(Entry<String, Double> l:C1Map.entrySet()){
			if(l.getKey().split("#").length !=3){
				continue;
			}
			sum += l.getValue();
		}
		for(Entry<String, Double> l:C1Map.entrySet()){
			if(l.getKey().split("#").length !=3){
				continue;
			}
			double sum1 = 0;
			for(Entry<String, Double> c:C1Map.entrySet()){
				if(c.getKey().split("#").length !=3){
					continue;
				}
				String cat1 = l.getKey().split("#")[0];
				if(c.getKey().split("#")[0].equals(cat1)){
					sum1 += c.getValue();
				}
				double c1value = Math.log(sum1 / (sum+1));
				Map<String,Double> map = new HashMap<String,Double>();
				map.put(cat1, c1value);
				C1ValueMap.put(l.getKey(), map);
			}
		}
		
		for(Entry<String, Double> c:C1Map.entrySet()){
			if(c.getKey().split("#").length !=3){
				continue;
			}
			String cat1 = c.getKey().split("#")[0];
			String cat2 = c.getKey().split("#")[1];
			String cat3 = c.getKey().split("#")[2];
			
			double sum12 = 0;
			double sum23 = 0;
			if(C1to2Map.containsKey(c.getKey())){
				for(Entry<String, Map<String, Double>> c1:C1to2Map.get(c.getKey()).entrySet()){
					for(Entry<String, Double> c2:c1.getValue().entrySet()){
						if(c1.getKey().equals(cat1)){
						}
						if(c1.getKey().equals(cat1)
								&& c2.getKey().equals(cat2)){
							sum12 += c2.getValue();
						}
						if(c1.getKey().equals(cat2)
								&& c2.getKey().equals(cat3)){
							sum23 += c2.getValue();
						}
					}
				}
				
				for(Entry<String, Map<String, Double>> c1:C1to2Map.get(c.getKey()).entrySet()){
					for(Entry<String, Double> c2:c1.getValue().entrySet()){
						if(c1.getKey().equals(cat1)
								&& c2.getKey().equals(cat2)){
							double value = Math.log(c2.getValue() / (sum12+1));
							if(C1to2ValueMap.containsKey(c.getKey())){
								if(C1to2ValueMap.get(c.getKey()).containsKey(cat1)){
									C1to2ValueMap.get(c.getKey()).get(cat1).put(cat2, value);
								}else{
									Map<String,Double> map = new HashMap<String,Double>();
									map.put(cat2, value);
									C1to2ValueMap.get(c.getKey()).put(cat1, map);
								}
							}else{
								Map<String,Double> map = new HashMap<String,Double>();
								map.put(cat2, value);
								Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
								map2.put(cat1, map);
								C1to2ValueMap.put(c.getKey(), map2);
							}
						}
						if(c1.getKey().equals(cat2)
								&& c2.getKey().equals(cat3)){
							sum23 += c2.getValue();
							double value = Math.log(c2.getValue() / (sum23+1));
							if(C1to2ValueMap.containsKey(c.getKey())){
								if(C1to2ValueMap.get(c.getKey()).containsKey(cat2)){
									C1to2ValueMap.get(c.getKey()).get(cat2).put(cat3, value);
								}else{
									Map<String,Double> map = new HashMap<String,Double>();
									map.put(cat3, value);
									C1to2ValueMap.get(c.getKey()).put(cat2, map);
								}
							}else{
								Map<String,Double> map = new HashMap<String,Double>();
								map.put(cat3, value);
								Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
								map2.put(cat2, map);
								C1to2ValueMap.put(c.getKey(), map2);
							}
						}
					}
				}
			}
		}
		
		/**word layer calculate*/
		
		double w1sum = 0;
		for(Entry<String, Map<String, Double>> c:CW1Map.entrySet()){
			String indcat = c.getKey();
			//first word layer
			for(Entry<String, Double> w:CW1Map.get(indcat).entrySet()){
				w1sum += w.getValue();
			}
			for(Entry<String, Double> w:CW1Map.get(indcat).entrySet()){
				double w1value = Math.log(w.getValue() / (w1sum+1));
				if(CW1ValueMap.containsKey(indcat)){
					CW1ValueMap.get(indcat).put(w.getKey(), w1value);
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(w.getKey(), w1value);
					CW1ValueMap.put(indcat, map);
				}
			}
			//second word layer
			for(Entry<String, Map<String, Double>> w:CW1to2Map.get(indcat).entrySet()){
				double w2sum = 0;
				for(Entry<String, Double> ww:w.getValue().entrySet()){
					w2sum += ww.getValue();
				}
				for(Entry<String, Double> ww:w.getValue().entrySet()){
					double w2value = Math.log(ww.getValue() / (w2sum+1));
					
					if(CW1to2ValueMap.containsKey(indcat)){
						if(CW1to2ValueMap.get(indcat).containsKey(w.getKey())){
							CW1to2ValueMap.get(indcat).get(w.getKey()).put(ww.getKey(), w2value);
						}else{
							Map<String,Double> map = new HashMap<String,Double>();
							map.put(ww.getKey(), w2value);
							CW1to2ValueMap.get(indcat).put(w.getKey(), map);
						}
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(ww.getKey(), w2value);
						Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
						map2.put(w.getKey(), map);
						CW1to2ValueMap.put(indcat, map2);
					}
				}
			}	
		}
	}

	/**construct industry classify model
	 * @param number 
	 * @param resourcePath 
	 * @throws IOException */
	private static void constructSerModel(String corpusModelPath) throws IOException {
		
		C1Map = new HashMap<String,Double>();
		C1to2Map = new HashMap<String,Map<String,Map<String,Double>>>();
		
		CW1Map = new HashMap<String,Map<String,Double>>();
		CW1to2Map = new HashMap<String,Map<String,Map<String,Double>>>();
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(corpusModelPath)),"utf-8"));
        String line = null;
        int num = 0;
        while((line = br.readLine()) != null){
        	String[] seg = line.split(",");
			if(seg.length ==10 ){
				
				if(!seg[1].matches("[0-9]*") && !seg[1].matches("[0-9]*") && seg[0].substring(0, 2).equals("201")){
					continue;
				}
				System.out.println(num++);
				
				String ind = seg[2]+"#"+seg[4]+"#"+seg[6];
				String[] inds = ind.split("#");
				String attr = null;
				String corpus = seg[8]+"。"+seg[9];
				

				if(C1Map.containsKey(ind)){
					C1Map.put(ind, C1Map.get(ind)+1.0);
				}else{
					C1Map.put(ind, 1.0);
				}
				//category item
				for(int i=0;i < inds.length-1;i++){

					//category layer
					if(C1to2Map.containsKey(ind)){
						if(C1to2Map.get(ind).containsKey(inds[i])){
							if(C1to2Map.get(ind).get(inds[i]).containsKey(inds[i+1])){
								C1to2Map.get(ind).get(inds[i]).put(inds[i+1], C1to2Map.get(ind).get(inds[i]).get(inds[i+1])+1.0);
							}else{
								C1to2Map.get(ind).get(inds[i]).put(inds[i+1], 1.0);
							}
						}else{
							Map<String,Double> map = new HashMap<String,Double>();
							map.put(inds[i+1], 1.0);
							C1to2Map.get(ind).put(inds[i], map);
						}
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(inds[i+1], 1.0);
						Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
						map2.put(inds[i], map);
						C1to2Map.put(ind, map2);
					}
					
				}
				
				//Word item
				List<Entry<String, Double>> sentMap = SentenceRankModel.sentenceRank(corpus,zbjdic,MP,vm);
				if(sentMap.isEmpty()){
					continue;
				}
				for(Entry<String, Double> s:sentMap){
					String cont = s.getKey();
					Double score = s.getValue();
					Result words = ToAnalysis.parse(cont,UserDefineLibrary.FOREST,zbjdic);
					List<Term> tag = new ArrayList<Term>();
					for(int m=0;m<words.size()-1;m++){

						if((!words.get(m).getNatureStr().contains("n")&& !words.get(m).getNatureStr().contains("v")&& !words.get(m).getNatureStr().matches("SER"))
								|| words.get(m).toString().split("/").length !=2
								|| words.get(m).getName().length()>=15
								|| words.get(m).getName().length()<=1){
							continue;
						}
						
						tag.add(words.get(m));
					}
					if(tag.size()<2){
						continue;
					}
					for(int n=0;n<tag.size()-1;n++){
						String t = tag.get(n).getName();
						String t2 = tag.get(n+1).getName();
						if(SerLibrary.containsKey(ind)){
							for(Entry<String, Map<String, Integer>> a:SerLibrary.get(ind).entrySet()){
								for(Entry<String, Integer> aa:a.getValue().entrySet()){
									if(aa.getKey().equals(t)){
										attr = a.getKey();
									}
								}
							}
						}
						attr = tag.get(n).getNatureStr();
						score = score * calculateScore(inds,t,attr);
						
						//one
						if(CW1Map.containsKey(ind)){
							if(CW1Map.get(ind).containsKey(t)){
								CW1Map.get(ind).put(t, CW1Map.get(ind).get(t) + score);
							}else{
								CW1Map.get(ind).put(t, score);
							}
						}else{
							Map<String,Double> map = new HashMap<String,Double>();
							map.put(t, score);
							CW1Map.put(ind, map);
						}
						
						//two
						if(CW1to2Map.containsKey(ind)){
							if(CW1to2Map.get(ind).containsKey(t)){
								if(CW1to2Map.get(ind).get(t).containsKey(t2)){
									CW1to2Map.get(ind).get(t).put(t2, CW1to2Map.get(ind).get(t).get(t2)+score);
								}else{
									CW1to2Map.get(ind).get(t).put(t2, score);
								}
								
							}else{
								Map<String,Double> map = new HashMap<String,Double>();
								map.put(t2, score);
								CW1to2Map.get(ind).put(t, map);
							}
						}else{
							Map<String,Double> map = new HashMap<String,Double>();
							map.put(t2, score);
							Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
							map2.put(t, map);
							CW1to2Map.put(ind, map2);
						}
					}
				}
			}
        }
		br.close();
	}

	/**construct base score*/
	private static double calculateScore(String[] inds, String t, String attr) {
		double score = 0.0;
		double P0 = 1;
		if(attr.equals("主题")
				|| attr.equals("类型")){
			P0 = 5;
		}
		if(attr.equals("热门")){
			P0 = 2;
		}
		double P1 = t.length();
		
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
				distance += indvec[i] * tagvec[i];
			}
			
			if(inds[k].equals(t)){
				distance +=100.0;
			}else if(inds[k].contains(t)){
				distance +=5.0;
			}
			
			score += P0*P1*(k+1)*distance;
		}
		
		return score;
	}

}
