package edu.isi.bmkeg.uml.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.log4j.Logger;
import org.jgrapht.DirectedGraph;
import org.jgrapht.alg.CycleDetector;
import org.jgrapht.graph.DefaultDirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import org.jgrapht.traverse.TopologicalOrderIterator;

import edu.isi.bmkeg.uml.interfaces.UmlComponentInterface;

public class UMLmodel implements Serializable {

	private static final long serialVersionUID = -7240976503559507344L;

	private static Logger logger = Logger.getLogger(UMLmodel.class);
	
	public static String XMI_POSEIDON = "xmiPoseidon";
	public static String XMI_MAGICDRAW = "xmiMagicDraw";
	public static String XMI_ARGOUML = "xmiArgoUML";
	public static String SQL_MYSQL = "sqlMySQL";
	public static String DATALOG = "datalog";

	public static String JAVA_IMPL = "java";
	public static String MYSQL_IMPL = "mysql";
	public static String UML_IMPL = "uml";
	public static String DLOG_IMPL = "dlog";
	public static String AS_IMPL = "as";

	private String imp = "java";

	private long id;

	private Map<String, UMLitem> items = new HashMap<String, UMLitem>();

	private String name;

	private String url;

	private String description;

	private UMLpackage topPackage;

	private String sourceType;

	private byte[] sourceData;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setName(String name) {
		this.name = name;
	}

	public String getName() {
		return name;
	}

	public void setId(long id) {
		this.id = id;
	}

	public long getId() {
		return id;
	}

	public void setTopPackage(UMLpackage topPackage) {
		this.topPackage = topPackage;
	}

	public UMLpackage getTopPackage() {
		return topPackage;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	public String getDescription() {
		return description;
	}

	public void setUrl(String url) {
		this.url = url;
	}

	public String getUrl() {
		return url;
	}

	public void setSourceType(String sourceType) {
		this.sourceType = sourceType;
	}

	public String getSourceType() {
		return sourceType;
	}

	public void setSourceData(byte[] sourceData) {
		this.sourceData = sourceData;
	}

	public byte[] getSourceData() {
		return sourceData;
	}

	public void setItems(Map<String, UMLitem> items) {
		this.items = items;
	}

	public Map<String, UMLitem> getItems() {
		return items;
	}

	public void setImp(String imp) {
		this.imp = imp;
	}

	public String getImp() {
		return imp;
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public Map<String, UMLpackage> listPackages() {
		return this.listPackages(".*");
	}
	
	public Map<String, UMLpackage> listPackages(String pkgPattern) {

		Map<String, UMLpackage> map = new HashMap<String, UMLpackage>();

		Pattern patt = Pattern.compile(pkgPattern);
		
		Object[] packs = this.getItems().values().toArray();
		for (int i = 0; i < packs.length; i++) {
			if (!(packs[i] instanceof UMLpackage)) {
				continue;
			}

			UMLpackage p = (UMLpackage) packs[i];
			
			String addr = p.getPkgAddress(); 
			Matcher m = patt.matcher(addr);
			if( m.find() ){
				map.put(addr, p);
			}
			
		}

		return map;

	}

	public List<UMLassociation> listAssociations() {
		return this.listAssociations(".*");
	}
	
	public List<UMLassociation> listAssociations(String pkgPattern) {

		List<UMLassociation> list = new ArrayList<UMLassociation>();

		Pattern patt = Pattern.compile(pkgPattern);

		Object[] items = this.getItems().values().toArray();
		for (int i = 0; i < items.length; i++) {
			
			if (!(items[i] instanceof UMLassociation)) {
				continue;
			}

			UMLassociation a = (UMLassociation) items[i];
			
			UMLclass c1 = a.getRole1().getDirectClass();
			UMLclass c2 = a.getRole2().getDirectClass();

			String addr1 = c1.getClassAddress(); 
			String addr2 = c2.getClassAddress(); 
			Matcher m1 = patt.matcher(addr1);
			Matcher m2 = patt.matcher(addr2);

			if( m1.find() && m2.find()){
				list.add(a);
			} else {
				int wait = 0;
				wait++;
			}
			
		}

		return list;

	}

	public Map<String, UMLclass> listAllClasses() {
		Map<String, UMLclass> map = new HashMap<String, UMLclass>();

		Object[] cArray = this.getItems().values().toArray();
		for (int i = 0; i < cArray.length; i++) {
			if (!(cArray[i] instanceof UMLclass)) {
				continue;
			}

			UMLclass c = (UMLclass) cArray[i];

			map.put(c.getClassAddress(), c);

		}

		return map;

	}

	public Map<String, UMLclass> listClasses() {
		return this.listClasses(".*");
	}
	
	public Map<String, UMLclass> listClasses(String pkgPattern) {
		Map<String, UMLclass> map = new HashMap<String, UMLclass>();
		
		Pattern patt = Pattern.compile(pkgPattern);

		Object[] cArray = this.getItems().values().toArray();
		for (int i = 0; i < cArray.length; i++) {
			if (!(cArray[i] instanceof UMLclass)) {
				continue;
			}			

			UMLclass c = (UMLclass) cArray[i];
			
			if (c.isDataType())
				continue;
			
			String addr = c.getClassAddress(); 
			Matcher m = patt.matcher(addr);
			if( m.find() ){
				map.put(addr, c);
			}

		}

		return map;

	}

	public Map<String, UMLclass> listTypes() {
		Map<String, UMLclass> map = new HashMap<String, UMLclass>();

		Object[] cArray = this.getItems().values().toArray();
		for (int i = 0; i < cArray.length; i++) {
			if (!(cArray[i] instanceof UMLclass)) {
				continue;
			}

			UMLclass c = (UMLclass) cArray[i];

			if (!c.isDataType())
				continue;

			map.put(c.getBaseName(), c);

		}

		return map;

	}
	
	public Map<String, UMLclass> listImplTypes() {
		Map<String, UMLclass> map = new HashMap<String, UMLclass>();

		Object[] cArray = this.getItems().values().toArray();
		for (int i = 0; i < cArray.length; i++) {
			if (!(cArray[i] instanceof UMLclass)) {
				continue;
			}

			UMLclass c = (UMLclass) cArray[i];

			if (!c.isDataType())
				continue;

			map.put(c.getImplName(), c);

		}

		return map;

	}
	

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public String debugString() {
		String debug_string = "";

		Object[] packs = this.getItems().values().toArray();
		for (int i = 0; i < packs.length; i++) {
			if (!(packs[i] instanceof UMLpackage)) {
				continue;
			}

			UMLpackage p = (UMLpackage) packs[i];

			debug_string += "	|\n";

			if (!p.equals(this.topPackage)) {
				debug_string += p.getPkgAddress() + "[" + p.getUuid() + "]\n";
			} else {
				debug_string += "<<top>>\n";
			}

			Object[] classes = p.getClasses().toArray();

			for (int j = 0; j < classes.length; j++) {
				UMLclass c = (UMLclass) classes[j];

				debug_string += c.debugString();

			}

		}

		return debug_string;

	}

	/**
	 * The UMLmodel object always has a 'top' level package denoting the
	 * container at the top of the source tree.
	 */
	public UMLmodel() throws Exception {

		UMLpackage top = new UMLpackage(UUID.randomUUID().toString());
		this.addItem(top);
		top.setModel(this);
		top.setImplName("|");
		top.setBaseName("|");
		top.computePackageAddress();
		String addr = top.getPkgAddress();
		this.listPackages().put(addr, top);

		this.topPackage = top;

		/*
		 * We need to predefine the primitive data types and basic classes
		 * (String, Blob, etc).
		 */

		String[] types = UmlComponentInterface.baseAttrTypes;
		for (int i = 0; i < types.length; i++) {
			addType(types[i]);
		}

	}

	public void addType(String tName) throws Exception {

		UMLclass tClass = new UMLclass();
		this.addItem(tClass);

		tClass.setImplName(tName);
		tClass.setBaseName(tName);
		tClass.setDataType(true);
		this.getTopPackage().getClasses().add(tClass);
		tClass.setPkg(this.getTopPackage());
		tClass.computeClassAddress();

	}

	public void addItem(UMLitem item) throws Exception {

		if (item.getUuid() == null || item.getUuid().length() == 0)
			throw new Exception("UUID not set");

		if ( this.getItems().containsKey(item.getUuid()) )
			throw new Exception("UUID already set in model");

		this.items.put(item.getUuid(), item);
		item.setModel(this);
	}

	public boolean checkClassExistence(String lookupString) {

		Iterator<UMLclass> it = this.listAllClasses().values().iterator();
		while (it.hasNext()) {
			UMLclass c = it.next();
			if (c.getBaseName().equals(lookupString)
					|| c.getClassAddress().equals(lookupString))
				return true;
		}

		return false;

	}

	/**
	 * Merges the two models. This destroys the model being merged.
	 * 
	 * @param that
	 * @throws Exception
	 */
	public void mergeModel(UMLmodel that) throws Exception {

		Set<UMLitem> toIgnore = new HashSet<UMLitem>();
		
		Map<String, UMLclass> lookup = this.listAllClasses();
		UMLpackage top = that.getTopPackage();
		
		toIgnore.add( top );
		toIgnore.addAll( that.listTypes().values() );
		
		//
		// check for uuid collisions
		//
		Set<UMLitem> toReformat = new HashSet<UMLitem>();
		for(UMLitem i: that.getItems().values() ) {
			if( this.getItems().containsKey( i.getUuid() ) ) {
				toReformat.add(i);
			}
		}
		for(UMLitem i : toReformat ) {
			that.getItems().remove(i.getUuid());
			i.setUuid(UUID.randomUUID().toString());
			that.getItems().put(i.getUuid(),i);
		}
		
		//
		// Map equivalences in <<proxy>> classes
		//
		Iterator<UMLclass> cIt = that.listClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass thatClass = cIt.next();
			UMLclass thisClass = lookup.get(thatClass.getClassAddress());
			
			if( thisClass != null ) {
				if( thatClass.getStereotype() != null && 
						thatClass.getStereotype().equals("proxy") ) {

					thisClass.mergeProxyClass(thatClass);
					toIgnore.add(thatClass);
				
				} else if( thisClass.getStereotype() != null && 
						thisClass.getStereotype().equals("proxy") ) {
				
					thatClass.mergeProxyClass(thisClass);
					toIgnore.add(thisClass);
				
				}	
			}
		}
		
		//
		// Map data type equivalences from that to this
		//
		Map<String, UMLclass> theseTypes = this.listTypes();
		cIt = that.listClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass thatClass = cIt.next();
			
			Iterator<UMLattribute> aIt = thatClass.getAttributes().iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();
				UMLclass thatType = a.getType();
				
				if (thatType.isDataType()) {
					if (theseTypes.containsKey(thatType.getBaseName())) {
						UMLclass thisType = theseTypes.get(thatType
								.getBaseName());
						a.setType(thisType);
					} else {
						throw new Exception("Type mismatch: "
								+ thatType.getBaseName() + " not found");
					}
				}

			}

		}
		
		// map packages from that to this.
		Map<String, UMLpackage> thatPkgMap = that.listPackages();
		Map<String, UMLpackage> thisPkgMap = this.listPackages();
		Iterator<String> keyIt = thatPkgMap.keySet().iterator();
		while (keyIt.hasNext()) {
			String key = keyIt.next();
			UMLpackage thatPkg = thatPkgMap.get(key);
			
			if( thisPkgMap.containsKey(key) ) {
				UMLpackage thisPkg = thisPkgMap.get(key);
				
				cIt = thatPkg.getClasses().iterator();
				while(cIt.hasNext()) {
					UMLclass c = cIt.next();
					thisPkg.getClasses().add(c);
					c.setPkg(thisPkg);
				}
				thatPkg.getClasses().clear();

				Iterator<UMLpackage> pIt = thatPkg.getChildren().iterator();
				while(pIt.hasNext()) {
					UMLpackage p = pIt.next();
					if( !thisPkgMap.containsKey( p.getPkgAddress() ) ) {
						thisPkg.getChildren().add(p);
						p.setParent(thisPkg);
					}
				}
				toIgnore.add(thatPkg);

			}
			
		}
		
		// Remove proxy class
		cIt = this.listClasses().values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = cIt.next();
			if( c.getStereotype() != null && c.getStereotype().equals("proxy") && toIgnore.contains(c) ) {
				c.getPkg().getClasses().remove(c);
				c.setPkg(null);
				this.items.remove(c.getUuid());
				c.setModel(null);
			}
		}
		
		// Put all items into the target model
		HashSet<UMLclass> types = new HashSet<UMLclass>(that.listTypes().values());
		Iterator<UMLitem> iIt = that.getItems().values().iterator();
		while (iIt.hasNext()) {
			UMLitem i = iIt.next();

			if( toIgnore.contains(i) ) 
				continue;
			
			i.setModel(this);
			this.items.put(i.getUuid(), i);

		}
		
		this.recomputeClasspaths();

		that.setTopPackage(null);
		that.setItems(null);
		top.setChildren(null);
		top.setClasses(null);
		
	}

	public void checkForProxy() throws Exception {
		
		List<UMLclass> cList = new ArrayList<UMLclass>( 
				this.listAllClasses().values());
		
		Iterator<UMLclass> cIt = cList.iterator();
		while( cIt.hasNext() ) {
			UMLclass c = cIt.next();

			Iterator<UMLattribute> aIt = c.getAttributes().iterator();
			while( aIt.hasNext() ) {
				UMLattribute a = aIt.next();
				if( a.getType().getStereotype() != null && 
						a.getType().getStereotype().equals("proxy") ) {
					throw new Exception( c.getClassAddress() + "." + a.getBaseName() + "---> proxy" );
				}
			}
			
			Iterator<UMLrole> rIt = c.getAssociateRoles().values().iterator();
			while( rIt.hasNext() ) {
				UMLrole r = rIt.next();
				if( r.getDirectClass().getStereotype() != null && 
						r.getDirectClass().getStereotype().equals("proxy") ) {
					throw new Exception( c.getClassAddress() + "." + r.getBaseName() + "---> proxy" );
				}
			}

			
		}		

	}
	
	
	public HashSet<UMLclass> lookupClass(String name) {

		HashSet<UMLclass> hits = new HashSet<UMLclass>();

		Object[] cArray = this.getItems().values().toArray();
		for (int i = 0; i < cArray.length; i++) {
			if (!(cArray[i] instanceof UMLclass)) {
				continue;
			}

			UMLclass c = (UMLclass) cArray[i];

			if (c.getBaseName().equals(name)) {
				hits.add(c);
			}

		}

		return hits;

	}

	public void convertToRelationalImplementation() throws Exception {
		this.convertToRelationalImplementation(".");
	}
	
	public void addPrimaryKeys(String pkgPattern) throws Exception {
		for(UMLclass c: this.listClasses(pkgPattern).values() ) {
			if (!c.getIsNew())
				continue;
			c.generatePrimaryKeyAttribute();
		}
	}
	
	public void convertToRelationalImplementation(String pkgPattern) throws Exception {

		this.addPrimaryKeys(pkgPattern);

		//
		// Derived association classes
		//
		Iterator<UMLassociation> assIt = this.listAssociations(pkgPattern).iterator();
		while (assIt.hasNext()) {
			UMLassociation ass = assIt.next();

			if (!ass.getIsNew())
				continue;

			UMLrole r1 = ass.getRole1();
			UMLrole r2 = ass.getRole2();

			/* HAD PREVIOUSLY SET THIS UP SO THAT SELF-REFERENTIAL 
			 * RELATIONSHIPS WOULD BE MODELED BY AN N-to-N RELATIONSHIP
			 * THIS PATENTLY DOES NOT WORK IF YOU WANT A 1-to-N RELATIONSHIP.
			 * NEED TO MAKE THIS FUNCTION. 
			 * if (r1.getDirectClass().equals(r2.getDirectClass())) {
				if ((r1.getNavigable() && r2.getMult_upper() != 1)
						|| (r2.getNavigable() && r1.getMult_upper() != 1)) {

					ass.generateDerivedAssociationAndClasses();

				}
			} else*/
			if (ass.getDesigned() && ((r1.getMult_upper() != 1 && r2.getMult_upper() != 1) 
							|| ass.getLinkClass() != null)) {

				ass.generateDerivedAssociationAndClasses();

			}

		}
 
		//
		// Derived attribute-based reference
		//
		for(UMLclass c : this.listClasses(pkgPattern).values()) {
			for(UMLattribute a : c.getAttributes()) {
				if (!a.getType().isDataType()) {
					if (!a.getIsNew())
						continue;
					a.generateAttributeBasedReference();
				}
			}

		}

		//
		// Foreign Keys
		//
		assIt = this.listAssociations(pkgPattern).iterator();
		while (assIt.hasNext()) {
			UMLassociation ass = assIt.next();

			if (!ass.getToImplement())
				continue;

			ass.generateForeignKeys(false);

		}

		//
		// Need to make sure that the ordering of attributes is always
		// consistent. Foreign Keys are a little random so we make
		// sure that they are always sorted at the end in alphabetical order
		// of their target classes / attribute names.
		//
		CLASSLOOP: for(UMLclass c : this.listClasses(pkgPattern).values()) {

			ArrayList<UMLattribute> ats = new ArrayList<UMLattribute>();
			HashMap<String, UMLattribute> pks = new HashMap<String, UMLattribute>();
			HashMap<String, UMLattribute> fks = new HashMap<String, UMLattribute>();
			Iterator<UMLattribute> aIt = c.getAttributes().iterator();
			while (aIt.hasNext()) {
				UMLattribute a = aIt.next();
				if (a.getStereotype() != null
						&& a.getStereotype().contains("PK")) {
					pks.put(a.getBaseName(), a);
				} else if (a.getStereotype() != null
						&& a.getStereotype().equals("FK")) {
					fks.put(a.getPk().getParentClass().getBaseName() + "<-"
							+ a.getBaseName(), a);
				} else {
					ats.add(a);
				}
			}

			String[] pkKeys = (String[]) pks.keySet().toArray(new String[0]);
			Arrays.sort(pkKeys);
			String[] fkKeys = (String[]) fks.keySet().toArray(new String[0]);
			Arrays.sort(fkKeys);

			ArrayList<UMLattribute> sortedAtts = new ArrayList<UMLattribute>();

			// Start with the sorted PKs
			for (int i = 0; i < pkKeys.length; i++) {
				sortedAtts.add(pks.get(pkKeys[i]));
			}
			// Add the normal attributes
			for (int i = 0; i < ats.size(); i++) {
				sortedAtts.add(ats.get(i));
			}
			// Add the sorted FKs
			for (int i = 0; i < fkKeys.length; i++) {
				sortedAtts.add(fks.get(fkKeys[i]));
			}

			c.setAttributes(sortedAtts);

		}

		//
		// Keys supporting inheritence
		//
		UMLclass longType = this.listTypes().get("long");
		CLASSLOOP: for(UMLclass c : this.listClasses(pkgPattern).values()) {

			// next CLASSLOOP if $class->{implement} == 0 || !$class->{_isNew};
			if (!c.getIsNew() || !c.getToImplement())
				continue CLASSLOOP;

			if (c.getParent() != null) {

				if (c.getPkArray().size() != c.getParent().getPkArray().size())
					throw new Exception(
							"Mismatch in the number of primary keys in Class "
									+ c.getBaseName() + " and it's parent "
									+ c.getParent().getBaseName());

				for (int i = 0; i < c.getPkArray().size(); i++) {

					UMLattribute ck = c.getPkArray().get(i);
					UMLattribute pk = c.getParent().getPkArray().get(i);
					
					ck.setType(longType);

					ck.setPk(pk);
					pk.getFk().add(ck);

				}

			}

		}

		// Convert implementation of all current items in model to isNew = FALSE
		Iterator<UMLitem> it = this.getItems().values().iterator();
		while (it.hasNext()) {
			UMLitem i = it.next();
			i.setIsNew(false);
		}

	}
	

	public DirectedGraph<UMLclass, DefaultEdge> readDependencyGraph(String pkgPattern)
			throws Exception {

		DirectedGraph<UMLclass, DefaultEdge> g = new DefaultDirectedGraph<UMLclass, DefaultEdge>(
				DefaultEdge.class);

		logger.debug("\n");
		logger.debug("Calculating RDBMS dependency");
		
		Iterator<UMLclass> cIt = this.listClasses(pkgPattern).values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = cIt.next();

			if (!c.getToImplement())
				continue;

			g.addVertex(c);
			
			logger.debug("adding " + c.toString());

		}

		cIt = this.listClasses(pkgPattern).values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = cIt.next();
			
			if (!c.getToImplement())
				continue;

			Iterator<UMLrole> rIt = c.getAssociateRoles().values().iterator();
			while (rIt.hasNext()) {
				UMLrole r = rIt.next();

				if (!r.getToImplement()) {
					continue;
				}

				if (r.getFkArray().size() != 1)
					throw new Exception("incorrect number of keys for "
							+ c.getBaseName() + "." + r.getBaseName());

				List<UMLattribute> fks = r.getFkArray();
				UMLattribute fk = fks.get(0);
				UMLattribute pk = fk.getPk();				
				
				if( !pk.getParentClass().getToImplement() ||
						!fk.getParentClass().getToImplement() ) 
					continue;

				//
				// Note that we permit classes to refer to themselves via their
				// PKs and FKs
				//
				if ( (g.containsVertex(pk.getParentClass()) && g.containsVertex(fk.getParentClass())) && 
						!g.containsEdge(pk.getParentClass(), fk.getParentClass())
						&& !pk.getParentClass().equals(fk.getParentClass())) {

					UMLrole or = r.otherRole();
					logger.debug(r.getDirectClass().getBaseName() + "." 
							+ or.getBaseName() + " <---> " +
							or.getDirectClass().getBaseName() + "." 
							+ r.getBaseName() );

					g.addEdge(pk.getParentClass(), fk.getParentClass());
		
				}

			}

		}

		cIt = this.listClasses(pkgPattern).values().iterator();
		while (cIt.hasNext()) {
			UMLclass c = cIt.next();
			
			if (!c.getToImplement())
				continue;
			
			if( c.getChildren().size() > 0 ) 
				logger.debug( c.getBaseName() );
			Iterator<UMLclass> childIt = c.getChildren().iterator();
			while (childIt.hasNext()) {
				UMLclass child = childIt.next();
				g.addEdge(c, child);
				logger.debug( "     <|--- " + child.getBaseName() );
				
			}
			
		}

		return g;

	}

	public List<UMLclass> readClassDependencyOrder() throws Exception {
		return this.readClassDependencyOrder(".");
	}
	
	public List<UMLclass> readClassDependencyOrder(String pkgPattern) throws Exception {

		List<UMLclass> l = new ArrayList<UMLclass>();

		Pattern patt = Pattern.compile(pkgPattern);

		DirectedGraph<UMLclass, DefaultEdge> g = this.readDependencyGraph(pkgPattern);
	
		Set<UMLclass> solitaires = new HashSet<UMLclass>(
				this.listClasses(pkgPattern).values()
				);
		
		CycleDetector<UMLclass, DefaultEdge> cycles = new CycleDetector<UMLclass, DefaultEdge>(g);
		if( cycles.detectCycles() ) {
			
			String msg = "\n";
			Iterator<UMLclass> cycleClassesIt = cycles.findCycles().iterator();
			while( cycleClassesIt.hasNext() ) {
				UMLclass cycleClass = cycleClassesIt.next();
				msg += cycleClass.readClassAddress() + "\n";
			}
			
			throw new Exception("Dependency cycles in graph:\n" + msg );
		}
		
		TopologicalOrderIterator<UMLclass, DefaultEdge> orderIterator = new TopologicalOrderIterator<UMLclass, DefaultEdge>(
				g);
		while (orderIterator.hasNext()) {
			UMLclass v = orderIterator.next();
			
			String addr = v.getClassAddress(); 
			Matcher m = patt.matcher(addr);
			if( m.find() ){
				l.add(v);
			} 
			
		}
		
		return l;

	}

	public void cleanModel() {

		Iterator<UMLitem> it = this.getItems().values().iterator();
		while (it.hasNext()) {
			UMLitem i = it.next();
			i.setIsNew(true);
		}

	}
	
	public void recomputeClasspaths() {

		Iterator<UMLclass> it = this.listAllClasses().values().iterator();
		while (it.hasNext()) {
			UMLclass c = it.next();
			c.computeClassAddress();
		}

	}
	
	public void filterClasses(String regex) {
		
		Pattern p = Pattern.compile(regex);
		
		Map<String,UMLclass> map = this.listClasses();
		Iterator<String> it = map.keySet().iterator();
		while( it.hasNext() ) {
			String key = it.next();
			UMLclass c = map.get(key);
			
			Matcher m = p.matcher(key);
			if( !m.find() ) {
				c.setToImplement(false);
			}
			
		}
			
	}
	
	/**
	 * Used to convert a model to generate 'QueryObjects'
	 * - move all classes in packages named '.model.' to a sub-package named '.model.qo'
	 * - rename all classes to <STEM>_qo
	 * @throws Exception
	 */
	public void convertToQuestionObjects() throws Exception {
				
		Map<String, UMLpackage> pkgMap = this.listPackages("\\.model");
		Iterator<String> pIt = pkgMap.keySet().iterator();
		while(pIt.hasNext()) {
			String addr = pIt.next();
			UMLpackage p = pkgMap.get(addr);
			addr = addr.substring(2,addr.length());
			if( addr.endsWith(".model") ) {
				UMLpackage parent = p.getParent();
				
				// add a new package into the hierarchy
				UMLpackage p2 = parent.addNewChildPackage("model");

				p.setParent(p2);
				p2.getChildren().add(p);
				parent.getChildren().remove(p);
				p.setBaseName("qo");
				p.setImplName("qo");
				p.computePackageAddress();
			}
		}
		
		Map<String, UMLclass> classMap = this.listClasses("\\.model\\.");
		Iterator<String> cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);
			c.setBaseName(c.getBaseName() + "_qo");
			c.setImplName(c.getImplName() + "_qo");
			c.computeClassAddress();
		}
		
	}

	/**
	 * Used to convert a model back from 'QueryObjects'
	 * - move all classes in packages named '.model.qo.' to the parent '.model.'
	 * - rename all classes to <STEM>
	 * @throws Exception
	 */

	public void convertFromQuestionObjects() throws Exception {
		
		Map<String, UMLpackage> pkgMap = this.listPackages("\\.model\\.");
		Iterator<String> pIt = pkgMap.keySet().iterator();
		while(pIt.hasNext()) {
			String addr = pIt.next();
			UMLpackage p = pkgMap.get(addr);
			addr = addr.substring(2,addr.length());
			if( addr.endsWith(".model.qo") ) {
				UMLpackage parent = p.getParent();
				if( !parent.getBaseName().equals("model") ) {
					throw new Exception("question object specification is broken: " + addr);
				}
				UMLpackage p2 = parent.getParent();
				p.setParent(p2);
				p2.getChildren().remove(parent);
				p2.getChildren().add(p);
				this.getItems().remove(parent.getUuid());
				parent.setModel(null);
				p.setBaseName("model");
				p.setImplName("model");
				p.computePackageAddress();
			}
		}
		
		Map<String, UMLclass> classMap = this.listClasses("\\.model\\.");
		Iterator<String> cIt = classMap.keySet().iterator();
		while(cIt.hasNext()) {
			String addr = cIt.next();
			UMLclass c = classMap.get(addr);
			if( addr.endsWith("_qo") ) {
				c.setBaseName(c.getBaseName().substring(0, c.getBaseName().length()-3));
				c.setImplName(c.getImplName().substring(0, c.getImplName().length()-3));
				c.computeClassAddress();
			}
		}
		
	}

}
