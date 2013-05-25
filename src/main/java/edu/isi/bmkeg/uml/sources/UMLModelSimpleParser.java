package edu.isi.bmkeg.uml.sources;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.io.IOUtils;
import org.xml.sax.InputSource;

import edu.isi.bmkeg.uml.model.UMLmodel;

public class UMLModelSimpleParser  {

	private String parserType;
    private ArrayList<UMLmodel> umlModels = new ArrayList<UMLmodel>();
    
    public UMLModelSimpleParser(String parserType) {
    	this.parserType = parserType;
    }

	public void parseUMLModelFile(File f) throws Exception {

		if( !f.exists() )
			throw new Exception("VPDMf spec file " + f.getPath() + " does not exist.");
		
		BufferedReader br = new BufferedReader(new FileReader(f));

        String modelName = f.getName().substring(0,f.getName().lastIndexOf("."));
 
        parseUMLModelFile(br, modelName);

        br.close();
	}
	
    public void parseUMLModelFile(BufferedReader br, String modelName) throws Exception {

    	byte[] data = IOUtils.toByteArray(br);

    	ByteArrayInputStream bais = new ByteArrayInputStream(data);		
		InputStreamReader isr = new InputStreamReader(bais);
		br = new BufferedReader(isr);
    	
    	InputSource is = new InputSource(br);
    	
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		UMLModelParserHandler h = new UMLModelParserHandler();
        
    	if( this.parserType.equals(UMLmodel.XMI_POSEIDON)) {
        	h = new PosiedonXMI_ModelParserHandler(modelName);
            parser.parse(is, h);
        } else if( this.parserType.equals(UMLmodel.XMI_MAGICDRAW)) {
        	h = new MagicDrawXMI_ModelParserHandler(modelName);
            parser.parse(is, h);
        }  else if( this.parserType.equals(UMLmodel.XMI_ARGOUML)) {
        	h = new ArgoUMLXMI_ModelParserHandler(modelName);
        	parser.parse(is, h);
        } 
    	
    	if(h.getExceptions().size() != 0 ) {
    		throw new Exception( h.getExceptions().toString() );
    	}
    
    	h.getUmlModel().setSourceData(data);
    	h.getUmlModel().setSourceType(parserType);
    	
    	this.getUmlModels().add(h.getUmlModel());
    	    	
    }

	public ArrayList<UMLmodel> getUmlModels() {
		return umlModels;
	}

	
    
}
