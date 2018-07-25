package com.zbj.alg.industry.demo;

/**
 * Created by octacon on 2017/10/11.
 */

import com.zbj.alg.industry.attention_model.IndLabel;
import com.zbj.alg.industry.attention_model.CategoryModel;

public class AttentionModelDemo {
    public static void main(String[] args) throws Exception {
        String modelPath = "./model";
        String data = "地产户外广告，时间比较紧";
        IndLabel indLabel = new CategoryModel("./model/");
        System.out.println(indLabel.getLabel(data));
    }
}

