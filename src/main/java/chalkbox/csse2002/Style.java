package chalkbox.csse2002;

import chalkbox.api.annotations.Asset;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;
import chalkbox.api.collections.Data;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * A class for reading .style files into the JSON submission as grades.
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
 *
 * <p>The above file would translate to the following format (where ... contains
 * the full file above).
 * <pre>
 * "style": {
 *      "raw": "...",
 *      "marks": {
 *          "Naming": 2,
 *          "Structure and layout": 2.5,
 *          "Good OO": 5,
 *          "Commenting": 2
 *      },
 *      "total": {
 *          "Naming": 10,
 *          "Structure and layout": 8,
 *          "Good OO": 5,
 *          "Commenting": 4
 *      }
 * }
 * </pre>
 */
@Processor
public class Style {
    /** Horrific Regex pattern for detecting mark categories
     *
     * <p>Searches for the pattern:
     * <pre>\n ANY_TOKENS COMMA WHITESPACE|NOTHING float|int FORWARD_SLASH int \n</pre>
     */
    private static final Pattern STYLE_PATTERN = Pattern.compile("(?:\\n|^)*([^\\n:]+):\\s*([0-9]*[.]*[0-9])\\s*\\/\\s*([0-9]*)\\n");

    /** Root directory of style files. Directory should include .style files in top level */
    private String styleRoot;
    private String[] categories = new String[]{
            "Naming",
            "Structure and layout",
            "Good OO",
            "Commenting"
    };

    /**
     * Loads the style root configuration from the config file.
     *
     * TODO: This should be converted into a field annotation
     * @param config
     */
    @Asset
    public void loadRoot(Map<String, String> config) {
        styleRoot = config.get("style");
    }

    /**
     * Read a style file into the JSON format described in the {@link Style}
     * documentation.
     *
     * Looks for a file in {@link Style#styleRoot}/{sid}.style
     *
     * @param collection A submission to read.
     * @return The submission.
     */
    @Pipe(stream = "submissions")
    public Collection readStyle(Collection collection) {
        Data data = collection.getResults();
        String sid = (String) data.get("sid");
        String stylePath = styleRoot + File.separator + sid + ".style";

        String style;
        try {
            style = new String(Files.readAllBytes(Paths.get(stylePath)));
        } catch (IOException e) {
            System.err.println("Missing style file for " + sid);
            return collection;
        }
        data.set("style.raw", style);

        Map<String, Float> grades = new HashMap<>();
        for (String category : categories) {
            grades.put(category, null);
        }

        Matcher matcher = STYLE_PATTERN.matcher(style);
        while (matcher.find()) {
            String category = matcher.group(1).trim();
            float result = Float.parseFloat(matcher.group(2).trim());
            grades.put(category, result);
        }

        boolean goodFormat = true;
        for (Map.Entry<String, Float> entry : grades.entrySet()) {
            if (entry.getValue() == null) {
                goodFormat = false;
                break;
            }
            data.set("style.marks." + entry.getKey(), entry.getValue());
        }

        return collection;
    }
}
