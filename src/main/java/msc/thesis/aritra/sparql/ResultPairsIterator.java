package msc.thesis.aritra.sparql;

import java.util.Iterator;


public interface ResultPairsIterator extends Iterator<String[]> {
    boolean hasNext();

    String[] next();

    void remove();
}
