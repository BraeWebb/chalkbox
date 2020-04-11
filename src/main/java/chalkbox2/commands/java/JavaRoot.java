package chalkbox2.commands.java;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "java", sortOptions = false,
        header = {
                ""
        },
        description = {
                "Java commands",
        },
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        mixinStandardHelpOptions = true,
        footer = {
                "",
                "",
                "Chalkbox is a MIT licensed project built originally by UQ Students -> UQ Academics.",
                ""
        },
        subcommands = {
            Conformance.class
        })
public class JavaRoot implements Runnable {

    public static void main(String... args) {
        var app = new JavaRoot();
        int exitCode = new CommandLine(app).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }
}