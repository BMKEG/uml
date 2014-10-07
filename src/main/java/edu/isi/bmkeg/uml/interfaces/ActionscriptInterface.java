package edu.isi.bmkeg.uml.interfaces;

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
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;

import com.google.common.io.Files;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.MapCreate;
import edu.isi.bmkeg.utils.TextUtils;
import edu.isi.bmkeg.utils.mvnRunner.LocalMavenInstall;

public class ActionscriptInterface extends UmlComponentInterface implements ImplConvert {	
	
	Logger log = Logger.getLogger(ActionscriptInterface.class);
	
	private boolean buildQuestions = false;
	
	private Map<String, String> queryObjectLookupTable;
	private Map<String, String> asLookupTable;
	
	private static String[] asTargetTypes = new String[] {
        "Number",	// long
        "int",   	// byte
        "int",  	// short
        "int",    	// int
        "Number",   // long
        "Number",  	// float
        "Number", 	// double
        "Boolean", 	// boolean
        "String",	// char
        "String",	// shortString
        "String",	// String
        "String", 	// longString
        "Object",	// blob
        "Object",	// image
        "Date",		// date
        "String",	// timestamp
        "String"
	};
	
	private static String[] asQuestionTargetTypes = new String[] { "String",
		"String", "String", "String", "String",
		"String", "String", "String",
		"String", "String", "String",
		"String", "String", "String",
		"String", "String", "String" };

	public ActionscriptInterface() throws Exception {

		this.buildLookupTable();

	}

	public void buildLookupTable() throws Exception {
		
		asLookupTable = new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, asTargetTypes));
		
		queryObjectLookupTable = new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, asQuestionTargetTypes));
		
		this.setLookupTable(asLookupTable);

	}
	
	public void buildFlexMojoMavenProject(File srcZipFile, File swfFile,
			String group, String artifactId, String version,
			String bmkegParentVersion) throws Exception {
		
		UMLmodel m = this.getUmlModel();
		
		if( group == null || group.length() == 0 ) {
			group = "bmkeg.isi.edu";
		}
		
		File targetDir = srcZipFile.getParentFile();

		String commandsString = "";

		File tempUnzippedDirectory = Files.createTempDir();
		String dAddr = tempUnzippedDirectory.getAbsolutePath();
		
		String asTemplateZipPath = ClassLoader.getSystemResource("edu/isi/bmkeg/uml/interfaces/as-template.zip").getFile();
		File asTemplateZip = new File(asTemplateZipPath);
		
		Converters.unzipIt(asTemplateZip, tempUnzippedDirectory);
		
		// 1. rename the base directory
		File unZippedSwf = new File(dAddr + "/as-template");
		File swfProject = new File(dAddr + "/" + this.getUmlModel().getName() + "-as");
		unZippedSwf.renameTo(swfProject);		

		// 2. edit the pom
		File pomFile = new File(swfProject.getPath() + "/pom.xml");
		String pomCode = TextUtils.readFileToString(pomFile);
		pomCode = pomCode.replaceAll("\\[GROUPID\\]", group);
		pomCode = pomCode.replaceAll("\\[ARTIFACTID\\]", artifactId);
		pomCode = pomCode.replaceAll("\\[VERSION\\]", version);
		
		pomCode = pomCode.replaceAll("\\[PARENT-VERSION\\]", bmkegParentVersion);

		FileUtils.writeStringToFile(pomFile, pomCode);
		
		// 3. add the extra source code
		File srcDir = new File(swfProject + "/src/main/flex/");
		this.buildQuestions = true;
		this.generateActionscriptForModel(srcDir);
		this.generateActionscriptForEvents(srcDir);
		
		// 4. zip up the archive
		Map<String, File> filesInSrc = new HashMap<String, File>();
		Converters.zipPrep(swfProject.getParent(), swfProject, filesInSrc);
		Converters.zipIt(filesInSrc, srcZipFile);
				
		if(swfFile == null)
			return;

		// 5. build the swf
		String out = LocalMavenInstall.runMavenCommand("clean compile -f " + pomFile.getAbsolutePath());
		log.debug(out);
        File f1 = new File(swfProject.getPath() + "/target/" + artifactId + "-"+ version + ".swc" );

        if( !f1.exists() )
        	throw new Exception("Build for " + f1.getName() + " failed. Check source\n ERROR:" + out);
        
        InputStream from = new FileInputStream(f1);
        OutputStream to = new FileOutputStream(swfFile);

        byte[] buff = new byte[1024];
        int len;
        while ((len = from.read(buff)) > 0) {
        	to.write(buff, 0, len);
        }
        from.close();
        to.close();

		Converters.recursivelyDeleteFiles(tempUnzippedDirectory);

	}	
	
	/**
	 * Generates and dumps a zip file of Actionscript classes from a UML model.
	 * 
	 * @param location
	 * @param onlyModelClasses
	 * @return
	 * @throws Exception
	 */
	public void generateActionscriptForModel(File dumpDir) throws Exception {
		
		this.convertAttributes();
		String dAddr = dumpDir.getPath();

		List<String> keys = new ArrayList<String>(this.getUmlModel().listPackages("\\.model\\.").keySet());
		
		Collections.sort(keys);

		// build directories
		for(int i=0; i<keys.size(); i++) {
			String addr = keys.get(i);
			String[] pkgArray =  addr.split("\\.");
			
			File currPkg = dumpDir;
			for(int j=1; j<pkgArray.length; j++) {
				String dirName = pkgArray[j];
				File pkgFile = new File(currPkg.getAbsolutePath() + "/" + dirName);
				
				if(!pkgFile.exists()) {
					pkgFile.mkdir();
					pkgFile.deleteOnExit();
				}

				currPkg = pkgFile;
				
			}
						
		}

		// build ActionScript classes
		// Default case : process all classes
		Map<String, UMLclass> classMap = this.getUmlModel().listClasses("\\.model\\.");
		Iterator<String> cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String key = cIt.next();
			UMLclass c = classMap.get(key);
			
			String addr = c.getClassAddress();
			
			// Check to see if the class is a set backing table... 
			// if so don't generate the source code.
			if( c.getStereotype() != null && c.getStereotype().equals("Link") ){
				continue;
			}
			
			String code = this.generateActionscriptCodeForClass(c);
			
			String fAddr = key.replaceAll("\\.", "/"); 
			fAddr = fAddr.substring(2,fAddr.length());
			File f = new File(dAddr + "/" + fAddr + ".as"); 
			f.deleteOnExit();
			
			FileUtils.writeStringToFile(f, code);
						
		}
		
		if( this.buildQuestions ) {
			
			this.getUmlModel().convertToQuestionObjects();
			
			this.setLookupTable(queryObjectLookupTable);
			this.convertAttributes();
					
			String pkgPattern = ".model.qo.";
			classMap = this.getUmlModel().listClasses(pkgPattern);
			cIt = classMap.keySet().iterator();
			while(cIt.hasNext()) {
				String addr = cIt.next();
				UMLclass c = classMap.get(addr);
			
				addr = addr.substring(2,addr.length());
				
				// Check to see if the class is a set backing table... 
				// if so don't generate the source code.
				if( c.getStereotype() != null && c.getStereotype().equals("Link") ){
					continue;
				}
				
				String fAddr = addr.replaceAll("\\.", "/"); 
				
				String code = this.generateActionscriptCodeForClass(c);
				
				File f = new File(dAddr + "/" + fAddr + ".as"); 
				
				FileUtils.writeStringToFile(f, code);
			}

			this.getUmlModel().convertFromQuestionObjects();

			this.setLookupTable(asLookupTable);
			this.convertAttributes();

		}

	}

	public void generateActionscriptForEvents(File dumpDir) throws Exception {
		
		this.convertAttributes();
		String dAddr = dumpDir.getPath();

		List<String> keys = new ArrayList<String>(this.getUmlModel().listPackages("\\.events\\.").keySet());
		
		Collections.sort(keys);

		// build directories
		for(int i=0; i<keys.size(); i++) {
			String addr = keys.get(i);
			String[] pkgArray =  addr.split("\\.");
			
			File currPkg = dumpDir;
			for(int j=1; j<pkgArray.length; j++) {
				String dirName = pkgArray[j];
				File pkgFile = new File(currPkg.getAbsolutePath() + "/" + dirName);
				
				if(!pkgFile.exists()) {
					pkgFile.mkdir();
					pkgFile.deleteOnExit();
				}

				currPkg = pkgFile;
				
			}
						
		}

		// build ActionScript classes
		// Default case : process all classes
		Map<String, UMLclass> classMap = this.getUmlModel().listClasses("\\.events\\.");
		Iterator<String> cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String key = cIt.next();
			UMLclass c = classMap.get(key);
			
			String addr = c.getClassAddress();
			
			// Check to see if the class is a set backing table... 
			// if so don't generate the source code.
			if( c.getStereotype() != null && c.getStereotype().equals("Link") ){
				continue;
			}
			
			String code = this.generateActionscriptCodeForEventClass(c);
			
			String fAddr = key.replaceAll("\\.", "/"); 
			fAddr = fAddr.substring(2,fAddr.length());
			File f = new File(dAddr + "/" + fAddr + "Event.as"); 
			f.deleteOnExit();
			
			FileUtils.writeStringToFile(f, code);
						
		}

	}
	
	/**
	 * Generates an AS model file that's equivalent to a java model file
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	private String generateActionscriptCodeForClass(UMLclass c) throws Exception {

		String code = "";
		
		String addr = c.getPkg().getPkgAddress();
		addr = addr.substring(2,addr.length());
		code += "package " + addr +"\n{\n";
		
		Set<String> repeatCheck = new HashSet<String>();
		
		Iterator<UMLattribute> aIt = c.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();
			UMLclass impC = a.getType();
	
			if(impC.isDataType()) 
				continue;

			if( !impC.getToImplement() )
				continue;
			
			addr = impC.getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
			
		}
		
		Iterator<String> rIt = c.getAssociateRoles().keySet().iterator();
		while(rIt.hasNext()) {
			String key = rIt.next();
			UMLrole r = c.getAssociateRoles().get(key);		

			if( !r.getToImplement() )
				continue;

			if( !r.getNavigable() )
				continue;
			
			if( !r.getDirectClass().getToImplement() )
				continue;
			
			addr = r.getDirectClass().getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( r.getMult_upper() != -1 && !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
		
		}
		
		if( c.getParent() != null ) {

			addr = c.getParent().getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
			
		}

		
		code += "	\n	import mx.collections.ArrayCollection;\n\n";
		code += "	[Bindable]\n";

		addr = c.getClassAddress();
		addr = addr.substring(2,addr.length());

		code += "	[RemoteClass(alias=\"" + addr + "\")]\n";
		
		code += "	public class " + c.getImplName();
		
		if( c.getParent() != null ) {
			code += " extends " + c.getParent().getImplName();		
		}

		code += "\n	{\n";

		code += "		public function " + c.getImplName() + "(){}\n\n";

		aIt = c.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();

			if(!a.getToImplement())
				continue;
			
			if( !a.getType().getToImplement() )
				continue;
			
			if( a.getFkRole() != null)
				continue;
			
			code += "		[Bindable]\n";		 									
			if( a.getStereotype() != null && 
					a.getStereotype().equals("PK") &&
					a.getParentClass().getParent() != null) {
				code += "		public override ";		 									
			} else {
				code += "		public ";		 									
			}	
			code += "var " + a.getImplName() + ":" + a.getType().getImplName() + ";\n"; 					
		
		}

		rIt = c.getAssociateRoles().keySet().iterator();
		while(rIt.hasNext()) {
			String key = rIt.next();
			UMLrole r = c.getAssociateRoles().get(key);
			
			if(!r.getToImplement())
				continue;

			if( !r.getDirectClass().getToImplement() )
				continue;

			if( !r.getNavigable() )
				continue;
			
			code += "		[Bindable]\n";		 									
			if( r.getMult_upper() != -1) {
				code += "		public var " + key + ":" + r.getDirectClass().getImplName() + ";\n";
			} else {
				code += "		public var " + key + ":ArrayCollection = new ArrayCollection();\n";
			}
		}
		
		code += "	}\n}";

		return code;
		
	}

	/**
	 * Generates an AS model file that's equivalent to a java model file
	 * 
	 * @param c
	 * @return
	 * @throws Exception
	 */
	private String generateActionscriptCodeForEventClass(UMLclass c) throws Exception {

		String code = "";
		
		String addr = c.getPkg().getPkgAddress();
		addr = addr.substring(2,addr.length());
		code += "package " + addr +"\n{\n";
		
		Set<String> repeatCheck = new HashSet<String>();
		
		Iterator<UMLattribute> aIt = c.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();
			UMLclass impC = a.getType();
	
			if(impC.isDataType()) 
				continue;
			
			addr = impC.getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
			
		}
		
		Iterator<String> rIt = c.getAssociateRoles().keySet().iterator();
		while(rIt.hasNext()) {
			String key = rIt.next();
			UMLrole r = c.getAssociateRoles().get(key);		

			addr = r.getDirectClass().getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( r.getMult_upper() != -1 && !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
		
		}
		
		if( c.getParent() != null ) {

			addr = c.getParent().getClassAddress();
			addr = addr.substring(2,addr.length());
			String stmt = "import " + addr + ";";
			if( !repeatCheck.contains(stmt) ) {
				code += "	" + stmt +"\n";
				repeatCheck.add(stmt);
			}
			
		}
		
		code += "	\n	import flash.events.Event;\n";
		code += "	\n	import mx.collections.ArrayCollection;\n\n";
				
		code += "	public class " + c.getImplName() + "Event extends Event\n	{\n";

		String eventName = "";
		String cn = c.getImplName(); 
		for(int i=0; i<cn.length()-1; i++) {
			String l = cn.substring(i,i+1);  
			String n = cn.substring(i+1,i+2);  
			eventName += l.toUpperCase();
			if( l.equals(l.toLowerCase()) && n.equals(n.toUpperCase()) ){
				eventName += "_";
			}
		}
		eventName += cn.substring(cn.length()-1,cn.length()).toUpperCase();
				
		code += "		public static const " + eventName + ":String = \"" + 
					cn.substring(0, 1).toLowerCase() + cn.substring(1, cn.length()) 
					+ "\";\n\n";
		
		List<String> attrStrings = new ArrayList<String>();
		
		aIt = c.getAttributes().iterator();
		while(aIt.hasNext()) {
			UMLattribute a = aIt.next();

			if(!a.getToImplement())
				continue;
			
			if( a.getFkRole() != null)
				continue;
			
			if( a.getStereotype() != null && 
					a.getStereotype().equals("PK") &&
					a.getParentClass().getParent() != null) {
				code += "		public override ";		 									
			} else {
				code += "		public ";		 									
			}	

			String s = a.getImplName() + ":" + a.getType().getImplName();
			attrStrings.add(s);
			code += "var " + s + ";\n"; 					
		
		}

		rIt = c.getAssociateRoles().keySet().iterator();
		while(rIt.hasNext()) {
			String key = rIt.next();
			UMLrole r = c.getAssociateRoles().get(key);
			
			if(!r.getToImplement())
				continue;

			String s = "";			
			if( r.getMult_upper() != -1) {
				s = key + ":" + r.getDirectClass().getImplName();
			} else {
				s = key + ":ArrayCollection";
			}
			attrStrings.add(s);
			code += "		public var " + s + ";\n";

		}
		
		code += "\n		// Constructor\n";
		code += "		public function " + cn + "Event(";
		Iterator<String> it = attrStrings.iterator();
		while(it.hasNext()){
			code += it.next();
			if( it.hasNext() )
				code += ", ";
		}
		code += ")\n		{\n";
		code += "			super(" + eventName + ");\n";

		it = attrStrings.iterator();
		while(it.hasNext()){
			String s = it.next();
			String ss = s.substring(0,s.indexOf(":"));
			code += "			this." + ss + " = " + ss + ";\n";
		}
		code += "		}\n";
			
		code += "\n		// Override the inherited clone() method, but don't return any state.\n";
		code += "		override public function clone() : Event\n";
		code += "		{\n";
		code += "			return new " + cn + "Event(";
		it = attrStrings.iterator();
		while(it.hasNext()){
			String s = it.next();
			String ss = s.substring(0,s.indexOf(":"));
			code += ss;
			if( it.hasNext() )
				code += ", ";
		}
		
		code += ");\n";
		
		code += "		}\n\n";
		
		code += "	}\n\n}";

		return code;
		
	}

	
	
}
