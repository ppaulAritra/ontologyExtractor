package msc.thesis.aritra.main;

import java.util.*;
/**
 * Manages the list of required files and database tables given a set of axioms to generate.
 *
 *
 */
public class RequirementsResolver {
    public final static HashMap<AxiomType, Set<DatabaseTable>> DATABASE_TABLE_DEPENDENCIES =
            new HashMap<AxiomType, Set<DatabaseTable>>();
    static {
        //TODO: move into file?
        addDatabaseDependency(AxiomType.CLASS_SUBSUMPTION_SIMPLE, DatabaseTable.CLASSES_TABLE,
                DatabaseTable.INDIVIDUALS_TABLE);
        addDatabaseDependency(AxiomType.CLASS_SUBSUMPTION_COMPLEX, DatabaseTable.CLASSES_TABLE,
                DatabaseTable.INDIVIDUALS_TABLE);
        addDatabaseDependency(AxiomType.PROPERTY_DOMAIN, DatabaseTable.INDIVIDUALS_TABLE,
                DatabaseTable.PROPERTIES_TABLE,
                DatabaseTable.PROPERTY_TOP_TABLE);

    }
    public static final HashMap<AxiomType, Set<TransactionTable>> TRANSACTION_TABLE_DEPENDENCIES = new HashMap
            <AxiomType, Set<TransactionTable>>();

    static {
        addTransactionDependency(AxiomType.CLASS_SUBSUMPTION_SIMPLE, TransactionTable.CLASS_MEMBERS);
        addTransactionDependency(AxiomType.CLASS_SUBSUMPTION_COMPLEX, TransactionTable.CLASS_MEMBERS);
        addTransactionDependency(AxiomType.PROPERTY_DOMAIN, TransactionTable.PROPERTY_RESTRICTIONS1);
    }

    private Set<AxiomType> activeAxiomTypes;
    private Set<TransactionTable> requiredTransactionTables;
    private Set<DatabaseTable> requiredDatabaseTables;
    public RequirementsResolver(Set<AxiomType> activeAxiomTypes) {
        this.activeAxiomTypes = activeAxiomTypes;

        requiredTransactionTables = getRequiredTransactionTables(activeAxiomTypes);
        requiredDatabaseTables = getRequiredDatabaseTables(activeAxiomTypes);
    }
    /**
     * Returns the set of required transaction tables for the active axiom types.
     *
     * @param activeAxiomTypes axiom types which should be generated
     * @return set of required transaction tables
     */
    private Set<TransactionTable> getRequiredTransactionTables(Set<AxiomType> activeAxiomTypes) {
        HashSet<TransactionTable> requiredTables = new HashSet<TransactionTable>();
        for (AxiomType a : activeAxiomTypes) {
            requiredTables.addAll(TRANSACTION_TABLE_DEPENDENCIES.get(a));
        }

        return requiredTables;
    }

    /**
     * Adds the dependency between the given axiom type and the given database tables.
     * <p/>
     * This means that the given axiom type requires the generation of all provided tables.
     *
     * @param t      axiom type
     * @param tables tables the axiom type depends on
     */
    private static void addDatabaseDependency (AxiomType t, DatabaseTable... tables) {
        List<DatabaseTable> allTables = Arrays.asList(tables);
        if (DATABASE_TABLE_DEPENDENCIES.containsKey(t)) {
            DATABASE_TABLE_DEPENDENCIES.get(t).addAll(allTables);
        }
        else {
            DATABASE_TABLE_DEPENDENCIES.put(t, new HashSet<DatabaseTable>(allTables));
        }
    }
    /**
     * Returns the set of required database tables for the active axiom types.
     *
     * @param activeAxiomTypes axiom types which should be generated
     * @return set of required database tables
     */
    private Set<DatabaseTable> getRequiredDatabaseTables(Set<AxiomType> activeAxiomTypes) {
        HashSet<DatabaseTable> requiredTables = new HashSet<DatabaseTable>();
        for (AxiomType a : activeAxiomTypes) {
            requiredTables.addAll(DATABASE_TABLE_DEPENDENCIES.get(a));
        }

        return requiredTables;
    }
    private static void addTransactionDependency(AxiomType t, TransactionTable... tables) {
        List<TransactionTable> allTables = Arrays.asList(tables);
        if (TRANSACTION_TABLE_DEPENDENCIES.containsKey(t)) {
            TRANSACTION_TABLE_DEPENDENCIES.get(t).addAll(allTables);
        }
        else {
            TRANSACTION_TABLE_DEPENDENCIES.put(t, new HashSet<TransactionTable>(allTables));
        }
    }
    /**
     * Returns true if the given database table is required in the current axiom configuration.
     *
     * @param table table to check whether it is required
     * @return true if table is required, otherwise false
     */
    public boolean isDatabaseTableRequired(DatabaseTable table) {
        return requiredDatabaseTables.contains(table);
    }
    /**
     * Returns true if the given transaction table is required in the current axiom configuration.
     *
     * @param table table to check whether it is required
     * @return true if table is required, otherwise false
     */
    public boolean isTransactionTableRequired(TransactionTable table) {
        return requiredTransactionTables.contains(table);
    }

}
