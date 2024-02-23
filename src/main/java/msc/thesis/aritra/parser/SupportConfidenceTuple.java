package msc.thesis.aritra.parser;

public class SupportConfidenceTuple {
    private Double support;
    private Double confidence;

    public SupportConfidenceTuple(Double support, Double confidence) {
        this.support = support;
        this.confidence = confidence;
    }

    public Double getSupport() {
        return support;
    }

    public Double getConfidence() {
        return confidence;
    }

    public void setSupport(Double support) {
        this.support = support;
    }

    public void setConfidence(Double confidence) {
        this.confidence = confidence;
    }
}