package info.md7.g11n4j.cli.validator;

import java.util.List;

public interface MessageValidator {

    boolean validate(String defaultLocale, List<String> compareLocales);
}
