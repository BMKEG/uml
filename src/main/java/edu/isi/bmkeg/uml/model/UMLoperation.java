package edu.isi.bmkeg.uml.model;

import java.util.ArrayList;
import java.util.List;

public class UMLoperation extends UMLitem {

	private static final long serialVersionUID = 7085220331796036395L;

	private UMLclass parentClass;
	
	private List<UMLparameter> parameters = new ArrayList<UMLparameter>();

	private UMLparameter returnType;
	
	private String code;
	
	private boolean isStatic;
		
	public UMLclass getParentClass() {
		return parentClass;
	}
	
	public void setParentClass(UMLclass parentClass) {
		this.parentClass = parentClass;
	}

	public List<UMLparameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<UMLparameter> parameters) {
		this.parameters = parameters;
	}

    public UMLparameter getReturnType() {
		return returnType;
	}

	public void setReturnType(UMLparameter returnType) {
		this.returnType = returnType;
	}
	
}
