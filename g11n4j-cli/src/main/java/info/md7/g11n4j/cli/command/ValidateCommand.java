package info.md7.g11n4j.cli.command;

import info.md7.g11n4j.cli.validator.MessageValidator;
import picocli.CommandLine;
import java.util.List;
import java.util.concurrent.Callable;

@CommandLine.Command(name = "validate", description = "Validate message consistency against a default locale")
public class ValidateCommand implements Callable<Integer> {

    @CommandLine.Option(names = {"--path", "-p"}, description = "Path to the directory containing i18n files", required = true)
    String basePath;

    @CommandLine.Option(names = {"--default", "-d"}, description = "The default locale to use as the source of truth (e.g., 'en')", required = true)
    String defaultLocale;

    @CommandLine.Option(names = {"--compare", "-c"}, split = ",", description = "Comma-separated list of locales to compare against the default", required = true)
    List<String> compareLocales;

    @Override
    public Integer call() {
        if (compareLocales.contains(defaultLocale)) {
            System.err.println("The --compare list cannot contain the default locale ('" + defaultLocale + "').");
            return 1;
        }
        MessageValidator validator = new MessageValidator(basePath);
        boolean success = validator.validate(defaultLocale, compareLocales);

        return success ? 0 : 1;
    }
}
