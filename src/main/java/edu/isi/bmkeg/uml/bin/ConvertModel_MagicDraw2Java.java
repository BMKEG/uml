package edu.isi.bmkeg.uml.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.isi.bmkeg.uml.builders.JavaUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;

public class ConvertModel_MagicDraw2Java{
	
	public static String USAGE = "arguments: <dir> <MagicDraw-file> <jar-file>"; 

	/**
	 * Main method to build take a MagicDraw File and dump out a set of ActionScript Model files
	 * @param args: 
	 */
	public static void main(String[] args) {
		
		if( args.length != 3 ) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		File loc = new File(args[0]);
		File magic = new File(loc.getPath() + "/" + args[1]);	
		File zip = new File(loc.getPath() + "/" + args[2]);	
				
		String pkgPattern = ".model.";
		
		// No Hibernate annotations, just keep it java
		boolean hibAnnot = false;
		
		if( zip.exists() ) {
			System.err.println(args[1]+ " already exists. Overwriting old version.");
			zip.delete();
		}
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		
		try {
		
			p.parseUMLModelFile(magic);
			
			UMLmodel m = p.getUmlModels().get(0);

			JavaUmlBuilder jUMLi = new JavaUmlBuilder();
			jUMLi.setUmlModel(m);
					
			jUMLi.generateSimpleJavaSourceModel(zip, pkgPattern);
					
		} catch (Exception e) {
			
			e.printStackTrace();
		
		}
		
	}

}
