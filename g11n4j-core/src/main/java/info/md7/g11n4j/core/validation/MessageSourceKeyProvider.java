package info.md7.g11n4j.core.validation;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public interface MessageSourceKeyProvider {

    Locale getDefaultLocale();

    List<Locale> getSupportedLocales();

    Map<Locale, Set<String>> getKeysByLocale();
}
