package msc.thesis.aritra.database;




import msc.thesis.aritra.sparql.ResultsIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class TerminologyExtractor extends Extractor {
	public static Logger log = LoggerFactory.getLogger(TerminologyExtractor.class);
	private int id;
	
	public TerminologyExtractor() throws SQLException, FileNotFoundException,
			IOException {
		super();
		this.id = 1;
	}


	public void initClassesTable() throws Exception {
		String query = sparqlFactory.classesQuery();
		log.info( "Sparql class table query "+query );
		log.info( "**********************  " );
		ResultsIterator iter = sparqlEngine.query( query, this.filter.getClassesFilter() );
		log.info( "Sparql class table iterator failed?  "+iter.isFailed() );
		log.info( "Sparql class table iterator has next?  "+iter.hasNext() );
		int id = 0;
        sqlDatabase.setAutoCommit(false);
		while( iter.hasNext() && !iter.isFailed() )
		{

			String sClass = iter.next();
			log.info( "Sparql class table query result "+sClass );
			String sName = getLocalName( sClass );
			String sCountClassIndQuery = sparqlFactory.classExtensionSizeQuery( sClass );
			int iSize = sparqlEngine.count( sCountClassIndQuery );
			log.info( sClass +" ... " );
			String sQuery = sqlFactory.insertClassQuery( this.id++, sClass, sName, iSize );
			sqlDatabase.execute( sQuery );
            if (id % 1000 == 0) {
                sqlDatabase.commit();
            }
        }
        sqlDatabase.setAutoCommit(true);
		log.info( "done: "+ id );
	}
	public void initPropertiesTable() {
		String sQuery1 = sparqlFactory.propertiesQuery();
		log.info( "Sparql properties table query "+sQuery1 );
		ResultsIterator iter = sparqlEngine.query( sQuery1, this.filter.getClassesFilter() );
		sqlDatabase.setAutoCommit(false);
		while( iter.hasNext() )
		{

			String sProp = iter.next();
			String sName = getLocalName( sProp );
			System.out.println( "Sparql property result "+sProp );
			String sQuery2 = sqlFactory.insertPropertyQuery( this.id++, sProp, sName );
			this.id = this.id + 2;
			sqlDatabase.execute( sQuery2 );
			if (this.id % 1001 == 0 || this.id % 1000 == 500) {
				sqlDatabase.commit();
			}
		}
		sqlDatabase.setAutoCommit(true);
		System.out.println( "done: "+ this.id );
	}
	public void initPropertyTopTable() throws SQLException {
		String sQuery = sqlFactory.selectPropertiesQuery();
		System.out.println( "***Sparql property top table query*** "+sQuery );
		ResultSet results = sqlDatabase.query( sQuery );
		sqlDatabase.setAutoCommit(false);
		while( results.next() )
		{
			String sPropURI = results.getString( "uri" );
			String sPropName = results.getString( "name" );
			int iPropID = results.getInt( "id" );
			int iTopID = iPropID + 1000;
			int iInvTopID = iPropID + 2000;
			String sInsert1 = sqlFactory.insertPropertyTopQuery( this.id++, 0, sPropURI, sPropName );
			String sInsert2 = sqlFactory.insertPropertyTopQuery( this.id++, 1, sPropURI, sPropName );
			sqlDatabase.execute( sInsert1 );
			sqlDatabase.execute( sInsert2 );
			if (iPropID % 1001 == 0) {
				sqlDatabase.commit();
			}
		}
		sqlDatabase.commit();
		sqlDatabase.setAutoCommit(true);
		System.out.println( "Setup.initPropertyTopTable: done" );
	}
}
