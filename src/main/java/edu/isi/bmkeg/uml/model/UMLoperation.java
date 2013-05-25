package edu.isi.bmkeg.uml.model;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity 
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="operation_id")
public class UMLoperation extends UMLitem {

	private static final long serialVersionUID = 7085220331796036395L;

	private UMLclass parentClass;
	
	private List<UMLparameter> parameters = new ArrayList<UMLparameter>();

	private UMLparameter returnType;
	
	private String code;
	
	private boolean isStatic;
		
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="class_id")
	public UMLclass getParentClass() {
		return parentClass;
	}
	
	public void setParentClass(UMLclass parentClass) {
		this.parentClass = parentClass;
	}

	@OneToMany
	@JoinTable(name="UMLoperation_parameters_UMLattributes",joinColumns=@JoinColumn(name="oId"),inverseJoinColumns=@JoinColumn(name="Id"))
    @org.hibernate.annotations.IndexColumn(name="parameterOrder")
	public List<UMLparameter> getParameters() {
		return parameters;
	}

	public void setParameters(List<UMLparameter> parameters) {
		this.parameters = parameters;
	}

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="return_id")	
    public UMLparameter getReturnType() {
		return returnType;
	}

	public void setReturnType(UMLparameter returnType) {
		this.returnType = returnType;
	}
	
}
