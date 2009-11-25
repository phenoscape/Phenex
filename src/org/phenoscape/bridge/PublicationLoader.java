package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.List;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

public class PublicationLoader {

	/** The db-host system property should contain the name of the database server. */
    public static final String DB_HOST = "db-host";
    /** The db-name system property should contain the name of the database. */
    public static final String DB_NAME = "db-name";
    /** The db-user system property should contain the database username. */
    public static final String DB_USER = "db-user";
    /** The db-password system property should contain the database password. */
    public static final String DB_PASSWORD = "db-password";
    /** The ontology-dir system property should contain the path to a folder with ontologies to be loaded. */
    public static final String PUBLICATION_DIR = "publication-dir";
    
    private Document doc;
    private String docLocation = 
    	"/home/cartik/workspace/OBDAPI/phenoscape/staging/publication_xml/Phenoscape_pubs_A_papers.xml";
    
    private List<Map<String, String>> records;
    private NodeList pubRecordNodes; 
    
    private String authors, pubType, title, pubAbstract, volume, pages, year, pubDate, keywords;
    
    public PublicationLoader(){
    	records = new ArrayList<Map<String, String>>();
    }
    
    private void openXmlDocument() throws ParserConfigurationException, 
    																	SAXException, IOException{
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	doc = docBuilder.parse(new File(docLocation)); 
    	//doc = docBuilder.parse(new File(System.getProperty(PUBLICATION_DIR)));
    }
        
    private void parsePublicationRecords(){
    	pubRecordNodes = doc.getElementsByTagName("record");
    
    	Map<String, String> record =  new HashMap<String, String>();
    	
    	for(int i = 0; i < pubRecordNodes.getLength(); i++){
    		System.out.println("Publication " + i);
    		Node recordNode = pubRecordNodes.item(i);
    		NodeList children = recordNode.getChildNodes();
    		for(int j = 0; j < children.getLength(); j++){
    			Node child = children.item(j);
    			String nodeName = child.getNodeName();
    			if(nodeName.equals("rec-number")){
    				System.out.println("\tRecord number: " + child.getTextContent());
    			}
    			else if(nodeName.equals("ref-type")){
    				System.out.println("\tReference type: " + child.getAttributes().item(0).getTextContent());
    			}
    			else if(nodeName.equals("contributors")){
    				Node authorsNode = child.getChildNodes().item(0);
    				processAuthors(authorsNode);
    			}
    			else if(nodeName.equals("titles")){
    				Node titleNode = child.getChildNodes().item(0);
    				Node secondaryTitleNode = child.getChildNodes().item(1);
    				System.out.println("\tPrimary title: " + titleNode.getTextContent());
    				if(secondaryTitleNode != null)
    					System.out.println("\tSecondary title: " + secondaryTitleNode.getTextContent());
    			}
    			else if(nodeName.equals("pages")){
    				System.out.println("\tPages: " + child.getTextContent());
    			}
    			else if(nodeName.equals("volume")){
    				System.out.println("\tVolume: " + child.getTextContent());
    			}
    			else if(nodeName.equals("keywords")){
    				Node kwNode = child.getChildNodes().item(0);
    				System.out.println("\tKeywords: " + kwNode.getTextContent());
    			}
    			else if(nodeName.equals("dates")){
    				Node yearNode = child.getChildNodes().item(0);
    				System.out.println("\tYear: " + yearNode.getTextContent());
    			}
    			else if(nodeName.equals("abstract")){
    				System.out.println("\tAbstract: " + child.getTextContent());
    			}
    		}
    	}
    }
    
    private void processAuthors(Node authorsNode){
    	String authorListing = "";
    	
    	NodeList listOfAuthorNodes = authorsNode.getChildNodes();
    	int numberOfAuthors = listOfAuthorNodes.getLength();
    	
    	switch(numberOfAuthors){
    		case 1:{
    			authorListing = listOfAuthorNodes.item(0).getTextContent();
    			break;
    		}
    		case 2:{
    			String firstAuthor = listOfAuthorNodes.item(0).getTextContent();
    			String secondAuthor = listOfAuthorNodes.item(1).getTextContent();
    			authorListing += firstAuthor + " and " + secondAuthor;
    			break;
    		}
    		default:{
    			int lastIndex = numberOfAuthors - 1;
    			String lastAuthor = listOfAuthorNodes.item(lastIndex).getTextContent();
    			for(int i = 0; i < numberOfAuthors - 1; i++){
    				Node authorNode = listOfAuthorNodes.item(i);
    				authorListing += authorNode.getTextContent() + ", "; 
    			}    	
    			authorListing = authorListing.substring(0, authorListing.lastIndexOf(","));
    			authorListing += ", and " + lastAuthor;
    		}
    	}
    	System.out.println("\tAuthors: " + authorListing);
    }
    
    public static void main(String[] args){
    	PublicationLoader pl = new PublicationLoader();
    	try{
    		pl.openXmlDocument();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    	pl.parsePublicationRecords();
    }

}
