package edu.isi.bmkeg.uml.model;

import java.net.URI;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import edu.isi.bmkeg.utils.Converters;

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

	public Set<UMLclass> getClasses() {
		return classes;
	}

	public Set<UMLassociation> getAssociations() {
		return associations;
	}

	public void setAssociations(Set<UMLassociation> associations) {
		this.associations = associations;
	}
    
	public void setParent(UMLpackage parent) {
		this.parent = parent;
	}

	public UMLpackage getParent() {
		return parent;
	}
	
	public void setChildren(Set<UMLpackage> children) {
		this.children = children;
	}

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

	public String readUrl() throws Exception {
		URI uri = Converters.convertUmlAddressToUri(this.readPackageAddress()+"/");
		String url = uri.toURL().toString();
		return url;
	}

	public URI readUri() throws Exception {
		if( this.getUri() != null ) 
			return this.getUri();
		else 
			return Converters.convertUmlAddressToUri(this.readPackageAddress()+"/");
	}

	public String readPrefix() throws Exception {

		URI uri = this.readUri();
		
		String prefix = "";		
		if( uri.getPath().length() > 0 ){
			String p = uri.getPath().substring(0, uri.getPath().length()-1);	
			if(p.endsWith("model")) 
				prefix = "";
			else 
				prefix = p.substring(p.lastIndexOf("/")+1, p.length());
		} else {
			prefix = uri.getHost();		
			prefix = prefix.replaceAll("www.", "");
			prefix = prefix.replaceAll(".com", "");
			prefix = prefix.replaceAll(".org", "");
			prefix = prefix.replaceAll(".edu", "");
			prefix = prefix.replaceAll(".net", "");
		
		}		

		return prefix;
	
	}

}
