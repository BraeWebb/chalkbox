package chalkbox2.commands.java;

import chalkbox2.api.Loggable;
import chalkbox2.components.java.ConformanceComponent;
import io.reactivex.rxjava3.core.Flowable;
import io.reactivex.rxjava3.schedulers.Schedulers;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

import java.util.ArrayList;

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
    String limit;

    @Option(names = "--limit-file", description = "<Not Implemented> Limit to a list of users in a file.")
    String limitFile;

    @Override
    public void run() {
        header();
        logger().info("Starting conformance checking.");

        // todo replace with an input file or scanning a folder
        // replace with a class instead of a strings
        var students = new ArrayList<String>();
        students.add("s123456");
        students.add("s123456");
        students.add("s123456");
        students.add("s999999");


        ConformanceComponent conformancer = new ConformanceComponent();
        try {
            conformancer.init();
        } catch (Exception e) {
            e.printStackTrace();
        }

        Flowable.fromIterable(students)                  // From a source list of submissions
                .distinct()                              // remove duplicates
                .parallel()                              // Multithread it
                .runOn(Schedulers.computation())         // How many threads ( num of cpus )
                .filter(this::available)                 // filter out submissions that dont need to be run
                .map(v -> v + "hello")                   // work to be done
                .sequential()                            // bring back into a single list
                .blockingSubscribe(System.out::println); // what do we do with that

    }

    private void header() {
        if (!this.silent) {
            System.out.println();
            System.out.println("----------------------------------");
            System.out.println("         Java Conformance         ");
            System.out.println("----------------------------------");
            System.out.println();
        }
    }

    private boolean available(String submission) {
        // replace with the creation of a set from the limit file and limit options
        return "s999999".equals(submission);
    }
}