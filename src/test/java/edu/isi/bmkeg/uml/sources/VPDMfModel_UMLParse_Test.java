package edu.isi.bmkeg.uml.sources;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.context.ApplicationContext;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

import edu.isi.bmkeg.uml.interfaces.ActionscriptInterface;
import edu.isi.bmkeg.uml.interfaces.JavaUmlInterface;
import edu.isi.bmkeg.uml.interfaces.MysqlUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.springContext.AppContext;

@RunWith(SpringJUnit4ClassRunner.class)
@ContextConfiguration(locations={"/edu/isi/bmkeg/uml/sources/appCtx-UMLTestNoJPA.xml"})
public class VPDMfModel_UMLParse_Test {

	ApplicationContext ctx;
	UMLmodel m;
	UMLModelSimpleParser p;
	
	File magic, vpdmf, resource;
	File zip;
	File zip2;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();
		magic = ctx.getResource("classpath:edu/isi/bmkeg/uml/sources/vpdmf-tests/vpdmf_rev.xml").getFile();
		vpdmf = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/vpdmfUser.xml").getFile();
		resource = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile();
		
		zip = new File(magic.getParentFile().getPath() + "/vpdmf_as.zip");
		zip2 = new File(magic.getParentFile().getPath() + "/kmrgGraph_as.zip");
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public final void testParseMagicDrawFile() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();
		int packageCount = m.listPackages().size();
		
		assertEquals(26, packageCount);
				
	}

	@Test
	public final void testGenerateActionScriptModel() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();

		ActionscriptInterface asi = new ActionscriptInterface();
		asi.setUmlModel(m);
				
//		asi.generateActionscriptForModel(zip, "\\.model(\\.|$)");
//		asi.generateActionscriptForModel(zip2, "\\.kmrgGraph(\\.|$)");
			
	}
	
	@Test
	public final void testVPDMfBasedMergeModels() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(resource);
		p.parseUMLModelFile(vpdmf);

		ArrayList<UMLmodel> models = p.getUmlModels();
		Iterator<UMLmodel> it = models.iterator();
		UMLmodel m1 = it.next();
		UMLmodel m2 = it.next();

		m1.mergeModel(m2);

		MysqlUmlInterface mysql = new MysqlUmlInterface();
		mysql.setUmlModel(m1);
		mysql.convertAttributes();
		System.out.println("~~~MYSQL~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println(m1.debugString());
		
		JavaUmlInterface java = new JavaUmlInterface();
		java.setUmlModel(m1);
		java.convertAttributes();
		System.out.println("~~~JAVA~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.print(m1.debugString());

		//assert("something")
		int i=0;
		i++;
		
	}
	
}

