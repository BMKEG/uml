package edu.isi.bmkeg.uml.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.xml.sax.helpers.DefaultHandler;

import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;

public class UMLModelParserHandler extends DefaultHandler {

	private UMLmodel umlModel;
    private Map<String, UMLclass> preexistingTypes = new HashMap<String,UMLclass>();

	private List<String> exceptions = new ArrayList<String>();

    
    public UMLModelParserHandler() throws Exception {
		this.umlModel = new UMLmodel();		
		this.preexistingTypes = this.umlModel.listTypes();    
    }
 
	public UMLmodel getUmlModel() {
		return umlModel;
	}

	public Map<String,UMLclass> getPreexistingTypes() {
		return preexistingTypes;
	}

	public List<String> getExceptions() {
		return exceptions;
	}

	public void setExceptions(List<String> exceptions) {
		this.exceptions = exceptions;
	}
	
}
