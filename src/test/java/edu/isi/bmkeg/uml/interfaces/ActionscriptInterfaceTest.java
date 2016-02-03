package edu.isi.bmkeg.uml.interfaces;

import java.io.File;

import org.junit.After;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

import edu.isi.bmkeg.uml.builders.ActionscriptBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import junit.framework.TestCase;

public class ActionscriptInterfaceTest extends TestCase {

	ActionscriptBuilder asi;
	
	File magic;
	File zip, swc;
	
	@Before
	public void setUp() throws Exception {
        
		magic = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile());	
		zip = new File("target" + "/ooevv-asModel.zip");	
		swc = new File("target" + "/ooevv-asModel.swc");
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);
		
		UMLmodel m = p.getUmlModels().get(0);
	
		asi = new ActionscriptBuilder();
		asi.setUmlModel(m);
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test @Ignore("Fails")
	public void testBuildSwf() throws Exception {
				
		asi.getUmlModel().convertToRelationalImplementation(".model.");
		asi.buildFlexMojoMavenProject(zip, swc, "edu.isi.bmkeg", "resource-as", "0.0.1", "0.1.0-SNAPSHOT");
		
	}

	@Test
	public void testBuildSrc() throws Exception {
				
		asi.getUmlModel().convertToRelationalImplementation(".model.");
		asi.buildFlexMojoMavenProject(zip, null, "edu.isi.bmkeg", "resource-as", "0.0.1", "0.1.0-SNAPSHOT");
		
	}

}
