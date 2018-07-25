package com.zbj.alg.industry.ontology;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Iterator;

import org.apache.jena.ontology.Individual;
import org.apache.jena.ontology.ObjectProperty;
import org.apache.jena.ontology.OntClass;
import org.apache.jena.ontology.OntModel;
import org.apache.jena.ontology.OntModelSpec;
import org.apache.jena.ontology.OntProperty;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Property;
import org.apache.jena.rdf.model.Statement;
import org.apache.jena.rdf.model.StmtIterator;


public class IndCategoryModeTrainByOntology {
	
	public static void main(String[] args) throws IOException{
		String path = "E:/tr_project_one/project/IndustryTagLibrary/Enterprice1498036053242.owl";
//		getAllIndividuals(path);
		onto(path);
	}
	@SuppressWarnings("rawtypes")
	public static void onto(String owlpath) {
		//create the model and import owl file
		OntModel model = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,null);
		try{
			model.read(new FileInputStream(owlpath),"");
		}catch(IOException ioe){
			System.out.println(ioe.toString());
		}
		//the classes number
		int j = 0;
		//list classes
		for(Iterator i = model.listIndividuals();i.hasNext();){
		    Individual oc = (Individual) i.next();
            System.out.println("*********Individual:"+oc.getLocalName()+"*********");
            for (StmtIterator ipp = oc.listProperties(); ipp.hasNext();){
            	Property p=ipp.nextStatement().getPredicate();
                String temp =  p.getLocalName();
                System.out.println(temp+ " : "+oc.getPropertyValue(p));
            }
		}
//		for(Iterator i = model.listClasses();i.hasNext();){
//			j++;
//			OntClass c = (OntClass)i.next();
//			String strClass = c.getModel().expandPrefix(c.getURI());
//			System.out.println(j+" "+strClass);
//			for (Iterator allIndivs1 = c.listInstances(); allIndivs1.hasNext();) {
//				Individual indiv1 = (Individual) allIndivs1.next();
//				String indv1 = indiv1.toString();
//				System.out.println("  "+"hasInstances"+" "+indv1);				
//				for(Iterator classPryOfIndiv=indiv1.listProperties();classPryOfIndiv.hasNext();){
//					System.out.println(classPryOfIndiv.next());  
////					OntProperty proyOfIindivOne = (OntProperty)classPryOfIndiv.next();
//					
////					for(Iterator oneSubclass = classPryOfIndiv.;oneSubclass.hasNext();){
////						
////					}
//					
//				}
////					Property classPryOfIndiv = (Property) allIndivs2.next();
////					System.out.println("Property:"+classPryOfIndiv.getLocalName());
////					String indv2 = classPryOfIndiv.toString();
////					System.out.println("   "+"hasInstances"+" "+indv2);
////				}
//			}
////			private static void modelRead(OntModel model) {
////				  int j =0;
////				  for(Iterator i = model.listIndividuals();i.hasNext();){
////				   Individual oc = (Individual) i.next();
////				            System.out.println("*********Individual:"+oc.getLocalName()+"*********");
////				            for (StmtIterator ipp = oc.listProperties(); ipp.hasNext();){
////				             Property p=ipp.nextStatement().getPredicate();
////				                String temp =  p.getLocalName();
////				                System.out.println(temp+ " : "+oc.getPropertyValue(p));
////				            }
////				  }
////			}
//			//list property for each class
////			for(Iterator y=c.listDeclaredProperties(true);y.hasNext();){
////				OntProperty property=(OntProperty)y.next();
////				String strPropertyName=property.getModel().expandPrefix(property.getURI());
////				String strRange=property.getRange().toString();
////				String strRangeName=property.getModel().expandPrefix(strRange);
////				//show just the "has" Properties
////				if(strPropertyName.split("#")[1].contains("Has_")){
////					System.out.print("	");
////					System.out.print(strPropertyName);
////					System.out.println(strRangeName);
////				}
////			}
//		}
	}
	
	public static void getAllIndividuals(String owlpath) {  
		OntModel ontModel = ModelFactory.createOntologyModel(OntModelSpec.OWL_MEM,null);
		try{
			ontModel.read(new FileInputStream("E:/tr_project_one/project/IndustryTagLibrary/Industry.owl"),"");
		}catch(IOException ioe){
			System.out.println(ioe.toString());
		}
        String str;  

        // 迭代出本体文件中所有的实例  
        for (Iterator allIndivs = ontModel.listIndividuals(); allIndivs.hasNext();) {  
            Individual indiv = (Individual) allIndivs.next();  
            //对象属性命名空间  
            String namespace = indiv.toString().substring(0,indiv.toString().indexOf("#") + 1);  
            //实例所属的类  
            OntClass classOfIndiv = indiv.getOntClass();  
              
            //实例所属类的所有属性  
            for (Iterator classPryOfIndivs = classOfIndiv.listProperties(); classPryOfIndivs.hasNext();)   
            {  
                OntProperty classPryOfIndiv = (OntProperty) classPryOfIndivs.next();  
                System.out.println(classPryOfIndivs.next());  
                String classPryOfIndivstr = classPryOfIndiv.toString();  
                String info = null;  
                info = "实例URI:"  
                        + indiv  
                        + " 实例名："  
                        + indiv.toString().substring(indiv.toString().indexOf("#") + 1) + "  实例所属类："  
                        + classOfIndiv  
                        +" 属性URI："  
                        +classPryOfIndivstr;  
                  
                // 获取这个实例的属性值  
                if (indiv.getPropertyValue(classPryOfIndiv) != null) {  
                    String pryValueOfIndiv = indiv.getPropertyValue(classPryOfIndiv).toString();  
                    /*判断对象属性或数据属性 
                     * 如果实例属性值中包括"^^"和"#"，则为数据属性，否则为对象属性 
                     */  
                    if (pryValueOfIndiv.contains("^^")&&pryValueOfIndiv.contains("#")) {  
                        info = info   
                                + "  属性值："  
                                + pryValueOfIndiv.substring(0, pryValueOfIndiv.indexOf("^^"));  
                    }else{  
                        info = info  
                                + "  属性值："  
                                + pryValueOfIndiv.substring(pryValueOfIndiv.indexOf("#") + 1);  
                    }  
                } else {  
                    info = info + "   无属性值";  
                }  
                //输出实例信息  
                System.out.println(info);  
            }  
      
        }  

    } 
	
}
