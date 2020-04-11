package chalkbox2.commands.java;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

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
public class Conformance implements Runnable {
    private static final Logger logger = LogManager.getLogger(Conformance.class.getName());

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

    @Option(names = "--limit", description = "Limit to a single entry.")
    String limit;

    @Option(names = "--limit-file", description = "Limit to a list of users in a file.")
    String limitFile;

    @Override
    public void run() {
        header();
        logger.info("Starting conformance checking.");
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
}