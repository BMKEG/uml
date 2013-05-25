package edu.isi.bmkeg.uml.interfaces;

import static org.junit.Assert.*;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.utils.springContext.AppContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/edu/isi/bmkeg/uml/sources/appCtx-UMLTestNoJPA.xml"})
public class OwlUmlInterfaceTest {

	ApplicationContext ctx;
	
	OwlUmlInterface oui;
	
	File ooevvUml, karmaUml;
	File ooevvOwl, karmaOwl;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();

		ooevvUml = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile();	
		ooevvOwl = new File(ooevvUml.getParent() + "/ooevv.owl");	

		karmaUml = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/karma.xml").getFile();	
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
	
		oui = new OwlUmlInterface();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(ooevvOwl, "http://bmkeg.isi.edu/ooevv/", ".model.");
				
	}

	@Test
	public void testGenerateOwlForKarma() throws Exception {

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(karmaUml);		
		
		UMLmodel m = p.getUmlModels().get(0);
	
		oui = new OwlUmlInterface();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(karmaOwl, "http://www.isi.edu/infoint/", ".model.");
				
	}

	
}
