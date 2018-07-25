package com.zbj.alg.industry.ontology;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.jena.datatypes.RDFDatatype;
import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.ontology.DatatypeProperty;
import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.query.Dataset;
import org.apache.jena.query.DatasetFactory;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;

public class IndustryOntology {

	public static void main(String[] args) throws IOException{
		
		String Path = "http://www.zbj.com/industry/";
		
		OntModel ontModel = InitialConstructModel(Path);
		
		ontModel = OntologyModelGenerate(ontModel,Path);
		
		//写入文件
		FileOutputStream file = null;
		try {
		    file = new FileOutputStream("E:/tr_project_one/project/IndustryTagLibrary/Industry.owl");
		} catch (FileNotFoundException e) {
		    System.out.println("文件不存在");
		    e.printStackTrace();
		}
		ontModel.write(file, "RDF/XML");
	}

	private static OntModel OntologyModelGenerate(OntModel ontModel, String Path) throws IOException {

		String testFile = "E:/tr_project_one/project/IndustryTagLibrary/IndTagLibrary.new";
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(new File(testFile)), "utf-8"));
		RDFDatatype datatype =  XSDDatatype.XSDstring;
		String line = null;
		int num = 0;
		while ((line = br.readLine()) != null){
			System.out.println(num++);
			
			String[] seg = line.split("\t");
			if(seg.length >=4  
					&& seg[1].split("#").length ==2){
				String cat1 = seg[1].split("#")[0];
				String cat2 = seg[1].split("#")[1];
				String attr = seg[2];
				
				Individual c1 = ontModel.createIndividual(Path+"/#"+cat1, ontModel.getOntClass(Path+"/#"+"一级类目"));
				Individual c2 = ontModel.createIndividual(Path+"/#"+cat2, ontModel.getOntClass(Path+"/#"+"二级类目"));
				c1.addProperty(ontModel.getProperty(Path+"/#"+ "Has_子类"), c2);
				
				Property P = ontModel.getProperty(Path+"/#"+"Has_"+attr);
//				Property Fre = ontModel.createDatatypeProperty(Path+"/#"+"Has_"+cat1+"_"+cat2+"_频率");

				for(int i=3;i<seg.length;i++){

					String tag = seg[i].split("/")[0];
					String fre = seg[i].split("/")[1];
					tag = tag.replaceAll("[\\p{Punct}\\p{Space}]+", "");
					Individual B = ontModel.createIndividual(Path+"/#"+tag, ontModel.getOntClass(Path+"/#"+attr));
					
					c2.addProperty(P, B);
//					B.addProperty(Fre, fre, datatype);
				}
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
		OntClass c0 = ontModel.createClass(Path+"/#"+"Industry");
		OntClass c1 = ontModel.createClass(Path+"/#"+"一级类目");
		OntClass c2 = ontModel.createClass(Path+"/#"+"二级类目");
		OntClass c3 = ontModel.createClass(Path+"/#"+"行业");
		OntClass c4 = ontModel.createClass(Path+"/#"+"产品");
		OntClass c5 = ontModel.createClass(Path+"/#"+"主营");
		OntClass c6 = ontModel.createClass(Path+"/#"+"机构类型");
		OntClass c7 = ontModel.createClass(Path+"/#"+"热门");
		OntClass c8 = ontModel.createClass(Path+"/#"+"职位");

		//定义本体属性
		ObjectProperty p1 = ontModel.createObjectProperty(Path+"/#"+ "Has_行业");
		ObjectProperty p2 = ontModel.createObjectProperty(Path+"/#"+ "Has_产品");
		ObjectProperty p3 = ontModel.createObjectProperty(Path+"/#"+ "Has_主营");
		ObjectProperty p4 = ontModel.createObjectProperty(Path+"/#"+ "Has_机构类型");
		ObjectProperty p5 = ontModel.createObjectProperty(Path+"/#"+ "Has_热门");
		ObjectProperty p6 = ontModel.createObjectProperty(Path+"/#"+ "Has_职位");
		ObjectProperty p7 = ontModel.createObjectProperty(Path+"/#"+ "Has_子类");
		//定义数据类型属性
//		DatatypeProperty dp = ontModel.createDatatypeProperty(Path+"/#"+"Has_频率");
		//属性关联
		c0.addSubClass(c1);
		c1.addSubClass(c2);
		c2.addSubClass(c3);
		c2.addSubClass(c4);
		c2.addSubClass(c5);
		c2.addSubClass(c6);
		c2.addSubClass(c7);
		c2.addSubClass(c8);
		c1.addProperty(p7, c2);
		c2.addProperty(p1, c3);
		c2.addProperty(p2, c4);
		c2.addProperty(p3, c5);
		c2.addProperty(p4, c6);
		c2.addProperty(p5, c7);
		c2.addProperty(p6, c8);
		
//		dp.addDomain(c3);
//		dp.addDomain(c4);
//		dp.addDomain(c5);
//		dp.addDomain(c6);
//		dp.addDomain(c7);
//		dp.addDomain(c8);
//		dp.addDomain(c9);
//		dp.addDomain(c10);
		
		return ontModel;
	}
}
