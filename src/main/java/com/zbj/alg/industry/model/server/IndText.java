package com.zbj.alg.industry.model.server;

public class IndText {
    private String IndCatOne;     //行业一级类目信息
    private String IndCatTwo;     //行业二级类目信息
    private String id;     //服务、案例、订单的id
    private String singleCorpus;     //查询、案例、服务的文本内容(通常服务和案例只取title)
    private String corpusTitle; //订单标题
    private String corpusContent; //订单内容
    private Integer type;       //文本类型      1:查询 2:服务 3:案例 4:订单
    
	public String getIndCatOne() {
		return IndCatOne;
	}
	public void setIndCatOne(String indCatOne) {
		IndCatOne = indCatOne;
	}
	public String getIndCatTwo() {
		return IndCatTwo;
	}
	public void setIndCatTwo(String indCatTwo) {
		IndCatTwo = indCatTwo;
	}
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	public String getSingleCorpus() {
		return singleCorpus;
	}
	public void setSingleCorpus(String singleCorpus) {
		this.singleCorpus = singleCorpus;
	}
	public String getCorpusTitle() {
		return corpusTitle;
	}
	public void setCorpusTitle(String corpusTitle) {
		this.corpusTitle = corpusTitle;
	}
	public String getCorpusContent() {
		return corpusContent;
	}
	public void setCorpusContent(String corpusContent) {
		this.corpusContent = corpusContent;
	}
	public Integer getType() {
		return type;
	}
	public void setType(Integer type) {
		this.type = type;
	}
    
    
}
