package edu.isi.bmkeg.uml.interfaces;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import edu.isi.bmkeg.uml.model.UMLattribute;
import edu.isi.bmkeg.uml.model.UMLclass;
import edu.isi.bmkeg.uml.model.UMLrole;
import edu.isi.bmkeg.utils.MapCreate;

public class MysqlUmlInterface extends UmlComponentInterface implements ImplConvert {

	private String dbName;

	private String host;

	private String user;

	private String password;

	public void setDbName(String dbName) {
		this.dbName = dbName;
	}

	public String getDbName() {
		return dbName;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getHost() {
		return host;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getUser() {
		return user;
	}

	public void setPassword(String password) {
		this.password = password;
	}

	public String getPassword() {
		return password;
	}

	public MysqlUmlInterface() throws Exception {

		this.buildLookupTable();

		String[] words = new String[] { "mod", "index"};

		this.setStopWords(words);

	}

	public void buildLookupTable() throws Exception {
		String[] mySqlTargetTypes = new String[] { "SERIAL", "CHAR(1)",
				"MEDIUMINT", "INT", "BIGINT", "FLOAT", "DOUBLE", "BOOL",
				"CHAR(1)", "VARCHAR(25)", "VARCHAR(255)", "MEDIUMTEXT", "LONGBLOB",
				"LONGBLOB", "DATE", "TIMESTAMP", "TEXT" };

		this.setLookupTable(new HashMap<String, String>(MapCreate.asMap(
				UmlComponentInterface.baseAttrTypes, mySqlTargetTypes)));

	}

	public String generateSqlForModel() throws Exception {

		return this.generateSqlForModel(".");
		
	}
	
	public String generateSqlForModel(String pkgPattern) throws Exception {

		this.convertAttributes();

		List<UMLclass> orderedTables = this.getUmlModel().readClassDependencyOrder(pkgPattern);

		//
		// Build tables
		//
		this.dbName = this.getUmlModel().getName().toLowerCase();

		String sqlOutput = "";

		Iterator<UMLclass> tIt = orderedTables.iterator();
		while (tIt.hasNext()) {
			UMLclass tab = tIt.next();
			
			if( !tab.getToImplement()) {
				continue;
			}
			
			sqlOutput += this.generate_CREATE_TABLE_sql(tab) + ";\n";

		}

		tIt = orderedTables.iterator();
		while (tIt.hasNext()) {
			UMLclass tab = tIt.next();

			if( !tab.getToImplement()) {
				continue;
			}

			sqlOutput += this.generate_CREATE_UNIQUE_CONSTRAINT_sql(tab);

		}

		tIt = orderedTables.iterator();
		while (tIt.hasNext()) {
			UMLclass tab = tIt.next();

			if( !tab.getToImplement()) {
				continue;
			}

			sqlOutput += this.generate_CREATE_INDEX_sql_for_table(tab);

		}



		return sqlOutput;

	}

	private String generate_CREATE_TABLE_sql(UMLclass tab) {

		String sqlOutput = "CREATE TABLE " + tab.getImplName() + " (";

		Iterator<UMLattribute> atIt = tab.getAttributes().iterator();
		while (atIt.hasNext()) {
			UMLattribute at = atIt.next();

			if (!at.getToImplement())
				continue;

			if (at.getType().getBaseName().equals("serial")) {

				sqlOutput += at.getImplName()
						+ " BIGINT NOT NULL AUTO_INCREMENT";

			} else {

				sqlOutput += at.getImplName() + " "
						+ at.getType().getImplName();

				if (tab.getPkArray().contains(at)) {

					sqlOutput += " NOT NULL";

				}

			}

			sqlOutput += ", ";

			if (at.getPk() != null) {

				sqlOutput += "INDEX " + at.getImplName() + "_idx ("
						+ at.getImplName() + "), ";
				sqlOutput += "FOREIGN KEY (" + at.getImplName()
						+ ") REFERENCES ";
				sqlOutput += at.getPk().getParentClass().getImplName();
				sqlOutput += "(" + at.getPk().getImplName() + "), ";

			}

		}

		/*
		 * # if( defined $tab->{ParentClass} ) { # # my @pks =
		 * 
		 * @{$tab->{PrimaryKeyArray}->get_elements()}; # my @parentPks =
		 * 
		 * @{$tab->{ParentClass}->{PrimaryKeyArray}->get_elements()}; # #
		 * confess "Can't generate inheritence keys with more than one column" #
		 * if scalar(@pks) > 1 || scalar(@parentPks) > 1; # # $sql_output .=
		 * "INDEX $pks[0]->{Name}_idx ($pks[0]->{Name}), "; # $sql_output .=
		 * "FOREIGN KEY ($pks[0]->{Name}) REFERENCES "; # $sql_output .=
		 * "$parentPks[0]->{ParentClass}->{Name}"; # $sql_output .=
		 * "($parentPks[0]->{Name}), "; # # }
		 */

		sqlOutput += "primary key (";

		Iterator<UMLattribute> pkIt = tab.getPkArray().iterator();
		while (pkIt.hasNext()) {
			UMLattribute pk = pkIt.next();
			sqlOutput += pk.getImplName();
			if (pkIt.hasNext())
				sqlOutput += ", ";
		}

		sqlOutput += ")";

		// previously set TYPE=INNODB. In MySQL 5.5 onwards the system's default is INNODB
		sqlOutput += ")ENGINE=INNODB";
		//sqlOutput += ")";

		return sqlOutput;

	}

	private String generate_CREATE_UNIQUE_CONSTRAINT_sql(UMLclass tab) {

		/*
		 * 
		 * my $uniqueCount = scalar grep {$_->{Stereotype} eq "unique"}
		 * 
		 * @{ $tab->{Attributes}->get_elements() };
		 * 
		 * if( !defined $tab->{LinkAssociation} && $uniqueCount == 0 ) { return;
		 * }
		 */

		String sqlOutput = "";

		/*
		 * Table is a set-backing-table.
		 */
		if (tab.getLinkAssociation() != null) {

			sqlOutput = "ALTER TABLE " + tab.getImplName() + " ";
			
			String idxName = tab.getImplName() + "_idx";
			if( idxName.length() > 64 )
				idxName = tab.getImplName().substring(0,60) + "_idx";

			sqlOutput += " ADD UNIQUE " + idxName + " (";

			ArrayList<UMLattribute> atts = new ArrayList<UMLattribute>();
			Iterator<UMLattribute> attIt = tab.getAttributes().iterator();
			while (attIt.hasNext()) {
				UMLattribute att = attIt.next();
				if (att.getStereotype().equals("FK")) {
					sqlOutput += att.getImplName() + ", ";
				}
			}
			sqlOutput = sqlOutput.substring(0, sqlOutput.length() - 2);

			sqlOutput += ");\n";

		}

		//
		// Table has unique attributes specified.
		//
		/*
		 * IGNORE THIS FOR NOW. MECHANISM FOR PLACING UNIQUE CONSTRAINTS INTO
		 * THE MODEL IS STEREOTYPES... IS THIS THE RIGHT WAY TO DO THIS?
		 * 
		 * elsif( $uniqueCount > 0 ) {
		 * 
		 * 
		 * $sql_output = "ALTER TABLE $tab->{Name} "; $sql_output .=
		 * " ADD UNIQUE $tab->{Name}_idx";
		 * 
		 * @atts = grep {$_->{Stereotype} eq "unique"}
		 * 
		 * @{ $tab->{Attributes}->get_elements() };
		 * 
		 * my $temp = ""; for( $i=0; $i < scalar @atts; $i++) { my $a =
		 * $atts[$i]; $temp .= $sql_output . $i . " (" . $a->{Name} . ");\n" }
		 * 
		 * $sql_output = $temp;
		 * 
		 * }
		 */

		return sqlOutput;

	}

	/**
	 *  Add indexes for each index element in table (based on type)
	 * @param tab
	 * @return
	 */
	private String generate_CREATE_INDEX_sql_for_table(UMLclass tab) {

		String sqlOutput = "";
		
		Iterator<UMLattribute> ieIt = tab.getAttributes().iterator();
		while (ieIt.hasNext()) {
			UMLattribute ie = ieIt.next();
						
			if( !ie.isIdx() ) 
				continue;

			sqlOutput += "CREATE ";
			
			if( ie.isUnique() ) 
				sqlOutput += "UNIQUE ";
			
			sqlOutput += "INDEX IDX__" + ie.getBaseName() + " ON " + 
					tab.getImplName() + "(" + ie.getImplName();

			String t = ie.getType().getBaseName();
			if( t.equals("blob") || t.equals("image") 
					|| t.equals("longString") || t.equals("String") )
					sqlOutput += "(50)";
			
			sqlOutput += ");\n";

		}
		
		return sqlOutput;

	}
	
	
	private String generate_ALTER_TABLE_sql_for_table(UMLclass tab) {

		String sqlOutput = "CREATE INDEX PK_" + tab.getImplName() + " ON "
				+ tab.getImplName() + "(";

		Iterator<UMLattribute> pkIt = tab.getPkArray().iterator();
		while (pkIt.hasNext()) {
			UMLattribute pk = pkIt.next();
			sqlOutput += pk.getImplName();
			if (pkIt.hasNext())
				sqlOutput += ", ";
		}

		sqlOutput += ") WITH PRIMARY\n\n";

		//
		// Identify all roles that are represented as foreign keys
		// Need to run through attributes in $tab and find all
		// unique ForeignKeys
		// [From Perl Cookbook, page 102]

		HashSet<UMLattribute> fka = new HashSet<UMLattribute>();
		HashSet<UMLrole> fkRoles = new HashSet<UMLrole>();
		Iterator<UMLattribute> aIt = tab.getAttributes().iterator();
		while (aIt.hasNext()) {
			UMLattribute a = aIt.next();
			if (a.getFkRole() != null) {
				fka.add(a);
				fkRoles.add(a.getFkRole());
			}
		}

		//
		// Standard SQL to add FK
		//
		// ALTER TABLE table {
		// ADD {COLUMN field type[(size)] [NOT NULL] [CONSTRAINT index] |
		// ALTER COLUMN field type[(size)] |
		// CONSTRAINT multifieldindex} |
		// DROP {COLUMN field I CONSTRAINT indexname} }
		//
		Iterator<UMLrole> fkrIt = fkRoles.iterator();
		while (fkrIt.hasNext()) {
			UMLrole fkr = fkrIt.next();
			UMLrole pkr = fkr.otherRole();

			List<UMLattribute> tempFKA = fkr.getFkArray();

			List<UMLattribute> tempPKA = new ArrayList<UMLattribute>();
			Iterator<UMLattribute> fkIt = tab.getAttributes().iterator();
			while (fkIt.hasNext()) {
				UMLattribute fk = fkIt.next();
				UMLattribute pk = fk.getPk();
				tempPKA.add(pk);
			}

			sqlOutput += "ALTER TABLE " + tab.getImplName() + " {";
			sqlOutput += " ADD CONSTRAINT FK_" + pkr.getImplName()
					+ " FOREIGN KEY (";

			aIt = tempFKA.iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();
				sqlOutput += a.getImplName();
				if (aIt.hasNext())
					sqlOutput += ", ";
			}

			sqlOutput += ") REFERENCES " + pkr.getDirectClass().getImplName()
					+ " (";

			aIt = tempPKA.iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();
				sqlOutput += a.getImplName();
				if (aIt.hasNext())
					sqlOutput += ", ";
			}

			sqlOutput += ")";

		}

		return sqlOutput;

	}
	
	public String generateLoadDataCommands() throws Exception {

		String loadDataCommands = "";
		

		Iterator<UMLclass> cIt = this.getUmlModel().listClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = cIt.next();

			if( !c.getToImplement() ) {
				continue;
			}

			String atts = "";
			String ugh = "";
			Iterator aIt = c.getAttributes().iterator();
			while (aIt.hasNext()) {
				UMLattribute a = (UMLattribute) aIt.next();
				if (a.getToImplement()) {
					if (atts.length() > 0)
						atts += ",";
					atts += a.getBaseName();
				}
			}

			//
			// Somewhat Ugly Hack: to make sure that MySQL uses the order of
			// columns we provide
			//
			// - this works by adding an extra column to the backup text file so
			// there is a forced mismatch between our load statement and the
			// file.
			// In this case MySQL will use the column names in the order that we
			// specify, otherwise it will use it's own order and misassign
			// columns.
			//
			String rAtts = atts;
			UMLattribute a = (UMLattribute) c.getPkArray().get(0);
			atts += "," + a.getBaseName();

			loadDataCommands += "LOAD DATA LOCAL INFILE 'SUB_FILEPATH_HERE/"
					+ c.getBaseName() + ".dat' REPLACE INTO TABLE "
					+ c.getBaseName()
					+ " FIELDS TERMINATED BY '\\t\\t\\t' LINES TERMINATED BY "
					+ "'\\n\\n\\n' (" + rAtts + ");\n";
		}
		
		return loadDataCommands;
		
	}

}
