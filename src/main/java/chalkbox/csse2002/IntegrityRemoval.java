package chalkbox.csse2002;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

//todo(issue:#21) Refactor this to not be in CSSE2002, should be generic
/**
 * Flag all the students under integrity investigation with a flag.
 *
 * If a students ID is in the given list of students then it gets
 * integrity:true set in the results data.
 */
@Processor
public class IntegrityRemoval {
    @ConfigItem(key = "cases",
            description = "Path to a file containing student numbers of integrity cases")
    public String listPath;

    private boolean hasError = false;
    private List<String> students = new ArrayList<>();

    @Prior
    public void loadList(Map<String, String> config) {
        try {
            List<String> contents = Files.readAllLines(Paths.get(listPath));
            for (String student : contents) {
                students.add(student.trim());
            }
        } catch (IOException e) {
            hasError = true;
        }
    }

    @Pipe(stream = "submissions")
    public Collection setFlag(Collection submission) {
        if (students.contains((String) submission.getResults().get("sid"))) {
            submission.getResults().set("integrity", true);
        }

        return submission;
    }
}
