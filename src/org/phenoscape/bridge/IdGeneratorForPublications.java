package org.phenoscape.bridge;

import java.io.IOException;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class IdGeneratorForPublications {

    public static final String PUBLICATION_DIR = 
    	"https://phenoscape.svn.sourceforge.net/svnroot/phenoscape/trunk/data/publications/Phenoscape_pubs_A_papers.xml";

    private static final String RECORD_STRING = "record";
    private static final String PUBLICATION_ID = "ID";
    
    private static final String NAMESPACE_STRING = "PUBLICATION:";
    
    private Document doc;
    private NodeList pubRecordNodes; 
    private Logger log; 
    
    public IdGeneratorForPublications(){
    	super();

    	log = Logger.getLogger("PublicationIdGeneratorLog");
    }
    
    private void openXmlDocument() throws ParserConfigurationException, 
		SAXException, IOException{
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	doc = docBuilder.parse(PUBLICATION_DIR);
    }
	
    private void parseXmlDocumentAndAddGeneratedIdToEachRecord(){
    	String generatedID; 
		pubRecordNodes = doc.getElementsByTagName(RECORD_STRING);
		for(int i = 0; i < pubRecordNodes.getLength(); i++){
			generatedID = generateIdForPublication(i);
			log.fine(generatedID);
			Node recordNode = pubRecordNodes.item(i);
			Node idNode = doc.createAttribute(PUBLICATION_ID);
			idNode.setNodeValue(generatedID);
			recordNode.appendChild(idNode);
    	}
    }

	private String generateIdForPublication(int i) {
		String id = i + "";
		int len = id.length();
		while(len < 7){
			++len;
			id = "0" + id;
		}
		
		return NAMESPACE_STRING + id;
	}
    
	/* ..TODO
	private void exportNewXmlDocument(){
		
	}
	*/
	
	 public static void main(String[] args){
	    	try{
	    		IdGeneratorForPublications idGen = new IdGeneratorForPublications();
	    		idGen.openXmlDocument();
	        	idGen.parseXmlDocumentAndAddGeneratedIdToEachRecord();
	    	}
	    	catch(Exception e){
	    		e.printStackTrace();
	    	}
	    }
}
