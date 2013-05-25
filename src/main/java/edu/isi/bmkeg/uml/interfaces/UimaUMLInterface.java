package edu.isi.bmkeg.uml.interfaces;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.Writer;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;

import javassist.ClassPool;

import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.apache.uima.UIMAFramework;
import org.apache.uima.cas.CAS;
import org.apache.uima.cas.Feature;
import org.apache.uima.cas.Type;
import org.apache.uima.cas.TypeSystem;
import org.apache.uima.cas.impl.CASImpl;
import org.apache.uima.resource.ResourceInitializationException;
import org.apache.uima.resource.metadata.FeatureDescription;
import org.apache.uima.resource.metadata.FsIndexDescription;
import org.apache.uima.resource.metadata.TypeDescription;
import org.apache.uima.resource.metadata.TypePriorities;
import org.apache.uima.resource.metadata.TypeSystemDescription;
import org.apache.uima.tools.jcasgen.GUI;
import org.apache.uima.tools.jcasgen.IError;
import org.apache.uima.tools.jcasgen.IMerge;
import org.apache.uima.tools.jcasgen.IProgressMonitor;
import org.apache.uima.tools.jcasgen.JCasTypeTemplate;
import org.apache.uima.tools.jcasgen.JCas_TypeTemplate;
import org.apache.uima.tools.jcasgen.Jg;
import org.apache.uima.tools.jcasgen.LogThrowErrorImpl;
import org.apache.uima.tools.jcasgen.TypeInfo;
import org.apache.uima.tools.jcasgen.Waiter;
import org.apache.uima.util.CasCreationUtils;
import org.apache.uima.util.XMLInputSource;
import org.apache.uima.util.XMLizable;
import org.jdom.Attribute;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;

import com.google.common.io.Files;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.MapCreate;
import edu.isi.bmkeg.utils.mvnRunner.LocalMavenInstall;

public class UimaUMLInterface extends JavaUmlInterface implements
		ImplConvert {

	Logger log = Logger.getLogger("edu.isi.bmkeg.uml.interfaces.UimaUMLInterface");

	private boolean annotFlag = true;
	private ClassPool pool = ClassPool.getDefault();
	private String uimaPattern;

	private Map<String, String> uimaLookupTable;
	
	private static String[] uimaTargetTypes = new String[] { "uima.cas.Long",
			"uima.cas.Long", "uima.cas.Long", "uima.cas.Long", "uima.cas.Long",
			"uima.cas.String", "uima.cas.String", "uima.cas.String",
			"uima.cas.String", "uima.cas.String", "uima.cas.String",
			"uima.cas.String", "uima.cas.TOP", "uima.cas.TOP",
			"uima.cas.String", "uima.cas.String", "uima.cas.String" };

	public UimaUMLInterface(String uimaPattern) throws Exception {
		super();
		
		this.uimaPattern = uimaPattern;
		
		uimaLookupTable = new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, uimaTargetTypes));
	
	}

	public void buildLookupTable() throws Exception {

		super.buildLookupTable();
		
		this.uimaLookupTable = new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, uimaTargetTypes));
		
		this.setLookupTable(this.uimaLookupTable);

	}
	
	@Override
	public void buildJpaMavenProject(File srcJarFile, File jarFile, 
			String group, String artifactId, String version,
			String repoId, String repoUrl ) throws Exception {
		
		UMLmodel m = this.getUmlModel();
		
		if( group == null || group.length() == 0 ) {
			group = "bmkeg.isi.edu";
		}
		
		File targetDir = srcJarFile.getParentFile();

		Map<String, File> filesInSrcJar = new HashMap<String, File>();
		String commandsString = "";

		File tempUnzippedDirectory = Files.createTempDir();
		
		tempUnzippedDirectory.deleteOnExit();
		String dAddr = tempUnzippedDirectory.getAbsolutePath();

		//
		// CREATE A MAVEN PROJECT STEM IN THIS TEMP LOCATION
		// 
		File src = new File(tempUnzippedDirectory.getPath() + "/src");
		src.mkdir();

		File main = new File(tempUnzippedDirectory.getPath() + "/src/main");
		main.mkdir();

		File main_java = new File(tempUnzippedDirectory.getPath() + "/src/main/java");
		main_java.mkdir();

		File main_resources = new File(tempUnzippedDirectory.getPath() + "/src/main/resources");
		main_resources.mkdir();

		File main_resources_vpdmf = new File(tempUnzippedDirectory.getPath() + "/src/main/resources/vpdmf");
		main_resources_vpdmf.mkdir();

		File main_resources_uima = new File(tempUnzippedDirectory.getPath() + "/src/main/resources/type");
		main_resources_uima.mkdir();
		
		File test = new File(tempUnzippedDirectory.getPath() + "/src/test");
		test.mkdir();
		
		File test_java = new File(tempUnzippedDirectory.getPath() + "/src/test/java");
		test_java.mkdir();

		File test_resources = new File(tempUnzippedDirectory.getPath() + "/src/test/resources");
		test_resources.mkdir();
		
		File target = new File(tempUnzippedDirectory.getPath() + "/target");
		target.mkdir();
		
		//
		// Build Basic pom.xml file
		// 
		String pom = "<project xmlns=\"http://maven.apache.org/POM/4.0.0\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"\n";
		pom += "	xsi:schemaLocation=\"http://maven.apache.org/POM/4.0.0 http://maven.apache.org/xsd/maven-4.0.0.xsd\">\n";
		pom += "	<modelVersion>4.0.0</modelVersion>\n";
		
		pom += "	<groupId>" + group + "</groupId>\n";
		pom += "	<artifactId>" + artifactId + "</artifactId>\n";
		pom += "	<version>" + version + "</version>\n";
		pom += "	<packaging>jar</packaging>\n";

		pom += "	<build>\n";
		pom += "		<plugins>\n";
		pom += "			<plugin>\n";
		pom += "				<groupId>org.apache.maven.plugins</groupId>\n";
		pom += "		    	<artifactId>maven-surefire-plugin</artifactId>\n";
		pom += "			    <version>2.12</version>\n";
		pom += "			</plugin>\n";
		pom += "			<plugin>\n";
		pom += "				<groupId>org.apache.maven.plugins</groupId>\n";
		pom += "				<artifactId>maven-source-plugin</artifactId>\n";
		pom += "				<version>2.1.2</version>\n";
		pom += "				<executions>\n";
		pom += "					<execution>\n";
		pom += "						<goals>\n";
		pom += "							<goal>jar</goal>\n";
		pom += "						</goals>\n";
		pom += "					</execution>\n";
		pom += "				</executions>\n";
		pom += "			</plugin>\n";
		pom += "		</plugins>\n";
		pom += "	</build>\n";
		
		pom += "	<distributionManagement>\n";
		if( repoId.endsWith("-SNAPSHOT") ) {
			pom += "		<snapshotRepository>\n";
			pom += "			<id>" + repoId + "</id>\n";
			pom += "			<name>SNAPSHOT repository</name>\n";
			pom += "			<url>" + repoUrl + "</url>\n";
			pom += "		</snapshotRepository>\n";
		} else {
			pom += "		<repository>\n";
			pom += "			<id>" + repoId + "</id>\n";
			pom += "			<name>Repository</name>\n";
			pom += "			<url>" + repoUrl + "</url>\n";
			pom += "		</repository>\n";
		}
		pom += "	</distributionManagement>\n";
		
		pom += "	<repositories>\n";
		pom += "		<repository>\n";
		pom += "			<id>com.springsource.repository.bundles.release</id>\n";
		pom += "			<name>SpringSource Enterprise Bundle Repository - SpringSource Releases</name>\n";
		pom += "			<url>http://repository.springsource.com/maven/bundles/release</url>\n";
		pom += "		</repository>\n";
		pom += "		<repository>\n";
		pom += "			<id>com.springsource.repository.bundles.external</id>\n";
		pom += "			<name>SpringSource Enterprise Bundle Repository - External Releases</name>\n";
		pom += "			<url>http://repository.springsource.com/maven/bundles/external</url>\n";
		pom += "		</repository>\n";
		pom += "	</repositories>\n";
		
		pom += "	<dependencies>\n";
		pom += "		<dependency>\n";
		pom += "			<groupId>org.hibernate.javax.persistence</groupId>\n";
		pom += "			<artifactId>hibernate-jpa-2.0-api</artifactId>\n";
		pom += "			<version>1.0.1.Final</version>\n";
		pom += "			<scope>provided</scope>\n";		
		pom += "		</dependency>\n";
		pom += "		<dependency>\n";
		pom += "			<groupId>org.uimafit</groupId>\n";
		pom += "			<artifactId>uimafit</artifactId>\n";
		pom += "			<version>1.4.0</version>\n";
		pom += "			<scope>provided</scope>\n";		
		pom += "		</dependency>\n";
		pom += "	</dependencies>\n";
		pom += "</project>\n";

		File pomFile = new File(tempUnzippedDirectory.getPath() + "/pom.xml");
		Writer output = new BufferedWriter(new FileWriter(pomFile));
		try {
			output.write(pom);
		} finally {
			output.close();
		}
		filesInSrcJar.put("pom.xml", pomFile);

		//
		// Write the model file to this temporary location
		//
		String suffix = ".tmp";
		if (m.getSourceType().equals(UMLmodel.XMI_MAGICDRAW))
			suffix = "_mgd.xml";
		else if (m.getSourceType().equals(UMLmodel.XMI_POSEIDON))
			suffix = "_pos.xml";

		File uml = new File(main_resources_vpdmf.getPath() + "/" + m.getName() + suffix);
		FileOutputStream fos = new FileOutputStream(uml);
		fos.write(m.getSourceData());
		fos.close();
		filesInSrcJar.put("src/main/resources/model/" + uml.getName(), uml);
		
		// Build JPA classes for the domain to act as intermediaries for the model 
		// if you need to instantiate java object graphs for the views. Or if 
		// you'd prefer to write your own functions against the underlying database. 

		this.setLookupTable( this.getJavaLookupTable() );
		Map<String,File> javaFiles = this.generateJavaCodeForModel(main_java,  "\\.model\\.", true);

		this.setLookupTable( this.uimaLookupTable );
		javaFiles.putAll( this.generateJCasTypeDescriptionForModel(main_java, main_resources_uima, 
				this.uimaPattern, true) );
		
		Iterator<String> keyIt = javaFiles.keySet().iterator();
		while(keyIt.hasNext()) {
			String key = keyIt.next();
			
			if( key.startsWith("uimaType/") ) {
				File f = javaFiles.get(key);
				filesInSrcJar.put("src/main/resources/uimaTypes/" + f.getName(), f);
			} else {
				filesInSrcJar.put("src/main/java/" + key, javaFiles.get(key));
			}
		}

		Converters.jarIt(filesInSrcJar, srcJarFile);
		
		if(jarFile == null)
			return;
		
		//
		// Use maven to compile the code 
		//
		//MavenCli cli = new MavenCli();
		//int result = cli.doMain(new String[]{"compile"},
		//		tempUnzippedDirectory.getPath(),
		//		System.out, System.out);

		String out = LocalMavenInstall.runMavenCommand("package -f " + pomFile.getAbsolutePath());
		log.debug(out);
		        
        File f1 = new File(dAddr + "/target/" + artifactId + "-"+ version + ".jar" );
        
        if( !f1.exists() )
        	throw new Exception("Build for " + f1.getName() + " failed. Check source");
        
        InputStream from = new FileInputStream(f1);
        OutputStream to = new FileOutputStream(jarFile);

        byte[] buff = new byte[1024];
        int len;
        while ((len = from.read(buff)) > 0) {
        	to.write(buff, 0, len);
        }
        from.close();
        to.close();

		Converters.recursivelyDeleteFiles(tempUnzippedDirectory);

	}
	
	public Map<String, File> generateJCasTypeDescriptionForModel(File javaDir, 
			File resourceDir, String pkgPattern, boolean annotFlag)
			throws Exception {

		this.annotFlag = annotFlag;
		
		this.getUmlModel().cleanModel();
		this.convertAttributes();

		Map<String,File> filesInZip = new HashMap<String,File>();
		
		String dAddr = javaDir.getAbsolutePath();

		//
		// build All Uima types
		//
		// generate XML for UIMA type
		XMLOutputter outputter = new XMLOutputter();

		String inputs = resourceDir.getAbsolutePath() + "/"
				+ this.getUmlModel().getName() + ".xml";
		File typeFile = new File(inputs);
		
		String outputs = javaDir.getAbsolutePath();
		FileWriter writer = new FileWriter(inputs);

		Document xmlTypeDoc = this.generateUimaTypeJDOM(pkgPattern);

		outputter.output(xmlTypeDoc, writer);
		writer.close();
		
		// hack. Put this in the zip and move it to resources later. 
		filesInZip.put("uimaType/" + typeFile.getName(), typeFile);

		// run JCasGen
		// This is here to make sure the system runs. 
		// Jg jg = new Jg();
		// String[] inputsOutputs = new String[] { inputs, outputs };
		// jg.main0(inputsOutputs, null, null, new LogThrowErrorImpl());

		// run JCasGen from hack
		log.debug("Running JCasGen for model");
		JgHack jgh = new JgHack();
		jgh.runJCasGenWithoutUI(inputs, outputs, null);

		List<String> javaFilePaths = new ArrayList<String>();

		// Bug - our hacked version of JCasGen can only create class files
		// with the names set to the whole class address. Run over them here 
		// and rename them appropriately.
		// - at the same time, check them and add to the zip file.
		Map<String, UMLclass> classes = this.getUmlModel().listClasses(pkgPattern);
		Iterator<String> keyIt = classes.keySet().iterator();
		while(keyIt.hasNext()) {
			String key = keyIt.next();;
			UMLclass c = classes.get(key);
			
			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if (c.getStereotype() != null && c.getStereotype().equals("Link")) {
				continue;
			}
			
			String base = key.substring(2).replaceAll("\\.model\\.", ".uimaTypes.");

			String fAddr = base.replaceAll("\\.", File.separator).
					replaceAll(c.getImplName() + "$" , base );
			String tAddr = base.replaceAll("\\.", File.separator);
			
			File f1 = new File( dAddr + File.separator + fAddr + ".java");
			File t1 = new File( dAddr + File.separator + tAddr + ".java");
			f1.renameTo(t1);
			
			File f2 = new File( dAddr + File.separator + fAddr + "_Type.java" );
			File t2 = new File( dAddr + File.separator + tAddr + "_Type.java");
			f2.renameTo(t2);

			filesInZip.put(tAddr + ".java", t1);
			filesInZip.put(tAddr + "_Type.java", t2);
				
			javaFilePaths.add(t1.getAbsolutePath());
			javaFilePaths.add(t2.getAbsolutePath());
			
		}

		return filesInZip;
		
	}

	/**
	 * Generates UIMA XML for the class.
	 * 
	 * We adopt a general modeling convention: the UML models go in 
	 * a package called 'model', these uima elements will go into 
	 * an equivalent package called 'uimaTypes' 
	 * 
	 * @param c
	 * @param pkgPattern
	 * @return
	 * 
	 *         EXAMPLE FROM UIMA DOCS
	 * 
	 *         <?xml version="1.0" encoding="UTF-8" ?> <typeSystemDescription
	 *         xmlns="http://uima.apache.org/resourceSpecifier">
	 *         <name>TutorialTypeSystem</name> <description>Type System
	 *         Definition for the tutorial examples - as of Exercise
	 *         1</description> <vendor>Apache Software Foundation</vendor>
	 *         <version>1.0</version> <types> <typeDescription>
	 *         <name>org.apache.uima.tutorial.RoomNumber</name>
	 *         <description></description>
	 *         <supertypeName>uima.tcas.Annotation</supertypeName> <features>
	 *         <featureDescription> <name>building</name> <description>Building
	 *         containing this room</description>
	 *         <rangeTypeName>uima.cas.String</rangeTypeName>
	 *         </featureDescription> </features> </typeDescription> </types>
	 *         </typeSystemDescription>
	 */
	private Document generateUimaTypeJDOM(String pkgPattern) {

		// Create the root element
		Element typeSystemElement = new Element("typeSystemDescription");

		// create the document
		Document typeSystemDocument = new Document(typeSystemElement);

		// add an attribute to the root element
		typeSystemElement.setAttribute(new Attribute("name", this.getUmlModel()
				.getName()));
		if (this.getUmlModel().getDescription() != null)
			typeSystemElement.setAttribute(new Attribute("description", this
					.getUmlModel().getDescription()));

		typeSystemElement
				.setAttribute(new Attribute("vendor", "bmkeg.isi.edu"));
		typeSystemElement.setAttribute(new Attribute("version", "0.0.0"));

		Element types = new Element("types");

		List<Element> typesList = new ArrayList<Element>();

		List<String> javaFilePaths = new ArrayList<String>();
		Map<String, UMLclass> classMap = this.getUmlModel().listClasses(
				pkgPattern);

		// Kludge to include ViewTable & other VPDMf classes.
		Map<String, UMLclass> vpdmfClassMap = this.getUmlModel().listClasses("vpdmf.model.");		
		classMap.putAll(vpdmfClassMap);
		
		Iterator<String> cIt = classMap.keySet().iterator();
		while (cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);
			
			String cAddr = c.readClassAddress();
			String tAddr = cAddr.replaceAll("\\.model\\.", ".uimaTypes.");

			addr = addr.substring(2, addr.length());

			// Check to see if the class is a set backing table...
			// if so don't generate the source code.
			if (c.getStereotype() != null && c.getStereotype().equals("Link")) {
				continue;
			}

			Element typeDescription = new Element("typeDescription");
			typesList.add(typeDescription);

			Element name = new Element("name");
			name.addContent(tAddr);
			typeDescription.addContent(name);

			String parent = "uima.tcas.Annotation";
			if (c.getParent() != null) {
				parent = c.getParent().readClassAddress()
						.replaceAll("\\.model\\.", ".uimaTypes.");;
			}
			Element supertypeName = new Element("supertypeName");
			supertypeName.addContent(parent);
			typeDescription.addContent(supertypeName);

			Element features = new Element("features");
			typeDescription.addContent(features);

			List<Element> featuresList = new ArrayList<Element>();

			Iterator<UMLattribute> aIt = c.getAttributes().iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();

				if( !a.getToImplement() || !a.getType().isDataType()) {
					continue;
				}
				
				Element featureDescription = new Element("featureDescription");
				featuresList.add(featureDescription);

				Element fName = new Element("name");
				featureDescription.addContent(fName);
				fName.setText(a.getImplName());

				Element fDesc = new Element("description");
				featureDescription.addContent(fDesc);
				if (a.getDocumentation() != null)
					fDesc.setText(a.getDocumentation());

				Element fType = new Element("rangeTypeName");
				featureDescription.addContent(fType);
				fType.setText(a.getType().getImplName());

			}

			features.addContent(featuresList);

		}

		types.addContent(typesList);

		typeSystemElement.addContent(types);

		return typeSystemDocument;

	}

	/**
	 * Hacked subclass to run the basic jcasgen function without invoking the UI
	 * tool.
	 * 
	 * Code is copied directly from org.apache.uima.tools.jcasgen.Jg, based
	 * around directly triggering main1 method filled out and trimmed to only
	 * accommodate type system generation without any reference to the UI system
	 * (progress monitor, error reporting, etc).
	 * 
	 * MOST CODE IS CONSTRUCTED VERBATIM WITHOUT UNDERSTANDING ITS UNDERLYING
	 * LOGIC
	 * 
	 * @param inputFile
	 *            - note input file cannot be a jar file.
	 * @param outputDir
	 */
	private class JgHack extends Jg {

		private Map builtInTypes = new HashMap();
		private TypeSystem builtInTypeSystem;
		private Map extendableBuiltInTypes = new HashMap();
		private FeatureDescription[] emptyFds = new FeatureDescription[0];

		// a Map of types and the xml files that were merged to create them
		private Map mergedTypesAddingFeatures = new TreeMap();

		private TypeSystem typeSystem = null;

		private Type casStringType;

		private Type tcasAnnotationType;

		private Map imports = new HashMap();

		private CAS cas;

		private Waiter waiter;

		private String packageName;

		private String simpleClassName;

		private Set noGenTypes = new HashSet();

		/**
		 * Constructor, more hackery, instead of the static constructors within
		 * Jg.
		 */
		private JgHack() {

			addBuiltInTypeInfo("uima.cas.TOP",
					"org.apache.uima.cas.FeatureStructure");
			addBuiltInTypeInfo("uima.cas.Integer", "int");
			addBuiltInTypeInfo("uima.cas.Float", "float");
			addBuiltInTypeInfo("uima.cas.String", "String");
			addBuiltInTypeInfo("uima.cas.Byte", "byte");
			addBuiltInTypeInfo("uima.cas.Short", "short");
			addBuiltInTypeInfo("uima.cas.Long", "long");
			addBuiltInTypeInfo("uima.cas.Double", "double");
			addBuiltInTypeInfo("uima.cas.Boolean", "boolean");

			addBuiltInTypeInfo("uima.cas.TOP", "org.apache.uima.jcas.cas.TOP");
			addBuiltInTypeInfo("uima.cas.FSArray",
					"org.apache.uima.jcas.cas.FSArray", "uima.cas.TOP");
			addBuiltInTypeInfo("uima.cas.IntegerArray",
					"org.apache.uima.jcas.cas.IntegerArray", "uima.cas.Integer");
			addBuiltInTypeInfo("uima.cas.FloatArray",
					"org.apache.uima.jcas.cas.FloatArray", "uima.cas.Float");
			addBuiltInTypeInfo("uima.cas.StringArray",
					"org.apache.uima.jcas.cas.StringArray", "uima.cas.String");
			addBuiltInTypeInfo("uima.cas.BooleanArray",
					"org.apache.uima.jcas.cas.BooleanArray", "uima.cas.Boolean");
			addBuiltInTypeInfo("uima.cas.ByteArray",
					"org.apache.uima.jcas.cas.ByteArray", "uima.cas.Byte");
			addBuiltInTypeInfo("uima.cas.ShortArray",
					"org.apache.uima.jcas.cas.ShortArray", "uima.cas.Short");
			addBuiltInTypeInfo("uima.cas.LongArray",
					"org.apache.uima.jcas.cas.LongArray", "uima.cas.Long");
			addBuiltInTypeInfo("uima.cas.DoubleArray",
					"org.apache.uima.jcas.cas.DoubleArray", "uima.cas.Double");
			addBuiltInTypeInfo("uima.cas.AnnotationBase",
					"org.apache.uima.jcas.cas.AnnotationBase");
			addBuiltInTypeInfo("uima.tcas.Annotation",
					"org.apache.uima.jcas.tcas.Annotation");
			addBuiltInTypeInfo("uima.tcas.DocumentAnnotation",
					"org.apache.uima.jcas.tcas.DocumentAnnotation");
			addBuiltInTypeInfo("uima.cas.EmptyFloatList",
					"org.apache.uima.jcas.cas.EmptyFloatList");
			addBuiltInTypeInfo("uima.cas.EmptyFSList",
					"org.apache.uima.jcas.cas.EmptyFSList");
			addBuiltInTypeInfo("uima.cas.EmptyIntegerList",
					"org.apache.uima.jcas.cas.EmptyIntegerList");
			addBuiltInTypeInfo("uima.cas.EmptyStringList",
					"org.apache.uima.jcas.cas.EmptyStringList");
			addBuiltInTypeInfo("uima.cas.FloatList",
					"org.apache.uima.jcas.cas.FloatList");
			addBuiltInTypeInfo("uima.cas.FSList",
					"org.apache.uima.jcas.cas.FSList");
			addBuiltInTypeInfo("uima.cas.IntegerList",
					"org.apache.uima.jcas.cas.IntegerList");
			addBuiltInTypeInfo("uima.cas.StringList",
					"org.apache.uima.jcas.cas.StringList");
			addBuiltInTypeInfo("uima.cas.NonEmptyFloatList",
					"org.apache.uima.jcas.cas.NonEmptyFloatList");
			addBuiltInTypeInfo("uima.cas.NonEmptyFSList",
					"org.apache.uima.jcas.cas.NonEmptyFSList");
			addBuiltInTypeInfo("uima.cas.NonEmptyIntegerList",
					"org.apache.uima.jcas.cas.NonEmptyIntegerList");
			addBuiltInTypeInfo("uima.cas.NonEmptyStringList",
					"org.apache.uima.jcas.cas.NonEmptyStringList");
			addBuiltInTypeInfo("uima.cas.Sofa", "org.apache.uima.jcas.cas.Sofa");

			CAS tcas = null;
			try {
				tcas = CasCreationUtils.createCas((TypeSystemDescription) null,
						null, new FsIndexDescription[0], casCreateProperties);

			} catch (ResourceInitializationException e1) {
				// never get here
			}

			builtInTypeSystem = ((CASImpl) tcas).getTypeSystemImpl();
			((CASImpl) tcas).commitTypeSystem();

			for (Iterator it = builtInTypeSystem.getTypeIterator(); it
					.hasNext();) {
				Type type = (Type) it.next();
				if (type.isFeatureFinal()) {
					noGenTypes.add(type.getName());
					continue;
				}
				String typeName = type.getName();
				List fs = type.getFeatures();
				List features = new ArrayList(fs.size());
				if (null != fs) {
					for (int i = 0; i < fs.size(); i++) {
						Feature f = (Feature) fs.get(i);
						String fName = f.getName();
						String fTypeName = fName.substring(0,
								fName.indexOf(':'));
						if (typeName.equals(fTypeName))
							features.add(f);
					}
					FeatureDescription[] fds = new FeatureDescription[features
							.size()];
					for (int i = 0; i < features.size(); i++) {
						FeatureDescription fd = UIMAFramework
								.getResourceSpecifierFactory()
								.createFeatureDescription();
						Feature f = (Feature) features.get(i);
						fd.setName(f.getShortName());
						fd.setRangeTypeName(f.getRange().getName());
						fds[i] = fd;
					}
					extendableBuiltInTypes.put(typeName, fds);
				} else
					extendableBuiltInTypes.put(typeName, emptyFds);
			}

		}

		private void addBuiltInTypeInfo(String casName, String javaName,
				String casElementName) {
			TypeInfo ti = new TypeInfo(casName, javaName, casElementName);
			builtInTypes.put(casName, ti);
		}

		private void addBuiltInTypeInfo(String casName, String javaName) {
			addBuiltInTypeInfo(casName, javaName, null);
		}

		/**
		 * Runs the basic jcasgen function without invoking the UI tool.
		 * 
		 * Code is copied directly from org.apache.uima.tools.jcasgen.Jg, main1
		 * method and trimmed to only accommodate type system generation without
		 * any reference to the progress monitor.
		 * 
		 * @param inputFile
		 *            - note input file cannot be a jar file.
		 * @param outputDir
		 */
		private void runJCasGenWithoutUI(String inputFile,
				String outputDirectory, String classPath) throws Exception {

			error = new LogThrowErrorImpl();

			TypeSystemDescription typeSystemDescription = null;
			TypeDescription[] tds = null;

			String xmlSourceFileName = inputFile.replaceAll("\\\\", "/");
			URL url;
			File file = new File(inputFile);
			if (!file.exists())
				throw new Exception(inputFile + " not found.");

			if (null == outputDirectory || outputDirectory.equals("")) {
				File dir = file.getParentFile();
				if (null == dir)
					throw new Exception(inputFile + " needs directory.");
				outputDirectory = dir.getPath() + File.separator + "JCas";
			}
			url = file.toURI().toURL();

			// code to read xml and make cas type instance
			CASImpl casLocal = null;

			XMLInputSource in = new XMLInputSource(url);
			XMLizable specifier = UIMAFramework.getXMLParser().parse(in);

			mergedTypesAddingFeatures.clear();
			typeSystemDescription = mergeTypeSystemImports(((TypeSystemDescription) specifier));

			TypePriorities typePriorities = null;
			FsIndexDescription[] fsIndexDescription = null;

			// no ResourceManager, since everything has been
			// imported/merged by previous actions
			casLocal = (CASImpl) CasCreationUtils.createCas(
					typeSystemDescription, typePriorities, fsIndexDescription);

			tds = typeSystemDescription.getTypes();

			// Generate type classes from DEFAULT templates
			generateAllTypesFromTemplates(outputDirectory, tds, casLocal,
					JCasTypeTemplate.class, JCas_TypeTemplate.class);

		}

		private void generateAllTypesFromTemplates(String outputDirectory,
				TypeDescription[] tds, CASImpl aCas, Class jcasTypeClass,
				Class jcas_TypeClass) throws IOException,
				InstantiationException, IllegalAccessException {

			// Create instances of Template classes
			IJCasTypeTemplate jcasTypeInstance = (IJCasTypeTemplate) jcasTypeClass
					.newInstance();
			IJCasTypeTemplate jcas_TypeInstance = (IJCasTypeTemplate) jcas_TypeClass
					.newInstance();

			Set generatedBuiltInTypes = new TreeSet();

			this.cas = aCas;
			this.typeSystem = cas.getTypeSystem();
			this.casStringType = typeSystem.getType(CAS.TYPE_NAME_STRING);
			this.tcasAnnotationType = typeSystem
					.getType(CAS.TYPE_NAME_ANNOTATION);

			for (int i = 0; i < tds.length; i++) {
				TypeDescription td = tds[i];
				// System.out.println("Description: " + td.getDescription() );
				if (noGenTypes.contains(td.getName()))
					continue;
				if (td.getSupertypeName().equals("uima.cas.String"))
					continue;

				// if the type is built-in - augment it with the built-in's
				// features
				FeatureDescription[] builtInFeatures = (FeatureDescription[]) extendableBuiltInTypes
						.get(td.getName());
				if (null != builtInFeatures) {
					generatedBuiltInTypes.add(td.getName());
					List newFeatures = setDifference(td.getFeatures(),
							builtInFeatures);
					int newFeaturesSize = newFeatures.size();
					if (newFeaturesSize > 0) {
						int newSize = builtInFeatures.length + newFeaturesSize;
						FeatureDescription[] newFds = new FeatureDescription[newSize];
						System.arraycopy(builtInFeatures, 0, newFds, 0,
								builtInFeatures.length);
						for (int j = builtInFeatures.length, k = 0; k < newFeaturesSize; j++, k++)
							newFds[j] = (FeatureDescription) newFeatures.get(k);
						td.setFeatures(newFds);
					} else {
						// The only built-in type which is extensible is
						// DocumentAnnotation.
						// If we get here, the user defined DocumentAnnotation,
						// but did not add any features
						// In this case, skip generation
						continue;
					}
				}
				generateClassesFromTemplate(td, outputDirectory,
						jcasTypeInstance, jcas_TypeInstance);
			}
		}

		private TypeSystemDescription mergeTypeSystemImports(
				TypeSystemDescription tsd)
				throws ResourceInitializationException {
			Collection tsdc = new ArrayList(1);
			tsdc.add(tsd.clone());
			mergedTypesAddingFeatures.clear();
			TypeSystemDescription mergedTsd = CasCreationUtils
					.mergeTypeSystems(tsdc, createResourceManager(),
							mergedTypesAddingFeatures);
			return mergedTsd;
		}

		private void generateClassesFromTemplate(TypeDescription td,
				String outputDirectory, IJCasTypeTemplate jcasTypeInstance,
				IJCasTypeTemplate jcas_TypeInstance) throws IOException {
			simpleClassName = removePkg(getJavaName(td));
			generateClass(outputDirectory, td,
					jcasTypeInstance.generate(new Object[] { this, td }),
					getJavaName(td));
			simpleClassName = removePkg(getJavaName_Type(td));
			generateClass(outputDirectory, td,
					jcas_TypeInstance.generate(new Object[] { this, td }),
					getJavaName_Type(td));
		}

		private void generateClass(String outputDirectory, TypeDescription td,
				String sourceContents, String className) throws IOException {

			String pkgName = getJavaPkg(td);
			String qualifiedClassName = (0 != pkgName.length()) ? pkgName + "."
					+ className : className;
			String targetContainer = outputDirectory + '/'
					+ pkgName.replace('.', '/');
			String targetPath = targetContainer + "/" + className + ".java";
			File targetFile = new File(targetPath);

			(new File(targetContainer)).mkdirs();
			FileWriter fw = new FileWriter(targetPath);
			try {
				fw.write(sourceContents);
			} finally {
				fw.close();
			}

		}

		String getPkg(String nameWithPkg) {
			int lastDot = nameWithPkg.lastIndexOf('.');
			if (lastDot >= 0)
				return nameWithPkg.substring(0, lastDot);
			return "";
		}

		String getPkg(TypeDescription td) {
			return getPkg(td.getName());
		}

		String getJavaPkg(TypeDescription td) {
			TypeInfo bi = (TypeInfo) builtInTypes.get(td.getName());
			if (null == bi)
				return getPkg(td);
			return getPkg(bi.javaNameWithPkg);
		}

		String getJavaNameWithPkg(String casTypeName) {
			TypeInfo bi = (TypeInfo) builtInTypes.get(casTypeName);
			return (null == bi) ? casTypeName : bi.javaNameWithPkg;
		}

		boolean hasPkgPrefix(String name) {
			return name.lastIndexOf('.') >= 0;
		}

		String getJavaName(TypeDescription td) {
			return getJavaName(td.getName());
		}

		String getJavaName_Type(TypeDescription td) {
			return getJavaName(td) + "_Type";
		}

		String getJavaName(String name) {

			if (!hasPkgPrefix(name))
				return name;
			String javaNameWithPkg = getJavaNameWithPkg(name);
			String simpleName = removePkg(javaNameWithPkg);
			if (getPkg(javaNameWithPkg).equals(packageName))
				return simpleName;
			if (javaNameWithPkg.equals(imports.get(simpleName)))
				return simpleName;
			return javaNameWithPkg;
		}

		List setDifference(FeatureDescription[] newFeatures,
				FeatureDescription[] alreadyDefinedFeatures) {
			List result = new ArrayList();
			outerLoop: for (int i = 0; i < newFeatures.length; i++) {
				for (int j = 0; j < alreadyDefinedFeatures.length; j++) {
					if (isSameFeatureDescription(newFeatures[i],
							alreadyDefinedFeatures[j]))
						continue outerLoop;
				}
				result.add(newFeatures[i]);
			}
			return result;
		}

		private boolean isSameFeatureDescription(FeatureDescription f1,
				FeatureDescription f2) {
			if (!f2.getName().equals(f1.getName()))
				return false;
			if (!f2.getRangeTypeName().equals(f1.getRangeTypeName()))
				return false;
			return true;
		}

	}

	private class TypeInfo {

		String xmlName;

		String javaNameWithPkg;

		String javaName; // name without package prefix if in this package

		boolean isArray = false;

		String arrayElNameWithPkg;

		boolean used = false;

		TypeInfo(String xmlName, String javaName) {
			this.xmlName = xmlName;
			this.javaNameWithPkg = javaName;
			this.javaName = Jg.removePkg(javaName);
			this.isArray = false;
			this.arrayElNameWithPkg = "";
		}

		TypeInfo(String xmlName, String javaName, String arrayElNameWithPkg) {
			this(xmlName, javaName);
			if (null != arrayElNameWithPkg) {
				this.isArray = true;
				this.arrayElNameWithPkg = arrayElNameWithPkg;
			}
		}
	}

}