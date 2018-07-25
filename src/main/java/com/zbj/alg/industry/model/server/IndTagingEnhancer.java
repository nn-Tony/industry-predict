package com.zbj.alg.industry.model.server;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import com.zbj.alg.def.common.Model;
import org.nlpcn.commons.lang.tire.domain.Forest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.zbj.alg.industry.attention_model.CategoryModel;
import com.zbj.alg.industry.model.InitialCatModel;
import com.zbj.alg.industry.model.InitialDictionary;
import com.zbj.alg.industry.utils.Sort;
import com.zbj.alg.nlp.crf.ModelParse;
import com.zbj.alg.nlp.server.CRF;
import com.zbj.alg.nlp.server.CRFEnhancer;
import com.zbj.alg.seg.domain.Nature;
import com.zbj.alg.seg.domain.Result;
import com.zbj.alg.seg.domain.Term;
import com.zbj.alg.seg.library.UserDefineLibrary;
import com.zbj.alg.seg.service.ServiceSegModelEnhance;
import com.zbj.alg.seg.splitWord.IndexAnalysis;

public class IndTagingEnhancer implements IndTaging,Model {

	private static Logger log = LoggerFactory.getLogger(IndTagingEnhancer.class);

	private static Forest inddic = new Forest();
	private static Forest serdic = new Forest();
	private static Forest zbjdic = new Forest();

	private static Map<String, Map<String, Map<String, Integer>>> indTagLibrary = null;

	private static Map<String, Map<String, Double>> categoryWordMap = null;
	private static Map<String, Double> categoryMap = null;
	private static Map<String, Double> wordMap = null;
	// 新增set存放行业标签
	private static Set<String> set0;
	private static Set<String> set;

	// CRF模型解析
	private ModelParse MP = new ModelParse();
	CRF crf = null;
//	private final String CRFmodelpath = "crf-corpus/crfmodel.txt";
	private final String IndTagFile = "IndTagLibrary";
	private final String IndCatModel = "IndCategoryModel";

	String SerDicPath = "ServiceTagLibrary";
	String ZBJDicPath = "zbjsmall.dic";
	String IndSimSerPath = "indWordsSiSer";

	// Attention model
	private CategoryModel categoryModel = null;

	public IndTagingEnhancer(String modelPath) throws IOException {
		if (!modelPath.endsWith(File.separator)) {
			modelPath += File.separator;
		}

		ServiceSegModelEnhance.getInstance();
		crf = new CRFEnhancer();

		set0 = InitialDictionary.loadServiceWords(modelPath + IndSimSerPath);
		set = InitialDictionary.insertIndDic(inddic, modelPath + IndTagFile);
		InitialDictionary.insertSerDic(serdic, modelPath + SerDicPath);
		InitialDictionary.insertZBJDic(zbjdic, modelPath + ZBJDicPath);
		log.info("insert dictionary success");

		indTagLibrary = InitialDictionary.loadIndLibrary(modelPath + IndTagFile);
		log.info("load industry library success");

		wordMap = InitialCatModel.loadWordMap(modelPath + IndCatModel);
		categoryMap = InitialCatModel.loadCategoryMap(modelPath + IndCatModel);
		categoryWordMap = InitialCatModel.loadCategoryWordMap(modelPath + IndCatModel);
		log.info("insert IndTagFile success");

//		String CRFmodelRealpath = modelPath + CRFmodelpath;
//		MP.parse(CRFmodelRealpath);
//		log.info("加载CRF查询解析模型完成");

//		categoryModel = new CategoryModel(modelPath + "attention_model/");
//		log.info("Attention模型加载完成");
	}

	/***
	 * convert text to industry tag
	 * @param text input corpus
	 * @param cateSize return the number of industry category
	 * @param labelSize return the number of industry tag in this category
	 * @param alg  可选 attention, bayes
	 * @return List<IndTagGroup>
	 */
	public List<IndTagGroup> getLabels(IndText text, int cateSize, int labelSize, String alg) {
		List<IndTagGroup> tagGroup = new ArrayList<IndTagGroup>();
		List<Term> tagWords = new ArrayList<Term>();
		Integer type = text.getType();
		switch (type) {
		case 1:// text type is query
			String cont = text.getSingleCorpus();
//			tagWords = takeBackWordsByCRF(cont, 30, 4000);
			tagWords = takeBackWordsByVOC(cont,500);

			if (alg.equals("bayes")) {
				List<IndCat> indcat = indCatPredict(tagWords, cateSize);
				for (IndCat cat : indcat) {
					IndTagGroup singleGroup = new IndTagGroup();
					List<Tag> listTag = new ArrayList<Tag>();
					listTag = indTagExtract(tagWords, cat, labelSize);
					if (listTag.size() >= 2) {// industry-2;attr-1
						singleGroup.setIndcat(cat);
						singleGroup.setTags(listTag);
						tagGroup.add(singleGroup);
					}
				}
			}
			if (alg.equals("attention")) {
				String data = text.getSingleCorpus();
				String label = categoryModel.getLabel(data);
				String[] names = label.split("#");
				IndCat indCat = new IndCat();
				indCat.setIndCatOne_name(names[0]);
				indCat.setIndCatTwo_name(names[1]);
				indCat.setPredictScore(0.0);
				IndTagGroup indTagGroup = new IndTagGroup();
				List<Tag> listTag = new ArrayList<Tag>();
				listTag = indTagExtract(tagWords, indCat, labelSize);
				indTagGroup.setIndcat(indCat);
				indTagGroup.setTags(listTag);
				tagGroup.add(indTagGroup);
			}
			break;

		case 2:// text type is service
			String cont2 = text.getSingleCorpus();
//			tagWords = takeBackWordsByCRF(cont2, 30, cont2.length());
			tagWords = takeBackWordsByVOC(cont2,500);

			if (alg.equals("bayes")) {
				List<IndCat> indcat = indCatPredict(tagWords, cateSize);
				for (IndCat cat : indcat) {
					IndTagGroup singleGroup = new IndTagGroup();
					List<Tag> listTag = new ArrayList<Tag>();
					listTag = indTagExtract(tagWords, cat, labelSize);
					if (listTag.size() >= 2) {// industry-2;attr1
						singleGroup.setIndcat(cat);
						singleGroup.setTags(listTag);
						tagGroup.add(singleGroup);
					}
				}
			}
			if (alg.equals("attention")) {
				String data = text.getSingleCorpus();
				String label = categoryModel.getLabel(data);
				String[] names = label.split("#");
				IndCat indCat = new IndCat();
				indCat.setIndCatOne_name(names[0]);
				indCat.setIndCatTwo_name(names[1]);
				indCat.setPredictScore(0.0);
				IndTagGroup indTagGroup = new IndTagGroup();
				List<Tag> listTag = new ArrayList<Tag>();
				listTag = indTagExtract(tagWords, indCat, labelSize);
				indTagGroup.setIndcat(indCat);
				indTagGroup.setTags(listTag);
				tagGroup.add(indTagGroup);
			}
			break;

		case 3:// text type is task
			String cont3 = text.getSingleCorpus();
//			tagWords = takeBackWordsByCRF(cont3, 30, 4000);
			tagWords = takeBackWordsByVOC(cont3,500);

			if (alg.equals("bayes")) {
				List<IndCat> indcat = indCatPredict(tagWords, cateSize);
				for (IndCat cat : indcat) {
					IndTagGroup singleGroup = new IndTagGroup();
					List<Tag> listTag = new ArrayList<Tag>();
					listTag = indTagExtract(tagWords, cat, labelSize);
					if (listTag.size() >= 2) {// industry-2;attr1
						singleGroup.setIndcat(cat);
						singleGroup.setTags(listTag);
						tagGroup.add(singleGroup);
					}
				}
			}
			if (alg.equals("attention")) {
				String data = text.getSingleCorpus();
				String label = categoryModel.getLabel(data);
				String[] names = label.split("#");
				IndCat indCat = new IndCat();
				indCat.setIndCatOne_name(names[0]);
				indCat.setIndCatTwo_name(names[1]);
				indCat.setPredictScore(0.0);
				IndTagGroup indTagGroup = new IndTagGroup();
				List<Tag> listTag = new ArrayList<Tag>();
				listTag = indTagExtract(tagWords, indCat, labelSize);
				indTagGroup.setIndcat(indCat);
				indTagGroup.setTags(listTag);
				tagGroup.add(indTagGroup);
			}
			break;

		case 4:// text type is case
			String cont4 = text.getSingleCorpus();
//			tagWords = takeBackWordsByCRF(cont4, 30, 4000);
			tagWords = takeBackWordsByVOC(cont4,500);

			if (alg.equals("bayes")) {
				List<IndCat> indcat = indCatPredict(tagWords, cateSize);
				for (IndCat cat : indcat) {
					IndTagGroup singleGroup = new IndTagGroup();
					List<Tag> listTag = new ArrayList<Tag>();
					listTag = indTagExtract(tagWords, cat, labelSize);
					if (listTag.size() >= 2) {// industry-2;attr1
						singleGroup.setIndcat(cat);
						singleGroup.setTags(listTag);
						tagGroup.add(singleGroup);
					}
				}
			}
			if (alg.equals("attention")) {
				String data = text.getSingleCorpus();
				String label = categoryModel.getLabel(data);
				String[] names = label.split("#");
				IndCat indCat = new IndCat();
				indCat.setIndCatOne_name(names[0]);
				indCat.setIndCatTwo_name(names[1]);
				indCat.setPredictScore(0.0);
				IndTagGroup indTagGroup = new IndTagGroup();
				List<Tag> listTag = new ArrayList<Tag>();
				listTag = indTagExtract(tagWords, indCat, labelSize);
				indTagGroup.setIndcat(indCat);
				indTagGroup.setTags(listTag);
				tagGroup.add(indTagGroup);
			}
			break;
		}
		return tagGroup;
	}

	/***
	 * label recall based on vocabulary
	 * @param cont
	 * @param maxNum
	 * @return
	 */
	private List<Term> takeBackWordsByVOC(String cont, Integer maxNum) {
		List<Term> catWords = new ArrayList<Term>();
		List<Term> tagWords = new ArrayList<Term>();
		String cont1 = cont.replaceAll("\\s*|\t|\r|\n", "");
		if (cont1.length() > maxNum) {
			cont1 = cont1.substring(0, maxNum);
		}

		Result words = IndexAnalysis.parse(cont1, UserDefineLibrary.FOREST, inddic);
		for (Term w : words) {
			if (w.toString().split("/").length != 2 || w.getName().length() >= 15 || w.getName().length() <= 1) {
				continue;
			} else if (set.contains(w.getName())) {
				catWords.add(w);
			}
		}
		for (Term c : catWords) {
			if (set0.contains(c.getName())) {
				continue;
			} else {
				tagWords.add(c);
			}
		}
		if (tagWords.isEmpty()) {
			return catWords;
		} else {
			return tagWords;
		}
	}

	/***
	 * label recall based on CRF
	 * @param cont
	 * @param minNum
	 * @param maxNum
	 * @return
	 */
	private List<Term> takeBackWordsByCRF(String cont, Integer minNum, Integer maxNum) {
		
		List<Term> catWords = new ArrayList<Term>();
		List<Term> tagWords = new ArrayList<Term>();
		String cont1 = cont.replaceAll("\\s*|\t|\r|\n", "");

		if (cont1.length() >= minNum && cont1.length() <= maxNum) {
			Map<Term, String> queryAttribute = crf.getWordLabel(cont1, inddic, MP);
			for (Entry<Term, String> qa : queryAttribute.entrySet()) {
				if (qa.getKey().getName().length() >= 15 || qa.getKey().getName().length() <= 1) {
					continue;
				} else if (set.contains(qa.getKey().getName())) {
					tagWords.add(qa.getKey());
				}
			}
		} else if (cont1.length() > maxNum) {
			String cont2 = cont1.substring(0, maxNum);
			Map<Term, String> queryAttribute = crf.getWordLabel(cont2, inddic, MP);
			for (Entry<Term, String> qa : queryAttribute.entrySet()) {
				if (qa.getKey().getName().length() >= 15 || qa.getKey().getName().length() <= 1
						|| qa.getValue().split("##")[0].equals("TH")) {
					continue;
				} else if (set.contains(qa.getKey().getName())) {// &&// !(qa.getValue().split("##")[0].equals("TH"))
																	
					tagWords.add(qa.getKey());
				}
			}
		} else {
			Result Words = IndexAnalysis.parse(cont1, UserDefineLibrary.FOREST, inddic);
			Map<Term, String> queryAttribute = crf.getWordLabel(cont1, inddic, MP);
			int flagID = 0;
			int flagTH = 0;
			for (Term w : Words) {
				for (Entry<Term, String> qa : queryAttribute.entrySet()) {
					if (qa.getKey().getName().equals(w.getName())) {
						w.setNature(new Nature(qa.getValue()));
						if (w.getNatureStr().equals("ID")) {
							flagID = 1;
						} else if (w.getNatureStr().equals("TH")) {
							flagTH = 1;
						}
					}
				}
				if (w.toString().split("/").length != 2 || w.getName().length() >= 15 || w.getName().length() <= 1) {
					continue;
				} else if (set.contains(w.getName())) {
					catWords.add(w);
				}
			}
			if (flagID == 1 && flagTH == 1) {
				for (Term cw : catWords) {
					if (!(cw.getNatureStr().equals("TH"))) {
						tagWords.add(cw);
					}
				}
			} else {
				tagWords = catWords;
			}
		}
		return tagWords;
	}

	/***
	 * extract industry tags
	 * @param tagWords
	 * @param c
	 * @param labelSize
	 * @return
	 */
	private List<Tag> indTagExtract(List<Term> tagWords, IndCat c, int labelSize) {

		List<Tag> listTag = new ArrayList<Tag>();
		Tag tc1 = new Tag();
		tc1.setName(c.getIndCatOne_name());
		tc1.setProperty("一级行业类目");
		tc1.setScore(c.getPredictScore());
		listTag.add(tc1);

		Tag tc2 = new Tag();
		tc2.setName(c.getIndCatTwo_name());
		tc2.setProperty("二级行业类目");
		tc2.setScore(c.getPredictScore());
		listTag.add(tc2);

		String cat = c.getIndCatOne_name() + "#" + c.getIndCatTwo_name();
		if (indTagLibrary.containsKey(cat)) {
			Map<String, Map<String, Integer>> attrTagMap = indTagLibrary.get(cat);
			Set<String> set = new HashSet<String>();
			for (Term t : tagWords) {
				for (Entry<String, Map<String, Integer>> attr : attrTagMap.entrySet()) {
					if (attr.getValue().containsKey(t.getName()) && !set.contains(t.getName())) {
						set.add(t.getName());
						Tag tw = new Tag();
						tw.setName(t.getName());
						tw.setProperty(attr.getKey());
						tw.setScore(attr.getValue().get(t.getName()) + 0.0);
						listTag.add(tw);
					}
				}
			}
		}
		return listTag;
	}

	/***
	 * industry category predict
	 * @param catWords
	 * @param cateSize
	 * @return
	 */
	private List<IndCat> indCatPredict(List<Term> catWords, int cateSize) {

		Map<String, Double> predictMap = new HashMap<String, Double>();
		if (catWords.size() > 0) {
			Double allcount = 0.0;
			for (Term qw : catWords) {
				if (wordMap.containsKey(qw.getName())) {
					allcount += wordMap.get(qw.getName());
				}
			}
			
			for (Entry<String, Double> c : categoryMap.entrySet()) {
				Double sum = 0.0;
				Double pc = c.getValue();

				if (categoryWordMap.containsKey(c.getKey())) {
					int count = 0;
					Set<String> relatedWord = new HashSet<String>();
					for (Term qw : catWords) {
						if (categoryWordMap.get(c.getKey()).containsKey(qw.getName())) {
							Double parameter = 0.0;
							if (wordMap.containsKey(qw.getName())) {
								// weaken the low-frequency words
								parameter = wordMap.get(qw.getName()) / allcount;
								parameter += parameter;
								// Strengthen the category word
								if (c.getKey().contains(qw.getName())) {
									parameter += Math.exp(-count++) * 10 * parameter;
								}
							}
							// if (c.getKey().contains(qw.getName())) {
							relatedWord.add(qw.getName());
							// }
							Double pwc = categoryWordMap.get(c.getKey()).get(qw.getName());
							sum += pwc * 1 / (parameter + 1);// 考虑优化Math.log(arg0)
						} else {
							sum += -13.0;
						}
					}

					String cat = "";
					if (relatedWord.isEmpty()) {
						cat = c.getKey();
					} else {
						cat = catLayerPredict(c.getKey(), relatedWord);
					}
					predictMap.put(cat, pc + sum);

				}
			}
		}
		// sort
		List<Map.Entry<String, Double>> predictMapSort = null;
		// 装载输出
		List<IndCat> predictCatgoryList = new ArrayList<IndCat>();
		if (!predictMap.isEmpty()) {
			predictMapSort = Sort.sortMap(predictMap);
			int count = 0;
			Double tmpScore = 0.0;
			for (Entry<String, Double> p : predictMapSort) {
				count++;
				if (count > cateSize || (tmpScore < 0 && (tmpScore - p.getValue()) > 10)) {
					break;
				}
				tmpScore = p.getValue();
				IndCat cat = new IndCat();
				String[] Name = p.getKey().split("#");
				if (Name.length == 2) {
					cat.setIndCatOne_name(Name[0]);
					cat.setIndCatTwo_name(Name[1]);
					cat.setPredictScore(p.getValue());
					predictCatgoryList.add(cat);
				} else {
					cat.setIndCatOne_name(Name[0]);
					cat.setPredictScore(p.getValue());
					predictCatgoryList.add(cat);
				}
			}
		} else {
			IndCat cat = new IndCat();
			cat.setIndCatOne_name("其他");
			cat.setIndCatTwo_name("其他");
			cat.setPredictScore(-100.0);
			predictCatgoryList.add(cat);
		}

		return predictCatgoryList;
	}

	/***
	 * category layer predict
	 * @param cat
	 * @param relatedWord
	 * @return
	 */
	private String catLayerPredict(String cat, Set<String> relatedWord) {
		String ret = "";
		String cat1 = cat.split("#")[0];
		for (String s : relatedWord) {
			if (!(cat1.equals(s))) {
				ret = cat;
				break;
			} else {
				ret = cat1;
			}
		}
		return ret;
	}

	/***
	 * get the industry tags
	 * @param text
	 * @return
	 */
	@Override
	public List<Tag> getIndTags(IndText text) {
		
		String cont = text.getSingleCorpus();
		if(cont.length() > 10000){
			cont = cont.substring(0,10000);
		}
		String cont1 = cont.replaceAll("\\s*|\t|\r|\n", "");
		
		List<String> tagList = new ArrayList<String>();
		List<Tag> indTags = new ArrayList<Tag>();
		Map<String,List<String>> map = new HashMap<String,List<String>>();

		Result words = IndexAnalysis.parse(cont1, UserDefineLibrary.FOREST, inddic);
		for (Term w : words) {
			if (w.toString().split("/").length != 2 || w.getName().length() >= 15 || w.getName().length() <= 1) {
				continue;
			} else if (set.contains(w.getName())) {
				if(!(tagList.contains(w.getName()))){
					tagList.add(w.getName());
				}	
			}
		}	
		
		for(Entry<String, Map<String, Map<String, Integer>>> ind:indTagLibrary.entrySet()){
			Map<String, Map<String, Integer>> attrTagMap = indTagLibrary.get(ind.getKey());
			for(String tl:tagList){
				for (Entry<String, Map<String, Integer>> attr : attrTagMap.entrySet()) {
					if(attr.getValue().containsKey(tl)){
						if(map.containsKey(attr.getKey())){
							if(map.get(attr.getKey()).contains(tl)){
								continue;
							}else{
								map.get(attr.getKey()).add(tl);
								Tag tag = new Tag();
								tag.setProperty(attr.getKey());
								tag.setName(tl);
								indTags.add(tag);
							}
						}else{
							List<String> list = new ArrayList<String>();
							list.add(tl);
							map.put(attr.getKey(), list);
							Tag tag = new Tag();
							tag.setProperty(attr.getKey());
							tag.setName(tl);
							indTags.add(tag);
						}
					}
				}
			}
		}
		return indTags;
	}

	/***
	 * llt version needs
	 */
	@Override
	public void destroy() {

	}
}