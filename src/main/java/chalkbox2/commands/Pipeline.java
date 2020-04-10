package chalkbox2.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "Pipeline",
        header = "Runs a pipeline of chalkbox operations",
        description = "Accepts a pipeline file which allows the running of multiple steps to generate an output."
)
public class Pipeline implements Runnable {

    @Option(names = "--silent", description = "Silences formatting.")
    boolean silent;

    @Override
    public void run() {
        header();

        // something needs to parse the pipeline file.
        // something then needs to execute the pipeline file.
        // something then needs to report the current status and progress of the pipeline.
    }

    private void header() {
        if (!this.silent) {
            System.out.println();
            System.out.println("----------------------------------");
            System.out.println("        ChalkBox Pipelines        ");
            System.out.println("----------------------------------");
            System.out.println();
        }
    }
}
