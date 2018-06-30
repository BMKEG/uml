package edu.isi.bmkeg.uml.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.isi.bmkeg.uml.interfaces.JavaUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;

public class ConvertModel_MagicDraw2Java{
	
	public static String USAGE = "arguments: <MagicDraw-file> <jar-file> <pkg-pattern>"; 

	/**
	 * Main method to build take a MagicDraw File and dump out a set of ActionScript Model files
	 * @param args: 
	 */
	public static void main(String[] args) {
		
		if( args.length != 4 ) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		File magic = new File(args[0]);	
		File zip = new File(args[1]);					
		File src = new File(args[2]);					
		String pkgPattern = args[3];
				
		if( zip.exists() ) {
			System.err.println(args[1]+ " already exists. Overwriting old version.");
			zip.delete();
		}
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		
		try {
		
			p.parseUMLModelFile(magic);
			
			UMLmodel m = p.getUmlModels().get(0);

			JavaUmlInterface jUMLi = new JavaUmlInterface();
			jUMLi.setUmlModel(m);
					
			jUMLi.generateSimpleJavaSourceModel(src, zip, pkgPattern);
					
		} catch (Exception e) {
			
			e.printStackTrace();
		
		}
		
	}

}
