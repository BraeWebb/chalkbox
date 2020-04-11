package chalkbox2.commands.collectors;

import picocli.CommandLine;
import picocli.CommandLine.Command;

@Command(name = "collectors", sortOptions = false,
        header = {
                ""
        },
        description = {
                "collector commands",
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
public class CollectorsRoot implements Runnable {

    public static void main(String... args) {
        var app = new CollectorsRoot();
        int exitCode = new CommandLine(app).execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }
}
