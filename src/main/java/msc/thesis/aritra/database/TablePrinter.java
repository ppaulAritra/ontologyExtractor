package msc.thesis.aritra.database;

import hu.ssh.progressbar.ProgressBar;
import hu.ssh.progressbar.console.ConsoleProgressBar;
import msc.thesis.aritra.sparql.ResultPairsIterator;
import msc.thesis.aritra.sparql.ResultsIterator;
import msc.thesis.aritra.sparql.SPARQLFactory;
import msc.thesis.aritra.sparql.SPARQLQueryEngine;
import msc.thesis.aritra.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.*;
import java.util.*;

public class TablePrinter {
    public static final Logger log = LoggerFactory.getLogger(TablePrinter.class);
    // SPARQL
    private static final int SAME_INSTANCE = 2000001;
    private static final int DIFFERENT_INSTANCE = 2000002;
    // DATABASE
    private SPARQLQueryEngine queryEngine;
    private SPARQLFactory m_sparqlFactory;
    // caching of (atomic) class ids
    private SQLDatabase sqlDatabase;
    // caching of property ids
    private SQLFactory sqlFactory;
    private HashMap<String, String> m_hmClass2ID;

    private String classesFilter;
    private String individualsFilter;


    public TablePrinter() throws SQLException, FileNotFoundException, IOException {
        if (!Settings.loaded()) {
            Settings.load();
        }
        this.classesFilter = Settings.getString("classesFilter");
        this.individualsFilter = Settings.getString("individualsFilter");
        queryEngine = SPARQLQueryEngine.createEngine();
        m_sparqlFactory = new SPARQLFactory();
        sqlDatabase = SQLDatabase.instance();
        sqlFactory = new SQLFactory();
    }

/*
* it will print class id for each individual
*
* */
    public void printClassMembers(String sOutFile) throws SQLException, IOException {
        BufferedWriter writer = new BufferedWriter(new FileWriter(sOutFile));
        // read individuals from database
        String sQuery1 = sqlFactory.selectIndividualsQuery();
        ResultSet results = sqlDatabase.query(sQuery1);
        int iDone = 0;
        while (results.next()) {
            String sId = results.getString("id");
            String sInd = results.getString("uri");
            StringBuilder sbLine = new StringBuilder();
            // get individual classes
            ResultsIterator iter = queryEngine.query(m_sparqlFactory.individualClassesQuery(sInd), this.classesFilter);

            while (iter.hasNext()) {
                String sClass = iter.next();
                String sClassID = getClassID(sClass);

                if (sClassID != null) {
                    sbLine.append(sClassID);
                    sbLine.append("\t");
                }

            }
            iDone++;
            if (sbLine.length() > 0) {
               log.info("TablePrinter.print: " + sInd + " (" + sId + ") -> " + sbLine.toString());
                writer.write(sbLine.toString());
                writer.newLine();
            }
        }
        results.getStatement().close();
        System.out.println("TablePrinter.write: " + sOutFile);
        log.info("TablePrinter.write: " + sOutFile);
        writer.flush();
        writer.close();
        System.out.println("TablePrinter: done (" + iDone + ")");
    }

    public String getClassID(String sURI) throws SQLException {
        if (m_hmClass2ID == null) {
            m_hmClass2ID = new HashMap<String, String>();
            ResultSet results = sqlDatabase.query(sqlFactory.selectClassesQuery());
            while (results.next()) {
                String sClass = results.getString("uri");
                String sID = results.getString("id");
                m_hmClass2ID.put(sClass, sID);
            }
            results.getStatement().close();
        }
        return m_hmClass2ID.get(sURI);
    }

}
