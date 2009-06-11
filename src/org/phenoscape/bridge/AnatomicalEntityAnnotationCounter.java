package org.phenoscape.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Map;

import org.obd.query.Shard;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;

public class AnatomicalEntityAnnotationCounter {

	/**
	 * This class counts the taxon annotations for each anatomical entity
	 * and loads them into a static text file
	 */
	
	/** The db-host system property should contain the name of the database server. */
    public static final String DB_HOST = "db-host";
    /** The db-name system property should contain the name of the database. */
    public static final String DB_NAME = "db-name";
    /** The db-user system property should contain the database username. */
    public static final String DB_USER = "db-user";
    /** The db-password system property should contain the database password. */
    public static final String DB_PASSWORD = "db-password";
    /** The file-loc system property should contain the location where this file will be stored */
    public static final String TEXT_FILE_LOC = "file-loc";

    private Shard shard;
    private Connection conn;
    
    /**
	 * @INPUT - None
     * This query retrieves the number of annotations for every anatomical entity, only 
     * from the Phenoscape side of the project. These counts are returned in order from 
     * the highest to the lowest
     */
     private String annotCountQuery = 
    	"SELECT " +
    	"entity_node.uid AS entity_id, " +
    	"COUNT(*) AS annotation_count " +
    	"FROM " +
    	"node AS taxon_node " +
    	"JOIN (link AS exhibits_link " +
    	"JOIN (link AS inheres_in_link " +
    	"JOIN node AS entity_node " +
    	"ON (inheres_in_link.object_id = entity_node.node_id)) " +
    	"ON (exhibits_link.object_id = inheres_in_link.node_id)) " +
    	"ON (taxon_node.node_id = exhibits_link.node_id AND " +
    	"	taxon_node.source_id = (SELECT node_id FROM node WHERE uid = 'teleost-taxonomy'))  " +
    	"WHERE " +
    	"exhibits_link.is_inferred = 'f' AND " +
    	"inheres_in_link.is_inferred = 'f' AND " +
    	"exhibits_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'PHENOSCAPE:exhibits') AND " +
    	"inheres_in_link.predicate_id = (SELECT node_id FROM node WHERE uid = 'OBO_REL:inheres_in') " +
    	"GROUP BY entity_node.uid " +
    	"ORDER BY annotation_count DESC ";
    
    private String textFileLocation = System.getProperty(TEXT_FILE_LOC) + "/annotationCountByEntity.txt";
    
    /**
     * Constructor initializes the shard and the connection to the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public AnatomicalEntityAnnotationCounter() throws SQLException, ClassNotFoundException {
		super();
		this.shard = this.initializeShard();
		this.conn = ((AbstractSQLShard)shard).getConnection();
	
	}

    /**
     * @PURPOSE The purpose of this method is to get the count of taxon annotations
     * for every anatomical entity and write these counts to a tab delimited text file
     * @throws SQLException
     * @throws IOException
     */
    private void writeAnnotationCountsToFile() 
		throws SQLException, IOException{
    	
    	Statement pStmt  = conn.createStatement();
    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(textFileLocation)));
    	String entityId, line;
    	int count;
    	
    	Map<String, Integer> countsForEntity = new HashMap<String, Integer>();

    	ResultSet rs = pStmt.executeQuery(annotCountQuery);
    	while(rs.next()){
    		entityId = rs.getString(1);
    		count = rs.getInt(2);
    		
    		if(shard.getCompositionalDescription(entityId, false).getGenus() != null)
    			entityId = shard.getCompositionalDescription(entityId, false).getGenus().toString();
    		
    		if(entityId.startsWith("TAO")){
    			if(countsForEntity.containsKey(entityId))
    				count += countsForEntity.get(entityId);
    			countsForEntity.put(entityId, count);
    		}
    	}
    	for(String key : countsForEntity.keySet()){
			line = key + "\t\t" + shard.getNode(key).getLabel() + "\t\t" + countsForEntity.get(key) + "\n";
			bw.write(line);
		}
    	bw.flush();
    	bw.close();
	}
	
	/**
     * This method connects the shard to the database given the systems
     * parameters for DB location, name, DB username and DB password
     * @return
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    private Shard initializeShard() throws SQLException, ClassNotFoundException {
        OBDSQLShard obdsql = new OBDSQLShard();
        obdsql.connect("jdbc:postgresql://" + System.getProperty(DB_HOST) + "/" + System.getProperty(DB_NAME), System.getProperty(DB_USER), System.getProperty(DB_PASSWORD));
        return obdsql;
    }

    /**
     * @PURPOSE This is the main method, which creates an instance of the AEAC and invokes the instance method
     * 'writeAnnotationCounts'
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException{
    	AnatomicalEntityAnnotationCounter aeac = new AnatomicalEntityAnnotationCounter();
    	aeac.writeAnnotationCountsToFile();
    }
}

