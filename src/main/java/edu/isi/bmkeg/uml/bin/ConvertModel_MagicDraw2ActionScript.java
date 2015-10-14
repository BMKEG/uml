package edu.isi.bmkeg.uml.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import edu.isi.bmkeg.uml.interfaces.ActionscriptInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;

public class ConvertModel_MagicDraw2ActionScript {
	
	public static String USAGE = "arguments: <MagicDraw-path> <src-output-path>"; 
	
	/**
	 * Main method to build take a MagicDraw File and dump out a set of ActionScript Model files
	 * @param args: 
	 */
	public static void main(String[] args) {
		
		if( args.length != 2 ) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		File magic = new File(args[0]);	
		File zip = new File(args[1]);	
		
		String pkgPattern = ".model.";
		
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		
		try {
		
			p.parseUMLModelFile(magic);
			
			UMLmodel m = p.getUmlModels().get(0);

			ActionscriptInterface asi = new ActionscriptInterface();
			asi.setUmlModel(m);
					
			asi.generateActionscriptForModel(zip);
		
		} catch (Exception e) {
			
			e.printStackTrace();
		
		}
		
	}

}
