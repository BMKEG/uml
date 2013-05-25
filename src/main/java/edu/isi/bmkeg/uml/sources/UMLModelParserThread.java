package edu.isi.bmkeg.uml.sources;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;
import org.xml.sax.helpers.DefaultHandler;

import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.parser.ParserThread;

public class UMLModelParserThread extends ParserThread {

    protected UMLmodel umlModel;
	private String parserType;

	public UMLModelParserThread(String type) throws Exception {

		super();

		if (!type.equals(UMLmodel.XMI_MAGICDRAW)
				&& !type.equals(UMLmodel.XMI_POSEIDON)) {
			throw new Exception("Parser Type " + type + ", not recognized");
		}

		this.parserType = type;

	}
	
    public void parseFile(File file) {

        this.setFile(file);

        this.start();

    }

    public Object construct()  {
        File f = this.getFile();

        if (f == null || parserType == null) {
            return null;
        } 
        
        try {
        	UMLModelParserHandler h = null;       
       
            BufferedReader br = new BufferedReader(
                    new FileReader(this.getFile()));

            InputSource is = new InputSource(br);
            SAXParser parser = SAXParserFactory.newInstance().newSAXParser();

            String modelName = f.getName().substring(0,f.getName().lastIndexOf("."));

        	if( this.parserType.equals(UMLmodel.XMI_POSEIDON)) {
	        	h = new PosiedonXMI_ModelParserHandler(modelName);
	            parser.parse(is, h);
	        } else if( this.parserType.equals(UMLmodel.XMI_MAGICDRAW)) {
	        	h = new MagicDrawXMI_ModelParserHandler(modelName);
	            parser.parse(is, h);
	        } 

            this.umlModel = h.getUmlModel();

        } catch (Exception e) {
        	
            e.printStackTrace();
            return new Boolean(false);        
        
        }

        return new Boolean(true);

    }

}