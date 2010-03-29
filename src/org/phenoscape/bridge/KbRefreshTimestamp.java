package org.phenoscape.bridge;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import org.obd.query.Shard;
import org.obd.query.impl.AbstractSQLShard;
import org.obd.query.impl.OBDSQLShard;

public class KbRefreshTimestamp {
	/**
	 * This class counts the phenotypes associated with every gene
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
     * This query retrieves the KB refresh timestamp
     */
    private String geneCountQuery = 
    	"SELECT " +
    	"notes " +
    	"FROM " +
    	"obd_schema_metadata";
    
    private String textFileLocation = System.getProperty(TEXT_FILE_LOC) + "/kbRefreshTimestamp.txt";
    
    /**
     * Constructor initializes the shard and the connection to the database
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public KbRefreshTimestamp() throws SQLException, ClassNotFoundException {
		super();
		this.shard = this.initializeShard();
		this.conn = ((AbstractSQLShard)shard).getConnection();
	
	}
    
    /**
     * @PURPOSE The purpose of this method is to get the timestamp when the KB was last refreshed 
     * and write this to a text file
     * @throws SQLException
     * @throws IOException
     */
    private void writeTimestampToFile() 
		throws SQLException, IOException{
    	
    	Statement pStmt  = conn.createStatement();
    	BufferedWriter bw = new BufferedWriter(new FileWriter(new File(textFileLocation)));
    	String timestamp;

    	ResultSet rs = pStmt.executeQuery(geneCountQuery);
    	while(rs.next()){
    		timestamp = rs.getString(1);
    		timestamp = timestamp.substring(0, timestamp.indexOf("_"));
			bw.write(timestamp);
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
     * @PURPOSE This is the main method, which creates an instance of the KRT and invokes the instance method
     * 'writeTimestampToFile'
     * @param args
     * @throws SQLException
     * @throws ClassNotFoundException
     * @throws IOException
     */
    public static void main(String[] args) throws SQLException, ClassNotFoundException, IOException{
    	KbRefreshTimestamp krt = new KbRefreshTimestamp();
    	krt.writeTimestampToFile();
    }
}
