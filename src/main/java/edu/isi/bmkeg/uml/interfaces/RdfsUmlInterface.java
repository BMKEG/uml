package edu.isi.bmkeg.uml.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import javassist.ClassPool;
import jena.schemagen;

import org.apache.log4j.Logger;
import org.semanticweb.owlapi.model.OWLRestriction;

import com.hp.hpl.jena.ontology.OntClass;
import com.hp.hpl.jena.ontology.OntModel;
import com.hp.hpl.jena.ontology.OntModelSpec;
import com.hp.hpl.jena.ontology.OntProperty;
import com.hp.hpl.jena.rdf.model.ModelFactory;
import com.hp.hpl.jena.rdf.model.Resource;
import com.hp.hpl.jena.vocabulary.XSD;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLpackage;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.MapCreate;

public class RdfsUmlInterface extends UmlComponentInterface implements ImplConvert {
	
	Logger log = Logger.getLogger("edu.isi.bmkeg.uml.interfaces.RdfUmlInteface");
	
	private Set<String> toOmit = new HashSet<String>();
	
	private ClassPool pool = ClassPool.getDefault();
	
	private OntModel m;
	
	private Map<String, Resource> dataTypes = new HashMap<String, Resource>();
	
	private String stem;
	
	private static String[] rdfsTargetTypes = new String[] {
        "serial", 
        "byte",   
        "short",  
        "int",    
        "long",   
        "float",  
        "double", 
        "boolean",
        "char",   
        "shortString",
        "String",     
        "longString", 
        "blob",			
        "image",        
        "date",         
        "timestamp",
        "url"
	};
	
	public RdfsUmlInterface(String stem) throws Exception {

		this.stem = stem;
		
		this.buildLookupTable();
		
		this.toOmit.add("ViewTable");
		this.toOmit.add("ViewLinkTable");
		this.toOmit.add("vpdmfUser");
		this.toOmit.add("KnowledgeBase");
		
		m = ModelFactory.createOntologyModel(OntModelSpec.RDFS_MEM_TRANS_INF);
		
		dataTypes.put("serial", XSD.xlong );
		dataTypes.put("byte", XSD.base64Binary );
		dataTypes.put("short", XSD.xshort );	
		dataTypes.put("int", XSD.xint );
		dataTypes.put("long", XSD.xlong );
		dataTypes.put("float", XSD.xfloat );
		dataTypes.put("double", XSD.xdouble );
		dataTypes.put("boolean", XSD.xboolean );
		dataTypes.put("char", XSD.xstring );
		dataTypes.put("shortString", XSD.xstring );
		dataTypes.put("String", XSD.xstring );
		dataTypes.put("longString", XSD.xstring );
		dataTypes.put("blob", XSD.base64Binary );
		dataTypes.put("image", XSD.base64Binary );
		dataTypes.put("date", XSD.date );
		dataTypes.put("timestamp", XSD.dateTime );
		dataTypes.put("url", XSD.anyURI );
		
	}

	public void buildLookupTable() throws Exception {
		
		this.setLookupTable(new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, rdfsTargetTypes)));
				
	}
		
	public void saveUmlAsRdfs(File rdfsFile) throws Exception {

		if (rdfsFile.exists()) {
			rdfsFile.delete();
		}
		
		this.getUmlModel().cleanModel();
				
		this.convertAttributes();
		
		// need to add namespaces for every 'model' package
		Map<String, UMLpackage> pkgMap = this.getUmlModel().listPackages(".model.");
		Iterator<String> pIt = pkgMap.keySet().iterator();
		while(pIt.hasNext()) {
			String addr = pIt.next();
			UMLpackage p = pkgMap.get(addr);
			String pUri = Converters.pkg2Uri(stem, p.readPackageAddress(), "model");	
			String shortName = pUri.substring(pUri.lastIndexOf("/")+1);
			m.setNsPrefix(shortName, pUri);		
		}
		
		Map<String, UMLclass> classMap = this.getUmlModel().listClasses(".model.");
		
		
		/*try {
			
			classMap.remove("|.edu.isi.bmkeg.terminology.model.Term");
			classMap.remove("|.edu.isi.bmkeg.terminology.model.Ontology");
			classMap.remove("|.edu.isi.bmkeg.terminology.model.TermMapping");
			
		} catch (Exception e) {
			
		}*/
		
		//
		// Add each class and name it. 
		// 
		Iterator<String> cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			addr = addr.substring(2,addr.length());
			
			// Check to see if the class is a set backing table... 
			// if so don't generate the source code.
			if( (c.getStereotype() != null && c.getStereotype().equals("Link")) 
					|| toOmit.contains(c.getBaseName()) ){
				continue;
			}
			
			String uri = Converters.pkg2Uri(stem, addr, "model");
			
			m.createClass(uri);
			
			log.debug("Addding " + uri );
														
		}
		
		//
		// Add inheritance relationships. 
		// 
		cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);
			
			// Check to see if the class is a set backing table... 
			// if so don't generate the source code.
			if( (c.getStereotype() != null && c.getStereotype().equals("Link")) 
					|| toOmit.contains(c.getBaseName()) ){
				continue;
			}
			
			UMLclass parent = c.getParent();
	
			if( parent != null ) {
			
				if( toOmit.contains(parent.getBaseName()) ){
					continue;
				}
				
				String pUri = Converters.pkg2Uri(stem, parent.readClassAddress(), "model");				
				OntClass pRdfs = m.getOntClass( pUri );
				
				String cUri = Converters.pkg2Uri(stem, c.readClassAddress(), "model");
				OntClass cRdfs = m.getOntClass( cUri );
				
				pRdfs.addSubClass(cRdfs);
				cRdfs.addSuperClass(pRdfs);

				log.debug( pUri + " ---parentOf---> " + cUri );
			
			}
		
		}
		
		//
		// Add datatype & object type properties to classes from UML attributes and roles. 
		// - note that OWL uses universal definitions for properties. 
		//   They are not scoped to the enclosing class, so we will check the UML definitions and 
		//   throw an exception if two properties have the same name and a different class as it's
		//   range.
		// 
		Map<String, String> propMap = new HashMap<String, String>();
		cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);
			
			// Check to see if the class is a set backing table... 
			// if so don't generate the source code.
			if( (c.getStereotype() != null && c.getStereotype().equals("Link")) 
					|| toOmit.contains(c.getBaseName()) ){
				continue;
			}
			log.debug("Setting properties for " + c.getBaseName() );
			
			//
			// This is where we will put all the class-level restrictions.
			//
			Set<OWLRestriction> restrictions = new HashSet<OWLRestriction>();
			
			String ns = Converters.pkg2Uri(stem, c.getPkg().readPackageAddress(), "model");				
			
			OntClass cc = m.getOntClass( ns + "/" + c.getBaseName() );

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
				if( a.getFkRole() != null || !a.getToImplement()) {
					continue;
				}
				
				if( !propMap.containsKey(ns + "/" + a.getBaseName()) ) {
					
					propMap.put(ns + "/" + a.getBaseName(), a.getUuid() );
					
					// in Jena, they don't differentiate between DataProperties and ObjectProperties and 
					if( a.getType().isDataType() ) {
						
						OntProperty dp = m.createOntProperty( ns + "/" + a.getBaseName() );

						log.debug(" Added property: " + ns + "/" + a.getBaseName() );
						
						dp.addDomain(cc);
						dp.addRange( this.dataTypes.get( a.getType().getBaseName() ) );
												
					} else {

						OntProperty op = m.createOntProperty( ns + "/" + a.getBaseName() );

						log.debug(" Added property: " + ns + "/" +  a.getBaseName() );

						op.addDomain(cc);
						
						String tcUri = Converters.pkg2Uri(stem, a.getType().readClassAddress(), "model" );		
						OntClass tc = m.getOntClass( tcUri);
 						op.addRange(tc);
						
					}
					
					//log.debug("Adding object/range restrictions for ." + a.getBaseName() + "--->" + a.getType().getBaseName() );
					
				} else {
					
					String key = propMap.get( ns + "/" + a.getBaseName() );
				
					UMLattribute firstAttr = (UMLattribute) c.getModel().getItems().get(key);
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
					

					OntProperty op = m.getOntProperty( ns + "/" + a.getBaseName() );
					op.addDomain(cc);

					log.debug("Adding for " + ns + "/" + a.getBaseName() );
					
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
				
				if (r.getImplementz() != null ){
					r = r.getImplementz();			
				} 
							
				if( !propMap.containsKey( ns + "/" + r.getBaseName()) ) {
				
					propMap.put( ns + "/" + r.getBaseName(), r.getUuid() );
					
					OntProperty op = m.createOntProperty( ns + "/" + r.getBaseName() );					
					
					log.debug(" Added property: " + ns + "/" + r.getBaseName() );

					op.addDomain(cc);
					
					String tcUri = Converters.pkg2Uri(stem, r.getDirectClass().readClassAddress(), "model" );		
					OntClass tc = m.getOntClass( tcUri);
					op.addRange(tc);
					
				} else {
					
					String key = propMap.get( ns + "/" + r.getBaseName() );
					
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
										
					OntProperty op = m.getOntProperty( ns + "/" + r.getBaseName() );
					
					op.addDomain(cc);
					
				}
				
			}				
				
		}

		BufferedWriter out = new BufferedWriter(new FileWriter(rdfsFile));
		m.write(out,"RDF/XML-ABBREV");
				
	}	
	
	public void schemagenify(File rdfsFile, File out, String javaPkg) throws Exception {
		
		schemagen s = new schemagen();
				
		String[] args = new String[] { 
				"-n", this.getUmlModel().getName() + "_RDFS", 
				"--package", javaPkg,
				"-i", rdfsFile.getAbsolutePath(), 
				"-o", out.getAbsolutePath()
				};
		
		s.main(args);
		
	}
	
		
}
