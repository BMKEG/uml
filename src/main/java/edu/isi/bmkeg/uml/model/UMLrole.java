package edu.isi.bmkeg.uml.model;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class UMLrole extends UMLitem {

	private static final long serialVersionUID = -3879572217333140356L;

	private UMLassociation ass;

    private int mult_lower;

    private int mult_upper;

    private boolean navigable;

    private UMLclass associateClass;
    
    private UMLclass directClass;

    /**
     * The key column for the name of the role used by the referring class.
     */
    private String roleKey;
    
    private List<UMLattribute> fkArray = new ArrayList();

    private Set<UMLrole> implementedBy = new HashSet<UMLrole>();

    private UMLrole implementz;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
    
    public UMLrole(String uuid) {
    	super(uuid);
    }

    public UMLrole() {}
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setAss(UMLassociation ass) {
		this.ass = ass;
	}

	public UMLassociation getAss() {
		return ass;
	}

	public void setMult_lower(int mult_lower) {
		this.mult_lower = mult_lower;
	}

	public int getMult_lower() {
		return mult_lower;
	}

	public void setMult_upper(int mult_upper) {
		this.mult_upper = mult_upper;
	}

	public int getMult_upper() {
		return mult_upper;
	}

	public void setNavigable(boolean navigable) {
		this.navigable = navigable;
	}

	public boolean getNavigable() {
		return navigable;
	}

	public void setAssociateClass(UMLclass associateClass) {
		this.associateClass = associateClass;
	}

	public UMLclass getAssociateClass() {
		return associateClass;
	}

	public void setDirectClass(UMLclass directClass) {
		this.directClass = directClass;
	}

	public UMLclass getDirectClass() {
		return directClass;
	}

	public void setFkArray(List<UMLattribute> fkArray) {
		this.fkArray = fkArray;
	}

	public List<UMLattribute> getFkArray() {
		return fkArray;
	} 

	public void setRoleKey(String key) {
		this.roleKey = key;
	}

	public String getRoleKey() {
		return roleKey;
	} 
	
	public UMLrole getImplementz() {
		return implementz;
	}

	public void setImplementz(UMLrole implementz) {
		this.implementz = implementz;
	}
    
    public void setImplementedBy(Set<UMLrole> implementedBy) {
		this.implementedBy = implementedBy;
	}

	public Set<UMLrole> getImplementedBy() {
		return implementedBy;
	} 


	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
    public UMLrole otherRole() {        
        
        UMLassociation a = this.getAss();
        if(a.getRole1().equals(this)) 
            return a.getRole2();
        else 
            return a.getRole1();
        
    }

	public String readCleanAddress() throws Exception {

		
		UMLclass c = this.getAssociateClass();
		String addr = c.getClassAddress() + "." + this.getImplName();
		addr = addr.substring(2);

		if( !this.getNavigable() ) {
			throw new Exception(addr + " is non navigable, no need for it's address");
		}

		return addr;

	}

	
}
