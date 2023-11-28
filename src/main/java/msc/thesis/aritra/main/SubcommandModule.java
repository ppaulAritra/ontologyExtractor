package msc.thesis.aritra.main;

import net.sourceforge.argparse4j.inf.Namespace;

public interface SubcommandModule {
    public void runSubcommand(Namespace namespace);
}
