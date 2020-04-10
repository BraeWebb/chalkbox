package chalkbox2.commands;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.IVersionProvider;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;

@Command(name = "chalkbox", sortOptions = false,
        header = {
                " _____  _   _   ___   _      _   __   ______  _____ __   __",
                "/  __ \\| | | | / _ \\ | |    | | / /   | ___ \\|  _  |\\ \\ / /",
                "| /  \\/| |_| |/ /_\\ \\| |    | |/ /    | |_/ /| | | | \\ V /",
                "| |    |  _  ||  _  || |    |    \\    | ___ \\| | | | /   \\",
                "| \\__/\\| | | || | | || |____| |\\  \\   | |_/ /\\ \\_/ // /^\\ \\",
                " \\____/\\_| |_/\\_| |_/\\_____/\\_| \\_/   \\____/  \\___/ \\/   \\/",
                "                                                         ",
                "Copyright: Brae Webb and Emily Bennett",
                ""},
        description = {
                "Demonstrates picocli subcommands parsing and usage help.",
            },
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        mixinStandardHelpOptions = true, version = "subcommand demo 3.0",
        footer = {
                "",
                "",
                "Chalkbox is a MIT licensed project built originally by UQ Students -> UQ Academics.",
                ""
            })
public class Chalkbox implements Runnable {

    public static void main(String... args) {
        var app = new Chalkbox();
        int exitCode = new CommandLine(app)
                .addSubcommand("run", new Pipeline())
                .execute(args);
        System.exit(exitCode);
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }
}
