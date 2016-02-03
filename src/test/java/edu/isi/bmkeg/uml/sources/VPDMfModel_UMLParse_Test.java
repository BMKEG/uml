package edu.isi.bmkeg.uml.sources;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.uml.builders.ActionscriptBuilder;
import edu.isi.bmkeg.uml.builders.JavaUmlBuilder;
import edu.isi.bmkeg.uml.builders.MysqlUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import junit.framework.TestCase;

public class VPDMfModel_UMLParse_Test extends TestCase {

	UMLmodel m;
	UMLModelSimpleParser p;
	
	File magic, vpdmf, resource;
	File zip;
	File zip2;
	
	@Before
	public void setUp() throws Exception {
        
		magic = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/sources/vpdmf-tests/vpdmf_rev.xml").getFile());
		vpdmf = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/vpdmfUser.xml").getFile());
		resource = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile());
		
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

		ActionscriptBuilder asi = new ActionscriptBuilder();
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

		MysqlUmlBuilder mysql = new MysqlUmlBuilder();
		mysql.setUmlModel(m1);
		mysql.convertAttributes();
		System.out.println("~~~MYSQL~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.println(m1.debugString());
		
		JavaUmlBuilder java = new JavaUmlBuilder();
		java.setUmlModel(m1);
		java.convertAttributes();
		System.out.println("~~~JAVA~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");
		System.out.print(m1.debugString());

		//assert("something")
		int i=0;
		i++;
		
	}
	
}

