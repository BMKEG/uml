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
public class RdfsUmlInterfaceTest {

	ApplicationContext ctx;
	
	RdfsUmlInterface rdfsInt;
	
	File ooevvUml;
	File ooevvRdfs;
	File ooevvOut;
	
	String uri = "http://bmkeg.isi.edu/ooevv/";
	String stem= "edu.isi.bmkeg";
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();

		ooevvUml = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile();	
		ooevvRdfs = new File(ooevvUml.getParent() + "/ooevv_rdfs.xml");	
		ooevvOut = new File(ooevvUml.getParent() + "/ooevv_rdfs");	

		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(ooevvUml);		
		
		UMLmodel m = p.getUmlModels().get(0);
	
		rdfsInt = new RdfsUmlInterface(stem);
		rdfsInt.setUmlModel(m);
		
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testGenerateRdfsForOoEVV() throws Exception {
	
		rdfsInt.saveUmlAsRdfs(ooevvRdfs);
				
	}

	@Test
	public void testSchemagenify() throws Exception {

		rdfsInt.schemagenify(ooevvRdfs, ooevvOut, "edu.isi.bmkeg.ooevv.rdfs");
				
	}
	
}
