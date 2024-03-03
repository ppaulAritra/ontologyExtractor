package msc.thesis.aritra;

import msc.thesis.aritra.database.*;
import msc.thesis.aritra.main.AxiomType;
import msc.thesis.aritra.main.DatabaseTable;
import msc.thesis.aritra.main.RequirementsResolver;
import msc.thesis.aritra.main.TransactionTable;
import msc.thesis.aritra.parser.AssociationRulesParser;
import msc.thesis.aritra.parser.FrequentPropParser;
import msc.thesis.aritra.parser.ParsedRule;
import msc.thesis.aritra.util.CacheTable;
import msc.thesis.aritra.util.CheckpointUtil;
import msc.thesis.aritra.util.FileExtensionFilter;
import msc.thesis.aritra.util.Settings;
import org.openjena.atlas.iterator.Iter;
import org.semanticweb.owlapi.model.IRI;
import org.semanticweb.owlapi.model.OWLAxiom;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.sql.SQLException;
import java.util.*;

public class OntologyExtractor {
    public static Logger log = LoggerFactory.getLogger(OntologyExtractor.class);
    private CheckpointUtil chk;
    private SQLDatabase sqlDatabase;
    private Setup setup;
    private TerminologyExtractor terminologyExtractor;
    private IndividualsExtractor individualsExtractor;
    private TablePrinter tablePrinter;
    private Set<AxiomType> activeAxiomTypes;
    private RequirementsResolver requirementsResolver;
    private AssociationRulesParser parser;
    private FrequentPropParser propParser = new FrequentPropParser();

    /**
     * Initialize ontology extractor to write the created ontology to the file specified in the configuration file.
     *
     * @throws IOException
     * @throws SQLException
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public OntologyExtractor() throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
        this(Settings.getString("ontology"));
    }

    /**
     * Initialize ontology extractor to write created ontology to the given <code>ontologyFile</code>.
     *
     * @param ontologyFile name of file to write generated ontology to
     * @throws IOException
     * @throws SQLException
     */
    public OntologyExtractor(String ontologyFile)
            throws IOException, SQLException {
        if (!Settings.loaded()) {
            Settings.load();
        }
        this.activeAxiomTypes = getSelectedAxiomTypes();
        this.sqlDatabase = SQLDatabase.instance();
        this.setup = new Setup();
        this.tablePrinter = new TablePrinter();
        this.terminologyExtractor = new TerminologyExtractor();
        this.individualsExtractor = new IndividualsExtractor();
        this.chk = new CheckpointUtil(Settings.getString("transaction_tables") + "/checkpoints");
        this.requirementsResolver = new RequirementsResolver(activeAxiomTypes);
        this.parser = new AssociationRulesParser();

    }

    public boolean disconnect() {
        try {
            this.sqlDatabase.close();
            return true;
        } catch (SQLException e) {
            return false;
        }
    }

    public boolean setupDatabase() throws Exception {
        return chk.performCheckpointedOperation("setupdatabase", () -> setup.setupSchema());
    }

    /**
     * Returns the set of all axiom types activated in the configuration.
     *
     * @return set of activated axiom types
     */
    public Set<AxiomType> getSelectedAxiomTypes() {
        HashSet<AxiomType> activeTypes = new HashSet<AxiomType>();
        if (Settings.isAxiomActivated("c_sub_c")) {
            activeTypes.add(AxiomType.CLASS_SUBSUMPTION_SIMPLE);
        }

        if (Settings.isAxiomActivated("c_and_c")) {
            activeTypes.add(AxiomType.CLASS_SUBSUMPTION_COMPLEX);
        }
        if (Settings.isAxiomActivated("exists_p_T")) {
            activeTypes.add(AxiomType.PROPERTY_DOMAIN);
        }
        if (Settings.isAxiomActivated("exists_pi_T")) {
            activeTypes.add(AxiomType.PROPERTY_RANGE);
        }
        if (Settings.isAxiomActivated("exists_p_c")) {
            activeTypes.add(AxiomType.PROPERTY_REQUIRED_FOR_CLASS);
        }
        for (AxiomType v : activeTypes) {
            System.out.println("Enabled: " + v);
        }

        return activeTypes;
    }

    public void createTransactionTables() throws Exception {
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.CLASS_MEMBERS)) {
            chk.performCheckpointedOperation("classmembers", () -> {
                deleteFile(TransactionTable.CLASS_MEMBERS);
                try {
                    tablePrinter.printClassMembers(TransactionTable.CLASS_MEMBERS.getAbsoluteFileName());
                } catch (SQLException e) {
                    log.error("Error creating class members transaction table", e);
                    return false;
                } catch (IOException e) {
                    log.error("Error creating class members transaction table", e);
                    return false;
                }
                return true;
            });
        }
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_RESTRICTIONS1)) {
            chk.performCheckpointedOperation("propertyrestrictions1", () -> {
                deleteFile(TransactionTable.PROPERTY_RESTRICTIONS1);
                try {
                    tablePrinter.printPropertyRestrictions(TransactionTable.PROPERTY_RESTRICTIONS1.getAbsoluteFileName(),
                            0);
                    generateFrequentRoles();
                } catch (SQLException e) {
                    log.error("Error creating property restrictions 1 transaction table", e);
                    return false;
                } catch (IOException e) {
                    log.error("Error creating property restrictions 1 transaction table", e);
                    return false;
                }
                return true;
            });
        }
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.PROPERTY_RESTRICTIONS2)) {
            chk.performCheckpointedOperation("propertyrestrictions2", () -> {
                deleteFile(TransactionTable.PROPERTY_RESTRICTIONS2);
                try {
                    tablePrinter.printPropertyRestrictions(TransactionTable.PROPERTY_RESTRICTIONS2.getAbsoluteFileName(),
                            1);
                } catch (SQLException e) {
                    log.error("Error creating property restrictions 2 transaction table", e);
                    return false;
                } catch (IOException e) {
                    log.error("Error creating property restrictions 2 transaction table", e);
                    return false;
                }
                return true;
            });
        }
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.EXISTS_PROPERTY_MEMBERS)) {
            chk.performCheckpointedOperation("existspropertymembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.EXISTS_PROPERTY_MEMBERS);

                    try {

                        File f = new File(Settings.getString("frequent_props") + "propertyrestrictions1.txt");
                        HashSet<Integer> parsedFrequentElements = propParser.parse(f);
                        List<HashSet<Integer>> frequentElements = getFrequentRoleAndClasses(parsedFrequentElements);
                        tablePrinter.printExistsPropertyMembers(
                                TransactionTable.EXISTS_PROPERTY_MEMBERS.getAbsoluteFileName(),
                                0, frequentElements.get(0), frequentElements.get(1));
                    } catch (SQLException e) {
                        log.error("Error creating exists property members transaction table", e);
                        return false;
                    } catch (IOException e) {
                        log.error("Error creating exists property members transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }
    }

    /**
     * Deletes the transaction table file for the given table. Errors and exceptions are silently ignored!
     *
     * @param table table whose file representation should be deleted
     */
    private void deleteFile(TransactionTable table) {
        File f = new File(table.getAbsoluteFileName());
        f.delete();
        try {
            f.createNewFile();
        } catch (IOException e) {
            log.warn("Unable to create transaction table file '{}'", f.getAbsolutePath());
        }
    }

    public boolean terminologyAcquisition() throws Exception {
        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.CLASSES_TABLE)) {
            chk.performCheckpointedOperation("initclassestable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() throws Exception {
                    terminologyExtractor.initClassesTable();
                    return true;
                }
            });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.INDIVIDUALS_TABLE)) {
            chk.performCheckpointedOperation("initindividualstable", () -> {
                try {
                    individualsExtractor.initIndividualsTable();
                } catch (SQLException e) {
                    return false;
                }
                return true;
            });
        }
        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTIES_TABLE)) {
            chk.performCheckpointedOperation("initpropertiestable",
                    () -> {
                        terminologyExtractor.initPropertiesTable();
                        return true;
                    });
        }

        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.PROPERTY_TOP_TABLE)) {
            chk.performCheckpointedOperation("initpropertytoptable", () -> {
                try {
                    terminologyExtractor.initPropertyTopTable();
                } catch (SQLException e) {
                    return false;
                }
                return true;
            });
        }
        if (requirementsResolver.isDatabaseTableRequired(DatabaseTable.CLASSES_EXISTS_PROPERTY_TABLE)) {
            chk.performCheckpointedOperation("initclassesexistspropertytable",
                    new CheckpointUtil.CheckpointedOperation() {
                        @Override
                        public boolean run() {
                            try {
                                terminologyExtractor.initClassesExistsPropertyTable();
                            } catch (SQLException e) {
                                return false;
                            }
                            return true;
                        }
                    });
        }
        return true;
    }

    /**
     * Starts the external mining process on all relevant transaction table files.
     *
     * @throws IOException
     */
    public void mineAssociationRules() throws IOException {
        TransactionTable[] relevantTransactionTables = this.getRelevantExistingTransactionTables();
        File ruleFile = new File(Settings.getString("association_rules"));
        if (!ruleFile.exists()) {
            ruleFile.mkdirs();
        }
        this.deleteAssociationRuleFiles();
        for (TransactionTable table : relevantTransactionTables) {
            /**
             *    -tr for association rule
             *    -s for minimum support(positive: percentage of transactions negative: absolute number of transactions)
             *    -m for minimum number of item
             *    -n maximum number of item
             *    -c minimum confidence of a rule as a percentage
             *    -v output format for item set information
             *    %20 number of decimal digit. here 20
             */
            ProcessBuilder p = new ProcessBuilder(Settings.getString("apriori"),
                    "-tr", "-s0.05", "-c10", "-m2", "-n2", "-v (%5s, %5c)",
                    table.getAbsoluteFileName(), //transaction table file
                    table.getAbsoluteAssociationRuleFileName() // output file
            );
            p.redirectOutput(ProcessBuilder.Redirect.PIPE);
            Process process = p.start();
            String line;
            BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
            while ((line = input.readLine()) != null) {
                log.debug(line);
            }
            input.close();
        }
    }

    public void generateFrequentRoles() throws IOException {
        String inputFile = Settings.getString("transaction_tables") + "propertyrestrictions1.txt";
        String outPutFile = Settings.getString("frequent_props") + "propertyrestrictions1.txt";
        ProcessBuilder p = new ProcessBuilder(Settings.getString("apriori"),
                "-ts", "-s-100", "-m1", "-n1", "-v (%5s)",
                inputFile, //transaction table file
                outPutFile // output file
        );
        p.redirectOutput(ProcessBuilder.Redirect.PIPE);
        Process process = p.start();
        String line;
        BufferedReader input = new BufferedReader(new InputStreamReader(process.getInputStream()));
        while ((line = input.readLine()) != null) {
            log.debug(line);
        }
        input.close();
    }


    /**
     * Returns an array of all relevant existing transaction table files in the transaction
     * table directory.
     *
     * @return all transaction table files contained the the transaction tables directory and relevant for the
     * current run configuration
     */
    private TransactionTable[] getRelevantExistingTransactionTables() {
        File file = new File(Settings.getString("transaction_tables"));

        HashMap<String, TransactionTable> relevantTables = new HashMap<String, TransactionTable>();
        HashSet<TransactionTable> relevantExistingTables = new HashSet<TransactionTable>();
        for (TransactionTable table : requirementsResolver.getRequiredTransactionTables()) {
            relevantTables.put(table.getFileName(), table);
        }

        for (File tableFile : file.listFiles(new FileExtensionFilter(".txt"))) {
            if (relevantTables.containsKey(tableFile.getName())) {
                relevantExistingTables.add(relevantTables.get(tableFile.getName()));
            }
        }

        return relevantExistingTables.toArray(new TransactionTable[relevantExistingTables.size()]);
    }

    /**
     * Deletes all association rule files which are generated by the current run configuration.
     * This method is used to ensure that no old files interfere with the overall process.
     * Files which would not be generated by the current run configuration are left untouched.
     */
    private void deleteAssociationRuleFiles() {
        File ruleFileDirectory = new File(Settings.getString("association_rules"));

        HashSet<String> relevantRuleFileNames = new HashSet<String>();

        for (TransactionTable table : requirementsResolver.getRequiredTransactionTables()) {
            relevantRuleFileNames.add(table.getAssociationRuleFileName());
        }

        for (File ruleFile : ruleFileDirectory.listFiles(new FileExtensionFilter(".txt"))) {
            if (relevantRuleFileNames.contains(ruleFile.getName())) {
                if (!ruleFile.delete()) {
                    log.warn("Unable to delete existing rule file: {}", ruleFile.getAbsolutePath());
                }
            }
        }
    }

    private void deleteAssociationRuleNameFiles() {
        File ruleFileDirectory = new File(Settings.getString("association_rules_name"));

        HashSet<String> relevantRuleFileNames = new HashSet<String>();

        for (TransactionTable table : requirementsResolver.getRequiredTransactionTables()) {
            relevantRuleFileNames.add(table.getAssociationRuleFileName());
        }

        for (File ruleFile : ruleFileDirectory.listFiles(new FileExtensionFilter(".txt"))) {
            if (relevantRuleFileNames.contains(ruleFile.getName())) {
                if (!ruleFile.delete()) {
                    log.warn("Unable to delete existing rule file: {}", ruleFile.getAbsolutePath());
                }
            }
        }
    }

    public void parseAssociationRules()
            throws IOException, SQLException {
        CacheTable cacheTable = new CacheTable(this.sqlDatabase);
        HashMap<String, String> cachedClassName = cacheTable.getClassNames();
        HashMap<String, String> cachedUriName = cacheTable.getClassUrl();
        HashMap<String, String> cachedPropertyName = cacheTable.getPropertyNames();
        HashMap<String, String> cachedClassNameForExPropClass = cacheTable.getClassAndPropertyNameForExPropClass().get(1);
        HashMap<String, String> cachedPropNameForExPropClass = cacheTable.getClassAndPropertyNameForExPropClass().get(0);

        /* Concept Subsumption: c and c sub c */
        log.debug("Subsumption");
        File f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.CLASS_SUBSUMPTION_COMPLEX).getAbsoluteAssociationRuleFileName());
        System.out.println(f.getAbsolutePath());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        } else if (!activeAxiomTypes.contains(AxiomType.CLASS_SUBSUMPTION_COMPLEX)) {
            log.info("Skipped complex class subsumption because not activated");
        } else {
            List<ParsedRule> axioms = this.parser.parse(f, false);
            Collections.sort(axioms, (o1, o2) -> (o2.getConf().compareTo(o1.getConf())));
            BufferedWriter writer = new BufferedWriter(new FileWriter(TransactionTable.CLASS_MEMBERS.getAbsoluteFileName("association_rules_name")));
            for (ParsedRule pa : axioms) {
                StringBuilder sbLine = new StringBuilder();
                // for 2 antecedent
              /*  sbLine.append(cachedClassName.get(pa.getCons())+" <- "+ cachedClassName.get(pa.getAntecedent1())+", "
                        +cachedClassName.get(pa.getAntecedent2())+ "( "+ pa.getSuppConfTuple().getSupport()+" "+pa.getSuppConfTuple().getConfidence()+ ")");*/
                // for 1 antecedent

                String containsInData = tablePrinter.checkIfSubclass(cachedUriName.get(pa.getCons()), cachedUriName.get(pa.getAntecedent1()));
                sbLine.append(cachedClassName.get(pa.getCons()) + " <- " + cachedClassName.get(pa.getAntecedent1())
                        + "( " + pa.getSuppConfTuple().getSupport() + " " + pa.getSuppConfTuple().getConfidence() + ") " + containsInData);
                if (sbLine.length() > 0) {
//
                    writer.write(sbLine.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        }

        log.debug("Number of Axioms: {}");

        /* Object Property Domain: exists p.T sub c */
        log.debug("Object Property Domain: exists_p_T_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_DOMAIN)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        } else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_DOMAIN)) {
            log.info("Skipped property domain because not activated");
        } else {
            List<ParsedRule> axioms = this.parser.parse(f, false);
            Collections.sort(axioms, (o1, o2) -> (o2.getConf().compareTo(o1.getConf())));
            BufferedWriter writer = new BufferedWriter(new FileWriter(TransactionTable.PROPERTY_RESTRICTIONS1.getAbsoluteFileName("association_rules_name")));
            for (ParsedRule pa : axioms) {
                /*System.out.println(pa.getCons() + " " + cachedClassName.get(pa.getCons()) + " " + cachedPropertyName.get(pa.getCons())
                +" " +pa.getAntecedent1() + " " + cachedClassName.get(pa.getAntecedent1()) + " " + cachedPropertyName.get(pa.getAntecedent1()));*/

                StringBuilder sbLine = new StringBuilder();
                String name1 = (pa.getCons().startsWith("T") || pa.getCons().startsWith("B")) ? cachedPropertyName.get(pa.getCons()) : cachedClassName.get(pa.getCons());
                String name2 = (pa.getAntecedent1().startsWith("T") || pa.getAntecedent1().startsWith("B")) ? cachedPropertyName.get(pa.getAntecedent1()) : cachedClassName.get(pa.getAntecedent1());

                // for 2 ante
              /*  String name3 = pa.getAntecedent2() < 9000 ? cachedClassName.get(pa.getAntecedent2()) : cachedPropertyName.get(pa.getAntecedent2());

                sbLine.append(name1+" <- "+ name2 + ", "+ name3
                        + "( "+ pa.getSuppConfTuple().getSupport()+" "+pa.getSuppConfTuple().getConfidence()+ ")");*/

                // for 1 ante
                sbLine.append(name1 + " <- " + name2
                        + "( " + pa.getSuppConfTuple().getSupport() + " " + pa.getSuppConfTuple().getConfidence() + ")");
                if (sbLine.length() > 0) {
//
                    writer.write(sbLine.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        }
        log.debug("Number of Axioms: {}");

        /* Object Property Range: exists p^i.T sub c */
        log.debug("Object Property Range: exists_pi_T_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_RANGE)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        } else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_RANGE)) {
            log.info("Skipped property required for class because not activated");
        } else {
            List<ParsedRule> axioms = this.parser.parse(f, false);
            Collections.sort(axioms, (o1, o2) -> (o2.getConf().compareTo(o1.getConf())));
            BufferedWriter writer = new BufferedWriter(new FileWriter(TransactionTable.PROPERTY_RESTRICTIONS2.getAbsoluteFileName("association_rules_name")));
            for (ParsedRule pa : axioms) {
                StringBuilder sbLine = new StringBuilder();
                String name1 = (pa.getCons().startsWith("T") || pa.getCons().startsWith("B")) ? cachedPropertyName.get(pa.getCons()) : cachedClassName.get(pa.getCons());
                String name2 = (pa.getAntecedent1().startsWith("T") || pa.getAntecedent1().startsWith("B")) ? cachedPropertyName.get(pa.getAntecedent1()) : cachedClassName.get(pa.getAntecedent1());


                //for 2 ante
//                String name3 = pa.getAntecedent2() < 9000 ? cachedClassName.get(pa.getAntecedent2()) : cachedPropertyName.get(pa.getAntecedent2());
//
//                sbLine.append(name1+" <- "+ name2 + ", "+ name3
//                        + "( "+ pa.getSuppConfTuple().getSupport()+" "+pa.getSuppConfTuple().getConfidence()+ ")");

                // for 1 ante
                sbLine.append(name1 + " <- " + name2
                        + "( " + pa.getSuppConfTuple().getSupport() + " " + pa.getSuppConfTuple().getConfidence() + ")");
                if (sbLine.length() > 0) {
//
                    writer.write(sbLine.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        }
        log.debug("Number of Axioms: {}");
        /* Object Property Domain: exists p.D sub c */
        log.debug("Object Property Domain: exists_p_D_sub_c");
        f = new File(requirementsResolver.getRequiredTransactionTable(AxiomType.PROPERTY_REQUIRED_FOR_CLASS)
                .getAbsoluteAssociationRuleFileName());
        if (!f.exists()) {
            log.warn("Unable to read: '{}'! Skipping...", f.getAbsolutePath());
        } else if (!activeAxiomTypes.contains(AxiomType.PROPERTY_REQUIRED_FOR_CLASS)) {
            log.info("Skipped property domain because not activated");
        } else {
            List<ParsedRule> axioms = this.parser.parse(f, false);
            Collections.sort(axioms, (o1, o2) -> (o2.getConf().compareTo(o1.getConf())));
            BufferedWriter writer = new BufferedWriter(new FileWriter(TransactionTable.EXISTS_PROPERTY_MEMBERS.getAbsoluteFileName("association_rules_name")));
            for (ParsedRule pa : axioms) {
                StringBuilder sbLine = new StringBuilder();

                // for 1 antecedent
                sbLine.append(
                        cachedPropNameForExPropClass.get(pa.getCons()) + "."
                                + cachedClassNameForExPropClass.get(pa.getCons())
                                + " <- "
                                + cachedPropNameForExPropClass.get(pa.getAntecedent1()) + "."
                                + cachedClassNameForExPropClass.get(pa.getAntecedent1())
                                + "( " + pa.getSuppConfTuple().getSupport()
                                + " " + pa.getSuppConfTuple().getConfidence() + ")"
                );
                if (sbLine.length() > 0) {
//
                    writer.write(sbLine.toString());
                    writer.newLine();
                }
            }
            writer.flush();
            writer.close();
        }

    }

    public List<HashSet<Integer>> getFrequentRoleAndClasses(HashSet<Integer> parsedFrequentElements) {
        List<HashSet<Integer>> result = new ArrayList<HashSet<Integer>>();
        HashSet<Integer> frequentProps = new HashSet<>();
        HashSet<Integer> frequentClass = new HashSet<>();
        Iterator<Integer> it = parsedFrequentElements.iterator();
        while (it.hasNext()) {
            Integer i = it.next();
            if (i < 9000)
                frequentClass.add(i);
            else
                frequentProps.add(i);
        }
        System.out.println("*********" + frequentProps.size());
        System.out.println("*********" + frequentClass.size());
        result.add(frequentProps);
        result.add(frequentClass);
        return result;
    }
}