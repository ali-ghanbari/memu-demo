package edu.iastate.memo.commons.relational;

/**
 * @author Ali Ghanbari (alig@iastate.edu)
 */
public class Solver {
    public static Process runBDDBDDB(final String dlogFileName, boolean async) throws Exception {
        final String[] cmd = {
                "java",
                "-Xmx16G",
                "-jar",
                "bddbddb-full.jar",
                dlogFileName
        };
        final ProcessBuilder pb = new ProcessBuilder(cmd).inheritIO();
        final Process process = pb.start();
        if (!async) {
            process.waitFor();
        }
        return process;
    }
}