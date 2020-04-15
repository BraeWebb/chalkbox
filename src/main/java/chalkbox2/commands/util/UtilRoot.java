package chalkbox2.commands.util;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "util", sortOptions = false,
        header = {
                ""
        },
        description = {
                "util commands",
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
        })
public class UtilRoot implements Runnable {

    public static void main(String... args) {
        var app = new UtilRoot();
        int exitCode = new CommandLine(app).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }
}