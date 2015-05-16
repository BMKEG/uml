package edu.isi.bmkeg.uml.interfaces;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLOntology;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.model.UMLpackage;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.uml.utils.OwlAPIUtility;
import edu.isi.bmkeg.utils.MapCreate;

public class OwlUmlInterface extends UmlComponentInterface implements
		ImplConvert {

	Logger log = Logger
			.getLogger("edu.isi.bmkeg.uml.interfaces.OwlUmlInteface");

	private OwlAPIUtility owlUtil;

	private Set<String> toOmit = new HashSet<String>();

	private ClassPool pool = ClassPool.getDefault();

	private static String[] owlTargetTypes = new String[] { "serial", "byte",
			"short", "int", "long", "float", "double", "boolean", "char",
			"String", "String", "String", "blob", "image", "date", "timestamp",
			"url" };

	public OwlUmlInterface() throws Exception {

		this.buildLookupTable();

		this.owlUtil = new OwlAPIUtility();

		this.toOmit.add("ViewTable");
		this.toOmit.add("ViewLinkTable");
		this.toOmit.add("vpdmfUser");
		this.toOmit.add("KnowledgeBase");

	}

	public void buildLookupTable() throws Exception {

		this.setLookupTable(new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, owlTargetTypes)));

	}

	public void saveUmlAsOwl(File owlFile, String uri, String pkgPattern)
			throws Exception {

		UMLmodel m = this.getUmlModel();

		if (owlFile.exists()) {
			owlFile.delete();
		}

		this.setUmlModel(m);

		m.cleanModel();
		this.convertAttributes();
		this.implementAllRoles(pkgPattern);

		OwlAPIUtility owlUtil = new OwlAPIUtility();
		OWLOntology o = owlUtil.createOntology(uri, owlFile.getAbsolutePath());
		owlUtil.setPrefix(uri);

		Map<String, UMLpackage> pkgMap = m.listPackages(pkgPattern);
		for( String key : pkgMap.keySet() ) {
			UMLpackage pkg = pkgMap.get(key);
			if( pkg.getUri() != null )
				owlUtil.setPrefix(pkg.readPrefix() + ":", pkg.getUri().toString());
		}
		
		Map<String, UMLclass> classMap = m.listClasses(pkgPattern);

		//
		// Add each class and name it.
		//
		Iterator<String> cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			addr = addr.substring(2, addr.length());

			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if ((c.getStereotype() != null && c.getStereotype().equals("Link"))
					|| toOmit.contains(c.getBaseName())) {
				continue;
			}

			owlUtil.addClass(c.readPrefix() + ":" + c.getBaseName(), o);
			owlUtil.addNameComment(c.getBaseName(), c.getBaseName(), o);

		}

		//
		// Add inheritance relationships.
		//
		cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if ((c.getStereotype() != null && c.getStereotype().equals("Link"))
					|| toOmit.contains(c.getBaseName())) {
				continue;
			}

			UMLclass parent = c.getParent();

			if (parent != null) {

				if (toOmit.contains(parent.getBaseName())) {
					continue;
				}

				owlUtil.addSubClassToClass(
						parent.readPrefix() + ":" + parent.getBaseName(),
						c.readPrefix() + ":" + c.getBaseName(), o);

			}

		}

		//
		// Add datatype & object type properties to classes from UML attributes
		// and roles.
		// - note that OWL uses universal definitions for properties.
		// They are not scoped to the enclosing class, so we will check the UML
		// definitions and
		// throw an exception if two properties have the same name and a
		// different class as it's
		// range.
		//
		cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if ((c.getStereotype() != null && c.getStereotype().equals("Link"))
					|| toOmit.contains(c.getBaseName())) {
				continue;
			}

			owlUtil.constructAllRestrictionsForUMLClass(c, o);

		}

		owlUtil.constructAllDomainRestrictions(o);

		owlUtil.saveOntology(o);

	}

	private void implementAllRoles(String pkgPattern) {

		Map<String, UMLclass> classMap = this.getUmlModel().listClasses(pkgPattern);

		Iterator<String> cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			Iterator<UMLrole> rIt = c.getAssociateRoles().values().iterator();
			while (rIt.hasNext()) {
				UMLrole r = rIt.next();
				r.setToImplement(true);
			}
		}

	}

}
