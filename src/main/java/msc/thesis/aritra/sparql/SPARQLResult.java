package msc.thesis.aritra.sparql;

import java.util.ArrayList;
import java.util.List;

public class SPARQLResult {
    private SPARQLHTTPQueryEngine m_engine;

    private String m_sQuery;

    private int m_iMaxChunkSize;

    private List<String> m_nextChunk;

    private int iNext = 0;

    private int m_iOffset = 0;

    private String filter;

    private boolean failed;
    protected SPARQLResult(SPARQLHTTPQueryEngine engine, String sQuery){
        m_sQuery = sQuery;
        m_engine = engine;
        m_nextChunk = new ArrayList<String>();
        m_iMaxChunkSize = m_engine.getChunkSize();
        this.failed = false;
    }
    public boolean existInData(){
        try{
            boolean b = m_engine.executeAsk(m_sQuery);
            System.out.println(b);
            return b;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
