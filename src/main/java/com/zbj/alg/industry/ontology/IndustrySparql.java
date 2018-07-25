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

public class IndustrySparql {
public static void main( String[] args ) throws UnsupportedEncodingException, FileNotFoundException{
		
		Model data = FileManager.get().loadModel("file:E:/tr_project_one/project/IndustryTagLibrary/Industry.owl");
		Reasoner reasoner = ReasonerRegistry.getRDFSReasoner();
		reasoner.setParameter(ReasonerVocabulary.PROPsetRDFSLevel, 
                ReasonerVocabulary.RDFS_DEFAULT);
		
		InfModel infmodel = ModelFactory.createInfModel(reasoner, data);
		
		String queryString = "prefix tag:<http://www.zbj.com/industry//#> "
				+ "SELECT ?一级 ?二级 ?行业 ?机构名称 ?机构类型 ?行业 ?主营 ?产品 ?热门 ?职位 ?地域 "
				+ "WHERE "
				+ "{ "
				+ "?一级 tag:Has_子类 tag:网络游戏 ."
//				+ "?二级 tag:Has_行业 ?行业 ."
//				+ "?二级 tag:Has_机构名称 ?机构名称 ."
//				+ "?二级 tag:Has_机构类型 ?机构类型 ."
//				+ "?二级 tag:Has_主营 ?主营 ."
				+ "tag:网络游戏 tag:Has_产品 ?产品 ."
//				+ "?二级 tag:Has_热门 ?热门 ."
//				+ "?二级 tag:Has_职位 ?职位 ."
//				+ "?二级 tag:Has_地域 ?地域 ."
				+ "}";
		
		
		Query query = QueryFactory.create(queryString);
		QueryExecution qexec = QueryExecutionFactory.create(query, infmodel);

		Set<String> Ind = new HashSet<String>();
		PrintWriter pw = new PrintWriter(new OutputStreamWriter(
				new FileOutputStream("E:/tr_project_one/project/IndustryTagLibrary/IndQueryResout.txt"), "utf-8"), true);
		try{
		    ResultSet results = qexec.execSelect();
		    int num = 0;
		    while( results.hasNext() ){
		    	QuerySolution soln = results.nextSolution();
		    	if(num++ < 100){
		    		System.out.println( "soln: " + soln.toString());
		    	}
		    }
		}
		finally{
			qexec.close();
		}
		pw.close();
		
		System.out.println( Ind.size()+"\n"+"done" );
		
	}

}
