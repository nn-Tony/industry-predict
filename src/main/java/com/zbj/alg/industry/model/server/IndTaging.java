package com.zbj.alg.industry.model.server;

import java.util.List;

public interface IndTaging {

	/***
	 * convert text to industry classes
	 * @param text
	 * @param cateSize
	 * @param labelSize
	 * @param alg  可选 attention, bayes
	 * @return industry classes and tags
	 */
	List<IndTagGroup> getLabels(IndText text, int cateSize, int labelSize, String alg);
	
	/**
	 * convert text to industry tags
	 * @param text
	 * @return tags
	 */
	List<Tag> getIndTags(IndText text);
	
}
