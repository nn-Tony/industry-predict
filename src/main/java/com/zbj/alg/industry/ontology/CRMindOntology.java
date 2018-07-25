package com.zbj.alg.industry.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;

public class CRMindOntology {
public static void main(String[] args) throws IOException{
		
		String Path = "http://www.zbj.com/industry/";
		
		OntModel ontModel = InitialConstructModel(Path);
		ontModel = OntologyModelGenerate(ontModel,Path);
		
		//写入文件
		FileOutputStream file = null;
		try {
		    file = new FileOutputStream("D:/Users/zbj/git/modelResource/OntologyModel/CRMIndustry.owl");
		} catch (FileNotFoundException e) {
		    System.out.println("文件不存在");
		    e.printStackTrace();
		}
		ontModel.write(file, "RDF/XML");

	}

	private static OntModel OntologyModelGenerate(OntModel ontModel, String Path) throws IOException {

		String testFile = "E:/tr_project_one/project/八戒软件项目/CRMindTag.ind";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(testFile)), "utf-8"));
		String line = null;
		int num = 0;
		while ((line = br.readLine()) != null){
			System.out.println(num++);
			
			String[] seg = line.split("\t");
			if(seg.length !=0){
				String cat1 = seg[0];
				
				Individual c1 = ontModel.createIndividual(Path+"/#"+cat1, ontModel.getOntClass(Path+"/#"+"一级分类"));
			}
		}
		br.close();
		
		return ontModel;
	}

	private static OntModel InitialConstructModel(String Path) {
		
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM);
		ontModel.setNsPrefix("tag", Path+"/#");
		ontModel.setNsPrefix("xsd", "http://www.w3.org/2001/XMLSchema#");
		//定义本体类
		OntClass c1 = ontModel.createClass(Path+"/#"+"一级分类");
//		OntClass c2 = ontModel.createClass(Path+"/#"+"二级分类");
//		OntClass c3 = ontModel.createClass(Path+"/#"+"行业");
//		OntClass c4 = ontModel.createClass(Path+"/#"+"产品");
//		OntClass c5 = ontModel.createClass(Path+"/#"+"主营");
//		OntClass c6 = ontModel.createClass(Path+"/#"+"机构类型");
//		OntClass c7 = ontModel.createClass(Path+"/#"+"机构名称");
//		OntClass c8 = ontModel.createClass(Path+"/#"+"热门");
//		OntClass c9 = ontModel.createClass(Path+"/#"+"职位");
//		OntClass c10 = ontModel.createClass(Path+"/#"+"地域");
//
//		//定义本体属性
//		ObjectProperty p1 = ontModel.createObjectProperty(Path+"/#"+ "Has_行业");
//		ObjectProperty p2 = ontModel.createObjectProperty(Path+"/#"+ "Has_产品");
//		ObjectProperty p3 = ontModel.createObjectProperty(Path+"/#"+ "Has_主营");
//		ObjectProperty p4 = ontModel.createObjectProperty(Path+"/#"+ "Has_机构类型");
//		ObjectProperty p5 = ontModel.createObjectProperty(Path+"/#"+ "Has_机构名称");
//		ObjectProperty p6 = ontModel.createObjectProperty(Path+"/#"+ "Has_热门");
//		ObjectProperty p7 = ontModel.createObjectProperty(Path+"/#"+ "Has_职位");
//		ObjectProperty p8 = ontModel.createObjectProperty(Path+"/#"+ "Has_地域");
//		ObjectProperty p9 = ontModel.createObjectProperty(Path+"/#"+ "Has_子类");
//		//定义数据类型属性
//		//DatatypeProperty dp = ontModel.createDatatypeProperty(Path+"/#"+"频率");
//		//属性关联
//		c1.addSubClass(c2);
//		c1.addProperty(p9, c2);
//		c2.addProperty(p1, c3);
//		c2.addProperty(p2, c4);
//		c2.addProperty(p3, c5);
//		c2.addProperty(p4, c6);
//		c2.addProperty(p5, c7);
//		c2.addProperty(p6, c8);
//		c2.addProperty(p7, c9);
//		c2.addProperty(p8, c10);
//		
		return ontModel;
	}
}
