package edu.isi.bmkeg.uml.model;

import java.net.URL;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrimaryKeyJoinColumn;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

@Entity 
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="package_id")
public class UMLpackage extends UMLitem {
		
	private static final long serialVersionUID = 716479591522767529L;

	private Set<UMLclass> classes = new HashSet<UMLclass>();
	
	private Set<UMLassociation> associations = new HashSet<UMLassociation>();
	
	private UMLpackage parent;

	private Set<UMLpackage> children = new HashSet<UMLpackage>();	
	
	private String pkgAddress;
	
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public UMLpackage(String uuid) {
    	super(uuid);
    }

    public UMLpackage() {}
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setClasses(Set<UMLclass> classes) {
		this.classes = classes;
	}

    @OneToMany(mappedBy="pkg",cascade=CascadeType.ALL)
	public Set<UMLclass> getClasses() {
		return classes;
	}

    @OneToMany(mappedBy="pkg",cascade=CascadeType.ALL)
	public Set<UMLassociation> getAssociations() {
		return associations;
	}

	public void setAssociations(Set<UMLassociation> associations) {
		this.associations = associations;
	}
    
	public void setParent(UMLpackage parent) {
		this.parent = parent;
	}

	@ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="parent_id")
	public UMLpackage getParent() {
		return parent;
	}
	
	public void setChildren(Set<UMLpackage> children) {
		this.children = children;
	}

	@OneToMany(mappedBy="parent")
	public Set<UMLpackage> getChildren() {
		return children;
	}
	
	public void setPkgAddress(String pkgAddress) {
		this.pkgAddress = pkgAddress;
	}

	public String getPkgAddress() {
		return pkgAddress;
	}

    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void computePackageAddress() {
		
		String address = this.getBaseName();

		UMLpackage c = this;
		while (c.getParent() != null ) {
			address = c.getParent().getBaseName() + "." + address;
			c = c.getParent();
		}

		this.setPkgAddress(address);

	}

	public String readPackageAddress() {
		return this.pkgAddress.substring(2);
	}
	
	/**
	 * Merges all the classes of that into this 
	 * @param c
	 */
	public void mergeClassesFrom(UMLpackage that) {
		
		// copy classes from that to this
		Iterator<UMLclass> cIt = that.classes.iterator();
		while( cIt.hasNext() ) {
			UMLclass c = cIt.next();
			this.classes.add(c);
			c.setPkg(this);
		}

		that.classes.clear();
		
	}
	
	public UMLpackage addNewChildPackage(String name) throws Exception {

		UMLmodel m = this.getModel();
		
		UMLpackage p = new UMLpackage();
		
		m.addItem(p);
		p.setModel(m);
		p.setImplName(name);
		p.setBaseName(name);
		p.setParent(this);
		this.getChildren().add(p);
		p.computePackageAddress();	
		
		return p;
	
	}

	public UMLclass addNewClass(String name) throws Exception {

		UMLmodel m = this.getModel();
		
		UMLclass c = new UMLclass();
		
		m.addItem(c);
		c.setModel(m);
		
		c.setImplName(name);
		c.setBaseName(name);
		c.setPkg(this);
		this.getClasses().add(c);
		
		c.computeClassAddress();
		
		return c;
	
	}	
}
