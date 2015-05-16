
package edu.isi.bmkeg.uml.model;

import java.net.URI;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import edu.isi.bmkeg.utils.Converters;

public class UMLclass extends UMLitem {
	
	private static final long serialVersionUID = 6460922669137311701L;

	private UMLpackage pkg;

    private boolean dataType = false;

    private List<UMLattribute> attributes = new ArrayList<UMLattribute>();

    private List<UMLattribute> pkArray = new ArrayList<UMLattribute>();

    private Set<UMLrole> directRoles = new HashSet<UMLrole>();

    private Map<String, UMLrole> associateRoles = new HashMap<String, UMLrole>();

    private UMLassociation linkAssociation;

    private Set<UMLclass> children = new HashSet<UMLclass>();
    
	private Set<UMLoperation> operations = new HashSet<UMLoperation>();

    private UMLclass parent;

    private String classAddress;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public UMLclass(String uuid) {
    	super(uuid);
    }

    public UMLclass() {}
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setPkg(UMLpackage pack) {
		this.pkg = pack;
	}

	public UMLpackage getPkg() {
		return pkg;
	}

	public void setDataType(boolean dataType) {
		this.dataType = dataType;
	}

	public boolean isDataType() {
		return dataType;
	}

	public void setAttributes(List<UMLattribute> attributes) {
		this.attributes = attributes;
	}

	public List<UMLattribute> getAttributes() {
		return attributes;
	}

	public void setDirectRoles(Set<UMLrole> directRoles) {
		this.directRoles = directRoles;
	}

	public Set<UMLrole> getDirectRoles() {
		return directRoles;
	}

	public void setAssociateRoles(Map<String, UMLrole> associateRoles) {
		this.associateRoles = associateRoles;
	}

	public Map<String, UMLrole> getAssociateRoles() {
		return associateRoles;
	}

	public void setLinkAssociation(UMLassociation linkAssociation) {
		this.linkAssociation = linkAssociation;
	}

	public UMLassociation getLinkAssociation() {
		return linkAssociation;
	}

	public void setChildren(Set<UMLclass> children) {
		this.children = children;
	}

	public Set<UMLclass> getChildren() {
		return children;
	}

	public void setParent(UMLclass parent) {
		this.parent = parent;
	}

	public UMLclass getParent() {
		return parent;
	} 
    
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public Set<UMLoperation> getOperations() {
		return this.operations;
	}

	public void setOperations(Set<UMLoperation> operations) {
		this.operations = operations; 		
	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	
	public void setPkArray(List<UMLattribute> pkArray) {
		this.pkArray = pkArray;
	}

	public List<UMLattribute> getPkArray() {
		return pkArray;
	}

	public void setClassAddress(String classAddress) {
		this.classAddress = classAddress;
	}

	public String getClassAddress() {
		return classAddress;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Merges all the roles (and inheritence relations) of that into this 
	 * @param c
	 */
	public void mergeProxyClass(UMLclass that) {
		
		// copy roles from that to this
		Iterator<UMLrole> drIt = that.directRoles.iterator();
		while( drIt.hasNext() ) {
			UMLrole r = drIt.next();
			r.setDirectClass(this);
			this.getDirectRoles().add(r);
		}

		Iterator<String> keyIt = that.associateRoles.keySet().iterator();
		while( keyIt.hasNext() ) {
			String key = keyIt.next();
			UMLrole r = that.associateRoles.get(key);
			r.setAssociateClass(this);
			this.getAssociateRoles().put(key, r);
		}
		
		// find classes with attributes that point to that.
		List<UMLclass> cList = new ArrayList<UMLclass>( 
				this.getModel().listAllClasses().values() 
				);
		cList.addAll( that.getModel().listClasses().values() );
		Iterator<UMLclass> cIt = cList.iterator();
		while( cIt.hasNext() ) {
			UMLclass c = cIt.next();

			Iterator<UMLattribute> aIt = c.getAttributes().iterator();
			while( aIt.hasNext() ) {
				UMLattribute a = aIt.next();
				if( a.getType().equals(that) ) {
					a.setType(this);
				}
			}
		}		

		if( that.getLinkAssociation() != null ) {
			this.setLinkAssociation(that.linkAssociation);
			that.linkAssociation.setLinkClass(this); 
		}

		// copy children from that to this
		Iterator<UMLclass> childIt = that.children.iterator();
		while( childIt.hasNext() ) {
			UMLclass child = childIt.next();
			this.children.add(child);
			child.setParent(this);
		}
		
		that.associateRoles.clear();
		that.directRoles.clear();
		
	}
	
    public String debugString() {        

		if( this.dataType )
			return "";
		
		String debug_string = "	|\n";
		debug_string += "	|-> (C) " + this.getBaseName();
		if (this.getParent() != null) {
			debug_string += ", ( " + this.getParent().getBaseName() + " )";
		} 
		debug_string += " [" + this.getUuid() + "]\n";
		
		if (this.getLinkAssociation() != null) {
			debug_string += "	|	|-> association class\n";
			debug_string += "	|	|	(R) "
					+ this.getLinkAssociation().getRole1().getBaseName() + "\n";
			debug_string += "	|	|	(R) "
					+ this.getLinkAssociation().getRole2().getBaseName() + "\n";
		}
		Object[] atts = this.getAttributes().toArray();
		for (int i = 0; i < atts.length; i++) {
			UMLattribute att = (UMLattribute) atts[i];
			debug_string += "	|	|-> (A) " + att.getBaseName() + "[";
			if( att.getType() != null ) {
				debug_string += att.getType().getImplName();
			}
			debug_string += "]\n";
		}
		debug_string += "	|	|\n";
		Object[] roleKeys = this.getAssociateRoles().keySet().toArray();
		for (int i = 0; i < roleKeys.length; i++) {
			
			String key = (String) roleKeys[i];
			UMLrole r = this.getAssociateRoles().get(key);
			
			debug_string += "	|	|-{" + key + "}-> " + r.getBaseName() + " = "
					+ r.getDirectClass().getBaseName() + " ( " + r.getMult_lower()
					+ "..";
			if (r.getMult_upper() == -1)
				debug_string += "*";
			else
				debug_string += r.getMult_upper();
			debug_string += ", nav:" + r.getNavigable() + ")\n";
		}
		return debug_string;        
        
    }
    
    public void computeClassAddress() {
    	this.getPkg().computePackageAddress();
    	this.classAddress = this.getPkg().getPkgAddress() + "." + this.getImplName();    
    }
    
  	public UMLclass lookupChildByName( String name ) {
	  	UMLclass c = null;
		Iterator<UMLclass> cIt = this.getChildren().iterator();
		while( cIt.hasNext() && c == null ) {
			UMLclass cc = cIt.next();
			if(cc.getBaseName().equals(name)) {
				c = cc;
			}						
		}
		return c;
  	}
    
	public UMLattribute lookupAttributeByName( String name ) {
		UMLattribute a = null;
		Iterator<UMLattribute> aIt = this.getAttributes().iterator();
		while( aIt.hasNext() && a == null ) {
			UMLattribute aa = aIt.next();
			if(aa.getBaseName().equals(name)) {
				a = aa;
			}						
		}
		return a;
  	}

	public String readUrl() throws Exception {
		URI uri = Converters.convertUmlAddressToUri(this.readClassAddress());
		String url = uri.toURL().toString();
		return url;
	}
	
	public String readPrefix() throws Exception {
		return this.getPkg().readPrefix();
	}
	
	public String readClassAddress() {
		return this.classAddress.substring(2);
	}
	
	public UMLclass readSourceClass() throws Exception {

		UMLclass tempClass = this;
		
		while( tempClass.getParent() != null ){
			if( tempClass.equals(tempClass.getParent() ) ) {
				throw new Exception(
						"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n" +
						"You have an error in the model " +
						" the class $temp_class->{Name} is definied as it's own parent. \n" +
						"If this class is 'ViewTable' then this may have come about \n" +
						"because of a missing parent class in the view specs.\n" +
						"Please check the model and try again.\n"+
						"~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~\n");

			}
			tempClass = tempClass.getParent();
		}
		
		return tempClass;
	}

	public void generatePrimaryKeyAttribute() throws Exception {

		//
		//	Get top level class of inheritence tree
		//	Call this the 'source class'
		//
		UMLclass sourceClass = this.readSourceClass();
		
		UMLmodel m = sourceClass.getModel();
		
		//
		// If a primary key array has not been defined for the source class
		//	then define it for the source class.
		//
		if( sourceClass.getPkArray().size() == 0 ) {
		
			ArrayList<UMLattribute> pks = new ArrayList<UMLattribute>();
			Iterator<UMLattribute> pkIt = sourceClass.getAttributes().iterator();
			while(pkIt.hasNext()) {
				UMLattribute possPk = pkIt.next();
				if( possPk.getStereotype() != null &&
						possPk.getStereotype().equals("PK") ) {
					pks.add(possPk);
				}
			}

			if( pks.size() == 0 ) {

				UMLattribute pk = new UMLattribute();
				this.getModel().addItem(pk);

				pk.setType( this.getModel().listTypes().get("serial") );
				pk.setImplName("vpdmfId");
				pk.setBaseName("vpdmfId");
				pk.setStereotype("PK");
				pks.add(pk);
				
			}
			
			pkIt = pks.iterator();
			while( pkIt.hasNext() ) {
				UMLattribute pk = pkIt.next();
				
				sourceClass.getAttributes().add(0, pk);
				pk.setParentClass(sourceClass);
				sourceClass.getPkArray().add(pk);
				
			} 
			
		} 

		//
		//	Finish if this is sourceClass or if there's already a PK
		//
		if( this.equals(sourceClass) ) 
			return;
		
		Iterator<UMLattribute> pkIt = sourceClass.getPkArray().iterator();
		while( pkIt.hasNext() ) {
			UMLattribute pk = pkIt.next();
			
			UMLattribute copy = pk.copy();
			m.addItem(copy);
			
			this.getAttributes().add(copy);
			copy.setParentClass(this);
			copy.setStereotype("PK");
		
			this.getPkArray().add(0,copy);
			
		}
		
	}
	
	public UMLattribute addNewAttribute(String name, String typeName) throws Exception {
		
		UMLattribute a = new UMLattribute();
		UMLmodel m = this.getModel();
		
		m.addItem(a);
		a.setModel(m);
		
		a.setImplName(name);
		a.setBaseName(name);
		a.setParentClass(this);
		this.getAttributes().add(a);
		
		Map<String, UMLclass> typeMap = m.listTypes();
		if( !typeMap.containsKey(typeName) ) {
			throw new Exception("Type " + typeName + " not found");
		}
		
		UMLclass type = typeMap.get(typeName);
		a.setType(type);
		
		return a;
		
	}
	
	public ArrayList<UMLattribute> listImplementedAttributes() {
		
		ArrayList<UMLattribute> impList = new ArrayList<UMLattribute>();

		Iterator<UMLattribute> aIt = this.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();
			if(a.getToImplement()) {
				impList.add(a);
			}
		}
				
		return impList;
		
	}
	
	public String toString() {
		return this.readClassAddress();
	}
	
  	
 }
