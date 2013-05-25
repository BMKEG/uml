package edu.isi.bmkeg.uml.model;

import java.io.Serializable;
import java.net.URI;
import java.util.UUID;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;

@Entity
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class UMLitem implements Serializable {

	private static final long serialVersionUID = -6488233964792344194L;

	private long id;

	private String uuid;

	private String implName;

	private String baseName;

	private String stereotype;

	private String documentation;

	private UMLmodel model;
	
	private URI uri;

	private boolean isNew = true;
	private boolean designed = true;
	private boolean toImplement = true;

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public UMLitem(String uuid) {
		this.uuid = uuid;
	}

	public UMLitem() {
		this.uuid = UUID.randomUUID().toString();
	}

	// ~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~

	public void setBaseName(String baseName) {
		this.baseName = baseName;
	}

	public String getBaseName() {
		return baseName;
	}

	public void setImplName(String implName) {
		this.implName = implName;
	}

	public String getImplName() {
		return implName;
	}

	public void setStereotype(String stereotype) {
		this.stereotype = stereotype;
	}

	public String getStereotype() {
		return stereotype;
	}

	public void setId(long id) {
		this.id = id;
	}

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	public long getId() {
		return id;
	}

	public void setUuid(String uuid) {
		this.uuid = uuid;
	}

	public String getUuid() {
		return uuid;
	}

	public void setModel(UMLmodel model) {
		this.model = model;
	}

	@ManyToOne(cascade = CascadeType.ALL)
	@JoinColumn(name = "model_id")
	public UMLmodel getModel() {
		return model;
	}

	public void setIsNew(boolean isNew) {
		this.isNew = isNew;
	}

	public boolean getIsNew() {
		return isNew;
	}

	public void setDesigned(boolean designed) {
		this.designed = designed;
	}

	public boolean getDesigned() {
		return designed;
	}

	public void setToImplement(boolean toImplement) {
		this.toImplement = toImplement;
	}

	public boolean getToImplement() {
		return toImplement;
	}

	@Lob
	public String getDocumentation() {
		return documentation;
	}

	public void setDocumentation(String documentation) {
		this.documentation = documentation;
	}

	public URI getUri() {
		return uri;
	}

	public void setUri(URI uri) {
		this.uri = uri;
	}
	
	public void writeStringToUri(String uriString) throws Exception {
		this.uri = new URI(uriString);
	}

}
