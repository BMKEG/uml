package edu.isi.bmkeg.uml.bin;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;

import org.apache.log4j.Logger;

import edu.isi.bmkeg.uml.interfaces.JavaUmlInterface;
import edu.isi.bmkeg.uml.interfaces.OwlUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;

public class ConvertModel_MagicDraw2Owl {

	Logger log = Logger
			.getLogger("edu.isi.bmkeg.uml.bin.ConvertModel_MagicDraw2Owl");

	public static String USAGE = "arguments: <directory> <umlFileName> <owlFileName> <owlNamespace>";

	/**
	 * Main method to build take a MagicDraw File and dump out an OWL Model file
	 * 
	 * @param args
	 *            :
	 */
	public static void main(String[] args) throws Exception {

		if (args.length != 4) {
			System.err.println(USAGE);
			System.exit(-1);
		}

		File dir = new File(args[0]);
		File magic = new File(dir + "/" + args[1]);
		File owl = new File(dir + "/" + args[2]);
		String ns = args[3];

		String pkgPattern = ".model.";

		if (owl.exists()) {
			System.err.println(owl.getPath()
					+ " already exists. Overwriting old version.");
			owl.delete();
		}

		if (!magic.exists()) {
			System.err.print("File: " + magic.getAbsolutePath()
					+ " does not exist");
			System.exit(-1);
		}

		UMLModelSimpleParser p = new UMLModelSimpleParser(
				UMLmodel.XMI_MAGICDRAW);

		p.parseUMLModelFile(magic);

		UMLmodel m = p.getUmlModels().get(0);

		OwlUmlInterface oui = new OwlUmlInterface();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(owl, ns, pkgPattern);

	}

}
