package edu.isi.bmkeg.uml.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.utils.MapCreate;

public class DataLogUMLInterface extends UmlComponentInterface implements ImplConvert {

	private static String[] datalogTargetTypes = new String[] {
        "long", 
        "byte",   
        "short",  
        "int",    
        "long",   
        "float",  
        "number", 
        "boolean",
        "char",   
        "shortString",
        "string",     
        "longString", 
        "other",			
        "Object",        
        "Date",         
        "Timestamp",
        "URL"
	};

	public DataLogUMLInterface() throws Exception {

		this.buildLookupTable();

	}

	public void buildLookupTable() throws Exception {
		
		this.setLookupTable(new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, datalogTargetTypes)));

	}

}
