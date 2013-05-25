package edu.isi.bmkeg.uml.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.isi.bmkeg.uml.interfaces.ActionscriptInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.uml.utils.UMLArchiveFileBuilder;

public class ConvertModel_UML2SQL {

	public static String USAGE = "arguments: <dir> <MagicDraw-file> <zip-file> [sheets]"; 
	
	/**
	 * Main method to build take a MagicDraw File and builds an archive file for the models 
	 * @param args: 
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {
		
		if( args.length < 3 || args.length > 4) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		File loc = new File(args[0]);	
		File umlFile = new File(args[1]);	
		File zip = new File(args[2]);	
		
		File sheets = null;
		if( args.length == 4 ) {				
			sheets = new File(args[3]);
		}

		if( zip.exists() ) {
			System.err.println(zip.getPath() + " already exists. Overwriting old version.");
			zip.delete();
		}
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		
		p.parseUMLModelFile(umlFile);
		
		UMLmodel m = p.getUmlModels().get(0);

		UMLArchiveFileBuilder afb = new UMLArchiveFileBuilder();
		afb.buildArchiveFile(m, sheets, zip);
	
		
	}
	
}
