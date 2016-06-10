package edu.isi.bmkeg.uml.interfaces;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.uml.builders.OwlUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import junit.framework.TestCase;

public class OwlUmlInterfaceTest extends TestCase {
	
	OwlUmlBuilder oui;
	
	File ooevvUml, karmaUml;
	File ooevvOwl, karmaOwl;
	
	@Before
	public void setUp() throws Exception {
       
		ooevvUml = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile());	
		ooevvOwl = new File(ooevvUml.getParent() + "/ooevv.owl");	

		karmaUml = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/karma.xml").getFile());	
		karmaOwl = new File(ooevvUml.getParent() + "/karma.owl");	
		
			
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGenerateOwlForOoEVV() throws Exception {
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(ooevvUml);		
		
		UMLmodel m = p.getUmlModels().get(0);
	
		oui = new OwlUmlBuilder();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(ooevvOwl, "http://bmkeg.isi.edu/ooevv/", ".model.");
				
	}

	@Test
	public void testGenerateOwlForKarma() throws Exception {

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(karmaUml);		
		
		UMLmodel m = p.getUmlModels().get(0);
	
		oui = new OwlUmlBuilder();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(karmaOwl, "http://www.isi.edu/infoint/", ".model.");
				
	}

	
}
