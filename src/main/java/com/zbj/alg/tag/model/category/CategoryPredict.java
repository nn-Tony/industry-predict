package com.zbj.alg.tag.model.category;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import org.nlpcn.commons.lang.tire.domain.Forest;

import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.splitWord.IndexAnalysis;
import com.zbj.alg.tag.model.crf.ModelParse;

public class CategoryPredict {
	
	/***
	 * 查询词属性解析
	 * @param query 查询词
	 * @param MP
	 * @param zbjdic 
	 * @return queryAttribute 查询词分词后由词，词性，属性
	 * @attribute TH-主题   ID-身份   DE-要求   ST-风格   TY-类型   OT-其他
	 */
	public static Map<Term, String> getWordLabel(String query, Forest zbjdic, ModelParse MP) {
		Map<Term,String> queryAttribute = new LinkedHashMap<Term,String>();
		
		List<String> queryNature = new ArrayList<String>();
		String newquery = query.replaceAll("\\s*|\t|\r|\n", "");
		if(newquery.equals("")) {  // 输入为空或全为特殊字符
			return queryAttribute; // 返回空map
		}
		Result parse = IndexAnalysis.parse(newquery,UserDefineLibrary.FOREST, zbjdic);
		for(Term t : parse) {
			queryNature.add(t.getName() + "&&" + t.getNatureStr());
		}
		String labelstr = MP.useTestViterbi(queryNature); // 对query分词后的各个词打标签
		if(labelstr.equals("")) {  // 标签集为空
			return queryAttribute; // 返回空map
		}
		
		String[] labels = labelstr.split("\t");
		for(int i=0,length=labels.length; i<length; i++){
			Term term = parse.get(i);
			if(!queryAttribute.containsKey(term)){
				queryAttribute.put(term, labels[i]);
			}
		}
		
		return queryAttribute;
	}
	
	/***
	 * 短查询处理
	 * @param queryWords
	 * @param wordProbMap 
	 * @param categoryProbMap 
	 * @param categoryWordProbMap 
	 * @return
	 */
	public static Map<String, Double> shortQueryPredict(Result queryWords, 
			Map<String, Double> wordProbMap, 
			Map<String, Double> categoryProbMap, 
			Map<String, Map<String, Double>> categoryWordProbMap) {
		Map<String,Double> predictMap = new HashMap<String,Double>();
		Double allcount = 0.0;
		for(Term qw:queryWords){
			if(wordProbMap.containsKey(qw.getName())
					&& queryWords.size()>1){
				allcount += wordProbMap.get(qw.getName());
			}
		}
		
		for(Entry<String, Double> c:categoryProbMap.entrySet()){
			Double sum = 0.0;
			Double pc = c.getValue();
			if (c.getKey().equals("其它")) {
				sum += -100;
			}
			if(categoryWordProbMap.containsKey(c.getKey())){
				for(Term qw:queryWords){
					if(categoryWordProbMap.get(c.getKey()).containsKey(qw.getName())){
						Double parameter = 0.0;
						if(wordProbMap.containsKey(qw.getName())
								&& queryWords.size()>1 ){
							parameter = wordProbMap.get(qw.getName())/allcount;
							//低频去词
							if(qw.getNatureStr().contains("C")){
								parameter += 3*parameter;
							}
							if(c.getKey().contains(qw.getName())){
								parameter += 2*parameter;
							}
							if(qw.getNatureStr().matches("[A-Z]*")){
								parameter += parameter;
							}
						}
						Double pwc = categoryWordProbMap.get(c.getKey()).get(qw.getName());
						sum  += pwc*1/(parameter+1);
					}else{
					sum  += -20.0;
					}
				}
				predictMap.put(c.getKey(), pc + sum);
			}
		}
		
		return predictMap;
	}
	/***
	 * 长查询处理
	 * @param queryWords
	 * @param categoryWordProbMap 
	 * @param categoryProbMap 
	 * @param wordProbMap 
	 * @return
	 */
	public static Map<String, Double> langQueryPredict(Result queryWords, Map<String, Double> wordProbMap, Map<String, Double> categoryProbMap, Map<String, Map<String, Double>> categoryWordProbMap) {
		Map<String,Double> predictMap = new HashMap<String,Double>();
		Double allcount = 0.0;
		Set <String> set = new HashSet<String>();
		for(Term qw:queryWords){
			if(qw.getName().length() < 2 || 
					(!qw.getNatureStr().matches("[A-Z]*")
							&& !qw.getNatureStr().matches("zbj")
							&& !qw.getNatureStr().contains("n")
							&& !qw.getNatureStr().contains("v"))){
				continue;
			}
			if(set.contains(qw.getName())){
				continue;
			}
			set.add(qw.getName());
			if(wordProbMap.containsKey(qw.getName())){
				allcount += wordProbMap.get(qw.getName());
			}
		}
		
		for(Entry<String, Double> c:categoryProbMap.entrySet()){
			Double sum = 0.0;
			if (c.getKey().equals("其它")) {
				sum += -100;
			}
			if(categoryWordProbMap.containsKey(c.getKey())){
				Set <String> set1 = new HashSet<String>();
				for(Term qw:queryWords){
					//词性去词
					if(qw.getName().length() < 2 || 
							(!qw.getNatureStr().matches("[A-Z]*")
									&& !qw.getNatureStr().matches("zbj")
									&& !qw.getNatureStr().contains("n")
									&& !qw.getNatureStr().contains("v"))){
						continue;
					}
					if(set1.contains(qw.getName())){
						continue;
					}
					set1.add(qw.getName());
					//低频去词
					Double parameter = 0.0;
					if(wordProbMap.containsKey(qw.getName())){
						parameter = wordProbMap.get(qw.getName())/allcount;
						if(parameter < 0.1){
							continue;
						}
					}
					if(categoryWordProbMap.get(c.getKey()).containsKey(qw.getName())){
						if(wordProbMap.containsKey(qw.getName())){
							//System.out.println(allcount+" "+wordProbMap.get(qw.getName())+qw.getName()+parameter);
							if(qw.getNatureStr().contains("C")){
								parameter += 3*parameter;
							}
							if(c.getKey().contains(qw.getName())){
								parameter += 2*parameter;
							}
							if(qw.getNatureStr().matches("[A-Z]*")){
								parameter += parameter;
							}
						}
						Double pwc = categoryWordProbMap.get(c.getKey()).get(qw.getName());
						sum  += pwc*1/(parameter+1);
					}else{
					sum  += -20.0;
					}
				}
				
				Double pc = c.getValue();
				//System.out.println(c.getKey()+"\t"+pc + sum);
				predictMap.put(c.getKey(), pc + sum);
			}
		}
		
		return predictMap;
	}

}
