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
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Map.Entry;

import org.nlpcn.commons.lang.tire.domain.Forest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbj.alg.industry.model.server.Tag;
import com.zbj.alg.industry.utils.Sort;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.ToAnalysis;


public class IndCategoryLanguageModelTrain {
	private static final Logger logger = LoggerFactory.getLogger(IndCategoryLanguageModelTrain.class);
	
	private static VectorModel vm = null;
	private static Forest inddic = new Forest();
	private static Forest serdic = new Forest();
	private static Forest zbjdic = new Forest();
	
	private static Map<String,Map<String,Double>> C1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> C1to2Map = null;
	private static Map<String,Map<String,Map<String,Double>>> C1to2ValueMap = null;
	
	private static Map<String,Map<String,Double>> CW1Map = null;
	private static Map<String,Map<String,Double>> CW1ValueMap = null;
	private static Map<String,Map<String,Map<String,Double>>> CW1to2Map = null;
	private static Map<String,Map<String,Map<String,Double>>> CW1to2ValueMap = null;
		
	private static Map<String,Map<String,Map<String,Integer>>> indTagLibrary = null;
	
	public static void main(String[] args) throws IOException{
		String resourcePath = "D:/Users/zbj/git/modelResource/";
		
		String vectorModelPath = resourcePath + "WordVec4GuideTag";
		String corpusModelPath = resourcePath + "IndOrderCorpus";
		
		String IndDicPath = resourcePath+"IndTagLibrary";
		String SerDicPath = resourcePath + "ServiceTagLibrary";
		String ZBJDicPath = resourcePath + "zbjsmall.dic";
		
		//initial dictionary and model
		ServiceSegModelEnhance.getInstance();
		InitialDictionary.insertIndDic(inddic,IndDicPath);
		InitialDictionary.insertSerDic(serdic,SerDicPath);
		InitialDictionary.insertZBJDic(zbjdic,ZBJDicPath);
		
		vm = VectorModel.loadFromFile(vectorModelPath);
		
		indTagLibrary = InitialDictionary.loadIndLibrary(resourcePath+"IndTagLibrary");
		
		//construct industry classify model
		constructIndModel(corpusModelPath);
		logger.info("construct industry classify model over");
		//calculate IndModel
		calculateIndModel(resourcePath);
		logger.info("calculate IndModel over");
		//output IndModel
		outputIndModel(resourcePath);
		logger.info("output IndModel over");
		//test IndModel
		testIndModel(resourcePath);
		logger.info("test IndModel over");
	}

	private static void testIndModel(String resourcePath) throws IOException {
		
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File("E:\\OntologyModel\\querywords.csv")), "gbk"));
        String line = null;
        int num = 0;
		Set<String> set = new HashSet<String>();
		while ((line = br.readLine()) != null){
        	String[] seg = line.split(",");
        	String title = seg[0];
        	if(seg.length ==4
        			&& !set.contains(title)
        			&& !title.matches("[0-9]*")
        			&& !title.matches("[a-z]*")
        			&& title.length() >=4
        			&& num++ < 100000){
            	set.add(title);
            	Map<String, List<String>> predictMap = predict(title);
            	System.out.println(predictMap+"\t"+title);
        	}
        }
		br.close();
	}

	private static Map<String, List<String>> predict(String title) throws IOException {
		Map<String,List<String>> resultMap = new HashMap<String,List<String>>();
		
		Result words = ToAnalysis.parse(title,UserDefineLibrary.FOREST,inddic);
		
		List<Term> tag = new ArrayList<Term>();
		for(int m=0;m<words.size()-1;m++){
			if((!words.get(m).getNatureStr().contains("n")&& !words.get(m).getNatureStr().contains("v")&& !words.get(m).getNatureStr().matches("IND"))
					|| words.get(m).toString().split("/").length !=2
					|| words.get(m).getName().length()>=15
					|| words.get(m).getName().length()<=1){
				continue;
			}
			tag.add(words.get(m));
		}
		
		Map<String,Double> predictMap = new HashMap<String,Double>();
		
		for(Entry<String, Map<String, Double>> c : C1ValueMap.entrySet()){
			String indcat = c.getKey();
			double score = 0;
			for(Entry<String, Double> cc:c.getValue().entrySet()){
				for(Entry<String, Double> ccc:C1to2ValueMap.get(indcat).get(cc.getKey()).entrySet()){
					score += cc.getValue() + ccc.getValue();
					if(tag.size()>=1 
							&&CW1ValueMap.containsKey(indcat)
							&& CW1ValueMap.get(indcat).containsKey(tag.get(0).getName())){
						score += CW1ValueMap.get(indcat).get(tag.get(0).getName());
						if(tag.size()>=2){
							for(int k=0;k<(tag.size()-1);k++){
								if(CW1to2ValueMap.containsKey(indcat) 
										&& CW1to2ValueMap.get(indcat).containsKey(tag.get(k).getName())
										&& CW1to2ValueMap.get(indcat).get(tag.get(k).getName()).containsKey(tag.get(k+1).getName())){
									score += CW1to2ValueMap.get(indcat).get(tag.get(k).getName()).get(tag.get(k+1).getName());
								}else{
									score += -10;
								}
							}
						}
					}else{						
						score += -10;
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
				List<String> listTag = new ArrayList<String>();
				String cat = re.getKey();
				if(indTagLibrary.containsKey(cat)){
					Map<String, Map<String, Integer>> attrTagMap = indTagLibrary.get(cat);
					Set<String> set = new HashSet<String>();
		 			for(Term t:words){
						for(Entry<String, Map<String, Integer>> attr:attrTagMap.entrySet()){
							if(attr.getValue().containsKey(t.getName())
									&& !set.contains(t.getName())){
								set.add(t.getName());
								Tag tw = new Tag();
								tw.setName(t.getName());
								tw.setProperty(attr.getKey());
								listTag.add(t.getName()+"/"+attr.getKey());
							}
						}
		 			}
				}
				if(listTag.size()>=1
						&& count++ >=0){
					resultMap.put(re.getKey()+"\t"+re.getValue(), listTag);
					break;
				}
			}
		}
		
		return resultMap;
	}

	private static void outputIndModel(String resourcePath) throws IOException {
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream(resourcePath+"IndLMCatModel"), "utf-8"), true);
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
					if(C2Map.containsKey(cc.getKey())){
						C2Map.get(cc.getKey()).put(ccc.getKey(), ccc.getValue());
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(ccc.getKey(), ccc.getValue());
						C2Map.put(cc.getKey(), map);
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
				pw.println("c12"+"\t"+c.getKey()+"#"+cc.getKey()+"\t"+c.getKey()+"\t"+cc.getKey()+"\t"+cc.getValue());
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

	private static void calculateIndModel(String resourcePath)  {
		
		//First
		C1ValueMap = new HashMap<String,Map<String,Double>>();
		C1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		
		CW1ValueMap = new HashMap<String,Map<String,Double>>();
		CW1to2ValueMap = new HashMap<String,Map<String,Map<String,Double>>>();
		
		double sum = 0;
		for(Entry<String, Map<String, Map<String, Double>>> l:C1to2Map.entrySet()){
			for(Entry<String, Map<String, Double>> ll:l.getValue().entrySet()){
				for(Entry<String, Double> lll:ll.getValue().entrySet()){
					sum += lll.getValue();
				}
			}
		}
		
		for(Entry<String, Map<String, Map<String, Double>>> c : C1to2Map.entrySet()){
			
			/**category layer calculate*/
			double sumcat1 = 0;
			for(Entry<String, Map<String, Double>> cc:c.getValue().entrySet()){
				double sumcat2 = 0;
				for(Entry<String, Map<String, Map<String, Double>>> l:C1to2Map.entrySet()){
					for(Entry<String, Map<String, Double>> ll:l.getValue().entrySet()){
						for(Entry<String, Double> lll:ll.getValue().entrySet()){
							if(ll.getKey().equals(cc.getKey())){
								sumcat2 += lll.getValue();
							}
						}
					}
				}
				
				sumcat1 += sumcat2;
				//first layer log value
				for(Entry<String, Double> ccc:cc.getValue().entrySet()){
					double value = Math.log(ccc.getValue() / (sumcat2+1));
					if(C1to2ValueMap.containsKey(c.getKey())){
						if(C1to2ValueMap.get(c.getKey()).containsKey(cc.getKey())){
							C1to2ValueMap.get(c.getKey()).get(cc.getKey()).put(ccc.getKey(), value);
						}else{
							Map<String,Double> map = new HashMap<String,Double>();
							map.put(ccc.getKey(), value);
							C1to2ValueMap.get(c.getKey()).put(cc.getKey(), map);
						}
					}else{
						Map<String,Double> map = new HashMap<String,Double>();
						map.put(ccc.getKey(), value);
						Map<String,Map<String,Double>> map2 = new HashMap<String,Map<String,Double>>();
						map2.put(cc.getKey(), map);
						C1to2ValueMap.put(c.getKey(), map2);
					}
				}
			}
			//second layer log value
			for(Entry<String, Map<String, Double>> cc:c.getValue().entrySet()){
				double c1value = Math.log(sumcat1 /(sum+1) );
				if(C1ValueMap.containsKey(c.getKey())){
					C1ValueMap.get(c.getKey()).put(cc.getKey(), c1value);
				}else{
					Map<String,Double> map = new HashMap<String,Double>();
					map.put(cc.getKey(), c1value);
					C1ValueMap.put(c.getKey(), map);
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
	 * @param corpusModelPath
	 * @throws IOException */
	private static void constructIndModel(String corpusModelPath) throws IOException {
		
		C1to2Map = new HashMap<String,Map<String,Map<String,Double>>>();
		
		CW1Map = new HashMap<String,Map<String,Double>>();
		CW1to2Map = new HashMap<String,Map<String,Map<String,Double>>>();
	    
	    BufferedReader br = new BufferedReader(new InputStreamReader(
                new FileInputStream(new File(corpusModelPath)),"utf-8"));
        String line = null;
        int num = 0;
        while((line = br.readLine()) != null){
        	String[] seg = line.split("\t");
			if(seg.length >=2 ){
				System.out.println(num++);
				String ind = seg[0];
				String[] inds = ind.split("#");
				String attr = null;
				String corpus = null;
				Double score = 1.0;

				if(seg[1].split("###").length == 2){
					corpus = seg[1].split("###")[1]; 
				}else{
					corpus = seg[1]; 
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
				Result words = ToAnalysis.parse(corpus,UserDefineLibrary.FOREST,inddic);
				List<Term> tag = new ArrayList<Term>();
				for(int m=0;m<words.size()-1;m++){
					if((!words.get(m).getNatureStr().contains("n")&& !words.get(m).getNatureStr().contains("v")&& !words.get(m).getNatureStr().matches("IND"))
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
					attr = tag.get(n).getNatureStr();
					//score = Math.log(score+1.0)*calculateScore(inds,t,attr);
					
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
		br.close();
	}

	/**construct base score*/
	private static double calculateScore(String[] inds, String t, String attr) {
		double score = 0.0;
		
		double P1 = 1.0;
		
		if(attr.equals("机构名称")){
			P1 += 20;
		}else if(attr.equals("机构类型")){
			P1 += 10;
		}else if(attr.equals("行业")){
			P1 += 5;
		}else if(attr.equals("主营")){
			P1 += 4;
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
				distance += indvec[i] * tagvec[i];
			}
			
			if(inds[k].equals(t)){
				distance +=100.0;
			}else if(inds[k].contains(t)){
				distance +=5.0;
			}
			
			score += P1*P2*(k+1)*(10*distance+0.5);
		}
		
		return score;
	}

}
