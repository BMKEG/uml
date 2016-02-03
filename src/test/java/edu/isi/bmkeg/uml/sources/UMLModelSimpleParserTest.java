package edu.isi.bmkeg.uml.sources;

import static org.junit.Assert.assertEquals;

import java.io.File;
import java.util.ArrayList;
import java.util.Map;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import junit.framework.TestCase;

public class UMLModelSimpleParserTest extends TestCase {

	UMLmodel m;
	UMLModelSimpleParser p;
	
	File magic;
	File poseidon;
	File argo;
	
	@Before
	public void setUp() throws Exception {
        
		magic = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile());
		argo = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/sources/argoUMLTest.uml").getFile());
		poseidon = new File(this.getClass().getClassLoader().getResource(
				"classpath:edu/isi/bmkeg/uml/sources/poseidon-core.xml").getFile());
		
	}

	@After
	public void tearDown() throws Exception {
		
	}

	@Test
	public final void testParsePoseidonFile() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_POSEIDON);
		p.parseUMLModelFile(poseidon);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();
		
		int packageCount = m.listPackages().size();
		
		assertEquals(12, packageCount);
			
	}

	@Test
	public final void testParseMagicDrawFile() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();
		int packageCount = m.listPackages().size();
		
		assertEquals(6, packageCount);
				
	}

	@Test
	public final void testParseArgoUMLFile() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_ARGOUML);
		p.parseUMLModelFile(argo);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();
		int packageCount = m.listPackages().size();
		
		Map<String,UMLclass> cList = m.listClasses();
		int classCount = cList.size();
		
		UMLclass author = cList.get("|.test.Author");
		int attCount = author.getAttributes().size();
		
		assertEquals(2, packageCount);
		assertEquals(4, classCount);
		assertEquals(2, attCount);
		
				
	}

	
	@Test
	public final void testConverToRelational() throws Exception {
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);

		ArrayList<UMLmodel> models = p.getUmlModels();
		UMLmodel m = models.iterator().next();
		m.convertToRelationalImplementation(".");
		
		int classCount = m.listClasses().size();

		assertEquals(12, classCount);

	}	

	
}
