package org.phenoscape.bridge;

import java.io.File;
import java.io.IOException;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Logger;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.obd.query.impl.OBDSQLShard;

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

    private static final String RECORD_STRING = "record";
    private static final String REF_TYPE_STRING = "ref-type";
    private static final String CONTRIBUTORS_STRING = "contributors";
    private static final String TITLES_STRING = "titles";
    private static final String PAGES_STRING = "pages";
    private static final String VOLUME_STRING = "volume";
    private static final String KEYWORDS_STRING = "keywords";
    private static final String DATES_STRING = "dates";
    private static final String ABSTRACT_STRING = "abstract";
    
    private static final String AUTHORS_STRING = "authors";
    private static final String TITLE_STRING = "title";
    private static final String SECONDARY_TITLE_STRING = "secondary-title";
    private static final String PUBLICATION_STRING = "publication";
    private static final String YEAR_STRING = "year";
    
    private String publicationName, authors, pubType, title, secondaryTitle, 
    				pubAbstract, volume, pages, year, keywords;
    
    private Connection conn;
    private Document doc;
    private NodeList pubRecordNodes; 
    private List<String> listOfFullPublicationNames;
    private List<Map<String, String>> listOfRecords;
    private Logger log; 
    
    public PublicationLoader() throws SQLException, ClassNotFoundException{
    	super();
    	listOfFullPublicationNames = new ArrayList<String>();
    	listOfRecords = new ArrayList<Map<String, String>>();
    	conn = this.connectToDatabase();
    	this.compilePublicationNamesInAnnotationMetadataTable();
    	log = Logger.getLogger("PublicationLoaderLog");
    }
    
    private Connection connectToDatabase() throws SQLException, ClassNotFoundException {
        OBDSQLShard obdsql = new OBDSQLShard();
        obdsql.connect("jdbc:postgresql://" + System.getProperty(DB_HOST) + "/" + System.getProperty(DB_NAME), System.getProperty(DB_USER), System.getProperty(DB_PASSWORD));
        return obdsql.getConnection();
    }
    
    private void disconnectFromDatabase() throws SQLException{
    	if(conn != null)
    		conn.close();
    	conn = null;
    }
    
    private void compilePublicationNamesInAnnotationMetadataTable() throws SQLException{
    	String publicationName;
    	String queryForPublicationNames = 
    		"SELECT DISTINCT " +
    		"publication " +
    		"FROM " +
    		"taxon_phenotype_metadata";
    	 
    	Statement stmt = conn.createStatement();
    	ResultSet rs = stmt.executeQuery(queryForPublicationNames);
    	while(rs.next()){
    		publicationName = rs.getString(1);
    		listOfFullPublicationNames.add(publicationName);
    	}
    }
    
    private void openXmlDocument() throws ParserConfigurationException, 
    																	SAXException, IOException{
    	DocumentBuilderFactory docBuilderFactory = DocumentBuilderFactory.newInstance();
    	DocumentBuilder docBuilder = docBuilderFactory.newDocumentBuilder();
    	doc = docBuilder.parse(new File(System.getProperty(PUBLICATION_DIR)));
    }
        
    private void parseXmlDocumentForPublicationInfo(){
    	Map<String, String> mapOfFieldsToValuesForEachRecord;
    	
    	pubRecordNodes = doc.getElementsByTagName(RECORD_STRING);
    	for(int i = 0; i < pubRecordNodes.getLength(); i++){
    		log.fine("Publication " + i);
    		mapOfFieldsToValuesForEachRecord = new HashMap<String, String>();
    		Node recordNode = pubRecordNodes.item(i);
    		NodeList children = recordNode.getChildNodes();
    		
    		for(int j = 0; j < children.getLength(); j++){
    			Node child = children.item(j);
    			String nodeName = child.getNodeName();
    			if(nodeName.equals(REF_TYPE_STRING)){
    				pubType = child.getAttributes().item(0).getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(REF_TYPE_STRING, pubType);
    			}
    			else if(nodeName.equals(CONTRIBUTORS_STRING)){
    				Node authorsNode = child.getChildNodes().item(0);
    				authors = processAuthors(authorsNode);
    				mapOfFieldsToValuesForEachRecord.put(AUTHORS_STRING, authors);
    			}
    			else if(nodeName.equals(TITLES_STRING)){
    				Node titleNode = child.getChildNodes().item(0);
    				Node secondaryTitleNode = child.getChildNodes().item(1);
    				title = titleNode.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(TITLE_STRING, title);
    				publicationName = getFullPublicationName(title);
    				mapOfFieldsToValuesForEachRecord.put(PUBLICATION_STRING, publicationName);
    				if(secondaryTitleNode != null){
    					secondaryTitle = secondaryTitleNode.getTextContent();
    					mapOfFieldsToValuesForEachRecord.put(SECONDARY_TITLE_STRING, secondaryTitle);
    				}
    			}
    			else if(nodeName.equals(PAGES_STRING)){
    				pages = child.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(PAGES_STRING, pages);
    			}
    			else if(nodeName.equals(VOLUME_STRING)){
    				volume = child.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(VOLUME_STRING, volume);
    			}
    			else if(nodeName.equals(KEYWORDS_STRING)){
    				Node kwNode = child.getChildNodes().item(0);
    				keywords = kwNode.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(KEYWORDS_STRING, keywords);
    			}
    			else if(nodeName.equals(DATES_STRING)){
    				Node yearNode = child.getChildNodes().item(0);
    				year = yearNode.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(YEAR_STRING, year);
    			}
    			else if(nodeName.equals(ABSTRACT_STRING)){
    				pubAbstract =  child.getTextContent();
    				mapOfFieldsToValuesForEachRecord.put(ABSTRACT_STRING, pubAbstract);
    			}
    		}
    		log.fine(this.publicationName);
    		listOfRecords.add(mapOfFieldsToValuesForEachRecord);
    	}
    }
    
    private String getFullPublicationName(String title) {
    	String publicationNameNameWithoutFormattingCharacters;
    	for(String fullPublicationName : this.listOfFullPublicationNames){
    		publicationNameNameWithoutFormattingCharacters =
    			this.stripPublicationNameOfFormattingCharacters(fullPublicationName);
    		if(publicationNameNameWithoutFormattingCharacters.contains(title))
    			return fullPublicationName;
    	}
		return title;
	}

    private String stripPublicationNameOfFormattingCharacters(String pubName){
    	String[] arrayOfFormattingCharacters = {"<i>","</i>","<b>","</b>"};
    	String newPubName = pubName;
    	for(String formattingCharacter : arrayOfFormattingCharacters){
    		if(newPubName.contains(formattingCharacter)){
    			newPubName = newPubName.replace(formattingCharacter, "");
    		}
    	}

    	return newPubName;
    }
    
	private String processAuthors(Node authorsNode){
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
    			authorListing += ", and " + lastAuthor;
    		}
    	}
    	return authorListing;
    }
    
	private void insertPublicationRecordsIntoDatabase() throws SQLException{
		String insertSql = 
			"INSERT INTO dw_publication_table (" +
			"publication, reference_type, authors, title, secondary_title, " +
			"volume, pages, keywords, publication_year, abstract" +
			") VALUES (" +
			"?, ?, ?, ?, ?, ?, ?, ?, ?, ?" +
			")";
		PreparedStatement pStmt = conn.prepareStatement(insertSql);
		for(Map<String, String> record : listOfRecords){
			pStmt.setString(1, record.get(PUBLICATION_STRING));
			pStmt.setString(2, record.get(REF_TYPE_STRING));
			pStmt.setString(3, record.get(AUTHORS_STRING));
			pStmt.setString(4, record.get(TITLE_STRING));
			pStmt.setString(5, record.get(SECONDARY_TITLE_STRING));
			pStmt.setString(6, record.get(VOLUME_STRING));
			pStmt.setString(7, record.get(PAGES_STRING));
			pStmt.setString(8, record.get(KEYWORDS_STRING));
			pStmt.setString(9, record.get(YEAR_STRING));
			pStmt.setString(10, record.get(ABSTRACT_STRING));
			
			log.fine(pStmt.toString());
			pStmt.execute();
		}
	}
	
    public static void main(String[] args){
    	try{
    		PublicationLoader pl = new PublicationLoader();
    		pl.openXmlDocument();
        	pl.parseXmlDocumentForPublicationInfo();
        	pl.insertPublicationRecordsIntoDatabase();
        	pl.disconnectFromDatabase();
    	}
    	catch(Exception e){
    		e.printStackTrace();
    	}
    }

}
