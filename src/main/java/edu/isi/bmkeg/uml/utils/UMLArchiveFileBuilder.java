package edu.isi.bmkeg.uml.utils;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.Timestamp;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;

import com.google.common.io.Files;

import edu.isi.bmkeg.uml.interfaces.MysqlUmlInterface;
import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.Converters;

/**
 * Class to generate and read VPDMf archive files.
 * 
 * @author burns
 * 
 */
public class UMLArchiveFileBuilder {

	private File tempUnzippedDirectory;
	private Map<String, File> filesInZip;
	
	private UMLExcelEngine xl = new UMLExcelEngine();
	
	/**
	 * Builds a UML archive file ('*_UML.zip') from (a) the model, (b) the
	 * views and (c) either the excel spreadsheet with the data or a directory of 
	 * tab delimited *.txt files saved from a spreadsheet.
	 * 
	 * Note, if the varFile is set to a directory, the archive will be generated within 
	 * the directory with a timestamped filename, otherwise the target file 
	 * is simply specified there. If a file already exists at that location, it will
	 * be overwritten. 
	 * 
	 * The archive is formatted as a zip file and contains (a) the source of the
	 * UMLmodel (model.xml) ... and maybe a jar with all the POJOs of the
	 * UMLmodel that can be read using reflection... (c) an excel spreadsheet of 
	 * the data in the system (d) subsidiary mysql files (1) the script that 
	 * generates the database (2) the script that uploads data from the files 
	 * (3) a set of datafiles that contain the data in an appropriately delimited manner.
	 * 
	 * @param modelFile
	 * @param viewDirectory
	 * @param excelSpreadsheet
	 * @param varFile
	 * @throws IOException
	 */
	public void buildArchiveFile(UMLmodel m, File dataDirOrFile, File varFile)
			throws Exception {

		File targetDir = varFile.getParentFile();

		if (varFile.isDirectory()) {

			varFile = generateVarFileFromFolder(m, varFile, "UML");

		}

		filesInZip = new HashMap<String, File>();
		String commandsString = "";

		tempUnzippedDirectory = Files.createTempDir();
		tempUnzippedDirectory.deleteOnExit();
		String dAddr = tempUnzippedDirectory.getAbsolutePath();

		//
		// Write the model file to this temporary location
		//
		String suffix = ".tmp";
		if (m.getSourceType().equals(UMLmodel.XMI_MAGICDRAW))
			suffix = "_mgd.xml";
		else if (m.getSourceType().equals(UMLmodel.XMI_POSEIDON))
			suffix = "_pos.xml";

		File uml = new File(dAddr + "/" + m.getName() + suffix);
		FileOutputStream fos = new FileOutputStream(uml);
		fos.write(m.getSourceData());
		fos.close();
		filesInZip.put(uml.getName(), uml);
		
		// TODO: not right now...
		//m.convertToJar(dAddr, "model.jar");
		
		//
		// Dump whole UML model as serialized object to file
	    //
		InputStream is = new ByteArrayInputStream(
	            Converters.objectToByteArray(m)
	            );
	    suffix = "uml.bin";
		File vpdmfFile = new File(dAddr + "/" + suffix);
		fos = new FileOutputStream(vpdmfFile);
		 // Transfer bytes from in to out
	    byte[] buf = new byte[1024];
	    int len;
	    while ((len = is.read(buf)) > 0) {
	        fos.write(buf, 0, len);
	    }
	    fos.close();
		filesInZip.put(vpdmfFile.getName(), vpdmfFile);
	    
		//
		// Save the sql to this location too.
		//
		MysqlUmlInterface mysql = new MysqlUmlInterface();
		mysql.setUmlModel(m);

		String sql = mysql.generateSqlForModel();
		
		//
		// Add extra SQL data for Spring security tables.
		//
		String securitySql = "create table users (" +
				"username varchar(50) not null primary key, " +
				"password varchar(50) not null," +
				"enabled boolean not null) engine = InnoDb;\n";
		securitySql += "create table authorities (" +
				"username varchar(50) not null," +
				"authority varchar(50) not null," +
				"foreign key (username) references users (username)," +
				"unique index authorities_idx_1 (username, authority)" +
				") engine = InnoDb;\n";
		securitySql += "create table groups (" +
				"id bigint unsigned not null auto_increment primary key," +
				"group_name varchar(50) not null" +
				") engine = InnoDb;\n";
		securitySql += "create table group_authorities (" +
				"group_id bigint unsigned not null," +
				"authority varchar(50) not null," +
				"foreign key (group_id) references groups (id)" +
				") engine = InnoDb;\n";
		securitySql += "create table group_members (" +
				"id bigint unsigned not null auto_increment primary key," +
				"username varchar(50) not null," +
				"group_id bigint unsigned not null," +
				"foreign key (group_id) references groups (id)" +
				") engine = InnoDb;\n";
		securitySql += "create table persistent_logins (" +
				"username varchar(64) not null," +
				"series varchar(64) primary key," +
				"token varchar(64) not null," +
				"last_used timestamp not null" +
				") engine = InnoDb;\n";
		
		File buildFile = new File(dAddr + "/build.sql");
		FileUtils.writeStringToFile(buildFile, sql + "\n" + securitySql);
		filesInZip.put("sqlFiles/build.sql", buildFile);

		//
		// Need to do some more stuff here...
		//
		if (dataDirOrFile == null) {
			dataDirOrFile = new File(dAddr + "/data.xls");

			FileOutputStream out = new FileOutputStream(dataDirOrFile);
			HSSFWorkbook wb = xl.generateBlankExcelSpreadsheetFromUMLmodel(m);
			wb.write(out);
			out.close();

		} else if (dataDirOrFile.isDirectory()) {

			filesInZip.putAll(xl.generateSQLDataFilesFromDirectory(m,
					dataDirOrFile, tempUnzippedDirectory));

		} else {

			//
			// Validate the XL file before putting it into the archive by
			// unpacking it
			// and setting up data in batch load files.
			//
			filesInZip.putAll(xl.generateSQLDataFilesFromExcel(m,
					dataDirOrFile, tempUnzippedDirectory));
			filesInZip.put("data.xls", dataDirOrFile);

		}

		if (varFile.exists()) {
			varFile.delete();
		}

		Converters.zipIt(filesInZip, varFile);
		Converters.recursivelyDeleteFiles(tempUnzippedDirectory);

	}

	private File generateVarFileFromFolder(UMLmodel m, File varFile, String suffix) {
		File targetDir;
		targetDir = varFile;

		Date now = Calendar.getInstance().getTime();
		Timestamp ts = new java.sql.Timestamp(now.getTime());
		String date = ts.toString();
		date = date.substring(0, date.indexOf(" "));
		
		String varFileName = m.getName() + "_" + date + 
				"-" + ts.getHours() + ts.getMinutes() + "_" + suffix + ".zip";

		varFile = new File(targetDir.getAbsolutePath() + "/" + varFileName);
		return varFile;
	}
	
	public void cleanUpTempArchiveLocation() throws Exception {

		Iterator<String> keyIt = filesInZip.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			File f = filesInZip.get(key);
			
			// we only permit removal of files from inside the unzipped directory.
			if (f.getAbsolutePath().startsWith(tempUnzippedDirectory.getAbsolutePath()))
				f.delete();
		}
		
	}
		
	
}
