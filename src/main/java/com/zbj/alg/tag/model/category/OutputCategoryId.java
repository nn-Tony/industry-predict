package com.zbj.alg.tag.model.category;

public class OutputCategoryId extends OutputCategory{
	private Integer categoryId1;
	private Integer categoryId2;
	private Integer categoryId3;
	
	public OutputCategoryId(){}
	public OutputCategoryId(OutputCategory outputCategory,Integer categoryId1,Integer categoryId2,Integer categoryId3){
		this.categoryId1 = categoryId1;
		this.categoryId2 = categoryId2;
		this.categoryId3 = categoryId3;
		this.setCategoryName1(outputCategory.getCategoryName1());
	}
	
	public Integer getCategoryId1(){
		return categoryId1;
	}
	public void setCategoryId1(Integer cateId1){
		categoryId1 = cateId1;
	}
	
	public Integer getCategoryId2(){
		return categoryId2;
	}
	public void setCategoryId2(Integer cateId2){
		categoryId2 = cateId2;
	}
	
	public Integer getCategoryId3(){
		return categoryId3;
	}
	public void setCategoryId3(Integer cateId3){
		categoryId3 = cateId3;
	}
	
	
}
