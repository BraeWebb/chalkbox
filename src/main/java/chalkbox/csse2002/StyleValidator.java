package chalkbox.csse2002;

import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

//todo(issue:#21) Refactor this to not be in CSSE2002, should be java
/**
 * A process for validating the format of .style files.
 *
 * <p>The .style file is used for marking style in CSSE2002, an example file
 * is demonstrated below.
 * <pre>
 * Naming: 2/10
 * Possible comments on naming style
 * * Other information
 * Structure and layout: 2.5/8
 * More comments
 *
 * Good OO: 5/5
 * Commenting: 2/4
 * General Comments:
 * Any extra comments should be ignored
 * </pre>
 */
@Processor
public class StyleValidator {

    /** Root directory of style files. Directory should include .style files in top level */
    @ConfigItem(key = "style",
                description = "Root directory of style files. Top level should have only .style files")
    public File styleRoot;

    /** The expected categories separated by a pipe (|) */
    @ConfigItem(description = "The expected style categories separated by a comma")
    public String[] categories;

    /**
     * Read a style file into the JSON format described in the {@link Style}
     * documentation.
     *
     * Looks for a file in {@link StyleValidator#styleRoot}/{sid}.style
     *
     * @param collection A submission to read.
     * @return The submission.
     */
    @Pipe
    public Collection validate(Collection collection) {
        String sid = (String) collection.getResults().get("sid");
        String stylePath = styleRoot + File.separator + sid + ".style";

        Data data = collection.getResults();
        int passingTests = 0;
        for (String test : data.keys("tests")) {
            String testKey = "tests." + test.replace(".", "\\.");

            if (data.get(testKey + ".passes") != null) {
                passingTests += Integer.parseInt(data.get("tests."
                        + test.replace(".", "\\.") + ".passes").toString());
            }
        }
        float testMarks = (passingTests / 77f) * 55;

        if (testMarks < 5) {
            System.err.println(sid);
            return collection;
        }

        /* Attempt to open the style file */
        String style;
        try {
            style = new String(Files.readAllBytes(Paths.get(stylePath)));
        } catch (IOException e) {
            System.err.println("Missing style file for " + sid);
            return collection;
        }

        /* Build a set of all the correctly formatted style categories */
        Set<String> foundCategories = new HashSet<>();
        Matcher matcher = Style.STYLE_PATTERN.matcher(style);
        while (matcher.find()) {
            foundCategories.add(matcher.group(1).trim().toLowerCase());
        }

        Set<String> expectedCategories = new HashSet<>(Arrays.asList(categories));
        /* goodFormat is true if the found categories in a file match the
         * categories expected */
        boolean goodFormat = foundCategories.equals(expectedCategories);
        if (!goodFormat) {
            System.err.print(sid + " is missing categories. Found: "
                    + foundCategories + System.lineSeparator() + style
                    + System.lineSeparator());
        }

        return collection;
    }
}
