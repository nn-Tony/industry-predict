package com.zbj.alg.industry.ontology;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashSet;
import java.util.Set;

import org.apache.jena.query.Query;
import org.apache.jena.query.QueryExecution;
import org.apache.jena.query.QueryExecutionFactory;
import org.apache.jena.query.QueryFactory;
import org.apache.jena.query.QuerySolution;
import org.apache.jena.query.ResultSet;
import org.apache.jena.rdf.model.InfModel;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.RDFNode;
import org.apache.jena.reasoner.Reasoner;
import org.apache.jena.reasoner.ReasonerRegistry;
import org.apache.jena.util.FileManager;
import org.apache.jena.vocabulary.ReasonerVocabulary;

public class BasicEnterpriceInfoSparqlQuery {
	public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException{

		Model data = FileManager.get().loadModel("file:E:/tr_project_one/project/hackathon/ZBJ_Marathon_end.ttl");
		
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, 
                ReasonerVocabulary.RDFS_DEFAULT);
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data );
		
		/***
		 * 企业基本信息——企业用户画像
		 */
		/*String queryString = "prefix tag:   <http://zbj//#>"
				+ "SELECT ?x ?a ?b ?c ?d ?e ?f ?g ?h ?i WHERE "
				+ "{ "
				+ "?x tag:企业_所属_行业  ?a ."
				+ "?x tag:企业_所属_主营 ?b ."
				+ "?x tag:企业_有_招聘职位 ?c ."
				+ "?x tag:企业_所属_类型 ?d ."
				+ "?x tag:企业_所属_城市 ?e ."
				+ "?x tag:企业_有_成立年限 ?f ."
				+ "?x tag:企业_有_注册资金 ?g ."
				+ "?x tag:企业_有_电话 ?h ."
				+ "?x tag:企业_有_邮箱 ?i ."
				+ "}";*/
		
		/***
		 * 企业——招聘职位——关联服务标签——关联平台服务商
		 */
		/*String queryString = "prefix tag:   <http://zbj//#>"
				+ "SELECT ?x ?c ?k ?l WHERE "
				+ "{ "
				+ "?x tag:企业_有_招聘职位 ?c ."
				+ "?c tag:招聘_转需求_关联服务 ?k ."
				+ "?l tag:服务商_有_服务 ?k ."
				+ "}"
				+ "LIMIT 5000";*/
		
		/***
		 * 企业——主营——关联招标——url
		 */
		/*String queryString = "prefix tag:   <http://zbj//#>"
				+ "SELECT ?x ?a ?b ?s ?t ?u WHERE "
				+ "{ "
				+ "?x tag:企业_所属_行业  ?a ."
				+ "?x tag:企业_所属_主营 ?b ."
				+ "?b tag:主营_关联招标_需求 ?s ."
				+ "?s tag:招标_有_标题 ?t ."
				+ "?s tag:招标_有_链接 ?u ."
				+ "}"
				+ "LIMIT 5000";*/
		
		/***
		 * 企业——关联政策(自营需求)——url
		 */
		/*String queryString = "prefix tag:   <http://zbj//#>"
				+ "SELECT ?x ?m ?n ?o ?p ?q WHERE "
				+ "{ "
				+ "?x tag:企业_所属_行业  ?a ."
				+ "?x tag:企业_所属_主营 ?b ."
				+ "?x tag:企业_关联政策_自营需求 ?m ."
				+ "?m tag:政策_所属_系统 ?n ."
				+ "?m tag:政策_所属_类型 ?o ."
				+ "?m tag:政策_有_标题 ?p ."
				+ "?m tag:政策_有_链接 ?q ."
				+ "}";*/

		/***
		 * 企业——关联招聘岗位——关联政策——关联招标
		 */
		String queryString = "prefix tag:   <http://zbj//#>"
				+ "SELECT ?x ?a ?m ?n ?b ?c ?e ?d ?f WHERE "
				+ "{ "
				+ "?x tag:企业_有_招聘职位 ?a ."
				+ "?a tag:招聘_转需求_关联服务 ?m ."
				+ "?n tag:服务商_有_服务 ?m ."
				
				+ "?x tag:企业_关联政策_自营需求 ?b ."
				
				/*+ "?x tag:企业_所属_主营 ?b ."
				+ "?b tag:主营_关联招标_需求 ?c ."*/
				
				+ "}";
		
		
		Query query = QueryFactory.create(queryString) ;
		QueryExecution qexec = QueryExecutionFactory.create(query, infmodel);

		Set<String> Enterprice = new HashSet<String>();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/tr_project_one/project/hackathon/Enterorice.txt"), "utf-8"), true);
		try{
		    ResultSet results = qexec.execSelect() ;
		    int num = 0;
		    while( results.hasNext() ){
		    	QuerySolution soln = results.nextSolution();
		    	RDFNode en = soln.get("x");
		    	Enterprice.add(en.toString().substring(13, en.toString().length()));
		    	if(num++ < 100){
		    		System.out.println( "soln: " + soln.toString());
		    	}
		    	//System.out.println( "soln: " + soln.toString());
		    }
		}
		finally{
			qexec.close();
		}
		
		for(String s:Enterprice){
			pw.println(s);
		}
		pw.close();
		
		System.out.println( Enterprice.size()+"\n"+"done" );
	}
}
