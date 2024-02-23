package msc.thesis.aritra.sparql;

import java.util.Collection;
import java.util.Iterator;
import java.util.stream.StreamSupport;

public interface ResultsIterator extends Iterator<String> {
    @Override
    boolean hasNext();

    @Override
    String next();

    @Override
    void remove();

    public boolean isFailed();


}
