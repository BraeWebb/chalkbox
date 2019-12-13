package chalkbox.csse2002;

import chalkbox.api.annotations.ConfigItem;
import chalkbox.api.annotations.Pipe;
import chalkbox.api.annotations.Prior;
import chalkbox.api.annotations.Processor;
import chalkbox.api.collections.Collection;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.HashMap;
import java.util.Map;

//todo(issue:#21) Refactor this to not be in CSSE2002, should be generic
/**
 * Process to assign allocated tutors based on a CSV containing the allocation.
 *
 * CSV needs to have two columns, the first column should be the student number
 * the second column should have the tutors name. e.g.
 * <pre>
 * s1234567,brae
 * s7654321,emily
 * s1111111,brae
 * s2222222,brae
 * s3333333,emily
 * </pre>
 */
@Processor
public class Allocate {
    private Map<String, String> allocations = new HashMap<>();

    @ConfigItem(description = "path to csv file containing map of sid to tutor")
    public String allocation;

    @Prior
    public void loadAllocations(Map<String, String> config) throws IOException {
        File allocationCSV = new File(allocation);

        if (!allocationCSV.exists()) {
            System.err.println("Unable to load allocation file");
            return;
        }

        for (String line : Files.readAllLines(allocationCSV.toPath())) {
            String[] lineParts = line.split(",");
            allocations.put(lineParts[0].trim(), lineParts[1].trim());
        }
    }

    @Pipe(stream = "submissions")
    public Collection allocate(Collection collection) {
        String sid = collection.getResults().get("sid").toString();
        collection.getResults().set("marker", allocations.getOrDefault(sid, "nobody"));

        return collection;
    }
}
