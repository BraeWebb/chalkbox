package chalkbox.java.junit;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * Hack in the JUnit pass/fail information
 */
@Processor
public class JUnitHack {
    private static final String SOLUTIONS_ROOT = "junit.solutions";
    private static final String SAMPLE_SOLUTION = "solution";

    @Pipe(stream = "submissions")
    public Collection hack(Collection submission) {
        Data data = submission.getResults();
        /* Determine the baseline amount of tests that pass for the sample solution */
        Map<String, Integer> baseline = new HashMap<>();
        String solutionPath = SOLUTIONS_ROOT + "." + SAMPLE_SOLUTION;
        for (String clazz : data.keys(solutionPath)) {
            String solutionKey = solutionPath + "." + clazz.replace(".", "\\.");

            if (data.get(solutionKey + ".passes") == null) {
                continue;
            }

            int passes = Integer.parseInt(data.get(solutionKey + ".passes").toString());
            baseline.put(clazz, passes);
        }

        /* Flag all the solutions that have a better amount of failing tests */
        for (String solution : data.keys(SOLUTIONS_ROOT)) {
            if (solution.equals(SAMPLE_SOLUTION)) {
                continue;
            }
            String solutionKey = SOLUTIONS_ROOT + "." + solution;

            for (String clazz : data.keys(solutionKey)) {
                String testKey = solutionKey + "." + clazz.replace(".", "\\.");
                data.set(testKey + ".correct", false);

                if (data.get(testKey + ".passes") == null) {
                    continue;
                }

                int passes = Integer.parseInt(data.get(testKey + ".passes").toString());

                if (baseline.containsKey(clazz)) {
                    if (passes < baseline.get(clazz)) {
                        data.set(testKey + ".correct", true);
                    }
                }
            }
        }

        return submission;
    }
}
