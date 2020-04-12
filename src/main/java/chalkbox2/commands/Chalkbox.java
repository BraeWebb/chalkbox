package chalkbox2.commands;

import chalkbox2.api.Loggable;
import chalkbox2.commands.clang.CRoot;
import chalkbox2.commands.collectors.CollectorsRoot;
import chalkbox2.commands.general.GeneralRoot;
import chalkbox2.commands.java.JavaRoot;
import chalkbox2.commands.python.PythonRoot;
import chalkbox2.commands.util.UtilRoot;
import picocli.CommandLine;
import picocli.CommandLine.Command;

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
                "Assessment assistant for programming subjects.",
            },
        synopsisHeading = "%n",
        descriptionHeading = "%nDescription:%n%n",
        parameterListHeading = "%nParameters:%n",
        optionListHeading = "%nOptions:%n",
        mixinStandardHelpOptions = true, version = "subcommand demo 3.0",
        footer = {
                "",
                "",
                "Chalkbox is a MIT licensed project built originally by",
                "the best UQ Academics I have ever had the pleasure",
                "of working with.",
                "                                    ~ Evan Hughes"
        },
        subcommands = {
                Pipeline.class,
                GeneralRoot.class,
                UtilRoot.class,
                JavaRoot.class,
                PythonRoot.class,
                CRoot.class,
                CollectorsRoot.class
        })
public class Chalkbox implements Runnable, Loggable {

    public static void main(String... args) {
        var app = new Chalkbox();
        System.exit(new CommandLine(app).execute(args));
    }

    @Override
    public void run() {
        CommandLine.usage(this, System.err);
    }
}
