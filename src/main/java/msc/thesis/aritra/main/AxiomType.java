package msc.thesis.aritra.main;
/**
 * Enumeration of axiom types .
 *
 *
 */
public enum AxiomType {
    /**
     * Class subsumption between two atomic classes
     * A subset B
     */
    CLASS_SUBSUMPTION_SIMPLE("class-subsumption-simple", "Class subsumption between two atomic classes"),
    /**
     * Class subsumption between a class intersection and an atomic class (A \sqcap B \sqsubseteq C)
     * A cap B subset C
     */
    CLASS_SUBSUMPTION_COMPLEX("class-subsumption-complex", "Class subsumption between a class intersection and an " +
            "atomic class (A \\sqcap B \\sqsubseteq C)", true),
    /**
     * Property domain restriction \exists p.T \sqsubseteq D
     */
    PROPERTY_DOMAIN("property-domain", "Property domain restriction \\exists p.T \\sqsubseteq D");

    private boolean hasSecondAntecedent;
    private String name;
    private String description;

    /**
     * Initializes an axiom type which might need parsing of a second antecedent.
     *
     * @param name                name of this axiom type
     * @param hasSecondAntecedent set to true if parsing of second antecedent is required
     */
    private AxiomType(String name, String description, boolean hasSecondAntecedent) {
        this.name = name;
        this.hasSecondAntecedent = hasSecondAntecedent;
    }

    /**
     * Initialize an axiom type with the given name which does not need parsing of a second antecedent.
     * @param name    name of this axiom type
     */
    private AxiomType(String name, String description) {
        this(name, description, false);
    }

    @Override
    public String toString() {
        return name;
    }

}
