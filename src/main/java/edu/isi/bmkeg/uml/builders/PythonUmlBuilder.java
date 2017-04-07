package edu.isi.bmkeg.uml.builders;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.model.UMLpackage;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.MapCreate;
import edu.isi.bmkeg.utils.mvnRunner.LocalMavenInstall;
import javassist.ClassPool;
import javassist.CtClass;
import javassist.CtField;
import javassist.CtMethod;
import javassist.CtNewMethod;
import javassist.bytecode.ClassFile;
import javassist.bytecode.ConstPool;

public class PythonUmlBuilder extends UmlComponentBuilder implements ImplConvert {

	Logger log = Logger.getLogger("edu.isi.bmkeg.uml.interfaces.JavaUMLInteface");

	private boolean annotFlag = true;
	private ClassPool pool = ClassPool.getDefault();

	private Map<String, String> queryObjectLookupTable;
	private Map<String, String> pythonLookupTable;

	protected static String[] pythonTargetTypes = new String[] { "long", "byte", "short", "int", "long", "float",
			"double", "boolean", "char", "String", "String", "String", "byte[]", "Object", "java.util.Date",
			"java.util.Date", "URL" };

	protected static String[] javaQuestionTargetTypes = new String[] { "String", "String", "String", "String", "String",
			"String", "String", "String", "String", "String", "String", "String", "String", "String", "String",
			"String", "String" };

	public PythonUmlBuilder() throws Exception {

		this.buildLookupTable();

	}

	public void buildLookupTable() throws Exception {

		pythonLookupTable = new HashMap<String, String>(
				MapCreate.asMap(UmlComponentBuilder.baseAttrTypes, pythonTargetTypes));

		queryObjectLookupTable = new HashMap<String, String>(
				MapCreate.asMap(UmlComponentBuilder.baseAttrTypes, javaQuestionTargetTypes));

		this.setLookupTable(pythonLookupTable);

	}

	public Map<String, File> generatePythonCodeForModel(File dumpDir, String pkgPattern, boolean annotFlag)
			throws Exception {

		this.annotFlag = annotFlag;

		this.getUmlModel().cleanModel();

		Map<String, File> filesInZip = new HashMap<String, File>();

		this.convertAttributes();

		String dAddr = dumpDir.getAbsolutePath();

		List<String> keys = new ArrayList<String>(this.getUmlModel().listPackages(pkgPattern).keySet());

		Collections.sort(keys);

		//
		// build Python classes
		//
		List<String> pythonFilePaths = new ArrayList<String>();
		Map<UMLpackage, String> codeMap = new HashMap<UMLpackage, String>();
		Map<String, UMLclass> classMap = this.getUmlModel().listClasses(pkgPattern);
		Iterator<String> cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);

			addr = addr.substring(2, addr.length());

			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if (c.getStereotype() != null && c.getStereotype().equals("Link")) {
				continue;
			}

			String fAddr = addr.replaceAll("\\.", "/");

			String code = this.generatePythonCodeForClass(c, pkgPattern);

			UMLpackage p = c.getPkg();
			if (codeMap.containsKey(p)) {
				code = codeMap.get(p) + "\n\n" + code;
				codeMap.put(p, code);
			} else {
				codeMap.put(p, code);
			}

		}

		for (UMLpackage p : codeMap.keySet()) {

			String addr = p.getPkgAddress();
			addr = addr.substring(2, addr.length());
			String fAddr = addr.replaceAll("\\.", "/");

			File f = new File(dAddr + "/" + fAddr + ".py");

			FileUtils.writeStringToFile(f, codeMap.get(p));

			filesInZip.put(fAddr + ".py", f);

			pythonFilePaths.add(dumpDir.getAbsolutePath() + "/" + fAddr + ".py");

		}

		return filesInZip;

	}	

	/**
	 * Generates an java entity model source code file
	 * 
	 * @param c
	 *            - the class being generated from.
	 * @return
	 * @throws Exception
	 */
	protected String generatePythonCodeForClass(UMLclass c, String pkgPattern) throws Exception {

		String code = "";

		String addr = c.getPkg().getPkgAddress();

		try {
			addr = addr.substring(2, addr.length());
		} catch (StringIndexOutOfBoundsException e) {
			// top level package, ignore the error
		}

		if (c.getDocumentation() != null && c.getDocumentation().length() > 0) {
			code += this.commentOut(c.getDocumentation(), 0);
		}

		code += "class " + c.getImplName();

		if (c.getParent() != null) {
			code += "(" + c.getParent().getImplName() + ")";
		}

		code += ":\n";

		for (UMLattribute a : c.getAttributes()) {
			if (a.getFkRole() != null) {
				continue;
			}

			if (!a.getToImplement())
				continue;

			if (a.getDocumentation() != null && a.getDocumentation().length() > 0)
				code += this.commentOut(a.getDocumentation(), 1);

			code += this.generateAttrDeclaration(a);

		}

		Iterator<String> rIt = c.getAssociateRoles().keySet().iterator();
		while (rIt.hasNext()) {
			String key = rIt.next();
			UMLrole r = c.getAssociateRoles().get(key);

			if (!r.getNavigable())
				continue;

			if (!r.getToImplement())
				continue;

			if (r.getDocumentation() != null && r.getDocumentation().length() > 0)
				code += this.commentOut(r.getDocumentation(), 1);

			code += this.generateAttrDeclaration(r);

		}

		code += "\n\tdef __init__(self):\n\t\tpass\n";

		code += "\n\tdef __setitem__(self, key, item):\n\tself.data[key] = item\n";

		code += "\n\tdef __getitem__(self, key, item):\n\tself.data[key] = item\n";

		return code;

	}

	private String generateAttrDeclaration(UMLattribute a) {
		
		String code = "";
		String example = "";
		
		switch (a.getType().getBaseName()) {
			case "serial":  example = "0";
				break;
			case "byte":  example = "NaN";
        		break;
			case "short":  example = "0";
				break;
			case "int":  example = "0";
				break;
			case "long":  example = "0";
				break;
			case "float":  example = "0.0";
				break;
			case "double":  example = "0.0";
				break;
			case "char":  example = "\'\'";
				break;
			case "shortString":  example = "\'\'";
				break;
			case "String":  example = "\'\'";
				break;
			case "longString":  example = "\'\'";
				break;
			case "blob":  example = "NaN";
				break;
			case "image":  example = "NaN";
				break;
			case "date":  example = "1/1/1911";
				break;
			case "timestamp":  example = "\'\'";
				break;
			case "url":  example = "\'\'";
				break; 
		} 
		
		code = "\t" + a.getImplName() + " = " + example + "\n\n";
		
		return code;
	
	}

	private String generateAttrDeclaration(UMLrole r) {
		String code = "";

		if (r.getMult_upper() != -1) {
			code += "\t" + r.getImplName() + " = " + r.getDirectClass().getImplName() + "()\n\n";
		}
		// set backing tables
		else if (r.getImplementz() != null) {
			UMLrole rr = r.getImplementz();
			code += "\t" + r.getImplName() + " = []\n\n";

		}
		// lists
		else {

			code += "\t" + r.getImplName() + " = []\n\n";
		
		}

		return code;

	}

	private String commentOut(String toBeCommented, int indent) {

		String out = "";
		String[] words = toBeCommented.split("\\s+");

		out += this.indent(indent) + "\"\"\"\n";

		String l = "";
		for (int i = 0; i < words.length; i++) {
			l += words[i] + " ";
			if( l.length() > 60 ) {
				out += this.indent(indent) + " " + l + "\n";
				l = "";
			}
		}
		out += this.indent(indent) + " " + l + "\n";

		out += this.indent(indent) + "\"\"\"\n";

		return out;

	}

	private String indent(int indent) {

		String out = "";

		for (int j = 0; j < indent; j++) {
			out += "\t";
		}

		return out;

	}

	// FIXME: Add dependency to vpdmf-jpa.jar project instead of embedding code
	// for edu.isi.bmkeg.vpdmf.model
	// classes in every domain model.
	// This is needed because the vpdmf system needs to access the ViewTable
	// class which has to be obtained
	// from the vpdmf-jpa project.
	public void buildPythonProject(File srcJarFile, File jarFile, String group, String artifactId, String version,
			String bmkegParentVersion) throws Exception {

		UMLmodel m = this.getUmlModel();

		if (group == null || group.length() == 0) {
			group = "bmkeg.isi.edu";
		}

		File targetDir = srcJarFile.getParentFile();

		Map<String, File> filesInZipJar = new HashMap<String, File>();
		String commandsString = "";

		File tempUnzippedDirectory = Files.createTempDir();

		tempUnzippedDirectory.deleteOnExit();
		String dAddr = tempUnzippedDirectory.getAbsolutePath();

		//
		// CREATE A PROJECT STEM IN THIS TEMP LOCATION
		//
		File main_resources = new File(tempUnzippedDirectory.getPath() + "/resources");
		main_resources.mkdir();

		//
		// Write the model file to this temporary location
		//
		String suffix = ".tmp";
		if (m.getSourceType().equals(UMLmodel.XMI_MAGICDRAW))
			suffix = "_mgd.xml";
		else if (m.getSourceType().equals(UMLmodel.XMI_POSEIDON))
			suffix = "_pos.xml";

		File uml = new File(main_resources.getPath() + "/" + m.getName() + suffix);
		FileOutputStream fos = new FileOutputStream(uml);
		if (m.getSourceData() != null)
			fos.write(m.getSourceData());
		fos.close();
		filesInZipJar.put("resources/" + uml.getName(), uml);

		Map<String, File> pythonFiles = this.generatePythonCodeForModel(tempUnzippedDirectory, ".*", false);

		Iterator<String> keyIt = pythonFiles.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			filesInZipJar.put(key, pythonFiles.get(key));
		}

		Converters.zipIt(filesInZipJar, srcJarFile);

		if (jarFile == null)
			return;

		Converters.recursivelyDeleteFiles(tempUnzippedDirectory);

	}

	public Map<String, String> getQueryObjectLookupTable() {
		return queryObjectLookupTable;
	}

	public void setQueryObjectLookupTable(Map<String, String> queryObjectLookupTable) {
		this.queryObjectLookupTable = queryObjectLookupTable;
	}

	public Map<String, String> getJavaLookupTable() {
		return pythonLookupTable;
	}

	public void setJavaLookupTable(Map<String, String> javaLookupTable) {
		this.pythonLookupTable = javaLookupTable;
	}

}
