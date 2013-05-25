package edu.isi.bmkeg.uml.sources;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Vector;

import org.xml.sax.Attributes;

import edu.isi.bmkeg.uml.model.UMLassociation;
import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.model.UMLpackage;
import edu.isi.bmkeg.uml.model.UMLrole;

import org.xml.sax.helpers.DefaultHandler;

public class PosiedonXMI_ModelParserHandler extends UMLModelParserHandler {

    ArrayList<UMLpackage> packageStack = new ArrayList();
    UMLmodel umlModel;

    ArrayList<UMLclass> classStack = new ArrayList();
    UMLclass thisClass;
    
    HashMap thisHash;
    HashMap parentChildLookup = new HashMap();

    UMLassociation thisAssoc;
    UMLrole thisRole;
    
    String divider = " <|> ";
    
    Vector exceptions = new Vector();
    
    boolean error = false;
    
    String currentMatch = "";
    String currentText = "";
    String currentWord = "";

    public PosiedonXMI_ModelParserHandler(String modelName) throws Exception {
    	this.umlModel = new UMLmodel();
    	this.umlModel.setName(modelName);
    }
    
    public void startElement(String uri, String localName, String qName, Attributes attributes) {

        this.currentMatch += divider + qName;

        if (currentMatch.endsWith("UML:Namespace.ownedElement" +
                divider + "UML:Package")) {

            HashMap attrs = getAttrs(attributes);

            UMLpackage thisPack = new UMLpackage();
            thisPack.setModel(umlModel);
            thisPack.setImplName((String) attrs.get("name"));
            thisPack.setBaseName((String) attrs.get("name"));

            UMLpackage parent = getPackage();
            thisPack.setParent(parent);
            
            thisPack.computePackageAddress();
            String addr = thisPack.getPkgAddress();
            
            try {
				this.getUmlModel().addItem(thisPack);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

            this.packageStack.add(thisPack);

        } else if (currentMatch.endsWith("UML:Namespace.ownedElement" +
                divider + "UML:Class")) {
        	
            HashMap attrs = getAttrs(attributes);

            String id = (String) attrs.get("xmi.id");

            thisClass = new UMLclass();
            thisClass.setImplName((String) attrs.get("name"));
            thisClass.setBaseName((String) attrs.get("name"));
            
            classStack.add(thisClass);
            
            UMLpackage p = getPackage();
            p.getClasses().add(thisClass);
            thisClass.setPkg(p);
            
            thisClass.computeClassAddress();
            String addr = thisClass.getClassAddress();
            
            try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.classLookup.put(id, thisClass);


        } else if (currentMatch.endsWith( "UML:Namespace.ownedElement" +
                divider + "UML:DataType")) {
        	
            HashMap attrs = getAttrs(attributes);

            String id = (String) attrs.get("xmi.id");

            thisClass = new UMLclass();
            thisClass.setDataType(true);
            thisClass.setImplName((String) attrs.get("name"));
            thisClass.setBaseName((String) attrs.get("name"));
            
            classStack.add(thisClass);
            
            UMLpackage p = getPackage();
            p.getClasses().add(thisClass);
            thisClass.setPkg(p);

            thisClass.computeClassAddress();
            String addr = thisClass.getClassAddress();

            try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.classLookup.put(id, thisClass);

        } else if (currentMatch.endsWith(divider + "UML:Namespace.ownedElement" +
                divider + "UML:AssociationClass")) {
            
        	HashMap attrs = getAttrs(attributes);

            String id = (String) attrs.get("xmi.id");
            thisClass = new UMLclass();
            thisClass.setImplName((String) attrs.get("name"));
            thisClass.setBaseName((String) attrs.get("name"));
            
            classStack.add(thisClass);

            UMLpackage p = getPackage();
            p.getClasses().add(thisClass);
            thisClass.setPkg(p);
            
            thisClass.computeClassAddress();
            String addr = thisClass.getClassAddress();
            
            try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            this.classLookup.put(id, thisClass);

            thisAssoc = new UMLassociation();
            try {
				this.getUmlModel().addItem(thisAssoc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            if(attrs.containsKey("name")) {
            	thisAssoc.setImplName((String) attrs.get("name") + "_assoc");
            	thisAssoc.setBaseName((String) attrs.get("name") + "_assoc");
            } 

            thisClass.setLinkAssociation(thisAssoc);
            thisAssoc.setLinkClass(thisClass);

        } else if (currentMatch.endsWith("UML:Namespace.ownedElement" +
                divider + "UML:Association")) {
        	
            HashMap attrs = getAttrs(attributes);

            thisAssoc = new UMLassociation();
            try {
				this.getUmlModel().addItem(thisAssoc);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            if(attrs.containsKey("name")) {
            	thisAssoc.setImplName((String) attrs.get("name"));
            	thisAssoc.setBaseName((String) attrs.get("name"));
            }
            
        } else if (currentMatch.endsWith("UML:Association.connection" +
                divider + "UML:AssociationEnd")) {
            
        	HashMap attrs = getAttrs(attributes);

            boolean nav = Boolean.valueOf((String) attrs.get("isNavigable")).booleanValue();

            thisRole = new UMLrole();
            try {
				this.getUmlModel().addItem(thisRole);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
            thisRole.setImplName((String) attrs.get("name"));
            thisRole.setBaseName((String) attrs.get("name"));
            thisRole.setRoleKey(thisRole.getBaseName());

            thisRole.setNavigable(nav);

            if (thisAssoc.getRole1() == null) {
                thisAssoc.setRole1(thisRole);
            } else {
                thisAssoc.setRole2(thisRole);
            }
            thisRole.setAss(thisAssoc);
            

        } else if (currentMatch.endsWith("UML:MultiplicityRange") &&
                thisRole != null) {
        	
            HashMap attrs = getAttrs(attributes);
            
            thisRole.setMult_lower(Integer.valueOf((String) attrs.get("lower")).intValue());

            thisRole.setMult_upper(Integer.valueOf((String) attrs.get("upper")).intValue());
            
        } else if (currentMatch.endsWith("UML:AssociationEnd.participant" +
                divider + "UML:Class")) {

            HashMap attrs = getAttrs(attributes);
            this.roles2resolve.put(
                    thisRole,
                    (String) attrs.get("xmi.idref"));

        } else if (currentMatch.endsWith("UML:Classifier.feature" +
                divider + "UML:Attribute")) {

            HashMap attrs = getAttrs(attributes);
            thisAttr = new UMLattribute();
            try {
				this.getUmlModel().addItem(thisAttr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
            
            thisAttr.setImplName((String) attrs.get("name"));
            thisAttr.setBaseName((String) attrs.get("name"));

            thisClass.getAttributes().add(thisAttr);
            thisAttr.setParentClass(thisClass);

        } else if (currentMatch.endsWith("UML:Attribute" +
                divider + "UML2:TypedElement.type" +
                divider + "UML:DataType")) {

            HashMap attrs = getAttrs(attributes);
            this.attrs2resolve.put(
                    thisAttr,
                    (String) attrs.get("xmi.idref"));

        } else if (currentMatch.endsWith("UML:Attribute" +
                divider + "UML2:TypedElement.type" +
                divider + "UML:Class")) {

            HashMap attrs = getAttrs(attributes);
            this.attrs2resolve.put(
                    thisAttr,
                    (String) attrs.get("xmi.idref"));


        } else if (currentMatch.endsWith("UML:Generalization")) {
        	
            thisHash = new HashMap();

        } else if (currentMatch.endsWith("UML:Generalization" +
                divider + "UML:Generalization.child" +
                divider + "UML:Class")) {
        
        	HashMap attrs = getAttrs(attributes);

            thisHash.put("child", (String) attrs.get("xmi.idref"));

        } else if (currentMatch.endsWith("UML:Generalization" +
                divider + "UML:Generalization.parent" +
                divider + "UML:Class")) {

        	HashMap attrs = getAttrs(attributes);

            thisHash.put("parent", (String) attrs.get("xmi.idref"));

        }
    }

    private UMLpackage getPackage() {
        
    	UMLpackage pkg = null;
        if (!this.packageStack.isEmpty()) {
            pkg = (UMLpackage) this.packageStack.get(
                    this.packageStack.size() - 1);
        } else {
        	pkg = umlModel.getTopPackage();
        	this.packageStack.add(pkg);
        }

        return pkg;

    }

    private String getClassStackString() {
 
    	String out = "";
    	Iterator<UMLclass> it = this.classStack.iterator();
    	while(it.hasNext()) {
    		UMLclass c = it.next();
    		out += c.getBaseName() + "|";
    	}
    	
        return out;

    }
    
    private UMLclass getTopClass() {
    	
    	UMLclass topClass = null;
        if (!this.classStack.isEmpty()) {
        	topClass = this.classStack.get(this.classStack.size() - 1);
        }

        return topClass;

    }
    
    public void endElement(String uri, String localName, String qName) {

    	if (currentMatch.endsWith("UML:Package" +
                divider + "UML:Namespace.ownedElement" +
                divider + "UML:Class")) {

    	    UMLclass top = this.getTopClass();
            this.classStack.remove(top);
            thisClass = this.getTopClass();
    	
    	} else if (currentMatch.endsWith("UML:Package" +
                    divider + "UML:Namespace.ownedElement" +
                    divider + "UML:DataType")) {            
        
    	    UMLclass top = this.getTopClass();
            this.classStack.remove(top);
            thisClass = this.getTopClass();
    	
    	} else if (currentMatch.endsWith("UML:Package")) {

            UMLpackage top = this.getPackage();
            this.packageStack.remove(top);

        } else if (currentMatch.endsWith("UML:Package" +
                divider + "UML:Namespace.ownedElement" +
                divider + "UML:AssociationClass")) {
    		
            UMLclass top = this.getTopClass();
            this.classStack.remove(top);
            thisClass = this.getTopClass();

            thisAssoc = null;

        } else if (currentMatch.endsWith("UML:Association")) {

            thisAssoc = null;

        } else if (currentMatch.endsWith("UML:AssociationEnd")) {

            thisRole = null;

        } else if (currentMatch.endsWith("UML:Attribute")) {

            thisAttr = null;

        } else if (currentMatch.endsWith("UML:Generalization")) {

            this.parentChildLookup.put(
                    thisHash.get("parent"),
                    thisHash.get("child"));
            thisHash = null;

        }

        String c = this.currentMatch;
        this.currentMatch = c.substring(0, c.lastIndexOf(divider + qName));

    }

    public void characters(char[] ch, int start, int length) {
        String value = new String(ch, start, length);
        value = value.replaceAll("[\\n\\t]", "");
        if (value.length() > 0 &&
                currentMatch.endsWith(divider + "Block")) {
            currentText += value;
            currentWord += value;
        }

    }

    protected HashMap getAttrs(Attributes attributes) {

        HashMap map = new HashMap();

        int l = attributes.getLength();
        for (int i = 0; i < l; i++) {
            String t = attributes.getQName(i);
            String v = attributes.getValue(i);
            v = v.replaceAll("\\n", " ");
            v = v.replaceAll("\\s+", " ");
            v = v.replaceAll("^\\s", "");
            v = v.replaceAll("\\s$", "");
            map.put(t, v);
//            System.out.println(t + " : " + v);
        }

        return map;

    }

    private void resetHandler() throws Exception {
        this.setUmlModel(new UMLmodel());
        exceptions = new Vector();

        currentText = "";
        currentWord = "";
    }
    private HashMap classLookup = new HashMap();
    UMLattribute thisAttr;

    public void endDocument() {

        Iterator aIt = this.attrs2resolve.keySet().iterator();
        while (aIt.hasNext()) {
            UMLattribute a = (UMLattribute) aIt.next();
            String atKey = (String) this.attrs2resolve.get(a);
            
           	UMLclass c = (UMLclass) this.classLookup.get(atKey);
            a.setType(c);
                        
        }

        Iterator<UMLpackage> pIt = umlModel.listPackages().values().iterator();
        while (pIt.hasNext()) {
            UMLpackage p = pIt.next();
            umlModel.listPackages().put(p.getPkgAddress(), p);
            p.setModel(umlModel);
        }
        
        // CHECKS
        Iterator<UMLclass> cIt = this.getUmlModel().listAllClasses().values().iterator();
        while (cIt.hasNext()) {
            UMLclass c = (UMLclass) cIt.next();
            aIt = c.getAttributes().iterator();
            while (aIt.hasNext()) {
                UMLattribute a = (UMLattribute) aIt.next();
                if (a.getType() == null) {
                    System.err.print(c.getBaseName() + "." + a.getBaseName() + " type is not set\n");
                    
                }
            }
            
            umlModel.listAllClasses().put(c.getClassAddress(), c);
            c.setModel(umlModel);

        }

        Iterator rIt = this.roles2resolve.keySet().iterator();
        while (rIt.hasNext()) {
            UMLrole r = (UMLrole) rIt.next();
            String roleKey = (String) this.roles2resolve.get(r);
            UMLclass c = (UMLclass) this.classLookup.get(roleKey);
            r.setDirectClass(c);
            c.getDirectRoles().add( r );
        }

        Iterator it = this.parentChildLookup.keySet().iterator();
        while (rIt.hasNext()) {
            String pKey = (String) rIt.next();
            String cKey = (String) this.parentChildLookup.get(pKey);
            UMLclass p = (UMLclass) this.classLookup.get(pKey);
            UMLclass c = (UMLclass) this.classLookup.get(cKey);
            c.setParent(p);
        }
 
        // Run through the existing classes and make connections
        cIt = umlModel.listAllClasses().values().iterator();
        while (cIt.hasNext()) {
            UMLclass c = (UMLclass) cIt.next();

            rIt = c.getDirectRoles().iterator();
            while (rIt.hasNext()) {
                UMLrole r = (UMLrole) rIt.next();

                UMLrole or = r.otherRole();

                or.setAssociateClass(c);
                
                if( or.getBaseName() == null || or.getBaseName().length() == 0 ) {
                	String n = or.getDirectClass().getBaseName();
                	n = n.substring(0,1).toLowerCase() + n.substring(1,n.length());
                	or.setImplName(n);
                	or.setBaseName(n);
                }
                
                c.getAssociateRoles().put(or.getBaseName(),or);
                or.setRoleKey(or.getBaseName());
                
                UMLassociation ass = r.getAss();
                if( ass.getBaseName() == null ){ 
                	ass.setImplName(r.getBaseName() + "__" + or.getBaseName() );
                	ass.setBaseName(r.getBaseName() + "__" + or.getBaseName() );
                }
                
            }

        }
        
    }
    
    public void setUmlModel(UMLmodel umlModel) {
		this.umlModel = umlModel;
	}

	public UMLmodel getUmlModel() {
		return umlModel;
	}

	HashMap roles2resolve = new HashMap();
    HashMap attrs2resolve = new HashMap();
    HashMap parents2resolve = new HashMap();

}