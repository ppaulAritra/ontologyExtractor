package msc.thesis.aritra.sparql;

import msc.thesis.aritra.util.Parameter;
import msc.thesis.aritra.util.Settings;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;


public class SPARQLVirtuosoQueryEngine extends SPARQLQueryEngine {

    private final static Logger logger = LoggerFactory.getLogger(SPARQLVirtuosoQueryEngine.class);

    protected String endpointUri;

    protected String relevantGraph;

    private de.uni_mannheim.informatik.dws.dwslib.virtuoso.Query query;


    public SPARQLVirtuosoQueryEngine(String endpoint, String graph) {
        endpointUri = endpoint;
        relevantGraph = graph;

        logger.debug("Initialized query engine with: " + endpointUri + " " + relevantGraph);
        try {
            query = new de.uni_mannheim.informatik.dws.dwslib.virtuoso.Query(endpointUri, false);
        }
        catch (SQLException e) {
            // do this for being compatible with original engine
            //TODO: improve error handling
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }


    public ResultsIterator query(String query, String filter) {
        return new VirtuosoResultsIterator(this, query, filter);
    }

    public ResultPairsIterator queryPairs(String query, String filter) {
        return new VirtuosoResultsPairIterator(this, query, filter);
    }
    public ResultPairsIterator queryPairs(String query, HashSet<String> filter) {
        return new VirtuosoResultsPairIterator(this, query, filter);
    }
    protected List<String> execute(String queryString, String variableName, String filter) throws UnsupportedEncodingException, IOException {
        logger.debug("SPARQLQueryEngine.query: {}", queryString);

        List<String> set = new ArrayList<String>();

        if (relevantGraph != null) {
            queryString = queryString.replace("WHERE", String.format("FROM <%s> WHERE", relevantGraph));
        }

        try {
            ResultSet res = query.sparqlQueryRaw(queryString);
            System.out.println("query result? "+res.next());
            int colId = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals(variableName)) {
                    colId = i + 1;
                    break;
                }
            }
            if (colId == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            while (res.next()) {
                String uri = checkURISyntax(res.getString(colId));
                if (uri != null) {
                    if (filter == null || uri.startsWith(filter)) {
                        set.add(uri);
                    }
                }
            }

            res.getStatement().close();
            res.close();

        }
        catch (SQLException e) {
            throw new IOException(e);
        }

        return set;
    }

    protected List<String[]> execute(String queryString, String variableName1, String variableName2, String filter) throws Exception {
        logger.debug("SPARQLQueryEngine.query: {}", queryString);

        List<String[]> set = new ArrayList<String[]>();

        if (relevantGraph != null) {
            queryString = queryString.replace("WHERE", String.format("FROM <%s> WHERE", relevantGraph));
        }

        try {
            ResultSet res = query.sparqlQueryRaw(queryString);

            int colId1 = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals(variableName1)) {
                    colId1 = i + 1;
                    break;
                }
            }
            if (colId1 == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            int colId2 = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals(variableName2)) {
                    colId2 = i + 1;
                    break;
                }
            }
            if (colId2 == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            while (res.next()) {
                String r1 = res.getString(variableName1);
                String r2 = res.getString(variableName2);
                String uri1 = checkURISyntax(r1);
                String uri2 = checkURISyntax(r2);
                if (uri1 != null && uri2 != null) {
                    String s[] = {uri1, uri2};
                    if (filter == null || (s[0].startsWith(filter) && s[1].startsWith(filter))) {
                        set.add(s);
                    }
                }
            }

            res.getStatement().close();
            res.close();
        }
        catch (SQLException e) {
            throw new IOException(e);
        }

        return set;
    }
    protected List<String[]> execute(String queryString, String variableName1, String variableName2, HashSet<String> filter) throws Exception {
        logger.debug("SPARQLQueryEngine.query: {}", queryString);

        List<String[]> set = new ArrayList<String[]>();

        if (relevantGraph != null) {
            queryString = queryString.replace("WHERE", String.format("FROM <%s> WHERE", relevantGraph));
        }

        try {
            ResultSet res = query.sparqlQueryRaw(queryString);

            int colId1 = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals(variableName1)) {
                    colId1 = i + 1;
                    break;
                }
            }
            if (colId1 == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            int colId2 = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals(variableName2)) {
                    colId2 = i + 1;
                    break;
                }
            }
            if (colId2 == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            while (res.next()) {
                String r1 = res.getString(variableName1);
                String r2 = res.getString(variableName2);
                String uri1 = checkURISyntax(r1);
                String uri2 = checkURISyntax(r2);
                if (uri1 != null && uri2 != null) {
                    String s[] = {uri1, uri2};
                    if (filter == null || (filter.contains(s[0]) || filter.contains(s[1]))) {
                        set.add(s);
                    }
                }
            }

            res.getStatement().close();
            res.close();
        }
        catch (SQLException e) {
            throw new IOException(e);
        }

        return set;
    }
    public int count(String queryString) throws Exception {
        // System.out.println( "QueryEngine.count: "+ queryString +"\n" );
        List<String> set = new ArrayList<String>();

        if (relevantGraph != null) {
            queryString = queryString.replace("WHERE", String.format("FROM <%s> WHERE", relevantGraph));
        }

        try {
            ResultSet res = query.sparqlQueryRaw(queryString);

            int colId = -1;
            for (int i = 0; i < res.getMetaData().getColumnCount(); i++) {
                if (res.getMetaData().getColumnName(i + 1).equals("count")) {
                    colId = i + 1;
                    break;
                }
            }
            if (colId == -1) {
                throw new RuntimeException("Unable to find given variable name in result");
            }

            int count = 0;

            while (res.next()) {
                count = res.getInt(colId);
                break;
            }

            res.getStatement().close();
            res.close();
            return count;
        }
        catch (SQLException e) {
            throw new IOException(e);
        }
    }

    @Override
    public SPARQLResult query(String sQuery) {
        return null;
    }

    protected String checkURISyntax(String sURI) {
        if (sURI == null) {
            return null;
        }
        String s = sURI;
        s = s.replaceAll("'", "_");
        return s;
    }
}



