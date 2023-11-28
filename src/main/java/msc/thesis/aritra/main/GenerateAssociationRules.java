package msc.thesis.aritra.main;

import msc.thesis.aritra.OntologyExtractor;
import net.sourceforge.argparse4j.inf.Namespace;
import org.semanticweb.owlapi.model.OWLOntologyCreationException;
import org.semanticweb.owlapi.model.OWLOntologyStorageException;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.sql.SQLException;

public class GenerateAssociationRules implements SubcommandModule{
    private static OntologyExtractor ontologyExtractor;
    public void runSubcommand(Namespace namespace) {
        try {
            ontologyExtractor = new OntologyExtractor();
            ontologyExtractor.setupDatabase();
            ontologyExtractor.terminologyAcquisition();
            ontologyExtractor.createTransactionTables();
            ontologyExtractor.disconnect();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (OWLOntologyCreationException e) {
            e.printStackTrace();
        } catch (OWLOntologyStorageException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (SQLException e) {
            e.printStackTrace();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }
}
