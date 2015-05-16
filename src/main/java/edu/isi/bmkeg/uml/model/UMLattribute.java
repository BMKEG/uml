package edu.isi.bmkeg.uml.model;

import java.util.ArrayList;
import java.util.List;

public class UMLattribute extends UMLparameter {

	private static final long serialVersionUID = -3127028801604990254L;

	private UMLclass parentClass;

	private UMLrole fkRole;

	private UMLattribute pk;

	private List<UMLattribute> fk = new ArrayList<UMLattribute>();

	private boolean idx = false;
	private boolean unique = false;
	
	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public UMLattribute(String uuid) {
		super(uuid);
	}

	public UMLattribute() {
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setParentClass(UMLclass parentClass) {
		this.parentClass = parentClass;
	}

	public UMLclass getParentClass() {
		return parentClass;
	}


	public void setFkRole(UMLrole fkRole) {
		this.fkRole = fkRole;
	}

	public UMLrole getFkRole() {
		return fkRole;
	}

	public void setPk(UMLattribute pk) {
		this.pk = pk;
	}

	public UMLattribute getPk() {
		return pk;
	}

	public void setFk(List<UMLattribute> fk) {
		this.fk = fk;
	}

	public List<UMLattribute> getFk() {
		return fk;
	}

	public boolean isIdx() {
		return idx;
	}

	public void setIdx(boolean idx) {
		this.idx = idx;
	}

	public UMLattribute copy() throws Exception {

		UMLattribute copy = new UMLattribute();

		copy.setImplName(this.getImplName());
		copy.setBaseName(this.getBaseName());
		copy.setType(this.getType());

		if (copy.getType().getBaseName().equals("serial")) {
			UMLclass t = this.getModel().listTypes().get("long");
			copy.setType(t);
		}

		return copy;
	}

	public void generateAttributeBasedReference() throws Exception {

		UMLmodel m = this.getModel();
		UMLclass extRef = this.getType();

		if (extRef.isDataType())
			throw new Exception(this.getParentClass() + "."
					+ this.getBaseName()
					+ " does not contain an external reference!");

		// Has this step already been performed?
		if (this.getParentClass().getAssociateRoles()
				.containsKey(this.getBaseName()))
			return;

		this.setToImplement(false);

		UMLassociation newAss = new UMLassociation();
		m.addItem(newAss);

		newAss.setImplName(this.getImplName());
		newAss.setBaseName(this.getBaseName());
		newAss.setDesigned(false);
		
		UMLpackage pkg = this.getParentClass().getPkg();
		newAss.setPkg(pkg);
		pkg.getAssociations().add(newAss);

		UMLrole newRoleExt = new UMLrole();
		m.addItem(newRoleExt);

		newRoleExt.setImplName(this.getImplName());
		newRoleExt.setBaseName(this.getBaseName());
		newRoleExt.setNavigable(true);
		newRoleExt.setMult_lower(1);
		newRoleExt.setMult_upper(1);
		newRoleExt.setDesigned(false);

		extRef.getDirectRoles().add(newRoleExt);

		this.getParentClass().getAssociateRoles()
				.put(newRoleExt.getBaseName(), newRoleExt);
		newRoleExt.setDirectClass(extRef);
		newRoleExt.setAssociateClass(this.getParentClass());

		newAss.setRole1(newRoleExt);
		newRoleExt.setAss(newAss);

		UMLrole newRoleAt = new UMLrole();
		m.addItem(newRoleAt);

		newRoleAt.setImplName(this.getImplName());
		newRoleAt.setBaseName(this.getBaseName());
		newRoleAt.setNavigable(false);
		newRoleAt.setMult_lower(1);
		newRoleAt.setMult_upper(1);
		newRoleAt.setDesigned(false);

		this.getParentClass().getDirectRoles().add(newRoleAt);

		extRef.getAssociateRoles().put(newRoleAt.getBaseName(), newRoleAt);
		newRoleAt.setDirectClass(this.getParentClass());
		newRoleAt.setAssociateClass(extRef);

		newAss.setRole2(newRoleAt);
		newRoleAt.setAss(newAss);

	}

	public UMLattribute readSource() {
		UMLattribute source = this;
		while (source.getPk() != null) {
			source = source.getPk();
		}
		return source;
	}

	public String readCleanAddress() {

		UMLclass c = this.getParentClass();
		String addr = c.getClassAddress() + "." + this.getImplName();
		addr = addr.substring(2);

		return addr;

	}
	
	public String readPrefix() throws Exception {
		return this.getParentClass().getPkg().readPrefix();
	}

	public boolean isUnique() {
		return unique;
	}

	public void setUnique(boolean unique) {
		this.unique = unique;
	}

}
