package edu.isi.bmkeg.uml.interfaces;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.uml.builders.RdfsUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import junit.framework.TestCase;

public class RdfsUmlInterfaceTest extends TestCase {

	
	RdfsUmlBuilder rdfsInt;
	
	File ooevvUml;
	File ooevvRdfs;
	File ooevvOut;
	
	String uri = "http://bmkeg.isi.edu/ooevv/";
	String stem= "edu.isi.bmkeg";
	
	@Before
	public void setUp() throws Exception {
        
		ooevvUml = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile());	
		ooevvRdfs = new File(ooevvUml.getParent() + "/ooevv_rdfs.ttl");	
		ooevvOut = new File(ooevvUml.getParent() + "/ooevv_rdfs");	

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(ooevvUml);		
		
		UMLmodel m = p.getUmlModels().get(0);
	
		rdfsInt = new RdfsUmlBuilder(stem);
		rdfsInt.setUmlModel(m);
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGenerateRdfsForOoEVV() throws Exception {
	
		rdfsInt.saveUmlAsRdfs(ooevvRdfs);
				
	}

	@Test
	public void testSchemagenify() throws Exception {

		rdfsInt.schemagenify(ooevvRdfs, ooevvOut, "edu.isi.bmkeg.ooevv.rdfs");
				
	}
	
}
