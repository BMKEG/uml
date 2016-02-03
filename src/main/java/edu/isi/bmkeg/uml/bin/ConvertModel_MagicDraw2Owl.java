package edu.isi.bmkeg.uml.bin;

import java.io.File;

import org.apache.log4j.Logger;
import org.kohsuke.args4j.CmdLineException;
import org.kohsuke.args4j.CmdLineParser;
import org.kohsuke.args4j.Option;

import edu.isi.bmkeg.uml.builders.OwlUmlBuilder;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;

public class ConvertModel_MagicDraw2Owl {

	public static class Options {

		@Option(name = "-inXml", usage = "Output", required = true, metaVar = "INPUT_XML")
		public File inXml;
		
		@Option(name = "-outOwl", usage = "Pmid", required = true, metaVar = "OUTPUT_OWL")
		public File outOwl;

		@Option(name = "-namespace", usage = "FrgType", required = true, metaVar = "NAMESPACE")
		public String namespace = "";

		@Option(name = "-pkgPattern", usage = "Package Pattern", required = false, metaVar = "PKG_PATTERN")
		public String pkgPattern = ".*";

	}
		
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

		Options options = new Options();

		CmdLineParser parser = new CmdLineParser(options);
	
		try {

			parser.parseArgument(args);
		
		} catch (CmdLineException e) {

			System.err.println(e.getMessage());
			System.err.print("Arguments: ");
			parser.printSingleLineUsage(System.err);
			System.err.println("\n\n Options: \n");
			parser.printUsage(System.err);
			System.exit(-1);

		}
		
		File magic = options.inXml;
		File owl = options.outOwl;
		String ns = options.namespace;
		
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

		OwlUmlBuilder oui = new OwlUmlBuilder();
		oui.setUmlModel(m);

		oui.saveUmlAsOwl(owl, ns, options.pkgPattern);

	}

}
