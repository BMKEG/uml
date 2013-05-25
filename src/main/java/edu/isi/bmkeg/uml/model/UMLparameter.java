package edu.isi.bmkeg.uml.model;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name = "parameter_id")
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

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "type_id")
	public UMLclass getType() {
		return type;
	}

}
