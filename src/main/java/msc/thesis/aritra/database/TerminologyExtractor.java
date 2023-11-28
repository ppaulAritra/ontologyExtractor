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

}
