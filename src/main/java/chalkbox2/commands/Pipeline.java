package chalkbox2.commands;

import picocli.CommandLine.Command;
import picocli.CommandLine.Option;

@Command(
        name = "Pipeline",
        header = "Runs a pipeline of chalkbox operations",
        description = {
                "Pipelines follow after popular CI/CD flows where a range of steps can be defined with commands.",
                "Chalkbox functions as the runner and performs actions that would usually be run via cli within the",
                " application and report the current status to the user.",
                "",
                "Note: Not all commands are available through Pipelines."
        },
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        mixinStandardHelpOptions = true
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

        // name: "Do a full Conformance"
        // parameters:
        //  classPath: [
        //    "/home/wisebaldone/Projects/csse2002/chalkbox-2019s2/lib/junit/hamcrest-core-1.3.jar",
        //    "/home/wisebaldone/Projects/csse2002/chalkbox-2019s2/lib/junit/junit-4.12.jar"
        //  ]
        //
        //steps:
        //  - name: "Download Submissions"
        //    command: "collector svn"
        //    args:
        //      - base-url: dskfjsdkjfksd.com
        //      - output: submissions
        //
        //  - name: "Compile Submissions"
        //    command: "java compile"
        //    args:
        //      - targets: submissions // target for singular
        //      - classPath: {{ classPath }}
        //
        //  - name: "Generate Conformance Reports"
        //    command: "java conformance"
        //    args:
        //      - classPath: {{ classPath }}
        //      - solution: "./solution"
        //      - submissions: "submissions"
        //
        //  - name: "Save"
        //    command: "output json"
        //    args:
        //      - directory: "./json"
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
