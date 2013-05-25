
package edu.isi.bmkeg.uml.model;

import java.util.Iterator;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToOne;
import javax.persistence.PrimaryKeyJoinColumn;

@Entity 
@Inheritance(strategy=InheritanceType.JOINED)
@PrimaryKeyJoinColumn(name="association_id")
public class UMLassociation extends UMLitem {
	
	private static final long serialVersionUID = -5507426711188209987L;

	private UMLrole role1;

    private UMLrole role2;

    private UMLclass linkClass;

    private UMLpackage pkg;
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    public UMLassociation(String uuid) {
    	super(uuid);
    }

    public UMLassociation() {}
    
    //~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="role1_id")
    public UMLrole getRole1() {        
    	return role1;
    } 

    public void setRole1(UMLrole role1) {        
        this.role1 = role1;
    } 

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="role2_id")
    public UMLrole getRole2() {        
        return role2;
    } 

    public void setRole2(UMLrole role2) {        
        this.role2 = role2;
    } 

    @OneToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="linkClass_id")
    public UMLclass getLinkClass() {        
        return linkClass;
    } 

    public void setLinkClass(UMLclass linkClass) {        
        this.linkClass = linkClass;
    }
    
    @ManyToOne(cascade=CascadeType.ALL)
    @JoinColumn(name="pkg_id")
	public UMLpackage getPkg() {
		return pkg;
	}

	public void setPkg(UMLpackage pkg) {
		this.pkg = pkg;
	} 
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * 
	 * If an association has a n-to-m cardinality, 
	 *	- or is based on an association class
	 * 	- or is a self-referential relation with a -to-n cardinality...
	 *
	 *  - put in a linking-class (i.e. class---linkingClass---class)
	 */
	public void generateDerivedAssociationAndClasses()  throws Exception {

		String sType = "";
		if( this.getStereotype() != null && this.getStereotype().length() > 0 ) 
			sType = this.getStereotype() + ",";
		
		UMLrole r1 = this.getRole1();
		UMLrole r2 = this.getRole2();

		UMLclass c1 = r1.getDirectClass();
		UMLclass c2 = r2.getDirectClass();
	 
		// Order the roles and classes in the association 
		// based on the alphabetic order of the class names
		// for consistency within the schema.
		if( c1.getBaseName().compareTo(c2.getBaseName()) > 0 ) {
			UMLrole r11 = r1;
			UMLrole r22 = r2;
			
			UMLclass c11 = c1;
			UMLclass c22 = c2;
			
			r1 = r22;
			r2 = r11;
			c1 = c22;
			c2 = c11;
		}
		
		
		c2.getAssociateRoles().remove(r1.getBaseName());
		c2.getAssociateRoles().put(r1.getBaseName() + "__design", r1);

		c1.getAssociateRoles().remove(r2.getBaseName());
		c1.getAssociateRoles().put(r2.getBaseName() + "__design", r2);

		if( r1.getAssociateClass() == null ) 
			throw new Exception(c1.getImplName() + "." + r1.getImplName() + "'s associate class is null");

		if( r2.getAssociateClass() == null ) 
			throw new Exception(c2.getImplName() + "." + r2.getImplName() + "'s associate class is null");
		
	
		if(!r1.getAssociateClass().equals(c2) || !r2.getAssociateClass().equals(c1) ) 
			throw new Exception("mismatch assigning derived associations");
		
		UMLclass newClass = null;
		UMLpackage pkg = c1.getPkg();
		
		if( this.getLinkClass() == null ) {
		
			newClass = new UMLclass();
			newClass.setStereotype("Link");
			this.getModel().addItem(newClass);
			
			String temp = c1.getBaseName() + "_" + r1.getBaseName() + "__" + r2.getBaseName() + "_" + c2.getBaseName();

			int l = temp.length();
			if( l > 64 ) 
				throw new Exception( 
						"The name for the linking class: " + temp + 
						"is too long, (" + l + "). Please adjust the names of the constituent classes and roles" +
						"so that their total length is less than 64 characters.");
			
			newClass.setImplName(temp);
			newClass.setBaseName(temp);

			newClass.setPkg(pkg);
			pkg.getClasses().add(newClass);
			newClass.computeClassAddress();
						
			//
			//		This indicates that the class is not part of the original model
			//	and is considered part of an association class... These conditions 
			//	identify the class as an n-to-n class.
			//
			//		These are criteria that are used when deciding how to include foreign 
			//	keys into the primary keys of tables
			//
			this.setLinkClass(newClass);
			newClass.setLinkAssociation(this);			
			newClass.generatePrimaryKeyAttribute();
		
		} else {
		
			newClass = this.getLinkClass();
			newClass.setStereotype("Link");
		
		}
		
		//
		// (2) connect class1 and newClass
		// 
		UMLassociation newAss1 = new UMLassociation();
		
		this.getModel().addItem(newAss1);
		newAss1.setDesigned(false);
		newAss1.setImplName(newAss1.getUuid());
		newAss1.setBaseName(newAss1.getUuid());
		
		newAss1.setPkg(pkg);
		pkg.getAssociations().add(newAss1);		
	
		newAss1.setStereotype(sType + "Link");

		UMLrole newR1 = new UMLrole();
		this.getModel().addItem(newR1);
		newR1.setNavigable(r1.getNavigable());
		newR1.setMult_lower(1);
		newR1.setMult_upper(1);
		newR1.setDesigned(false);
		newR1.setStereotype("Link");
		
		r1.getImplementedBy().add(newR1);
		newR1.setImplementz(r1);
		
		newR1.setImplName(r1.getBaseName());
		newR1.setBaseName(r1.getBaseName());
		
		c1.getDirectRoles().add(newR1);
		newClass.getAssociateRoles().put( newR1.getBaseName(), newR1);
		newR1.setDirectClass(c1);
		newR1.setAssociateClass(newClass);

		newAss1.setRole1( newR1 );
		newR1.setAss( newAss1 );

		UMLrole newR2 = new UMLrole();
		this.getModel().addItem(newR2);
		newR2.setNavigable(r2.getNavigable());
		newR2.setMult_lower(0);
		newR2.setMult_upper(-1);
		newR2.setDesigned(false);
		newR2.setStereotype("Link");
		
		r2.getImplementedBy().add(newR2);
		newR2.setImplementz(r2);

		newR2.setImplName(r2.getBaseName());
		newR2.setBaseName(r2.getBaseName());
		
		newClass.getDirectRoles().add(newR2);
		c1.getAssociateRoles().put( newR2.getBaseName(), newR2);
		newR2.setDirectClass(newClass);
		newR2.setAssociateClass(c1);

		newAss1.setRole2( newR2 );
		newR2.setAss( newAss1 );
			
		//
		// (3) connect $class and $class2
		//
		UMLassociation newAss2 = new UMLassociation();
		this.getModel().addItem(newAss2);
		newAss2.setDesigned(false);
		newAss2.setImplName(newAss1.getUuid());
		newAss2.setBaseName(newAss1.getUuid());
		newAss2.setStereotype(sType + "Link");
		
		newAss2.setPkg(pkg);
		pkg.getAssociations().add(newAss2);		
		
		UMLrole newR3 = new UMLrole();
		this.getModel().addItem(newR3);
		newR3.setNavigable(r1.getNavigable());
		newR3.setMult_lower(0);
		newR3.setMult_upper(-1);
		newR3.setDesigned(false);
		newR3.setStereotype("Link");
		
		r1.getImplementedBy().add(newR3);
		newR3.setImplementz(r1);

		newR3.setImplName(r1.getBaseName());
		newR3.setBaseName(r1.getBaseName());
	
		newClass.getDirectRoles().add(newR3);
		c2.getAssociateRoles().put( newR3.getBaseName(), newR3);
		newR3.setDirectClass(newClass);
		newR3.setAssociateClass(c2);

		newAss2.setRole1( newR3 );
		newR3.setAss( newAss2 );

		UMLrole newR4 = new UMLrole();
		this.getModel().addItem(newR4);
		newR4.setNavigable(r2.getNavigable());
		newR4.setMult_lower(1);
		newR4.setMult_upper(1);
		newR4.setDesigned(false);
		newR4.setStereotype("Link");

		r2.getImplementedBy().add(newR4);
		newR4.setImplementz(r2);

		newR4.setImplName(r2.getBaseName());
		newR4.setBaseName(r2.getBaseName());
		
		c2.getDirectRoles().add(newR4);
		newClass.getAssociateRoles().put( newR4.getBaseName(), newR4);
		newR4.setDirectClass(c2);
		newR4.setAssociateClass(newClass);

		newAss2.setRole2( newR4 );
		newR4.setAss( newAss2 );
		
		//
		//	Mark the direct association as 'implement=0'
		//	Thus we don't use it when actually building code.
		//
		this.setToImplement(false);
		r1.setToImplement(false);
		r2.setToImplement(false);
		
		r1.setImplName(r1.getBaseName() + "__design");
		r2.setImplName(r2.getBaseName() + "__design");
		
	}
	
    /**
     * Given an association, let's generate the keys for it. 
     * Note that keys are ONLY concerned with the cardinality of the association,
     * not its navigability. 
     * 
     * - This function will quit and return undef with associations 
     * with a cardinality of n-to-n 
     * 
     * - if the foreign key of the association is going to be 
     * included in the primary key of the target table, then
     * we set $include_in_pk_flag to 1
     * 
     * ALSO
     * This is where we insert the order data
     * 
     */
    public void generateForeignKeys(boolean includeInPkFlag) throws Exception {        
    	
    	UMLrole r1 = this.role1;
    	UMLrole r2 = this.role2;
    	
    	if( !this.getToImplement() ) {
        	throw new Exception("Died attempting to generate foreign keys for a non-implementable " +
        			"association: " + this.getBaseName());    		
    	}
   	
    	if( r1.getDirectClass().getPkArray().size() == 0 && r1.getMult_upper() != 1) {
        	throw new Exception( "Need to generate primary keys for " +
        			r1.getDirectClass().getBaseName() + 
        			"[relation with " + r2.getDirectClass().getBaseName() + "]"); 
    		
    	}
    	if( r2.getDirectClass().getPkArray().size() == 0 && r2.getMult_upper() != 1) {
        	throw new Exception( "Need to generate primary keys for " +
        			r2.getDirectClass().getBaseName() + 
        			"[relation with " + r1.getDirectClass().getBaseName() + "]"); 
    		
    	}
    		
    	UMLrole rolePk = null;
    	UMLrole roleFk = null;
    	
    	/*
    	 * If the situation is as follows:
    	 * 
    	 * if $roles[0] is cardinality 0..n or 1..n
    	 * 
    	 * then copy $roles[1]->{Class}->{PrimaryKeyArray} to
    	 * 	$roles[0]->{ForeignKeyArray}
    	 */
    	if( r1.getMult_upper() != 1 && r2.getMult_upper() == 1 ) {
    	
    		rolePk = r2;
    		roleFk = r1;
    		
    	} else if( r1.getMult_upper() == 1 && r2.getMult_upper() != 1 ) {

    		rolePk = r1;
    		roleFk = r2;

    	} else if( r1.getMult_upper() == 1 && r2.getMult_upper() == 1 ) {
   		
    		/*
    		 * If the cardinality is 1-to-1, 
    		 * then we have to attempt to establish	
    		 * a directionality in the foreign-key/primary-key 
    		 * relationship.
    		 * 
    		 * base this on aggregation or directionality
    		 *  if this does not exist, we assign it arbitrarily
    		 */
     			
    		if( r1.getMult_lower() == 0 && r2.getMult_lower() == 1 ){
        		rolePk = r2;
        		roleFk = r1;
   			} else if( r1.getMult_lower() == 1 && r2.getMult_lower() == 0) {
        		rolePk = r1;
        		roleFk = r2;
   			} else if( r1.getNavigable() && !r2.getNavigable()){
        		rolePk = r1;
        		roleFk = r2;
			} else if( !r1.getNavigable() && r2.getNavigable()){
        		rolePk = r2;
        		roleFk = r1;
   			} // Arbitrarily set the direction roles[0] to roles[1]
			else {
        		rolePk = r1;
        		roleFk = r2;
    		}

    	} else {
    		
    		throw new Exception( "Died trying to generate keys for an n-to-n association: " +
    				this.getBaseName());

    	}

    	Iterator<UMLattribute> pkIt = rolePk.getDirectClass().getPkArray().iterator();
    	while( pkIt.hasNext() ) {
    		UMLattribute pk = pkIt.next();

    		String attName = rolePk.getBaseName() + "_id";
    		
    		UMLattribute fk = roleFk.getDirectClass().lookupAttributeByName(attName);
    		if( fk == null ) {
    			fk = pk.copy();
    			fk.setBaseName( rolePk.getBaseName() + "_id" );
    			fk.setImplName( rolePk.getBaseName() + "_id" );
        		fk.setDesigned(false);
        		fk.setParentClass(roleFk.getDirectClass());
        	}
    		
    		//
    		//	Check to see if attributes exist with the same name already.
    		//
    		
    		//$temp = $self->{Name};
    		//$temp =~ s/\s/_/ig;
    		
    		roleFk.getDirectClass().getAttributes().add(fk);
    		roleFk.getFkArray().add(fk);
    		rolePk.getFkArray().add(fk);
    		fk.setFkRole(roleFk);
    		
    		if( includeInPkFlag ) {
    			roleFk.getDirectClass().getPkArray().add(fk);
          		fk.setStereotype("PK/FK");
    		} else {
          		fk.setStereotype("FK");
        	}
    		
    		pk.getFk().add(fk);
    		fk.setPk(pk);
    		
    		// add the vpdmfOrder attribute if required.
        	if( this.getStereotype() != null &&
        			this.getStereotype().contains("ordered") ) {
    		
        		if( fk.getParentClass().lookupAttributeByName("vpdmfOrder") == null ) {
	    			UMLattribute orderAtt = new UMLattribute();
	    			orderAtt.setBaseName( "vpdmfOrder");
	    			orderAtt.setImplName( "vpdmfOrder");
	    			UMLclass t = this.getModel().listTypes().get("int");
	    			orderAtt.setType(t);
	    			orderAtt.setStereotype("vpdmfOrder");
	    			orderAtt.setDesigned(false);
	    			setStereotype("vpdmfOrder");
	    			
	    			orderAtt.setParentClass(fk.getParentClass());
	    			fk.getParentClass().getAttributes().add(orderAtt);
        		}
		
    		}
    		
    	}
    	
    }
	
}
