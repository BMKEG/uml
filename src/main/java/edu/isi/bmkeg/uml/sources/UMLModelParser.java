package edu.isi.bmkeg.uml.sources;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.util.HashSet;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.InputSource;

import edu.isi.bmkeg.uml.model.UMLmodel;
import edu.isi.bmkeg.utils.Converters;
import edu.isi.bmkeg.utils.parser.ParserEvent;
import edu.isi.bmkeg.utils.parser.ParserExecutable;

public class UMLModelParser extends ParserExecutable {

	private String parserType;
    private HashSet umlModels = new HashSet();

    public UMLModelParser(java.io.File folder, String type)  { 
    	
    	super(folder, ".xml");
    	
    	this.parserType = type;

    }

    public void parseCompleted(ParserEvent evt) throws Exception {
        Object o = evt.getSource();
        UMLModelParserThread p = (UMLModelParserThread) o;

        try {

            File f = p.getFile();
            System.out.println(f.getPath());

            this.umlModels.add(p.umlModel);

            System.out.print(p.umlModel.debugString());

        } catch (Exception e) {

            e.printStackTrace();

        }

        if (it != null && it.hasNext()) {
            File nextF = this.getNextFile();
            this.parseFile(nextF);
        } else {

            this.fireParserEvent(new ParserEvent(this, true));

        }

    }

    public void parseFile(File f) throws Exception {
        this.p = new UMLModelParserThread(this.parserType);
        this.p.addParseEventListener(this);
        this.p.parseFile(f);

    }

    public void loadAllFilesInDirectory(File dir) throws Exception {

        this.iFolder = dir;

        this.loadAllFiles(true);

        this.parseFilesInQueue();

    }
    
    public void parseUMLModelFile(File f) throws Exception {

        BufferedReader br = new BufferedReader(new FileReader(f));

        InputSource is = new InputSource(br);
        SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
		UMLModelParserHandler h = new UMLModelParserHandler();
        
		String modelName = f.getName().substring(0,f.getName().lastIndexOf("."));
		
    	if( this.parserType.equals(UMLmodel.XMI_POSEIDON)) {
        	h = new PosiedonXMI_ModelParserHandler(modelName);
            parser.parse(is, h);
        } else if( this.parserType.equals(UMLmodel.XMI_MAGICDRAW)) {
        	h = new MagicDrawXMI_ModelParserHandler(modelName);
            parser.parse(is, h);
        } 
    	
    	br.close();
    	
    	byte[] data = Converters.fileContentsToBytesArray(f);
    	h.getUmlModel().setSourceData(data);
    	h.getUmlModel().setSourceType(parserType);
    	
    	this.umlModels.add(h.getUmlModel());
    	    	
    }

    
}
