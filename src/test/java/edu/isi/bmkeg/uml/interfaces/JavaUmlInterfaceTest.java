package edu.isi.bmkeg.uml.interfaces;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.uml.builders.JavaUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import junit.framework.TestCase;

public class JavaUmlInterfaceTest extends TestCase{
	
	JavaUmlBuilder jui;
	
	File magic;
	File jar1, jar2;
	
	File resourceModel;	
	File jar3;
	
	@Before
	public void setUp() throws Exception {
        
		magic = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile());	
		jar1 = new File("target" + "/ooevv-model-jpa-src.zip");	
		jar2 = new File(magic.getParent() + "/ooevv-model-jpa.jar");	
		
		resourceModel = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile());	
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
		
		jui = new JavaUmlBuilder();
		jui.setUmlModel(m);

		jui.buildJpaMavenProject(jar1, null, "bmkeg.isi.edu", "ooevv-jpa-test", "1.2.3", "3.2.1");
//		jui.generateJarForModel(jar2, ".model.", true, true);
		
	}

	@Test
	public void testGenerateSimpleJavaForModel() throws Exception {
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(resourceModel);
		
		UMLmodel m = p.getUmlModels().get(0);
	
		m.convertToRelationalImplementation(".model.");
		
		jui = new JavaUmlBuilder();
		jui.setUmlModel(m);

		jui.generateSimpleJavaSourceModel(jar3, ".model.");
		
	}

	
	
}
