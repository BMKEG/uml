package edu.isi.bmkeg.uml.sources;

import java.net.URI;
import java.util.Collections;
import java.util.Iterator;
import java.util.Set;

import org.semanticweb.owlapi.model.ClassExpressionType;
import org.semanticweb.owlapi.model.OWLClass;
import org.semanticweb.owlapi.model.OWLClassExpression;
import org.semanticweb.owlapi.model.OWLObjectAllValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectIntersectionOf;
import org.semanticweb.owlapi.model.OWLObjectProperty;
import org.semanticweb.owlapi.model.OWLObjectSomeValuesFrom;
import org.semanticweb.owlapi.model.OWLObjectUnionOf;
import org.semanticweb.owlapi.model.OWLOntology;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.util.OWLOntologyWalker;
import org.semanticweb.owlapi.util.OWLOntologyWalkerVisitor;

import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.utils.OwlAPIUtility;

public class OWL2UMLParser {
	
	private OwlAPIUtility owlUtil;
	
	private UMLmodel umlModel;
		
	public void setUmlModel(UMLmodel umlModel) {
		this.umlModel = umlModel;
	}

	public UMLmodel getUmlModel() throws Exception {
		return umlModel;
	}
	
	public void setOwlAPI(OwlAPIUtility owlAPI) {
		this.owlUtil = owlAPI;
	}

	public OwlAPIUtility getOwlAPI() {
		return owlUtil;
	}
	
	public void buildModelFromOWLFile(String resource, String baseURL)
			throws Exception {
		
		OWLOntology o = this.owlUtil.loadOntology(baseURL, resource);
		URI namespace = o.getOntologyID().getOntologyIRI().toURI();

		OWLOntologyWalker walker = new OWLOntologyWalker(Collections.singleton(o));

		UML2OWLWalkerVisitor<Object> visitor = new UML2OWLWalkerVisitor<Object>(walker, namespace);		
		walker.walkStructure(visitor);
		umlModel = visitor.getM();
		
//		this.setBigData(new BigDataBean(journalName, resource, baseURL));
		
//		List<Map> classList = this.getBigData().listClassesInOWL();
//		List<Map> relationList = this.getBigData().listRelationsInOWL();
		
		int pause = 0;
		pause++;
		
	}
		
	private class UML2OWLWalkerVisitor<T> extends OWLOntologyWalkerVisitor<T> {
	
		private UMLmodel m;
		
		/**
		 * @return the m
		 */
		public UMLmodel getM() {
			return m;
		}

		public UML2OWLWalkerVisitor(OWLOntologyWalker walker, URI namespace) throws Exception {
			super(walker);
			m = new UMLmodel();
		}

		public T visit(OWLClass c) {

			OWLOntology o = getCurrentOntology();
			
			String l = OwlAPIUtility.readLabel(c.getAnnotationAssertionAxioms(o));

			//this.m.getClasses();
			UMLclass umlC = new UMLclass();
			umlC.setBaseName(l);
			umlC.setUri(c.getIRI().toURI());

			Iterator<OWLClassExpression> exIt = c.getEquivalentClasses(o)
					.iterator();

			while (exIt.hasNext()) {
				OWLClassExpression ex = exIt.next();
				evaluateClassExpressionForPropertyTargets(umlC, ex);
			}

			return null;

		}

		public void evaluateClassExpressionForPropertyTargets(UMLclass c, 
				OWLClassExpression ex) {

			ClassExpressionType type = ex.getClassExpressionType();
			if (type.equals(ClassExpressionType.OBJECT_INTERSECTION_OF)) {

				OWLObjectIntersectionOf ex1 = (OWLObjectIntersectionOf) ex;

				Set<OWLClassExpression> encExSet = ex1
						.getNestedClassExpressions();
				Iterator<OWLClassExpression> encExIt = encExSet.iterator();
				while (encExIt.hasNext()) {
					OWLClassExpression encEx = encExIt.next();
					if (!encEx.isClassExpressionLiteral() && !encEx.equals(ex)) {
						evaluateClassExpressionForPropertyTargets(c, encEx);
					}

				}


			} else if (type.equals(ClassExpressionType.OBJECT_UNION_OF)) {

				OWLObjectUnionOf ex1 = (OWLObjectUnionOf) ex;

				Set<OWLClassExpression> encExSet = ex1
						.getNestedClassExpressions();
				Iterator<OWLClassExpression> encExIt = encExSet.iterator();
				while (encExIt.hasNext()) {
					OWLClassExpression encEx = encExIt.next();
					if (!encEx.isClassExpressionLiteral() && !encEx.equals(ex)) {
						evaluateClassExpressionForPropertyTargets(c, encEx);
					}
				}

			} else if (type.equals(ClassExpressionType.OBJECT_ALL_VALUES_FROM)) {

				OWLObjectAllValuesFrom ex1 = (OWLObjectAllValuesFrom) ex;

				Set<OWLObjectProperty> pSet = ex1.getObjectPropertiesInSignature();
				OWLObjectProperty p = pSet.iterator().next();

				Set<OWLClass> targetSet = ex1.getClassesInSignature();
				OWLClass target = targetSet.iterator().next();

// TODO: Want to add the build instructions to the existing class here. 
				
				System.out.println("OBJECT_ALL_VALUES_FROM: " + ex.toString());
				System.out.println("   ==> " + p + " only " + target);

			} else if (type.equals(ClassExpressionType.OBJECT_SOME_VALUES_FROM)) {

				OWLObjectSomeValuesFrom ex1 = (OWLObjectSomeValuesFrom) ex;

				Set<OWLObjectProperty> pSet = ex1.getObjectPropertiesInSignature();
				OWLObjectProperty p = pSet.iterator().next();

				Set<OWLClass> targetSet = ex1.getClassesInSignature();
				OWLClass target = targetSet.iterator().next();

// TODO: Want to add the build instructions to the existing class here. 
//				targets.put(p, c);

				System.out.println("OBJECT_SOME_VALUES_FROM: " + ex.toString());
				System.out.println("   ==> " + p + " some " + target);

			}

		}		
		
	};
	
}
