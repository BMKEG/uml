package edu.isi.bmkeg.uml.sources;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;

import org.apache.log4j.Logger;
import org.xml.sax.Attributes;

import edu.isi.bmkeg.uml.model.UMLassociation;
import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLitem;
import edu.isi.bmkeg.uml.model.UMLpackage;
import edu.isi.bmkeg.uml.model.UMLrole;

public class MagicDrawXMI_ModelParserHandler extends UMLModelParserHandler {

	Logger log = Logger
			.getLogger("edu.isi.bmkeg.uml.sources.MagicDrawXMI_ModelParserHandler");

	ArrayList<UMLitem> packageStack = new ArrayList<UMLitem>();
	ArrayList<UMLclass> classStack = new ArrayList<UMLclass>();
	ArrayList<String> typeStack = new ArrayList<String>();

	String currentDocumentation = "";
	HashMap<String, String> documentation = new HashMap<String, String>();

	UMLclass thisClass;

	HashMap thisHash;
	HashMap<UMLclass, String> parentChildLookup = new HashMap<UMLclass, String>();
	HashSet<String> orderedAssocs = new HashSet<String>();
	HashSet<String> proxyClasses = new HashSet<String>();

	UMLattribute thisAttr;
	UMLassociation thisAssoc;
	UMLrole thisRole;

	String divider = " <|> ";

	boolean error = false;

	String currentMatch = "";
	String currentText = "";
	String currentWord = "";

	Map<String, UMLassociation> assoc2resolve = new HashMap<String, UMLassociation>();
	HashMap<String, String> rolesAndAssoc = new HashMap<String, String>();
	HashMap<UMLrole, String> roles2resolve = new HashMap<UMLrole, String>();
	HashMap<UMLattribute, String> attrs2resolve = new HashMap<UMLattribute, String>();
	HashMap<UMLclass, String> parents2resolve = new HashMap<UMLclass, String>();
	Map<String, String> packageNamespace = new HashMap<String, String>();

	public MagicDrawXMI_ModelParserHandler(String modelName) throws Exception {
		super();
		this.getUmlModel().setName(modelName);
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) {

		this.currentMatch += divider + qName;
		HashMap<String, String> attrs = getAttrs(attributes);
		String xmiType = attrs.get("xmi:type");
		String uuid = attrs.get("xmi:id");
		String association = attrs.get("association");
		String baseAssoc = attrs.get("base_Association");
		String name = attrs.get("name");

		if (xmiType != null) {
			this.typeStack.add(xmiType + "=" + name);
		} else {
			this.typeStack.add("-");
		}

		// In MagicDraw
		if (!runCheckOnCurrentMatch()) {
			return;
		}

		// the parser will use a package with a null uuid when classes
		// are in the top level
		if (currentMatch.endsWith(divider + "packagedElement") && uuid != null
				&& xmiType != null && xmiType.equals("uml:Package")) {

			UMLpackage thisPack = new UMLpackage(uuid);
			try {
				this.getUmlModel().addItem(thisPack);
			} catch (Exception e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
			thisPack.setImplName(attrs.get("name"));
			thisPack.setBaseName(attrs.get("name"));

			UMLpackage parent = getPackage();
			thisPack.setParent(parent);

			thisPack.computePackageAddress();
			String addr = thisPack.getPkgAddress();

			this.packageStack.add(thisPack);

			// System.out.println(this.currentMatch);
			// System.out.println("pkg:" + thisPack.getPkgAddress());

		} else if (currentMatch.endsWith(divider + "packagedElement")
				&& xmiType != null && xmiType.equals("uml:Class")) {

			String id = attrs.get("xmi.id");

			thisClass = new UMLclass(uuid);
			try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				e.printStackTrace();
			}

			thisClass.setImplName(attrs.get("name"));
			thisClass.setBaseName(attrs.get("name"));

			classStack.add(thisClass);

			UMLpackage p = getPackage();
			p.getClasses().add(thisClass);
			thisClass.setPkg(p);

			thisClass.computeClassAddress();
			String addr = thisClass.getClassAddress();

			// System.out.println("class:" + thisClass.getClassAddress());

		} else if (currentMatch.endsWith(divider + "packagedElement")
				&& xmiType != null && xmiType.equals("uml:DataType")) {

			String id = attrs.get("xmi.id");

			thisClass = new UMLclass(uuid);
			try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				e.printStackTrace();
			}

			thisClass.setDataType(true);
			thisClass.setImplName(attrs.get("name"));
			thisClass.setBaseName(attrs.get("name"));

			classStack.add(thisClass);

			UMLpackage p = getPackage();
			p.getClasses().add(thisClass);
			thisClass.setPkg(p);

			thisClass.computeClassAddress();
			String addr = thisClass.getClassAddress();

		} else if (currentMatch.endsWith(divider + "packagedElement")
				&& xmiType != null && xmiType.equals("uml:AssociationClass")) {

			thisClass = new UMLclass();
			try {
				this.getUmlModel().addItem(thisClass);
			} catch (Exception e) {
				e.printStackTrace();
			}

			thisClass.setImplName(attrs.get("name"));
			thisClass.setBaseName(attrs.get("name"));

			classStack.add(thisClass);

			UMLpackage p = getPackage();
			p.getClasses().add(thisClass);
			thisClass.setPkg(p);

			thisClass.computeClassAddress();
			String addr = thisClass.getClassAddress();

			// System.out.println("class:" + thisClass.getClassAddress());

			thisAssoc = new UMLassociation(uuid);
			try {
				this.getUmlModel().addItem(thisAssoc);
			} catch (Exception e) {
				e.printStackTrace();
			}

			p = getPackage();
			p.getAssociations().add(thisAssoc);
			thisAssoc.setPkg(p);

			if (name != null) {
				thisAssoc.setImplName(name);
				thisAssoc.setBaseName(name);
			}

			assoc2resolve.put(uuid, thisAssoc);
			thisClass.setLinkAssociation(thisAssoc);
			thisAssoc.setLinkClass(thisClass);

		} else if (currentMatch.endsWith(divider + "upperValue")
				&& thisRole != null) {

			int upper = Integer.valueOf(attrs.get("value")).intValue();
			thisRole.setMult_upper(upper);

		} else if (currentMatch.endsWith(divider + "lowerValue")
				&& thisRole != null) {

			String v = attrs.get("value");

			if (v == null) {
				thisRole.setMult_lower(0);
			} else {
				thisRole.setMult_lower(Integer.valueOf(v).intValue());
			}

		}
		//
		// This is where one end of the role is non-navigable.
		// This is encoded differently in the XMI model and it's serialization.
		//
		else if (currentMatch.endsWith(divider + "ownedAttribute")
				&& xmiType != null && xmiType.equals("uml:Property")
				&& association != null && thisClass != null) {

			thisRole = new UMLrole(uuid);

			try {
				this.getUmlModel().addItem(thisRole);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			thisRole.setImplName(name);
			thisRole.setBaseName(name);
			thisRole.setRoleKey(name);

			thisRole.setNavigable(true);
			thisRole.setMult_upper(1);
			thisRole.setMult_lower(1);

			thisClass.getAssociateRoles().put(name, thisRole);

			thisRole.setAssociateClass(thisClass);

			// this.rolesAndAssoc.put("", "");
			roles2resolve.put(thisRole, attrs.get("type"));
			rolesAndAssoc.put(uuid, association);

			log.debug("< Role from attribute designation>: "
					+ thisClass.getBaseName() + "." + thisRole.getBaseName()
					+ " = " + attrs.get("type"));

			// System.out.println("role:" + thisClass.getClassAddress() + "--->"
			// + thisRole.getBaseName());

		} else if (currentMatch.endsWith(divider + "ownedEnd")
				&& xmiType != null && xmiType.equals("uml:Property")
				&& thisAssoc != null) {

			thisRole = new UMLrole(uuid);

			try {
				this.getUmlModel().addItem(thisRole);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			if (name == null || name.length() == 0)
				name = uuid;

			thisRole.setImplName(name);
			thisRole.setBaseName(name);
			thisRole.setRoleKey(name);

			thisRole.setNavigable(false);
			thisRole.setMult_upper(1);
			thisRole.setMult_lower(1);

			roles2resolve.put(thisRole, attrs.get("type"));

			// System.out.println("role:" + thisClass.getClassAddress() + "--->"
			// + thisRole.getBaseName());

		} else if (currentMatch.endsWith(divider + "packagedElement")
				&& xmiType != null && xmiType.equals("uml:Association")) {

			thisAssoc = new UMLassociation(uuid);
			try {
				this.getUmlModel().addItem(thisAssoc);
			} catch (Exception e) {
				e.printStackTrace();
			}

			UMLpackage p = getPackage();
			p.getAssociations().add(thisAssoc);
			thisAssoc.setPkg(p);

			if (name != null) {
				thisAssoc.setImplName(name);
				thisAssoc.setBaseName(name);
			}

			assoc2resolve.put(uuid, thisAssoc);

		}
		// Presence of the 'ordered' stereotype applied to an association.
		else if (currentMatch.endsWith(":ordered") && baseAssoc != null) {

			orderedAssocs.add(baseAssoc);

		}
		// Presence of the 'proxy' stereotype applied to a class.
		else if (currentMatch.endsWith(":proxy")) {

			this.proxyClasses.add(attrs.get("base_Class"));

		} else if (currentMatch.endsWith(divider + "packagedElement" + divider
				+ "memberEnd")
				&& thisAssoc != null) {

			this.rolesAndAssoc.put(attrs.get("xmi:idref"), thisAssoc.getUuid());

		}
		/*
		 * Note that in MagicDraw files the structure of attributes are :
		 * 
		 * <ownedAttribute xmi:type='uml:Property'
		 * xmi:id='_15_5_1_668022b_1305238959644_150108_509' name='statement'
		 * visibility='private' isOrdered='false' isUnique='true' isLeaf='false'
		 * isStatic='false' isReadOnly='false' isDerived='false'
		 * isDerivedUnion='false' aggregation='none'> <type
		 * xmi:type='uml:PrimitiveType'
		 * href='http://schema.omg.org/spec/UML/2.0/uml.xml#String'>
		 * <xmi:Extension extender='MagicDraw UML 15.5' extenderID='MagicDraw
		 * UML 15.5'> <referenceExtension referentPath='UML Standard
		 * Profile::UML2 Metamodel::AuxiliaryConstructs::PrimitiveTypes::String'
		 * referentType='PrimitiveType'/> </xmi:Extension> </type>
		 * </ownedAttribute>
		 * 
		 * The structure identifying the attribute's type is in a lower-level
		 * part of the file.
		 */
		else if (currentMatch.endsWith(divider + "ownedAttribute")
				&& xmiType != null && xmiType.equals("uml:Property")
				&& association == null && thisClass != null) {

			thisAttr = new UMLattribute(uuid);
			try {
				this.getUmlModel().addItem(thisAttr);
			} catch (Exception e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}

			thisAttr.setImplName(attrs.get("name"));
			thisAttr.setBaseName(attrs.get("name"));

			thisClass.getAttributes().add(thisAttr);
			thisAttr.setParentClass(thisClass);

			if (attrs.containsKey("type")) {
				String typeId = attrs.get("type");
				this.attrs2resolve.put(thisAttr, typeId);
			}

			// System.out.println("attr:" + thisClass.getClassAddress() + "." +
			// thisAttr.getBaseName());

		}
		/*
		 * ... continued from the section immediately above...
		 */
		else if (currentMatch.endsWith(divider + "ownedAttribute" + divider
				+ "type")
				&& xmiType != null && xmiType.equals("uml:Class")) {

			String typeId = attrs.get("href").substring(1);
			this.attrs2resolve.put(thisAttr, typeId);

		}
		/*
		 * ... if the data type is being referred to from an outside file.
		 * Better make sure its one of the standard types.
		 */
		else if (currentMatch.endsWith(divider + "ownedAttribute" + divider
				+ "type" + divider + "xmi:Extension" + divider
				+ "referenceExtension")) {

			String path = attrs.get("referentPath");
			String typeId = path.substring(path.lastIndexOf("::") + 2,
					path.length());

			if (this.getPreexistingTypes().containsKey(typeId)) {
				UMLclass t = this.getPreexistingTypes().get(typeId);

				//
				// Adding support for permitting basic types
				// (Strings, etc) to participate in roles.
				//
				if (thisAttr == null && thisRole != null) {
					this.getExceptions().add(
							"Cannot use basic types in roles:"
									+ t.getBaseName());
				} else {
					thisAttr.setType(t);
				}

			} else if (typeId.equals("Element")) {
				System.out
						.println("Detected 'Element' references, usually this means "
								+ "that you've added new stereotypes to this model. This is not "
								+ "important for VPDMf modeling.");
			} else if (typeId.equals("Property")) {
				System.out
						.println("Detected 'Property' references, usually this means "
								+ "that you've added new stereotypes to this model. This is not "
								+ "important for VPDMf modeling.");

			} else {
				System.err.println("Don't recognize " + typeId);
			}

		}
		/*
		 * ... continued from the section immediately above...
		 */
		else if (currentMatch.endsWith(divider + "ownedAttribute" + divider
				+ "type")
				&& xmiType != null && xmiType.equals("uml:DataType")) {

			String typeId = attrs.get("href").substring(1);
			this.attrs2resolve.put(thisAttr, typeId);

			// System.out.println("attr:" + thisClass.getClassAddress() + "." +
			// thisAttr.getBaseName());

		} else if (currentMatch.endsWith("generalization") && xmiType != null
				&& xmiType.equals("uml:Generalization")) {

			this.parentChildLookup.put(thisClass, attrs.get("general"));

		} else if (currentMatch.endsWith(divider
				+ "MagicDraw_Profile:HyperlinkOwner")) {

			String packageId = attrs.get("base_Element");
			String url = attrs.get("hyperlinkTextActive");

			packageNamespace.put(packageId, url);

		} else if (currentMatch.endsWith(divider + "ownedComment")) {

			this.currentDocumentation = attrs.get("body");

		} else if (currentMatch.endsWith(divider + "ownedComment" + divider
				+ "annotatedElement")) {

			this.documentation.put(attrs.get("xmi:idref"),
					this.currentDocumentation);

		}

	}

	private UMLpackage getPackage() {

		UMLpackage pkg = null;
		if (!this.packageStack.isEmpty()) {
			pkg = (UMLpackage) this.packageStack
					.get(this.packageStack.size() - 1);
		} else {
			pkg = this.getUmlModel().getTopPackage();
			this.packageStack.add(pkg);
		}

		return pkg;

	}

	private String getClassStackString() {

		String out = "";
		Iterator<UMLclass> it = this.classStack.iterator();
		while (it.hasNext()) {
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

	/**
	 * Runs checks to see if we should parse the current line in the XML
	 * 
	 * @return
	 */
	private boolean runCheckOnCurrentMatch() {

		if (typeStack.contains("uml:Package=File View"))
			return false;

		if (currentMatch
				.startsWith(divider + "xmi:XMI" + divider + "uml:Model")
				|| currentMatch.startsWith(divider + "xmi:XMI" + divider
						+ "xmi:Extension" + divider + "proxies")
				|| currentMatch.endsWith(":ordered")
				|| currentMatch.endsWith(":proxy")
				|| currentMatch.endsWith(":HyperlinkOwner")) {

			return true;

		} else {

			return false;

		}

	}

	public void endElement(String uri, String localName, String qName) {

		if (runCheckOnCurrentMatch()) {

			if (currentMatch.endsWith(divider + "packagedElement")) {

				if (classStack.size() > 0) {
					UMLclass top = this.getTopClass();
					this.classStack.remove(top);
					thisClass = this.getTopClass();
				} else if (thisAssoc != null) {
					thisAssoc = null;
				} else if (typeStack.get(typeStack.size() - 1).startsWith(
						"uml:Package")) {
					UMLpackage top = this.getPackage();
					this.packageStack.remove(top);
				} else if (currentMatch.endsWith("UML:Package" + divider +
				  "UML:Namespace.ownedElement" + divider +
				  "UML:AssociationClass")) {
				  
				  UMLclass top = this.getTopClass();
				  this.classStack.remove(top); thisClass = this.getTopClass();
				  
				  thisAssoc = null;
				 
				} else if (currentMatch.endsWith("ownedAttribute")) {

					if (thisRole != null) {
						thisRole = null;
					} else {
						thisAttr = null;
					}
				
				}
				
			}

		}

		String c = this.currentMatch;

		this.typeStack.remove(this.typeStack.size() - 1);
		this.currentMatch = c.substring(0, c.lastIndexOf(divider + qName));

	}

	public void characters(char[] ch, int start, int length) {
		String value = new String(ch, start, length);
		value = value.replaceAll("[\\n\\t]", "");
		if (value.length() > 0 && currentMatch.endsWith(divider + "Block")) {
			currentText += value;
			currentWord += value;
		}

	}

	protected HashMap<String, String> getAttrs(Attributes attributes) {

		HashMap<String, String> map = new HashMap<String, String>();

		int l = attributes.getLength();
		for (int i = 0; i < l; i++) {
			String t = attributes.getQName(i);
			String v = attributes.getValue(i);
			v = v.replaceAll("\\n", " ");
			v = v.replaceAll("\\s+", " ");
			v = v.replaceAll("^\\s", "");
			v = v.replaceAll("\\s$", "");
			map.put(t, v);
			// System.out.println(t + " : " + v);
		}

		return map;

	}

	public void endDocument() {

		HashSet<UMLclass> exoticTypes = new HashSet<UMLclass>();

		Iterator<UMLattribute> aIt = this.attrs2resolve.keySet().iterator();
		while (aIt.hasNext()) {
			UMLattribute a = aIt.next();
			String atKey = this.attrs2resolve.get(a);

			String s = "ML_Standard_Profile.xml#";
			if (atKey.startsWith(s)) {
				atKey = atKey.substring(s.length());
			}

			UMLitem item = this.getUmlModel().getItems().get(atKey);

			if (item == null) {
				continue;
			}

			if (!(item instanceof UMLclass))
				System.err.print(item.getClass().getName() + "(id="
						+ item.getUuid() + ") is not a class");
			else {

				UMLclass c = (UMLclass) item;
				UMLclass t = this.getUmlModel().listTypes()
						.get(c.getBaseName());

				// Uses an exotic type such as 'longString'
				if (t != null && !c.equals(t)) {
					exoticTypes.add(c);
					c = t;
				}
				a.setType(c);

			}

		}

		Iterator<UMLitem> pIt = this.getUmlModel().getItems().values()
				.iterator();
		while (pIt.hasNext()) {
			UMLitem item = pIt.next();
			if (!(item instanceof UMLpackage))
				continue;

			UMLpackage p = (UMLpackage) item;
			this.getUmlModel().listPackages().put(p.getPkgAddress(), p);
			p.setModel(this.getUmlModel());

		}

		// CHECKS
		UMLclass sType = this.getUmlModel().listTypes().get("String");
		Iterator cIt = this.getUmlModel().listAllClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = (UMLclass) cIt.next();
			aIt = c.getAttributes().iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();
				if (a.getType() == null) {
					System.err.print(c.getBaseName() + "." + a.getBaseName()
							+ " type is not set, adding 'String'\n");
					a.setType(sType);
				}
			}

			this.getUmlModel().listAllClasses().put(c.getClassAddress(), c);
			c.setModel(this.getUmlModel());

		}

		Iterator rIt = this.roles2resolve.keySet().iterator();
		while (rIt.hasNext()) {
			UMLrole roleKey = (UMLrole) rIt.next();
			String classId = (String) this.roles2resolve.get(roleKey);

			// Role is 'active' i.e. defined as a property of a class
			if (this.getUmlModel().getItems().containsKey(classId)) {

				UMLitem item = this.getUmlModel().getItems().get(classId);

				UMLclass c = (UMLclass) item;
				roleKey.setDirectClass(c);

			}

		}

		rIt = this.rolesAndAssoc.keySet().iterator();
		while (rIt.hasNext()) {
			String roleKey = (String) rIt.next();

			String assocKey = this.rolesAndAssoc.get(roleKey);
			UMLassociation assoc = (UMLassociation) this.getUmlModel()
					.getItems().get(assocKey);

			// Role is 'active' i.e. defined as a property of a class
			if (this.getUmlModel().getItems().containsKey(roleKey)) {

				UMLitem item = this.getUmlModel().getItems().get(roleKey);

				if (item instanceof UMLattribute) {
					UMLattribute a = (UMLattribute) item;

					getExceptions()
							.add(a.getParentClass().getBaseName()
									+ "."
									+ a.getBaseName()
									+ " is an attribute that participates in an assocation. Consider "
									+ "making this a role\n");
					continue;

				}

				UMLrole r = (UMLrole) item;
				if (assoc.getRole1() == null)
					assoc.setRole1(r);
				else
					assoc.setRole2(r);
				r.setAss(assoc);

			}
			/*
			 * Note we DO NOT set the role if it is not named and navigable. {
			 * 
			 * UMLrole r = new UMLrole(roleKey); this.getUmlModel().addItem(r);
			 * r.setNavigability(false);
			 * 
			 * if(assoc.getRole1() == null ) assoc.setRole1(r); else
			 * assoc.setRole2(r); r.setAss(assoc);
			 * 
			 * }
			 */

		}

		Iterator<UMLclass> it = this.parentChildLookup.keySet().iterator();
		while (it.hasNext()) {
			UMLclass c = it.next();
			String pUid = this.parentChildLookup.get(c);
			UMLclass p = (UMLclass) this.getUmlModel().getItems().get(pUid);
			c.setParent(p);
			p.getChildren().add(c);

			log.debug("Parent: " + p.getImplName() + " - Child: "
					+ c.getImplName());
		}

		// use stereotypes to define some classes as 'proxy'
		Iterator<String> idIt = this.proxyClasses.iterator();
		while (idIt.hasNext()) {
			String id = idIt.next();
			UMLclass c = (UMLclass) this.getUmlModel().getItems().get(id);
			c.setStereotype("proxy");
			log.debug(c.getImplName() + "<<proxy>>");
		}

		// Run through the existing classes and make connections
		cIt = this.getUmlModel().listAllClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = (UMLclass) cIt.next();

			rIt = c.getAssociateRoles().values().iterator();
			while (rIt.hasNext()) {
				UMLrole r = (UMLrole) rIt.next();

				UMLrole or = r.otherRole();

				if (or == null) {

					UMLassociation ass = r.getAss();

					or = new UMLrole();
					or.setNavigable(false);
					or.setBaseName(or.getUuid());
					or.setImplName(or.getUuid());
					or.setMult_lower(0);
					or.setMult_upper(1);
					or.setAss(ass);
					if (ass.getRole1() == null) {
						ass.setRole1(or);
					} else {
						ass.setRole2(or);
					}

					or.setAssociateClass(r.getDirectClass());
					r.getDirectClass().getAssociateRoles()
							.put(or.getImplName(), or);

				}

				or.setDirectClass(c);
				c.getDirectRoles().add(or);

				UMLassociation ass = r.getAss();
				if (ass.getBaseName() == null) {
					ass.setImplName(r.getBaseName() + "__" + or.getBaseName());
					ass.setBaseName(r.getBaseName() + "__" + or.getBaseName());
				}

			}

		}

		Iterator<UMLitem> itemIt = this.getUmlModel().getItems().values()
				.iterator();
		while (itemIt.hasNext()) {
			UMLitem item = itemIt.next();

			if (item instanceof UMLassociation) {
				UMLassociation ass = (UMLassociation) item;

				this.checkAndFixRole(ass.getRole1());
				this.checkAndFixRole(ass.getRole2());
			}
		}

		// Check every association and every role
		cIt = this.getUmlModel().listAllClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = (UMLclass) cIt.next();

			rIt = c.getAssociateRoles().values().iterator();
			while (rIt.hasNext()) {
				UMLrole r = (UMLrole) rIt.next();
				UMLrole or = r.otherRole();

				if (or == null)
					getExceptions().add(
							"Can't find other role for " + c.getClassAddress()
									+ "." + r.getImplName());

				if (r.getAssociateClass() == null)
					getExceptions().add(
							"Can't find associate class for "
									+ c.getClassAddress() + "."
									+ r.getImplName());

				if (r.getDirectClass() == null)
					getExceptions().add(
							"Can't find direct class for "
									+ c.getClassAddress() + "."
									+ r.getImplName());

			}
		}

		Iterator<String> orderedIdsIt = this.orderedAssocs.iterator();
		while (orderedIdsIt.hasNext()) {
			String orderedId = orderedIdsIt.next();
			UMLassociation assoc = this.assoc2resolve.get(orderedId);
			assoc.setStereotype("ordered");
		}

		Iterator<String> pKeyIt = this.packageNamespace.keySet().iterator();
		while (pKeyIt.hasNext()) {
			String pKey = pKeyIt.next();
			UMLpackage pkg = (UMLpackage) this.getUmlModel().getItems()
					.get(pKey);
			try {
				pkg.setUri(new URI(this.packageNamespace.get(pKey)));
			} catch (URISyntaxException e) {
				System.err.print("Namespace incorrectly formed for "
						+ pkg.getPkgAddress() + " : "
						+ this.packageNamespace.get(pKey));
				e.printStackTrace();
			}

		}

		// Fill in children for packages.
		Iterator<UMLpackage> pkgIt = this.getUmlModel().listPackages().values()
				.iterator();
		while (pkgIt.hasNext()) {
			UMLpackage pkg = pkgIt.next();
			UMLpackage parent = pkg.getParent();
			if (parent != null)
				parent.getChildren().add(pkg);
		}

		// Clean up 'Exotic Types' that get added to the model during editing.
		Iterator<UMLclass> eIt = exoticTypes.iterator();
		while (eIt.hasNext()) {
			UMLclass e = eIt.next();

			this.getUmlModel().getItems().remove(e.getUuid());
			e.setModel(null);
			e.getPkg().getClasses().remove(e);
			e.setPkg(null);

		}

		Iterator<String> uidIt = this.documentation.keySet().iterator();
		while (uidIt.hasNext()) {
			String uid = uidIt.next();
			UMLitem item = this.getUmlModel().getItems().get(uid);
			String doc = this.documentation.get(uid);
			if (item != null)
				item.setDocumentation(doc);
		}

	}

	private UMLrole checkAndFixRole(UMLrole r) {

		UMLclass ac = r.getAssociateClass();
		UMLclass dc = r.getDirectClass();
		UMLrole or = r.otherRole();

		if (or == null) {
			getExceptions().add("Other role is null");
		}

		UMLclass oac = or.getAssociateClass();
		UMLclass odc = or.getDirectClass();

		if (ac == null && odc != null) {
			r.setAssociateClass(odc);
			ac = r.getAssociateClass();
		}

		if (dc == null && oac != null) {
			r.setDirectClass(oac);
			dc = r.getDirectClass();
		}

		log.debug(r.getDirectClass().getBaseName() + "." + r.getBaseName()
				+ "[mult:" + r.getMult_upper() + currentMatch + ", nav:"
				+ r.getNavigable() + "]");

		return r;

	}

}