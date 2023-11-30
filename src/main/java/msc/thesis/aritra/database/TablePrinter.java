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
    // caching of property chain ids
    // property cache
    private String[] cachedProperties = null;
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
    public void printPropertyRestrictions(String sOutFile, int iInverse) throws SQLException, IOException {
        // iInverse:
        //      0 --> Domain
        //      1 --> Range
        String properties[] = getProperties();
        BufferedWriter writer = new BufferedWriter(new FileWriter(sOutFile));
        // two hashmaps for each property: domain and range
        HashMap<String, Boolean>[] hmRanges = new HashMap[properties.length];
        HashMap<String, Boolean>[] hmDomains = new HashMap[properties.length];


        for (int i = 0; i < properties.length; i++) {
            String sProp = properties[i];
            System.out.println("printPropertyRestrictions: " + sProp);
            hmRanges[i] = new HashMap<String, Boolean>();
            hmDomains[i] = new HashMap<String, Boolean>();
            ResultPairsIterator iter =
                    queryEngine.queryPairs(m_sparqlFactory.propertyExtensionQuery(sProp), this.individualsFilter);
            while (iter.hasNext()) {
                String sPair[] = iter.next();
                hmDomains[i].put(sPair[0], true);
                hmRanges[i].put(sPair[1], true);
            }
        }
        String sQuery = sqlFactory.selectIndividualsQuery();
        ResultSet results = sqlDatabase.query(sQuery);
        ArrayList<String> chunk = new ArrayList<String>();
        HashMap<String, Integer> hmPropTops = getExistsPropertyTops(iInverse);
        int iDone = 0;
        while (results.next()) {
            int iIndID = results.getInt("id");
            String sIndURI = results.getString("uri");
            StringBuilder sbLine = new StringBuilder();
            boolean bComplex = false;
            for (int i = 0; i < properties.length; i++) {
                if ((iInverse == 0 && hmDomains[i].get(sIndURI) != null)
                        || (iInverse == 1 && hmRanges[i].get(sIndURI) != null)) {
                    String sPropURI = properties[i];
                    int iPropID = hmPropTops.get(sPropURI);
                    sbLine.append(iPropID).append("\t");
                    bComplex = true;
                }
            }
            // if( bComplex )
            //{
            ResultsIterator iter = queryEngine
                    .query(m_sparqlFactory.individualClassesQuery(sIndURI), this.classesFilter);
            while (iter.hasNext()) {
                String sClass = iter.next();
                String sClassID = getClassID(sClass);
                if (sClassID != null) {
                    sbLine.append(sClassID);
                    if (iter.hasNext()) {
                        sbLine.append("\t");
                    }
                }
            }
            //}
            iDone++;
            if (sbLine.length() > 0) {
//                System.out.println("TablePrinter.print: " + sIndURI + " (" + iIndID + ") -> " + sbLine.toString());
                writer.write(sbLine.toString());
                writer.newLine();
            }
        }
        results.getStatement().close();
        System.out.println("TablePrinter.write: " + sOutFile);
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
    public String[] getProperties() throws SQLException {
        if (cachedProperties != null) {
            return cachedProperties;
        }
        ArrayList<String> properties = new ArrayList<String>();
        String sQuery = sqlFactory.selectPropertiesQuery();
        ResultSet results = sqlDatabase.query(sQuery);
        while (results.next()) {
            String propertyUri = results.getString("uri");
            if (!PropertyBlacklist.isBlackListed(propertyUri)) {
                properties.add(propertyUri);
            }
        }
        results.getStatement().close();
        cachedProperties = properties.toArray(new String[properties.size()]);
        return cachedProperties;
    }
    public HashMap<String, Integer> getExistsPropertyTops(int iInverse) throws SQLException {
        HashMap<String, Integer> hmPropTops = new HashMap<String, Integer>();
        String sQuery = sqlFactory.selectPropertyRestrictionsQuery(iInverse);
        ResultSet results = sqlDatabase.query(sQuery);
        while (results.next()) {
            String sPropURI = results.getString("uri");
            int iPropID = results.getInt("id");
            hmPropTops.put(sPropURI, iPropID);
        }
        results.getStatement().close();
        return hmPropTops;
    }


}
