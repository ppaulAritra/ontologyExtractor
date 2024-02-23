package msc.thesis.aritra.sparql;


import msc.thesis.aritra.util.Parameter;
import msc.thesis.aritra.util.Settings;
import org.slf4j.Logger;

import java.util.HashSet;

public abstract class SPARQLQueryEngine {
    public static final Logger logger = org.slf4j.LoggerFactory.getLogger(SPARQLQueryEngine.class);

    public static SPARQLQueryEngine createEngine() {
        String endpoint = Settings.getString(Parameter.ENDPOINT);
        String graph = Settings.getString( Parameter.GRAPH );
        int chunk = Settings.getInteger( Parameter.SPARQL_CHUNK );
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);
        System.out.println("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new SPARQLVirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLHTTPQueryEngine(endpoint, graph, chunk);
        }
    }

    public static SPARQLQueryEngine createEngine(String endpoint, String graph, int chunk) {
        logger.debug("Initialized query engine with: " + endpoint + " " + graph + " " + chunk);

        if (endpoint.contains("jdbc:")) {
            return new SPARQLVirtuosoQueryEngine(endpoint, graph);
        }
        else {
            return new SPARQLHTTPQueryEngine(endpoint, graph, chunk);
        }
    }

    public abstract ResultsIterator query(String query, String filter);

    public abstract ResultPairsIterator queryPairs(String query, String filter);
    public abstract ResultPairsIterator queryPairs(String query, HashSet<String> filter);

    public abstract int count(String queryString) throws Exception;

    public abstract SPARQLResult query(String sQuery);
}
