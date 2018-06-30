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
public class JavaUmlInterfaceTest {

	ApplicationContext ctx;
	
	JavaUmlInterface jui;
	
	File magic;
	File jar1, jar2;
	
	File resourceModel;	
	File jar3;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();
		magic = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile();	
		jar1 = new File("target" + "/ooevv-model-jpa-src.zip");	
		jar2 = new File(magic.getParent() + "/ooevv-model-jpa.jar");	
		
		resourceModel = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile();	
		jar3 = new File("target" + "/resource-model-simple-src.jar");	
		
			
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
/*	@Test
	public void testGenerateJavaForModel() throws Exception {

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);
		
		UMLmodel m = p.getUmlModels().get(0);
	
		m.convertToRelationalImplementation(".model.");
		
		jui = new JavaUmlInterface();
		jui.setUmlModel(m);
		
		jui.buildJpaMavenProject(jar1, jar2, ".model.", "temp", "temp", "temp");
		//generateJarForModel(jar1, ".model.", false, true);
		
	}*/

	@Test
	public void testGenerateJPAForModel() throws Exception {

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);
		
		UMLmodel m = p.getUmlModels().get(0);
	
		m.convertToRelationalImplementation(".model.");
		
		jui = new JavaUmlInterface();
		jui.setUmlModel(m);

		jui.buildJpaMavenProject(jar1, null, "bmkeg.isi.edu", "ooevv-jpa-test", "1.2.3", "3.2.1");
//		jui.generateJarForModel(jar2, ".model.", true, true);
		
	}

	
}
