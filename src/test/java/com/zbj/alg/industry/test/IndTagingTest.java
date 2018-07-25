package com.zbj.alg.industry.test;

import java.io.IOException;
import java.util.List;

import com.zbj.alg.industry.model.server.IndCat;
import com.zbj.alg.industry.model.server.IndTagGroup;
import com.zbj.alg.industry.model.server.IndTaging;
import com.zbj.alg.industry.model.server.IndTagingEnhancer;
import com.zbj.alg.industry.model.server.IndText;
import com.zbj.alg.industry.model.server.Tag;

public class IndTagingTest {

	/***
	 * 类目预测
	 * @throws IOException
	 */
	public static void main(String argv[]) throws IOException {
		String modelPath = "G:/Tanruib/modelresource/industry/";
		IndTaging text2IndTag = new IndTagingEnhancer(modelPath);
		String str = "餐饮牌店面设计,1.用途：餐饮门店（外卖模式）2.面积：25平米3.参考图纸：无4.服务内容：设计施工图O8.布局设计：功能分区要通。10.请先微信沟通，我的微信就是我的手机号码。";
		IndText text = new IndText();
		text.setSingleCorpus(str);//游戏手机电玩打鱼扫码下载
		text.setType(1);
		List<IndTagGroup> Tags = text2IndTag.getLabels(text, 2, 5, "bayes");
		for(IndTagGroup tag:Tags){
			IndCat cat = tag.getIndcat();
			System.out.println(cat.getIndCatOne_name()+"#"+cat.getIndCatTwo_name()+"\t"+cat.getPredictScore());
			for(Tag tt:tag.getTags()){
				System.out.println(tt.getProperty()+"\t"+tt.getName());
			}
			System.out.println("\n");
		}
	}
}