package chalkbox.csse2002;

import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.math.RoundingMode;
import java.text.DecimalFormat;
import java.util.HashMap;
import java.util.Map;

/*
    todo(issue:#21) Refactor this to not be in CSSE2002, should be generic
    We can make this more generic by allowing lua or similar to be embedded
    in the config file.
 */

/**
 * Grader to calculate the grade of CSSE2002 Assignments.
 *
 * TOTAL = STYLE + (PASSING_TESTS / TOTAL_TESTS) * 55 + (JUNIT_GRADE / TOTAL_POSSIBLE) * 20
 *
 * STYLE is the the sum of all categories in read in from the style file, these
 * categories should total 25.
 *
 * PASSING_TESTS is the sum of the passing tests for each test class.
 *
 * TOTAL_TESTS is a fixed number of total tests.
 *
 * JUNIT_GRADE is the amount of solutions where amount passing tests are less
 * than the amount of tests that pass in for the sample solution.
 *
 * TOTAL_POSSIBLE is the amount of JUnit solutions available.
 */
@Processor
public class Grader {

    /* TODO: This is currently a constant in case test totals are calculated
      * correctly by an individual submission
      * the issue here is that if a test isn't run for some reason then we
      * don't know what the total possible tests are so we can't
      * calculate the total amount of tests */
    /** Total amount of possible passing tests */
    private static float TOTAL_TESTS = 146f;

    /**
     * Grade a submission.
     */
    @Pipe(stream = "submissions")
    public Collection grade(Collection submission) {
        Data data = submission.getResults();

        /* For rounding grades */
        DecimalFormat rounder = new DecimalFormat("###");
        rounder.setRoundingMode(RoundingMode.FLOOR);

        /* Calculate the percentage of passing java tests */
        int passingTests = 0;
        for (String test : data.keys("tests")) {
            String testKey = "tests." + test.replace(".", "\\.");

            if (data.get(testKey + ".passes") != null) {
                passingTests += Integer.parseInt(data.get("tests."
                        + test.replace(".", "\\.") + ".passes").toString());
            }
        }
        float testMarks = (passingTests / TOTAL_TESTS) * 45;
        data.set("grades.tests.passing", passingTests);
        data.set("grades.tests.percent", passingTests / TOTAL_TESTS);
        data.set("grades.tests.rawGrade", testMarks);

        /* Calculate the style marks total */
        float styleMarks = 0;
        for (String category : data.keys("style.marks")) {
            styleMarks += Float.parseFloat(data.get("style.marks." + category).toString());
        }

        /* Determine the baseline amount of tests that pass for the sample solution */
        Map<String, Integer> baseline = new HashMap<>();
        for (String clazz : data.keys("junit.solutions.solution")) {
            Object base = data.get("junit.solutions.solution."
                    + clazz.replace(".", "\\.") + ".passes");
            if (base == null) {
                continue;
            }
            int passes = Integer.parseInt(base.toString());
            baseline.put(clazz, passes);
        }

        /* Calculate the amount of solutions with less tests passing than the sample */
        float totalPossible = 0f;
        float junitGrade = 0f;
        for (String solution : data.keys("junit.solutions")) {
            if (solution.equals("output") || solution.equals("compiles")
                    || solution.equals("solution")) {
                continue;
            }

            for (String clazz : data.keys("junit.solutions." + solution)) {
                String testKey = "junit.solutions." + solution + "."
                        + clazz.replace(".", "\\.");

                if (data.get(testKey + ".passes") == null) {
                    continue;
                }

                int passes = Integer.parseInt(data.get(testKey + ".passes").toString());

                if (passes < baseline.get(clazz)) {
                    junitGrade += 1;
                    break;
                }
            }

            totalPossible += 1;
        }

        float junitMarks = totalPossible != 0 ? (junitGrade / totalPossible) * 20 : 0;

        data.set("grades.junit.possible", totalPossible);
        data.set("grades.junit.total", junitGrade);
        data.set("grades.junit.rawGrade", junitMarks);


        /* Cap: If F < 5, then S = 0 */
        if (testMarks < 5) {
            styleMarks = 0;
        }
        /* Cap:  If S > F, then S = F */
        if (styleMarks > testMarks) {
            styleMarks = Math.round(testMarks);
        }
        /* Cap: If J > F, then J = F */
        if (junitMarks > testMarks) {
            junitMarks = testMarks;
        }


        data.set("grades.tests.grade", Math.round(testMarks));
        data.set("grades.style", styleMarks);
        data.set("grades.junit.grade", Math.round(junitMarks));
        data.set("grades.total", Math.round(testMarks) + styleMarks + Math.round(junitMarks));

        System.out.println(data.get("sid") + ": " + data.get("grades"));

        return submission;
    }
}
