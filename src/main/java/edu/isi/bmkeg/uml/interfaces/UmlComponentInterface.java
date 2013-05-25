package edu.isi.bmkeg.uml.interfaces;

import java.io.File;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.model.UMLrole;

public abstract class UmlComponentInterface implements ImplConvert  {
	
	public static int FORWARD = 0;
	public static int REVERSE = 1;

	public static String[] baseAttrTypes = new String[] {
	        "serial", 
	        "byte",   
	        "short",  
	        "int",    
	        "long",   
	        "float",  
	        "double", 
	        "boolean",
	        "char",   
	        "shortString",
	        "String",     
	        "longString", 
	        "blob",			
	        "image",        
	        "date",         
	        "timestamp",
	        "url"
	};
	
	private File rootDirectory;
	private String codeFormat;
	private UMLmodel umlModel;
	
	private Map<String, String> lookupTable;
	
	private Set<String> stopwords = new HashSet<String>();
	
	public UmlComponentInterface () {
	}
	
	public void setRootDirectory(File rootDirectory) {
		this.rootDirectory = rootDirectory;
	}
	public File getRootDirectory() {
		return rootDirectory;
	}
	public void setCodeFormat(String codeFormat) {
		this.codeFormat = codeFormat;
	}
	public String getCodeFormat() {
		return codeFormat;
	}
	public void setUmlModel(UMLmodel umlModel) {
		this.umlModel = umlModel;
	}
	public UMLmodel getUmlModel() {
		return umlModel;
	}

	public Map<String, String> getLookupTable() {
		return lookupTable;
	}

	public void setLookupTable(Map<String, String> lookupTable) {
		this.lookupTable = lookupTable;
	}
	
	public void convertAttributes() {
		
		Map<String,UMLclass> types = this.umlModel.listTypes();
		Iterator<String> it = types.keySet().iterator();
		while(it.hasNext()) {
			String baseTypeName = it.next();
			UMLclass type = types.get(baseTypeName);
			type.setImplName(this.getLookupTable().get(baseTypeName));			
		}
		
		this.getUmlModel().recomputeClasspaths();
	
	}
	
	public void setStopWords(String[] words){
		
		for(int i=0; i<words.length; i++) {
			this.stopwords.add(words[i]);
		}
		
	}
	
	public void checkStopWords() throws Exception {
		
		Iterator<UMLclass> cIt = this.umlModel.listClasses().values().iterator();
		while( cIt.hasNext() ) {
			UMLclass c = cIt.next();
			
			if( this.stopwords.contains( c.getBaseName() ) ) {
				throw new Exception( "CLASS: " + c.getBaseName() + 
						" protected word in implmentation");
			}
			
			Iterator<UMLrole> rIt = c.getAssociateRoles().values().iterator();
			while( rIt.hasNext() ) {
				UMLrole r = rIt.next();			
				if( this.stopwords.contains( r.getBaseName() ) ) {
					throw new Exception( "ROLE: " + r.getBaseName() + 
							" protected word in implmentation");
				}
				UMLrole or = r.otherRole();
				if( this.stopwords.contains( or.getBaseName() ) ) {
					throw new Exception( "ROLE: " + or.getBaseName() + 
							" protected word in implmentation");
				}

			}			

			Iterator<UMLattribute> aIt = c.getAttributes().iterator();
			while( aIt.hasNext() ) {
				UMLattribute a = aIt.next();			
				if( this.stopwords.contains( a.getBaseName() ) ) {
					throw new Exception( "ATTRIBUTE: " + a.getBaseName() + 
							" protected word in implmentation");
				}
			}			
			
		}
		
		
		
	}
	
}
