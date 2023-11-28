package msc.thesis.aritra.database;

import msc.thesis.aritra.sparql.Filter;
import msc.thesis.aritra.sparql.ResultPairsIterator;
import msc.thesis.aritra.sparql.ResultsIterator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.HashMap;

public class IndividualsExtractor extends Extractor {
    public static Logger log = LoggerFactory.getLogger(IndividualsExtractor.class);
    public IndividualsExtractor() throws SQLException, FileNotFoundException,
            IOException {
        super();
    }


    public void initIndividualsTable() throws SQLException {
        HashMap<String, String> hmURI2Name = new HashMap<String, String>();
        // read classes from database
        String sQuery1 = sqlFactory.selectClassesQuery();
        ResultSet results = sqlDatabase.query(sQuery1);
        while (results.next()) {
            String sClass = results.getString("uri");
            int iClassID = results.getInt("id");
            String sQuery3 = sparqlFactory.classExtensionQuery(sClass);
            log.info("\n" + sClass + " (" + iClassID + ")");
            log.info("For Individual table:class extension query "+sQuery3);
            ResultsIterator iter = sparqlEngine.query(sQuery3, this.filter.getIndividualsFilter());
            int iClassInd = 0;
            while (iter.hasNext()) {
                iClassInd++;
                String sInd = iter.next();
                hmURI2Name.put(sInd, getLocalName(sInd));
            }
            log.info("Setup.initIndividuals( " + sClass + " ) ... " + iClassInd + " -> " + hmURI2Name.size());
        }
        int id = 0;

        sqlDatabase.setAutoCommit(false);
        for (String sInd : hmURI2Name.keySet()) {
            String sName = hmURI2Name.get(sInd);
            String sQuery2 = sqlFactory.insertIndividualQuery(id, sInd, sName);
            log.info("\nQUERY: " + sQuery2);
            sqlDatabase.execute(sQuery2);
            id++;
            if (id % 1000 == 0) {
                sqlDatabase.commit();
            }
        }
        sqlDatabase.commit();
        sqlDatabase.setAutoCommit(true);
        log.info("done: " + id);
    }


}
