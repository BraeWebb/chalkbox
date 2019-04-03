package chalkbox.api.common;

import java.io.File;
import java.io.IOException;

/**
 * Utility class for executing processes
 */
public class Execution {
    /**
     * Execute a process in a working directory
     *
     * @param working directory to run the process within
     * @param args the command line arguments to execute the process
     * @return the executed process
     * @throws IOException if an issue occurs executing the process
     */
    public static Process runProcess(File working, String... args)
            throws IOException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(working);

        Process process;
        try {
            process = builder.start();
            process.waitFor();
        } catch (InterruptedException e) {
            System.err.println("Program execution interrupted");
            return null;
        }

        return process;
    }

    /**
     * Execute a process in the current working directory
     *
     * @param args the command line arguments to execute the process
     * @return the executed process
     * @throws IOException if an issue occurs executing the process
     */
    public static Process runProcess(String... args) throws IOException {
        return runProcess(new File("."), args);
    }
}
