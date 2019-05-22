package chalkbox.api.common;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

/**
 * Utility class for executing processes
 */
public class Execution {
    /**
     * Execute a process in a working directory
     *
     * @param working directory to run the process within
     * @param timeout timeout for the process in miliseconds
     * @param args the command line arguments to execute the process
     * @return the executed process
     * @throws IOException if an issue occurs executing the process
     */
    public static Process runProcess(File working, int timeout, String... args)
            throws IOException, TimeoutException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.directory(working);

        return run(builder, timeout);
    }

    /**
     * Execute a process in a working directory with a set of environment variables
     *
     * @param environment environment variables
     * @param timeout timeout for the process in miliseconds
     * @param args the command line arguments to execute the process
     * @return the executed process
     * @throws IOException if an issue occurs executing the process
     */
    public static Process runProcess(Map<String, String> environment,
                                     int timeout, String... args)
            throws IOException, TimeoutException {
        ProcessBuilder builder = new ProcessBuilder(args);
        builder.environment().putAll(environment);

        return run(builder, timeout);
    }

    private static Process run(ProcessBuilder builder, int timeout)
            throws IOException, TimeoutException {
        Process process;
        try {
            process = builder.start();
            if (!process.waitFor(timeout, TimeUnit.MILLISECONDS)) {
                process.destroy();
                throw new TimeoutException();
            }
        } catch (InterruptedException e) {
            System.err.println("Program execution interrupted");
            return null;
        }

        return process;
    }

    /**
     * Execute a process in the current working directory
     *
     * @param timeout timeout for the process in miliseconds
     * @param args the command line arguments to execute the process
     * @return the executed process
     * @throws IOException if an issue occurs executing the process
     */
    public static Process runProcess(int timeout, String... args)
            throws IOException, TimeoutException {
        return runProcess(new File("."), timeout, args);
    }

    /**
     * Consume an input stream into a string.
     *
     * @param stream The input stream to consume.
     * @return The resulting string of the entire input stream.
     */
    public static String readStream(InputStream stream) {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        String line;

        StringBuilder result = new StringBuilder();
        try {
            while ((line = in.readLine()) != null) {
                result.append(line).append(System.lineSeparator());
            }
        } catch (IOException e) {
            return null;
        }

        return result.toString();
    }
}
