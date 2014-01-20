package edu.isi.bmkeg.uml.model;


public class UMLparameter extends UMLitem {

	private static final long serialVersionUID = -706837008576126610L;
	
	private UMLclass type;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public UMLparameter(String uuid) {
		super(uuid);
	}

	public UMLparameter() {
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setType(UMLclass type) {
		this.type = type;
	}

	public UMLclass getType() {
		return type;
	}

}
