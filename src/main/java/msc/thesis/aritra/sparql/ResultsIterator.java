package msc.thesis.aritra.sparql;

import java.util.Iterator;

public interface ResultsIterator extends Iterator<String> {
    @Override
    boolean hasNext();

    @Override
    String next();

    @Override
    void remove();

    public boolean isFailed();
}
