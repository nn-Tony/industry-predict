package com.zbj.alg.tag.model.category;

/**
 * Created by ChenCheng on 2016/8/24.
 * 类目对象
 */
public class Category {
    private String categoryOne_name;
    private String categoryOne_id;

    private String categoryTwo_name;
    private String categoryTwo_id;

    private String categoryThree_name;
    private String categoryThree_id;

    private Double predictScore;
    
    public Double getPredictScore() {
		return predictScore;
	}

	public void setPredictScore(Double predictScore) {
		this.predictScore = predictScore;
	}

	public String getCategoryOne_name() {
        return categoryOne_name;
    }

    public void setCategoryOne_name(String categoryOne_name) {
        this.categoryOne_name = categoryOne_name;
    }

    public String getCategoryOne_id() {
        return categoryOne_id;
    }

    public void setCategoryOne_id(String categoryOne_id) {
        this.categoryOne_id = categoryOne_id;
    }

    public String getCategoryTwo_name() {
        return categoryTwo_name;
    }

    public void setCategoryTwo_name(String categoryTwo_name) {
        this.categoryTwo_name = categoryTwo_name;
    }

    public String getCategoryTwo_id() {
        return categoryTwo_id;
    }

    public void setCategoryTwo_id(String categoryTwo_id) {
        this.categoryTwo_id = categoryTwo_id;
    }

    public String getCategoryThree_name() {
        return categoryThree_name;
    }

    public void setCategoryThree_name(String categoryThree_name) {
        this.categoryThree_name = categoryThree_name;
    }

    public String getCategoryThree_id() {
        return categoryThree_id;
    }

    public void setCategoryThree_id(String categoryThree_id) {
        this.categoryThree_id = categoryThree_id;
    }
}
