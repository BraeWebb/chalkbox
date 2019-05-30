package chalkbox.api.common.java;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * JUnit parser to parse the results of a JUnit test and extract results.
 */
public class JUnitParser {
    private int[] version;
    private boolean[] testResults;
    private int[] time;

    private int fails = 0;
    private int passes = 0;
    private String toString = null;

    private Map<Integer, String> failureMethods = new HashMap<>();
    private Map<Integer, String> errorMessages = new HashMap<>();

    private static final Pattern VERSION_PATTERN = Pattern.compile("JUnit version (\\d*).(\\d*)");
    private static final Pattern TIME_PATTERN = Pattern.compile("Time: (\\d*).(\\d*)");
    private static final Pattern SUCCESS_PATTERN = Pattern.compile("OK \\((\\d*) tests*\\)");
    private static final Pattern FAIL_PATTERN = Pattern.compile("Tests run: (\\d*),  Failures: (\\d*)");
    private static final Pattern TEST_HEADER_PATTERN = Pattern.compile("(\\d*)\\) (.*)\\((.*)\\)");

    /**
     * Get the formatted output of the test failures.
     *
     * @return The formatted output of the test results.
     */
    public String formatOutput() {
        if (toString == null) {
            toString = buildOutput();
        }
        return toString;
    }

    /**
     * Get the formatted output of the test failures.
     *
     * @return The formatted output of the test results.
     */
    private String buildOutput() {
        StringBuilder builder = new StringBuilder();

        /* Combine test failures and test errors */
        for (Map.Entry<Integer, String> failure : failureMethods.entrySet()) {
            builder.append(failure.getValue());
            builder.append(errorMessages.get(failure.getKey())).append(System.lineSeparator());
        }

        return builder.toString();
    }

    /**
     * @return The JUnit version of the execution.
     */
    public String getVersion() {
        return version[0] + "." + version[1];
    }

    /**
     * @return The execution time of the JUnit test.
     */
    public String getTime() {
        return time[0] + "." + time[1];
    }

    /**
     * @return The amount of tests that passed.
     */
    public int getPasses() {
        return passes;
    }

    /**
     * @return The amount of tests that failed.
     */
    public int getFails() {
        return fails;
    }

    /**
     * @return The amount of tests that were executed.
     */
    public int getTotal() {
        return passes + fails;
    }

    /**
     * Parse the input string as a JUnit test result.
     *
     * @param input JUnit execution output.
     * @return A JUnitParser containing the information about the results.
     *
     * @throws IOException If there is an issue reading the string (should never occur)
     * @throws JUnitParseException If there is a format issue with the input
     */
    public static JUnitParser parse(String input)
            throws IOException, JUnitParseException {
        InputStream stream = new ByteArrayInputStream(input.getBytes());
        return parse(stream);
    }

    /**
     * Parse the input stream as a JUnit test result.
     *
     * @param stream Stream of JUnit execution output.
     * @return A JUnitParser containing the information about the results.
     *
     * @throws IOException If there is an issue reading from the stream
     * @throws JUnitParseException If there is a format issue with the input
     */
    public static JUnitParser parse(InputStream stream)
            throws IOException, JUnitParseException {
        BufferedReader in = new BufferedReader(new InputStreamReader(stream));
        JUnitParser test = new JUnitParser();

        /* Used to keep track of the current test that is being parsed */
        boolean parsingTest = false;
        int testNum = 0;
        String testName;

        String line;
        Matcher matcher;
        while ((line = in.readLine()) != null) {
            /* Match the first few header lines of the output */
            if ((matcher = VERSION_PATTERN.matcher(line)).matches()) {
                String resultsLine = in.readLine();
                if (resultsLine == null) {
                    throw new JUnitParseException("Unable to read results line");
                }
                try {
                    test.parseHeader(matcher, resultsLine, in.readLine());
                } catch (NullPointerException e) {
                    throw new JUnitParseException("JUnit died when executing tests - call to System.exit?");
                }
                continue;
            }

            /* Match when all tests pass */
            if ((matcher = SUCCESS_PATTERN.matcher(line)).matches()) {
                test.passes = Integer.parseInt(matcher.group(1));
                test.fails = 0;
            }

            /* Match when some tests pass and some tests fail */
            if ((matcher = FAIL_PATTERN.matcher(line)).matches()) {
                test.passes = Integer.parseInt(matcher.group(1));
                test.fails = Integer.parseInt(matcher.group(2));
                test.passes = test.passes - test.fails;
            }

            /* Match the actual test results output */
            if ((matcher = TEST_HEADER_PATTERN.matcher(line)).matches()) {
                parsingTest = true;
                testNum = Integer.parseInt(matcher.group(1));
                testName = matcher.group(2);
                test.failureMethods.put(testNum, testName);
                test.errorMessages.put(testNum, "");
                continue;
            }
            /* Stop parsing a test when a blank line is reached */
            if (line.isEmpty()) {
                parsingTest = false;
            }
            /* Continue reading a test result */
            if (parsingTest) {
                test.errorMessages.put(testNum,
                        test.errorMessages.get(testNum)
                                + System.lineSeparator() + line);
            }
        }

        return test;
    }

    /**
     * Parse the header of a JUnit test output.
     *
     * <p>An example of this header is given below.
     * <pre>
     * JUnit version 4.12
     * ..E...E.
     * Time: 0.014
     * </pre>
     *
     * @param versionMatcher The Regex matcher that matched the version line
     * @param resultLine The line following the JUnit version, should have the results summary
     * @param timeLine The line following the resultLine, should have the execution time
     */
    private void parseHeader(Matcher versionMatcher,
                             String resultLine, String timeLine) {
        /* Record the JUnit version */
        int major = Integer.parseInt(versionMatcher.group(1));
        int minor = Integer.parseInt(versionMatcher.group(2));
        version = new int[]{major, minor};

        /* Record the summary of the test results for the JUnit execution */
        char[] resultsSummary = resultLine.toCharArray();
        testResults = new boolean[resultsSummary.length];
        for (int i = 0; i < resultsSummary.length; i++) {
            testResults[i] = resultsSummary[i] == '.';
        }

        /* Record how long it took to execute the tests */
        Matcher timeMatcher = TIME_PATTERN.matcher(timeLine);
        if (timeMatcher.matches()) {
            int seconds = Integer.parseInt(timeMatcher.group(1));
            int milliseconds = Integer.parseInt(timeMatcher.group(2));
            time = new int[]{seconds, milliseconds};
        }
    }
}
