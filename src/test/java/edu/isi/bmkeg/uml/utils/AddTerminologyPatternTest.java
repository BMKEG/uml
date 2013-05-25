package edu.isi.bmkeg.uml.utils;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.isi.bmkeg.uml.interfaces.JavaUmlInterface;
import edu.isi.bmkeg.uml.interfaces.OwlUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.utils.springContext.AppContext;


@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/edu/isi/bmkeg/uml/sources/appCtx-UMLTestNoJPA.xml"})
public class AddTerminologyPatternTest {

	ApplicationContext ctx;
	UMLmodel m, m2;
	UMLModelSimpleParser p;
	
	UMLArchiveFileBuilder afb;
	
	File resourceUmlFile;
	File buildFile;
	File lightArchiveFile;
	File xlFile;
	File heavyArchiveFile;
	File sheetDir;
	File ooevv, owlFile, jarFile;
	File ooevvZip;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();

		ooevv = ctx.getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevvTermDemo.xml").getFile();		
		owlFile = new File(ooevv.getParent() + "/ooevv_demo.owl");
		jarFile = new File(ooevv.getParent() + "/ooevv_demo.jar");
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(ooevv);
		
		m = p.getUmlModels().get(0);
							
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
/*	@Test
	public void testAddTerminologyPattern() throws Exception {
			
		m.convertToOwlImplementation(".model.");
		
		JavaUmlInterface oui = new JavaUmlInterface();
		oui.setUmlModel(m);
		oui.generateSimpleJavaSourceModel(jarFile, ".model.");
		
	}*/

	@Test
	public void testSaveAsOwl() throws Exception {
			
		OwlUmlInterface oui = new OwlUmlInterface();
		oui.setUmlModel(m);
		oui.saveUmlAsOwl(owlFile, "", ".model.");
		
		int pause = 0;
		pause++;
		
	}

}
