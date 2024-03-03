package msc.thesis.aritra.parser;

/**
 * Container for axiom data gained by parsing association rule files
 */
public class ParsedRule {
    private String antecedent1;
    private String antecedent2;
    private String cons;
    private SupportConfidenceTuple tuple;

    public ParsedRule(String ante1, String cons, double supp, double conf) {
        this.antecedent1 = ante1;
        this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
    }

    public ParsedRule(String ante1, String ante2, String cons, double supp, double conf) {
        this.antecedent1 = ante1;
        this.antecedent2 = ante2;
        this.cons = cons;
        this.tuple = new SupportConfidenceTuple(supp, conf);
    }

    /**
     * @return the ante1
     */
    public String getAntecedent1() {
        return antecedent1;
    }

    /**
     * @return the ante2
     */
    public String getAntecedent2() {
        return antecedent2;
    }

    /**
     * @return the cons
     */
    public String getCons() {
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
        int result = 1;

        return result;
    }
}
