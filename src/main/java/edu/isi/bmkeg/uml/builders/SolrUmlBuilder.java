package edu.isi.bmkeg.uml.builders;

import java.util.HashMap;

import javassist.ClassPool;

import org.apache.log4j.Logger;

import edu.isi.bmkeg.utils.MapCreate;

public class SolrUmlBuilder extends UmlComponentBuilder implements ImplConvert {
	
	Logger log = Logger.getLogger("edu.isi.bmkeg.uml.interfaces.SolrUmlInterface");

	private boolean annotFlag = true;
	private ClassPool pool = ClassPool.getDefault();
	
	private static String[] javaTargetTypes = new String[] {
        "long", 	// "serial", 
        "int", 		// "byte",   
        "int",		// "short",  
        "int",		// "int",    
        "long",	 	// "long",   
        "float",	// "float",  
        "double",	// "double", 
        "boolean", 	// "boolean",
        "string",	// "char",   
        "string", 	// "shortString",
        "string",	// "String",     
        "string", 	// "longString", 
        "binary", 	// "blob",			
        "binary",	// "image",        
        "date",		// "date",         
        "date",		// "timestamp",
        "string",	// "url"
	};

	public SolrUmlBuilder() throws Exception {

		this.buildLookupTable();

	}

	public void buildLookupTable() throws Exception {
		
		this.setLookupTable(new HashMap<String, String>(MapCreate.asMap(
				UmlComponentBuilder.baseAttrTypes, javaTargetTypes)));
				
	}
	
	
}
