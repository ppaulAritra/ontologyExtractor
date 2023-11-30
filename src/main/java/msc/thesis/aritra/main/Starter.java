package msc.thesis.aritra.main;
import msc.thesis.aritra.util.Settings;
import net.sourceforge.argparse4j.ArgumentParsers;
import net.sourceforge.argparse4j.inf.*;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.file.Paths;

public class Starter {
    public static void main(String[] args) {
        ArgumentParser parser = ArgumentParsers.newArgumentParser("goldminer")
                .description("Tool for Statistical Schema Induction");
        parser.addArgument("--minercfg").metavar("file").type(String.class)
                .help("File to read miner configuration from")
                .setDefault(System.getProperty("user.dir") + "/miner.properties");
        parser.addArgument("--axiomcfg").metavar("file").type(String.class)
                .help("File to read axiom configuration from")
                .setDefault(System.getProperty("user.dir") + "/axiom.properties");

        Subparsers subparsers = parser.addSubparsers().title("subcommands").description("valid subcommands");
        Subparser generateParser = subparsers.addParser("generate").help("Generate transaction tables");
        generateParser.setDefault("func", new GenerateAssociationRules());

        Subparser configStubParser = subparsers.addParser("genconfig")
                .help("Writes stubs for the configuration files to the files specified" +
                        " by the --axiomcfg and --minercfg parameters or (if not " +
                        "specified) into the current directory");
        configStubParser.setDefault("func", new GenerateConfigStubs());
        configStubParser.setDefault("subparser", "configStub");

        try {
            Namespace n = parser.parseArgs(args);
            if (n.getString("subparser") == null) {
                Settings.load(n.getString("minercfg"), n.getString("axiomcfg"));
            }
            ((SubcommandModule) n.get("func")).runSubcommand(n);
        }
        catch (ArgumentParserException e) {
            parser.handleError(e);
        }
        catch (FileNotFoundException e) {
            System.err.println("Configuration file not found: " + e.getMessage());
            return;
        }
        catch (IOException e) {
            System.err.println("Unable to read configuration file: " + e.getMessage());
            return;
        }
    }
}
