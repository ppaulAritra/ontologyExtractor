package msc.thesis.aritra.parser;

/**
 * Container for axiom data gained by parsing association rule files
 */
public class ParsedRule {
    private int antecedent1;
    private int antecedent2;
    private int cons;
    private SupportConfidenceTuple tuple;

    public ParsedRule(int ante1, int cons, double supp, double conf) {
        this.antecedent1 = ante1;
        this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
    }

    public ParsedRule(int ante1, int ante2, int cons, double supp, double conf) {
        this.antecedent1 = ante1;
        this.antecedent2 = ante2;
        this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
    }

    /**
     * @return the ante1
     */
    public int getAntecedent1() {
        return antecedent1;
    }

    /**
     * @return the ante2
     */
    public int getAntecedent2() {
        return antecedent2;
    }

    /**
     * @return the cons
     */
    public int getCons() {
        return cons;
    }

    /**
     * Returns the support for this axiom. Actually just wrapping {@code SupportConfidenceTuple.getSupport()}
     * @return support for this parsed axiom
     */
    public Double getSupp() {
        return tuple.getSupport();
    }

    /**
     * Returns the confidence for this axiom. Actually just wrapping {@code SupportConfidenceTuple.getConfidence()}
     * @return confidence for this parsed axiom
     */
    public Double getConf() {
        return tuple.getConfidence();
    }

    public SupportConfidenceTuple getSuppConfTuple() {
        return tuple;
    }


    public double getValue() {
        return tuple.getConfidence();
    }


    public void setValue(double value) {
        tuple.setConfidence(value);
    }



    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (o == null || getClass() != o.getClass()) {
            return false;
        }

        ParsedRule that = (ParsedRule) o;

        if (antecedent1 != that.antecedent1) {
            return false;
        }
        if (antecedent2 != that.antecedent2) {
            return false;
        }
        if (cons != that.cons) {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode() {
        int result = antecedent1;
        result = 31 * result + antecedent2;
        result = 31 * result + cons;
        return result;
    }
}
