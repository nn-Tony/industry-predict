package com.zbj.alg.tag.model.crf;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class ModelParse {
	//通用版本
	int weightMatricNum = 0;//权重矩阵维数
	int extendtempleNum = 0;//扩展模板个数
	int labelNum = 0;
	double[] weight = null;
	String[] label = null;
	Template temp = null;//U00:%x[-2,0]
	ExpendTemplate extemp = null;//20 U00:_B-1 

	public String useTestViterbi(List<String> words){
		if(words.size()<1) return null;
		boolean debug = false;
		Map<String,Map<String,Integer>> tempFeatureIds = extemp.tempFeatureIds;
		Map<String,List<String>> featureTemplatesplit = temp.featureTemplatesplit;
		int idBigram = extemp.idBigram;//Bigram的id
		Tables table = new Tables(words);
		if(debug) System.out.println("---------------------");
		if(debug) System.out.println("words"+words);
		//Viterbi 权重和路径
		double[] Viscoreag = new double[labelNum];//Viterbi
		Map<Integer,String> Vipath = new HashMap<Integer,String>();//Viterbi
		
		double[][] bgramBEM = new double[labelNum][labelNum];
		for(int i=0; i<labelNum; i++){
			for(int j=0; j<labelNum; j++){
				bgramBEM[i][j] = weight[idBigram+(i*labelNum)+j];
			}
		}
		
		//transform
		Map<String, List<Double>> weightBEM = new HashMap<String, List<Double>>();
		for(int i=0; i<label.length; i++){
			String str = label[i];
			weightBEM.put(str, new ArrayList<Double>());
		}
		
		for(int i=0,length=words.size(); i<length; i++){
			
			Map<String,List<Double>> weightBEMtmp = new HashMap<String,List<Double>>();
			for(int j=0; j<label.length; j++){
				String str = label[j];
				weightBEMtmp.put(str, new ArrayList<Double>());
			}

			String word = "";
			if(words.get(i).contains("&&")){
				word = words.get(i).split("&&")[0];
			}else{
				word = words.get(i);
			}
			
			if(debug) System.out.println("word\t"+word);
			for(Map.Entry<String,List<String>> entry:featureTemplatesplit.entrySet()){
				String tagsu = entry.getKey();//U00
				List<String> templates = entry.getValue();
				StringBuffer sb = new StringBuffer(); 
				String template = "";
				for(int j=0,length1=templates.size(); j<length1; j++){
					template = templates.get(j);
					String xy = template.replace("[", "").replace("]", "");;
					int x = Integer.parseInt(xy.split(",")[0]);
					int y = Integer.parseInt(xy.split(",")[1]);
					if(!sb.toString().equals("")){
						sb.append("/");
					}
					sb.append(table.getTag(i+x , y));
				}
				//Map<String,Integer>featureIds = tempFeatureIds.getOrDefault(tagsu, new HashMap<String,Integer>());
				//
				Map<String,Integer> featureIds = new HashMap<String,Integer>();
				if(tempFeatureIds.containsKey(tagsu)){
					featureIds = tempFeatureIds.get(tagsu);
				}
				if(featureIds.size()>0){
					String tagFeature = sb.toString();
					if(featureIds.containsKey(tagFeature)){
						int id = featureIds.get(tagFeature);
						if(debug)System.out.println("match Temp\t"+id+"\t"+tagsu+"\t"+template+"\t"+tagFeature
								+"\tC:"+weight[id]+"\tN:"+weight[id+1]);
//						weightC.add(weight[id]);
//						weightN.add(weight[id+1]);
						for(int j=0; j<label.length; j++){
							String str = label[j];
							List<Double> score = weightBEMtmp.get(str);
							score.add(weight[id+j]);
							weightBEMtmp.put(str, score);
						}
					}
				}
			}
			
			double[] sunBEM = new double[label.length];
			for(int j=0; j<label.length; j++){
				sunBEM[j] = 0;
				String str = label[j];
				List<Double> score = weightBEMtmp.get(str);
				for(int k=0; k<score.size(); k++){
					sunBEM[j] += score.get(k);
				}
			}
			
			
			//---------------------  Viterbi  ---------------------------
			if(i<1){
				for(int j=0; j<labelNum; j++){
					Viscoreag[j] = sunBEM[j];
					Vipath.put((j+1), label[j]);
				}

			}else{
				double[] presBEM = new double[labelNum];
				for(int j=0; j<labelNum; j++){
					presBEM[j] = Viscoreag[j];
				}
				
				//transform
				Map<Integer,String> Vipathsaved = new HashMap<Integer,String>();
				for(int j=0; j<labelNum; j++){
					Vipathsaved.put((j+1), Vipath.get((j+1)));
				}
				for(int j=0; j<labelNum; j++){//到达状态j所有路径
					double[] skj = new double[labelNum];
					for(int k=0; k<labelNum; k++){
						skj[k] = presBEM[k] + bgramBEM[k][j] + sunBEM[j];
					}
					double maxScore = skj[0];
					int maxIdex = 0;
					for(int k=1; k<labelNum; k++){
						if(skj[k]>maxScore){
							maxScore = skj[k];
							maxIdex = k;
						}
					}
					String pathPre = Vipathsaved.get((maxIdex+1));
					Vipath.put((j+1), pathPre+"\t"+label[j]);
					Viscoreag[j] = maxScore;
				}
			}
			//------------------------------------------------------------
			
		}
		String tags = "";
		double maxScore = Viscoreag[0];
		int maxIndex = 0;
		for(int i=1; i<labelNum; i++){
			if(maxScore<Viscoreag[i]){
				maxScore = Viscoreag[i];
				maxIndex = i;
			}
		}
		tags = Vipath.get(maxIndex+1);
		if(debug)System.out.println("标签序列："+tags);
		return tags;
		
	}
	//app开发
	public String useTestViterbi_V1(List<String> words,Set<String> set){
		if(words.size()<1) return null;
		if(words.size()==1) return words.get(0).split("&&")[0];
		boolean debug = false;
		Map<String,Map<String,Integer>> tempFeatureIds = extemp.tempFeatureIds;
		Map<String,List<String>> featureTemplatesplit = temp.featureTemplatesplit;
		int idBigram = extemp.idBigram;//Bigram的id
		Tables table = new Tables(words);
		if(debug) System.out.println("---------------------");
		if(debug) System.out.println("words"+words);
		//Viterbi 权重和路径
		double[] Viscoreag = new double[labelNum];//Viterbi
		Map<Integer,String> Vipath = new HashMap<Integer,String>();//Viterbi
//		//Bigram 转移权值		
		double[][] bgramBEM = new double[labelNum][labelNum];
		for(int i=0; i<labelNum; i++){
			for(int j=0; j<labelNum; j++){
				bgramBEM[i][j] = weight[idBigram+(i*labelNum)+j];
			}
		}
		
		//transform
		Map<String, List<Double>> weightBEM = new HashMap<String,List<Double>>();
		for(int i=0; i<label.length; i++){
			String str = label[i];
			weightBEM.put(str, new ArrayList<Double>());
		}
		
		for(int i=0,length=words.size(); i<length; i++){
			
			Map<String,List<Double>> weightBEMtmp = new HashMap<String,List<Double>>();
			for(int j=0; j<label.length; j++){
				String str = label[j];
				weightBEMtmp.put(str, new ArrayList<Double>());
			}

			String word = "";
			if(words.get(i).contains("&&")){
				word = words.get(i).split("&&")[0];
			}else{
				word = words.get(i);
			}
			
			if(debug) System.out.println("word\t"+word);
			for(Map.Entry<String,List<String>> entry:featureTemplatesplit.entrySet()){
				String tagsu = entry.getKey();//U00
				List<String> templates = entry.getValue();
				StringBuffer sb = new StringBuffer(); 
				String template = "";
				for(int j=0,length1=templates.size(); j<length1; j++){
					template = templates.get(j);
					String xy = template.replace("[", "").replace("]", "");;
					int x = Integer.parseInt(xy.split(",")[0]);
					int y = Integer.parseInt(xy.split(",")[1]);
					if(!sb.toString().equals("")){
						sb.append("/");
					}
					sb.append(table.getTag(i+x , y));
				}
				//Map<String,Integer>featureIds = tempFeatureIds.getOrDefault(tagsu, new HashMap<String,Integer>());
				//
				Map<String,Integer> featureIds = new HashMap<String,Integer>();
				if(tempFeatureIds.containsKey(tagsu)){
					featureIds = tempFeatureIds.get(tagsu);
				}
				//
				if(featureIds.size()>0){
					String tagFeature = sb.toString();
					if(featureIds.containsKey(tagFeature)){
						int id = featureIds.get(tagFeature);
						if(debug)System.out.println("match Temp\t"+id+"\t"+tagsu+"\t"+template+"\t"+tagFeature
								+"\tC:"+weight[id]+"\tN:"+weight[id+1]);
						for(int j=0; j<label.length; j++){
							String str = label[j];
							List<Double> score = weightBEMtmp.get(str);
							score.add(weight[id+j]);
							weightBEMtmp.put(str, score);
						}
					}
				}
			}
			
			double[] sunBEM = new double[label.length];
			for(int j=0; j<label.length; j++){
				sunBEM[j] = 0;
				String str = label[j];
				List<Double> score = weightBEMtmp.get(str);
				for(int k=0; k<score.size(); k++){
					sunBEM[j] += score.get(k);
				}
			}
			
			
			//---------------------  Viterbi  ---------------------------
			if(i<1){
				for(int j=0; j<labelNum; j++){
					Viscoreag[j] = sunBEM[j];
					Vipath.put((j+1), label[j]);
				}
			}else{
				double[] presBEM = new double[labelNum];
				for(int j=0; j<labelNum; j++){
					presBEM[j] = Viscoreag[j];
				}
				
				//transform
				Map<Integer,String> Vipathsaved = new HashMap<Integer,String>();
				for(int j=0; j<labelNum; j++){
					Vipathsaved.put((j+1), Vipath.get((j+1)));
				}
				for(int j=0; j<labelNum; j++){//到达状态j所有路径
					double[] skj = new double[labelNum];
					for(int k=0; k<labelNum; k++){
						skj[k] = presBEM[k] + bgramBEM[k][j] + sunBEM[j];
					}
					double maxScore = skj[0];
					int maxIdex = 0;
					for(int k=1; k<labelNum; k++){
						if(skj[k]>maxScore){
							maxScore = skj[k];
							maxIdex = k;
						}
					}
					String pathPre = Vipathsaved.get((maxIdex+1));
					Vipath.put((j+1), pathPre+"\t"+label[j]);
					Viscoreag[j] = maxScore;
				}
			}
			//------------------------------------------------------------
			
		}
		String tags = "";
		double maxScore = Viscoreag[0];
		int maxIndex = 0;
		for(int i=1; i<labelNum; i++){
			if(maxScore<Viscoreag[i]){
				maxScore = Viscoreag[i];
				maxIndex = i;
			}
		}
		tags = Vipath.get(maxIndex+1);
		if(debug)System.out.println("标签序列："+tags);
		return tags;
		
	}
	public void parse(String path){
		temp = new Template();//U00:%x[-2,0]
		extemp = new ExpendTemplate();//20 U00:_B-1 
		try{
			BufferedReader br = new BufferedReader(new InputStreamReader(
	 				new FileInputStream(new File(path)),"utf-8"));
			//头文件
			String version = br.readLine();
			String costfactor = br.readLine();
			weightMatricNum = Integer.parseInt(br.readLine().split(": ")[1]);
			
			weight = new double[weightMatricNum];
			String xsize = br.readLine();
			//标签
			br.readLine();
			String Line = "";
			String str = "";
			while((str=br.readLine()).length()>0){
				Line += str;
				Line += "\t";
			}
			String[] Lines = Line.trim().split("\t");
			labelNum = Lines.length;
			extendtempleNum = (weightMatricNum-labelNum*labelNum)/labelNum+1;
			label = new String[labelNum];
			for(int i=0; i<labelNum; i++){
				label[i] = Lines[i];
			}
			//模板
			String line = "";
			int weightindex = 0;
			int hang = 0;
			while((line=br.readLine())!=null){
				hang++;
				if(line.split(" ").length>1&&line.split(" ")[1].toLowerCase().equals("b")&&line.length()>1){
					extemp.addBigramTag(line);
					continue;
				}
				if(line.toLowerCase().contains("u")){
					if(line.toLowerCase().contains("%x[")){//模板
						temp.addTemplateSplit(line);
					}else{//扩展模板
						extemp.addBigramTag1(line);
					}
				}else if( hang>extendtempleNum && !line.equals("")){
					weight[weightindex] = Double.parseDouble(line);
					weightindex++;
				}
				
				line = "";
			}
			br.close();
			System.out.println("parse ok");
		}catch(Exception e){
			System.out.println(e.getMessage());
		}
	}


}
