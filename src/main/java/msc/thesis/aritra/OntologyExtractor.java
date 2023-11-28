package msc.thesis.aritra;

import msc.thesis.aritra.database.*;
import msc.thesis.aritra.main.AxiomType;
import msc.thesis.aritra.main.DatabaseTable;
import msc.thesis.aritra.main.RequirementsResolver;
import msc.thesis.aritra.main.TransactionTable;
import msc.thesis.aritra.util.CheckpointUtil;
import msc.thesis.aritra.util.FileExtensionFilter;
import msc.thesis.aritra.util.Settings;
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
     * @throws OWLOntologyCreationException
     * @throws OWLOntologyStorageException
     */
    public OntologyExtractor(String ontologyFile)
            throws IOException, SQLException, OWLOntologyCreationException,
            OWLOntologyStorageException {
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
    }

    public boolean disconnect() {
        try {
            this.sqlDatabase.close();
            return true;
        }
        catch (SQLException e) {
            return false;
        }
    }

    public boolean setupDatabase() throws Exception {
        return chk.performCheckpointedOperation("setupdatabase", new CheckpointUtil.CheckpointedOperation() {
            @Override
            public boolean run() {
                return setup.setupSchema();
            }
        });
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
            chk.performCheckpointedOperation("initindividualstable", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    try {
                        individualsExtractor.initIndividualsTable();
                    }
                    catch (SQLException e) {
                        return false;
                    }
                    return true;
                }
            });
        }

        return true;
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


        if (Settings.isAxiomActivated("exists_p_T_sub_c")) {
            activeTypes.add(AxiomType.PROPERTY_DOMAIN);
        }

        for (AxiomType v : activeTypes) {
            System.out.println("Enabled: " + v);
        }

        return activeTypes;
    }

    public void createTransactionTables() throws Exception {
        if (requirementsResolver.isTransactionTableRequired(TransactionTable.CLASS_MEMBERS)) {
            chk.performCheckpointedOperation("classmembers", new CheckpointUtil.CheckpointedOperation() {
                @Override
                public boolean run() {
                    deleteFile(TransactionTable.CLASS_MEMBERS);
                    try {
                        tablePrinter.printClassMembers(TransactionTable.CLASS_MEMBERS.getAbsoluteFileName());
                    }
                    catch (SQLException e) {
                        log.error("Error creating class members transaction table", e);
                        return false;
                    }
                    catch (IOException e) {
                        log.error("Error creating class members transaction table", e);
                        return false;
                    }
                    return true;
                }
            });
        }

    }

    /**
     * Deletes the transaction table file for the given table. Errors and exceptions are silently ignored!
     * @param table table whose file representation should be deleted
     */
    private void deleteFile(TransactionTable table) {
        File f = new File(table.getAbsoluteFileName());
        f.delete();
        try {
            f.createNewFile();
        }
        catch (IOException e) {
            log.warn("Unable to create transaction table file '{}'", f.getAbsolutePath());
        }
    }


}