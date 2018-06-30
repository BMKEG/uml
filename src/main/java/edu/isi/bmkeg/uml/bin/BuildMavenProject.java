package edu.isi.bmkeg.uml.bin;

import java.io.File;

import edu.isi.bmkeg.uml.interfaces.JavaUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.sources.UMLModelSimpleParser;
import edu.isi.bmkeg.utils.Converters;

public class BuildMavenProject {

	public static String USAGE = "arguments: <model> <pkgPattern> <target-dir> <group> <artifact> <version>"; 
	
	/**
	 * @param args
	 * @throws Exception 
	 */
	public static void main(String[] args) throws Exception {

		if( args.length != 6 ) {
			System.err.println(USAGE);
			System.exit(-1);
		}
		
		File magic = new File(args[0]);
		String pkgPattern = args[1];
		File dir = new File(args[2]);	
		String group = args[3];
		String artifactId = args[4];
		String version = args[5];
				
		UMLModelSimpleParser p = new UMLModelSimpleParser(UMLmodel.XMI_MAGICDRAW);
		p.parseUMLModelFile(magic);		
		UMLmodel m = p.getUmlModels().get(0);

		dir.mkdirs();

		// ~~~~~~~~~~~~~~~~~~~~
		// Java component build
		// ~~~~~~~~~~~~~~~~~~~~
		
		JavaUmlInterface java = new JavaUmlInterface();
		
		m.addPrimaryKeys(pkgPattern);
		java.setUmlModel(m);
		java.setBuildQuestions(false);

		String stem = artifactId+"-"+version;
		File jar = new File(stem+".jar");
		java.buildJpaMavenProject(jar, null, 
				group, artifactId, version, 
				pkgPattern,
				false);
		
		File srcDir = new File(dir+"/"+stem);
		srcDir.mkdirs();
		Converters.unzipIt(jar, srcDir);
		
		System.out.println("Model libraries built:" + dir);
		
	}

}
