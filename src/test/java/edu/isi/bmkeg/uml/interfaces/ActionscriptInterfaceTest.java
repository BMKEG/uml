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
public class ActionscriptInterfaceTest {

	ApplicationContext ctx;
	
	ActionscriptInterface asi;
	
	File magic;
	File zip, swc;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();
		magic = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile();	
		zip = new File(magic.getParent() + "/ooevv-asModel.zip");	
		swc = new File(magic.getParent() + "/ooevv-asModel.swc");
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);
		
		UMLmodel m = p.getUmlModels().get(0);
	
		asi = new ActionscriptInterface();
		asi.setUmlModel(m);
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testBuildSwf() throws Exception {
				
		asi.getUmlModel().convertToRelationalImplementation(".model.");
		asi.buildFlexMojoMavenProject(zip, swc, "edu.isi.bmkeg", "resource-as", "0.0.1", "0.1.0-SNAPSHOT");
		
	}

}
