package edu.isi.bmkeg.uml.utils;

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
public class UMLArchiveFileBuilderTest {

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
	File ooevv;
	File ooevvZip;
	
	@Before
	public void setUp() throws Exception {
        
		ctx = AppContext.getApplicationContext();

		afb = new UMLArchiveFileBuilder();

		resourceUmlFile = ctx.getResource(
				"classpath:edu/isi/bmkeg/uml/models/resource/resource.xml").getFile();

		ooevv = ctx.getResource(
				"classpath:edu/isi/bmkeg/uml/models/ooevv.xml").getFile();
				
		sheetDir = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/resource/sheets").getFile();
		
		heavyArchiveFile = ctx.getResource("classpath:edu/isi/bmkeg/uml/models/resource/resource_sheets_UML.zip").getFile();
		
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(resourceUmlFile);
		
		m = p.getUmlModels().get(0);
		m.convertToRelationalImplementation();
							
	}

	@After
	public void tearDown() throws Exception {
		
	}
	
	@Test
	public void testBuildArchiveFileFromSheetDir() throws Exception {
	
		afb.buildArchiveFile(m, sheetDir, heavyArchiveFile);
		
		// Get the approximate length of the archive file.
		long length = heavyArchiveFile.length();

		if( length < 16628636 && length > 16630636 ) {
			assertEquals("File should be roughly 16629636 bytes", 16629636, length);
		} else {
			System.err.println("File should be roughly 16629636 bytes, is actually " + length);
		}
		
	}
	
	@Test
	public void testReverseEngineeredModelFromCode() throws Exception {
	
		p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(ooevv);
		
		ooevvZip = new File(ooevv.getParentFile().getAbsolutePath() + "/ooevv-mysql.zip");
		
		m2 = p.getUmlModels().get(0);
		m2.convertToRelationalImplementation(".model.");
		m2.filterClasses("\\.model\\.");
		
		afb.buildArchiveFile(m2, null, ooevvZip);
		
		// Get the approximate length of the archive file.
		long length = ooevvZip.length();

		if( length < 16628636 && length > 16630636 ) {
			assertEquals("File should be roughly 16629636 bytes", 16629636, length);
		} else {
			System.err.println("File should be roughly 16629636 bytes, is actually " + length);
		}
		
	}

}
