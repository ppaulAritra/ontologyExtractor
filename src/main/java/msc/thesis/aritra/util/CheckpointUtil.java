package msc.thesis.aritra.util;

import java.io.File;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

public class CheckpointUtil {
    private File checkpointDir;
    private Set<String> reachedCheckpoints;

    /**
     * Initializes checkpoint logging in the given directory
     * @param checkpointDirName name of directory to log checkpoints to
     */
    public CheckpointUtil(String checkpointDirName) {
        this.checkpointDir = new File(checkpointDirName);
        if (!checkpointDir.exists()) {
            checkpointDir.mkdirs();
        }

        reachedCheckpoints = new HashSet<String>();

        File[] files = checkpointDir.listFiles();

        for (File file : files) {
            if (!file.getName().endsWith(".chkp")) {
                continue;
            }
            reachedCheckpoints.add(file.getName().split("\\.")[0]);
        }
    }

    /**
     * Marks the given checkpoint as reached
     * @param checkpoint name of checkpoint to mark as reached
     */
    public void reach(String checkpoint) {
        File newCheckpointFile = new File(checkpointDir.getAbsolutePath() + File.separator + checkpoint + ".chkp");
        try {
            newCheckpointFile.createNewFile();
            reachedCheckpoints.add(checkpoint);
        }
        catch (IOException e) {
            System.err.println("Unable to create checkpoint: " + checkpoint);
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }
    }

    /**
     * Returns true if the given checkpoint is already reached
     * @param checkpoint name of checkpoint to check if already reached
     * @return true if given checkpoint already reached
     */
    public boolean reached(String checkpoint) {
        return reachedCheckpoints.contains(checkpoint);
    }


    /**
     * Performs the given operation if the checkpoint is not yet reached.
     *
     * If the given checkpoint exists, this method does nothing.
     * Otherwise, it performs the given operation <code>op</code> and creates the corresponding
     * checkpoint if the run method returns true.
     *
     * @param checkpoint name name of checkpoint to check if already reached
     * @param op operation to perform if checkpoint not yet reached
     * @return return value of performed operation or true if checkpoint already reached
     */
    public boolean performCheckpointedOperation(String checkpoint, CheckpointedOperation op) throws Exception {
        if (reached(checkpoint)) {
            return true;
        }

        boolean res = op.run();

        if (res) {
            reach(checkpoint);
        }
        return res;
    }

    public static interface CheckpointedOperation {
        /**
         * Performs the actual operation
         * @return true if operation succeeded and checkpoint should be created, otherwise false.
         */
        public boolean run() throws Exception;
    }
}
