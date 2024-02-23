package msc.thesis.aritra.parser;

public class ParsedProperties {
    private int propId;
    private Double support;

    public ParsedProperties(int propId, Double support) {
        this.propId = propId;
        this.support = support;
    }

    public int getPropId() {
        return propId;
    }

    public Double getSupport() {
        return support;
    }
}
