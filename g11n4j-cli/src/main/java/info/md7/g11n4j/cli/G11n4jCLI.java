package info.md7.g11n4j.cli;

import info.md7.g11n4j.cli.command.ValidateCommand;
import picocli.CommandLine;

@CommandLine.Command(name = "g11n4j-cli",
        mixinStandardHelpOptions = true,
        version = "1.0",
        description = "A command-line tool for managing g11n4j localization resources.",
        subcommands = {ValidateCommand.class})
public class G11n4jCLI implements Runnable {

    @CommandLine.Spec
    CommandLine.Model.CommandSpec spec;

    public void run() {
        spec.commandLine().usage(System.out);
    }

    public static void main(String[] args) {
        int exitCode = new CommandLine(new G11n4jCLI()).execute(args);
        System.exit(exitCode);
    }
}
