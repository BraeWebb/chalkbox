package chalkbox2.commands.java;

import chalkbox2.api.Loggable;
import chalkbox2.api.Submission;
import chalkbox2.components.java.ConformanceComponent;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Command(
        name = "conformance",
        header = "",
        description = {
                ""
        },
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        mixinStandardHelpOptions = true
)
public class Conformance implements Runnable, Loggable {

    @Option(names = "--silent", description = "Silences formatting.")
    boolean silent;

    @Option(names = "--no-interactive", description = "Silences any warning prompts or user input, assumes defaults.")
    boolean noInteraction;

    @Option(names = "--output-format", description = "Output format to save the results.")
    String outputFormat = "json";

    @Option(names = "--output-folder", description = "Output folder to save the results.")
    String outputFolder = "output";

    @Option(names = "--template-folder", description = "Template files to be used.")
    String templateFolder = "templates";

    @Option(names = "--submissions", description = "Folder holding all the submissions.")
    String submissionFolder = "submissions";

    @Option(names = "--limit", description = "<Not Implemented> Limit to a single entry.")
    String limit = "";

    @Option(names = "--limit-file", description = "<Not Implemented> Limit to a list of users in a file.")
    String limitFile = "";

    // Stores the submissions that we should allow
    private Set<String> limited = new HashSet<>();

    @Override
    public void run() {
        header();
        logger().info("Starting conformance checking.");

        try {
            logger().info("Loading limits");
            loadLimit();
        } catch (IOException e) {
            logger().error(e.getMessage());
            logger().fatal("Unable to load limit file.");
        }

        var students = new ArrayList<Submission>();
        var student1 = new Submission();
        student1.setId("s123456");
        students.add(student1);

        ConformanceComponent conformancer = new ConformanceComponent();
        conformancer.setNoInteraction(noInteraction)
                .setTemplateFolder(templateFolder)
                .setSubmissionFolder(submissionFolder);

        try {
            conformancer.init();
        } catch (Exception e) {
            logger().error("Unable to compile the templates for conformance");
            logger().fatal(e.getMessage());
            return;
        }

        Flowable.fromIterable(students)                  // From a source list of submissions
                .distinct()                              // remove duplicates
                .parallel()                              // Multithread it
                .runOn(Schedulers.computation())         // How many threads ( num of cpus )
                .filter(this::available)                 // filter out submissions that dont need to be run
                .map(conformancer::run)                   // work to be done
                .sequential()                            // bring back into a single list
                .blockingSubscribe(System.out::println); // what do we do with that

    }

    /*
        Header - Prints out a Little header to stdout.
     */
    private void header() {
        if (!this.silent) {
            System.out.println();
            System.out.println("----------------------------------");
            System.out.println("         Java Conformance         ");
            System.out.println("----------------------------------");
            System.out.println();
        }
    }

    /*
        LoadLimit - Adds the limit, and the limit file to the limited user list.
        @throws: IOException when unable to read file.
     */
    private void loadLimit() throws IOException {
        if (!limit.isEmpty()) {
            limited.add(limit);
        }
        if (!limitFile.isEmpty()) {
            List<String> lines = Files.readAllLines(Path.of(limitFile));
            limited.addAll(lines);
        }
    }

    private boolean available(Submission submission) {
        if (limited.isEmpty()) {
            return true;
        }
        return limited.contains(submission.getId());
    }
}