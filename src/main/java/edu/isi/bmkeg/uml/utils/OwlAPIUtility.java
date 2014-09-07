package edu.isi.bmkeg.uml.utils;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.apibinding.OWLManager;
import org.semanticweb.owlapi.model.AddAxiom;
import org.semanticweb.owlapi.model.AddImport;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAnnotation;
import org.semanticweb.owlapi.model.OWLAnnotationAssertionAxiom;
import org.semanticweb.owlapi.model.OWLAnnotationProperty;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassAssertionAxiom;
import org.semanticweb.owlapi.model.OWLDataFactory;
import org.semanticweb.owlapi.model.OWLDataProperty;
import org.semanticweb.owlapi.model.OWLDataPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLDataPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLDataRange;
import org.semanticweb.owlapi.model.OWLDatatype;
import org.semanticweb.owlapi.model.OWLDeclarationAxiom;
import org.semanticweb.owlapi.model.OWLImportsDeclaration;
import org.semanticweb.owlapi.model.OWLIndividual;
import org.semanticweb.owlapi.model.OWLLiteral;
import org.semanticweb.owlapi.model.OWLNamedIndividual;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectPropertyAssertionAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyDomainAxiom;
import org.semanticweb.owlapi.model.OWLObjectPropertyRangeAxiom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyManager;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.semanticweb.owlapi.model.OWLRestriction;
import org.semanticweb.owlapi.util.DefaultPrefixManager;
import org.semanticweb.owlapi.util.SimpleIRIMapper;
import org.semanticweb.owlapi.vocab.OWL2Datatype;
import org.semanticweb.owlapi.vocab.OWLRDFVocabulary;

import uk.ac.manchester.cs.owl.owlapi.OWLImportsDeclarationImpl;
import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLitem;
import edu.isi.bmkeg.uml.model.UMLrole;

/**
 * Refer to ./owlapi/examples/OWLPrimer.java for example code
 * @author burns
 *
 */
public class OwlAPIUtility {

	Logger log = Logger.getLogger("edu.isi.bmkeg.uml.util.OwlAPIUtility");
	
	private OWLOntologyManager manager;
	private OWLDataFactory factory;

	private Map<String, OWLDatatype> dataTypes = new HashMap<String, OWLDatatype>();
	private Map<String, IRI> externalIRIs = new HashMap<String, IRI>();
	
	private Map<String, Set<String>> domains = new HashMap<String, Set<String>>();
	private Map<String, String> propMap = new HashMap<String, String>();

	private DefaultPrefixManager pm = null;
//	private PrefixManager rdfsPm;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	/**
	 * Sets up a OwlAPI store for use within Spring.
	 */
	public OwlAPIUtility() {
		manager = OWLManager.createOWLOntologyManager();
		factory = manager.getOWLDataFactory();
		pm = new DefaultPrefixManager();

		dataTypes.put("serial", factory.getOWLDatatype(OWL2Datatype.XSD_LONG.getIRI()) );
		dataTypes.put("byte", factory.getOWLDatatype(OWL2Datatype.XSD_BYTE.getIRI()) );
		dataTypes.put("short", factory.getOWLDatatype(OWL2Datatype.XSD_SHORT.getIRI()) );	
		dataTypes.put("int", factory.getOWLDatatype(OWL2Datatype.XSD_INTEGER.getIRI()) );
		dataTypes.put("long", factory.getOWLDatatype(OWL2Datatype.XSD_LONG.getIRI()) );
		dataTypes.put("float", factory.getOWLDatatype(OWL2Datatype.XSD_FLOAT.getIRI()) );
		dataTypes.put("double", factory.getOWLDatatype(OWL2Datatype.XSD_DOUBLE.getIRI()) );
		dataTypes.put("boolean",  factory.getOWLDatatype(OWL2Datatype.XSD_BOOLEAN.getIRI()) );
		dataTypes.put("char",  factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI()) );
		dataTypes.put("shortString",  factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI()) );
		dataTypes.put("String",  factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI()) );
		dataTypes.put("longString",  factory.getOWLDatatype(OWL2Datatype.XSD_STRING.getIRI()) );
		dataTypes.put("blob",  factory.getOWLDatatype(OWL2Datatype.XSD_BASE_64_BINARY.getIRI()) );
		dataTypes.put("image",  factory.getOWLDatatype(OWL2Datatype.XSD_BASE_64_BINARY.getIRI()) );
		dataTypes.put("date",  factory.getOWLDatatype(OWL2Datatype.XSD_DATE_TIME.getIRI()) );
		dataTypes.put("timestamp",  factory.getOWLDatatype(OWL2Datatype.XSD_DATE_TIME_STAMP.getIRI()) );
		dataTypes.put("url",  factory.getOWLDatatype(OWL2Datatype.XSD_ANY_URI.getIRI()) );
		
				
	}

	public void addOntologyMetadata(OWLOntology o) {

		externalIRIs.put("definition", IRI.create("http://purl.obolibrary.org/obo/IAO_0000119") );
		externalIRIs.put("definition editor", IRI.create("http://purl.obolibrary.org/obo/IAO_0000117") );
		externalIRIs.put("editor note", IRI.create("http://purl.obolibrary.org/obo/IAO_0000116"));
		externalIRIs.put("example of usage", IRI.create("http://purl.obolibrary.org/obo/IAO_0000112"));
		externalIRIs.put("editor preferred term", IRI.create("http://purl.obolibrary.org/obo/IAO_0000111"));
		externalIRIs.put("has obsolescence reason", IRI.create("http://purl.obolibrary.org/obo/IAO_0000231"));
		externalIRIs.put("has curation status", IRI.create("http://purl.obolibrary.org/obo/IAO_0000114"));
		externalIRIs.put("alternative term", IRI.create("http://purl.obolibrary.org/obo/IAO_0000118"));
		externalIRIs.put("imported from", IRI.create("http://purl.obolibrary.org/obo/IAO_0000412"));
		externalIRIs.put("definition", IRI.create("http://purl.obolibrary.org/obo/IAO_0000115"));
		externalIRIs.put("definition source", IRI.create("http://purl.obolibrary.org/obo/IAO_0000119"));
		
		OWLImportsDeclarationImpl owlImpDec = new OWLImportsDeclarationImpl(factory, IRI.create("http://purl.obolibrary.org/obo/iao/ontology-metadata.owl"));
		AddImport ai = new AddImport(o, owlImpDec);
		
		manager.applyChange(ai);
		
	}
	
	public IRI addFilenameMapping(String iri, String filename) {
		IRI ontologyIRI = IRI.create(iri);
		IRI resourceIRI = IRI.create(new File(filename));

		SimpleIRIMapper mapper = new SimpleIRIMapper(ontologyIRI, resourceIRI);
		manager.addIRIMapper(mapper);
		
		return ontologyIRI;
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Higher-level functions to convert between UML models and ontologies
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	/**
	 * 
	 * @param c
	 * @param propMap
	 * @param o
	 * @throws Exception
	 */
	public void constructAllRestrictionsForUMLClass (
			UMLclass c, 
			OWLOntology o) throws Exception {
		
		log.debug("Setting restrictions for " + c.getBaseName() );
		
		//
		// This is where we will put all the class-level restrictions.
		//
		Set<OWLRestriction> restrictions = new HashSet<OWLRestriction>();
		OWLClass cc = factory.getOWLClass(":" + c.readClassAddress(), pm);

		//
		// Add datatype & object type properties to classes from UML attributes. 
		// - note that OWL uses universal definitions for properties. 
		//   They are not scoped to the enclosing class, so we will check the UML definitions and 
		//   throw an exception if two properties have the same name and a different data type.
		// 
		Iterator<UMLattribute> aIt = c.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();
			
			// 
			// Only include attributes that are not foreign keys & are marked as 'implemented' 
			//
			if( a.getFkRole() != null || !a.getToImplement()  ) {
				continue;
			}
			
			// 
			// We use the pattern of having an attribute or a role called 'term',
			// pointing to the "edu.isi.bmkeg.terminology.model.Term" class as 
			// a pattern for this class to act as an OWL class.
			// 
			
			if( a.getBaseName().equals("term") 
					&& a.getType().readClassAddress().equals(
							"edu.isi.bmkeg.terminology.model.Term")
							) {
				continue;
			}
			
			if( !propMap.containsKey(a.getBaseName()) ) {
				
				propMap.put(a.getBaseName(), a.getUuid() );
				
				if( a.getType().isDataType() ) {
					
					this.addDataPropertyRange(a.getBaseName(), a.getType().getBaseName(), o);						
					this.addDataPropertyDomain(c.readClassAddress(), a.getBaseName(), o);	
					
					OWLDatatype dt = this.dataTypes.get( a.getType().getBaseName() );					
					OWLDataRange dataRng = factory.getOWLDatatypeRestriction(dt);
					
					OWLDataProperty dp = factory.getOWLDataProperty(":" + a.getBaseName(), pm);
					restrictions.add( factory.getOWLDataSomeValuesFrom(dp, dataRng) );			
					
				} else {

//					this.addObjectPropertyDomain(a.readCleanAddress(), c.readClassAddress(), o);					
					this.addObjectPropertyRange(a.getBaseName(), a.getType().readClassAddress(), o);						

					Set<String> domSet = new HashSet<String>();
					domSet.add(c.readClassAddress());
					domains.put(a.getBaseName(), domSet);
										
				}
				
				log.debug("Adding cardinality restrictions for ." + a.getBaseName() + "--->" + a.getType().getBaseName() );
				this.addNameComment(a.getBaseName(), a.getBaseName(), o);
				
			} else {
				
				String key = propMap.get( a.getBaseName() );
	
				UMLitem item = c.getModel().getItems().get(key);

				if( item instanceof UMLrole ) {
					System.out.print("ARG");
				}
				
				UMLattribute firstAttr = (UMLattribute) item;
				UMLclass type = firstAttr.getType();
				
				String t = type.getBaseName();
				String tt = a.getType().getBaseName();
				
				//
				//	serial and long values are interchangable.
				//
				if( !t.equals(tt) && 
						(t.equals("serial") && tt.equals("long")) &&
						(t.equals("long") && tt.equals("serial")) ) {
					throw new Exception( "For conversion to OWL, all properties must point to the same target class.\n" +
								firstAttr.getParentClass().getClassAddress() + "." + firstAttr.getBaseName() + "-->" + 
								type.getBaseName() + ",\n and " + a.getParentClass().getClassAddress() + "." + 
								a.getBaseName() + "-->" + a.getType().getBaseName() );
				}
				
				if( a.getType().isDataType() ) {
					
					this.addDataPropertyDomain(c.readClassAddress(), firstAttr.readCleanAddress(), o);						

					OWLDataProperty dp = factory.getOWLDataProperty(":" + firstAttr.getBaseName(), pm);
					OWLDatatype dt = this.dataTypes.get( firstAttr.getType().getBaseName() );					
					
					OWLDataRange dataRng = factory.getOWLDatatypeRestriction(dt);
					restrictions.add( factory.getOWLDataSomeValuesFrom(dp, dataRng) );			

				} else {

//					this.addObjectPropertyDomain(firstAttr.readCleanAddress(), c.readClassAddress(), o);											

					Set<String> domSet = domains.get(a.getBaseName());

					domSet.add(c.readClassAddress());
					domains.put(key, domSet);
					
				}

				log.debug("Adding for " + c.getBaseName() + "." + a.getBaseName() );
				
			}
				
		}
		
		//
		// Add object type properties to classes from UML roles. 
		// - note that OWL uses universal definitions for properties. 
		//   They are not scoped to the enclosing class, so we will check the UML definitions and 
		//   throw an exception if two properties have the same name and a different data type.
		// 
		Iterator<UMLrole> rIt = c.getAssociateRoles().values().iterator();
		while(rIt.hasNext()) {
			UMLrole r = rIt.next();
			
			// 
			// Only include roles that are not foreign keys & are marked as 'implemented' 
			//
			if( !r.getNavigable() || !r.getToImplement()) {
				continue;
			}

			if( r.getBaseName().equals("term") 
					&& r.getDirectClass().readClassAddress().equals(
							"edu.isi.bmkeg.terminology.model.Term")
							) {
				continue;
			}
			
			if (r.getImplementz() != null ){
				r = r.getImplementz();			
			} 
						
			if( !propMap.containsKey(r.getBaseName()) ) {
			
				propMap.put(r.getBaseName(), r.getUuid() );
				
				//this.addObjectPropertyDomain(r.readCleanAddress(), c.readClassAddress(), o);						
				this.addObjectPropertyRange(r.getBaseName(), r.getDirectClass().readClassAddress(), o);						
								
				this.addNameComment(r.getBaseName(), r.getBaseName(), o);
				
				Set<String> domSet = new HashSet<String>();
				domSet.add(c.readClassAddress());
				domains.put(r.getBaseName(), domSet);
				
			} else {
				
				String key = propMap.get(r.getBaseName() );
				
				UMLclass range = null;
				String signature = "";
				String id = "";
				if( c.getModel().getItems().get(key) instanceof UMLattribute ) {

					UMLattribute a = (UMLattribute) c.getModel().getItems().get(key);
					signature = a.getParentClass().getBaseName() + "." + a.getBaseName();
					id = a.readCleanAddress();
					range = a.getType();
					
				} else { 

					UMLrole firstRole = (UMLrole) c.getModel().getItems().get(key);
					signature = firstRole.getAssociateClass().getClassAddress() + "." + firstRole.getBaseName();
					range = firstRole.getDirectClass();					
					id = firstRole.readCleanAddress();
					
				}
				
				if( !range.equals(r.getDirectClass() ) ) {
					throw new Exception( "For conversion to OWL, all properties must point to the same target class.\n" +
							signature + "-->" + 
								range.getBaseName() + ",\n and " + r.getAssociateClass().getClassAddress() + "." + 
								r.getBaseName() + "-->" + r.getDirectClass().getBaseName() );
				}
				
				OWLObjectProperty op = factory.getOWLObjectProperty(":" + r.getBaseName(), pm);
				
				OWLClass tc = factory.getOWLClass(":" + range.readClassAddress(), pm);
				//this.addObjectPropertyDomain(id, c.readClassAddress(), o);
					
				Set<String> domSet = domains.get(r.getBaseName());
				domSet.add(c.readClassAddress());
				domains.put(r.getBaseName(), domSet);
				
			}
			
		}
			
	}
	
	public void constructAllDomainRestrictions (OWLOntology o) throws Exception {
		
		Iterator<String> propNameIt = this.domains.keySet().iterator();
		while(propNameIt.hasNext()) {
			String propName = propNameIt.next();
			Set<String> domSet = this.domains.get(propName);
			this.addObjectPropertyDomainSet(propName, domSet, o);
		}
		
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Small-scale functions to add things to ontologies
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	public void addClass(String cStr, OWLOntology o) {
		String prefix = pm.getDefaultPrefix();
		if (prefix != null) {
			cStr = cStr.replaceAll(prefix, "");
		}

		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLDeclarationAxiom a = factory.getOWLDeclarationAxiom(c);
		manager.addAxiom(o, a);
	}

	public void addSubClassToClass(String cStr, String scStr, OWLOntology o) {
		// TODO: Fix up the prefix management.  This code assumes that the
		// strings either come in without a prefix, or else they come with
		// the default prefix, which is then stripped.
		// Also see below for more places to fix this.
		String prefix = pm.getDefaultPrefix();
		if (prefix != null) {
			cStr = cStr.replaceAll(prefix, "");
			scStr = scStr.replaceAll(prefix, "");
		}

		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLClass sc = factory.getOWLClass(":" + scStr, pm);
		OWLAxiom a = factory.getOWLSubClassOfAxiom(sc, c);
		manager.addAxiom(o, a);
	}

	public void addIndividualToClass(String cStr, String iStr, OWLOntology o) {
		// TODO: Fix up the prefix management. (see above)
		String prefix = pm.getDefaultPrefix();
		if (prefix != null) {
			cStr = cStr.replaceAll(prefix, "");
			iStr = iStr.replaceAll(prefix, "");
		}

		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLClassAssertionAxiom a = factory.getOWLClassAssertionAxiom(c, i);
		manager.addAxiom(o, a);
		
	}
	
	public void addObjectPropertyToIndividual(String iStr, String propStr, String tStr, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLObjectProperty op = factory.getOWLObjectProperty(":" + propStr, pm);
		OWLNamedIndividual t = factory.getOWLNamedIndividual(":" + tStr, pm);
		
		OWLAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(op, i, t);
	    manager.applyChange(new AddAxiom(o, ax));
	    
	}
	
	public void addDataPropertyToIndividual(String iStr, String propStr, String value, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + propStr, pm);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dp, i, value);
	 	manager.applyChange(new AddAxiom(o, ax));

	}    

	public void addDataPropertyToIndividual(String iStr, String propStr, int value, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + propStr, pm);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dp, i, value);
	 	manager.applyChange(new AddAxiom(o, ax));

	}    

	public void addDataPropertyToIndividual(String iStr, String propStr, float value, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + propStr, pm);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dp, i, value);
	 	manager.applyChange(new AddAxiom(o, ax));

	}    

	public void addDataPropertyToIndividual(String iStr, String propStr, long value, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + propStr, pm);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dp, i, value);
	 	manager.applyChange(new AddAxiom(o, ax));

	}   
	
	public void addDataPropertyToIndividual(String iStr, String propStr, double value, OWLOntology o) {			

		OWLNamedIndividual i = factory.getOWLNamedIndividual(":" + iStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + propStr, pm);
		
		OWLAxiom ax = factory.getOWLDataPropertyAssertionAxiom(dp, i, value);
	 	manager.applyChange(new AddAxiom(o, ax));

	}    
	
	public void addDataPropertyDomain(String cStr, String attrStr, OWLOntology o) {

		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLDataProperty dp = factory.getOWLDataProperty(":" + attrStr, pm);
			
		OWLDataPropertyDomainAxiom ax = factory.getOWLDataPropertyDomainAxiom(dp, c);
		
		manager.addAxiom(o, ax);
	
	}
	
	public void addDataPropertyRange(String attrStr, String typeString, OWLOntology o) {
		
        OWLDatatype type = this.dataTypes.get(typeString);
		
		OWLDataProperty dp = factory.getOWLDataProperty(":" + attrStr, pm);
			
		OWLDataPropertyRangeAxiom ax = factory.getOWLDataPropertyRangeAxiom(dp, type);

		manager.applyChange(new AddAxiom(o, ax));
			
	}
	
	public void addObjectPropertyRange(String propStr, String cStr, OWLOntology o) {
		
		if( cStr.equals("edu.isi.bmkeg.terminology.model.Term") ) {
			return;
		}
		
		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLObjectProperty op = factory.getOWLObjectProperty(":" + propStr, pm);
			
		OWLObjectPropertyRangeAxiom ax = factory.getOWLObjectPropertyRangeAxiom(op, c);
		
		manager.applyChange(new AddAxiom(o, ax));
			
	}

	public void addObjectPropertyDomainSet(String propStr, Set<String> cStrSet, OWLOntology o) {
		
		Set<OWLClass> s = new HashSet<OWLClass>();
		
		Iterator<String> cStrIt = cStrSet.iterator();
		while( cStrIt.hasNext() ) {
			String cStr = cStrIt.next();
			
			OWLClass c = factory.getOWLClass(":" + cStr, pm);
			s.add(c);	
		}
		
		OWLObjectProperty op = factory.getOWLObjectProperty(propStr, pm);
		OWLObjectUnionOf union = factory.getOWLObjectUnionOf(s);
		
		OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(op, union);
		
		manager.applyChange(new AddAxiom(o, ax));
	
	}
	
	public void addObjectPropertyDomain(String propStr, String cStr, OWLOntology o) {
		
		OWLClass c = factory.getOWLClass(":" + cStr, pm);
		OWLObjectProperty op = factory.getOWLObjectProperty(":" + propStr, pm);
			
		OWLObjectPropertyDomainAxiom ax = factory.getOWLObjectPropertyDomainAxiom(op, c);
		
		manager.applyChange(new AddAxiom(o, ax));
	
	}

	public void addRelation(String relStr, String i1Str, String i2Str, OWLOntology o) {

		OWLObjectProperty prop = factory.getOWLObjectProperty(IRI.create(relStr));
		OWLIndividual individual = factory.getOWLNamedIndividual(IRI.create(i1Str));
		OWLIndividual object = factory.getOWLNamedIndividual(IRI.create(i2Str));

		OWLObjectPropertyAssertionAxiom ax = factory.getOWLObjectPropertyAssertionAxiom(prop, individual, object);

		manager.addAxiom(o, ax);
	}

	public void addNameComment(String iStr, String name, OWLOntology o) {
		
		IRI i = IRI.create(iStr);
		OWLLiteral n = factory.getOWLLiteral(name, "en");
			           
		OWLAnnotationProperty p = factory
				.getOWLAnnotationProperty(OWLRDFVocabulary.RDFS_LABEL.getIRI());

		OWLAnnotation ann = factory.getOWLAnnotation(p, n);
		OWLAnnotationAssertionAxiom ax = factory
				.getOWLAnnotationAssertionAxiom(i, ann);

		manager.addAxiom(o, ax);

	}

	public void addExternalAnnotation(String iStr, String externalAnnot, String annot, OWLOntology o) throws Exception {
		
		IRI i = IRI.create(iStr);

		OWLLiteral lit = factory.getOWLLiteral(annot, "en");

		if( !this.externalIRIs.containsKey(externalAnnot) )
			throw new Exception("Do not understand '" + externalAnnot + "' annotation type");
		
		OWLAnnotationProperty p = factory.getOWLAnnotationProperty(this.externalIRIs.get(externalAnnot));

		OWLAnnotation ann = factory.getOWLAnnotation(p, lit);
		OWLAnnotationAssertionAxiom ax = factory
				.getOWLAnnotationAssertionAxiom(i, ann);

		manager.addAxiom(o, ax);

	}

	
	public void setPrefix(String prefixUri) {

		pm.setDefaultPrefix(prefixUri);
	}

	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Manipulations on whole ontologies 
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	public void ImportOntology1IntoOntology2(OWLOntology o1, OWLOntology o2) {

		IRI o1_iri = o1.getOntologyID().getOntologyIRI();
		OWLImportsDeclaration imp = factory.getOWLImportsDeclaration(o1_iri);
		manager.applyChange(new AddImport(o2, imp));

	}
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Ontology Management Functions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~	
	public void saveOntology(OWLOntology o) throws OWLOntologyStorageException {

		this.manager.saveOntology(o);

	}
	
	public OWLOntology loadOntology(String uri, String filename) 
			throws Exception {

		IRI iri = this.addFilenameMapping(uri, filename);
		OWLOntology o = null;

		File f = new File(filename);
		if( f.exists() ) {
			o = this.manager.loadOntology(iri);
		} else {
			throw new Exception("File " + filename + " does not exist.");
		}
		
		return o;
	}
	
	public OWLOntology loadOntologyFromUri(String uri) 
			throws Exception {

		IRI iri = IRI.create(uri);
		OWLOntology o = this.manager.loadOntology(iri);
		
		return o;
		
	}
	
	public OWLOntology createOntology(String uri, String filename) 
			throws Exception {

		IRI iri = this.addFilenameMapping(uri, filename);
		OWLOntology o = null;

		File f = new File(filename);
		if( f.exists() ) {
			throw new Exception("File " + filename + " exists already.");
		} else {
			o = manager.createOntology(iri);
		}
		
		return o;
	}
	
	public OWLOntology loadOntologyFromStream(String uri, InputStream stream)
		throws OWLOntologyCreationException {

		OWLOntology o = this.manager.loadOntologyFromOntologyDocument(stream);
		return o;
	}
	
	
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~
	// Ontology inquiry functions
	//~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~		
	public static String readLabel(Set<OWLAnnotationAssertionAxiom> axSet) {
		
		String label = null;
		Iterator<OWLAnnotationAssertionAxiom> lIt = axSet.iterator();
		while(lIt.hasNext()) {
			OWLAnnotationAssertionAxiom l = lIt.next();
			if(l.getProperty().isLabel()) {
				label = ((OWLLiteral) l.getValue()).getLiteral();
				break;
			}
		}
		
		return label;

	}
	
}
