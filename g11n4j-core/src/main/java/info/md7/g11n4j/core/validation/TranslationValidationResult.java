package info.md7.g11n4j.core.validation;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TranslationValidationResult {

    private final Locale defaultLocale;
    private final Set<String> defaultKeys;
    private final Map<Locale, Set<String>> missingKeysByLocale;
    private final Map<Locale, Set<String>> extraKeysByLocale;
    private final List<String> errors;

    TranslationValidationResult(
            Locale defaultLocale,
            Set<String> defaultKeys,
            Map<Locale, Set<String>> missingKeysByLocale,
            Map<Locale, Set<String>> extraKeysByLocale,
            List<String> errors
    ) {
        this.defaultLocale = defaultLocale;
        this.defaultKeys = defaultKeys;
        this.missingKeysByLocale = missingKeysByLocale;
        this.extraKeysByLocale = extraKeysByLocale;
        this.errors = errors;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public Set<String> getDefaultKeys() {
        return defaultKeys;
    }

    public Map<Locale, Set<String>> getMissingKeysByLocale() {
        return missingKeysByLocale;
    }

    public Map<Locale, Set<String>> getExtraKeysByLocale() {
        return extraKeysByLocale;
    }

    public List<String> getErrors() {
        return errors;
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasMissingKeys() {
        return !missingKeysByLocale.isEmpty();
    }

    public boolean hasExtraKeys() {
        return !extraKeysByLocale.isEmpty();
    }

    public boolean isValid() {
        return !hasErrors() && !hasMissingKeys();
    }

    public String getSummary() {
        int missingLocales = missingKeysByLocale.size();
        int extraLocales = extraKeysByLocale.size();
        int errorCount = errors.size();
        StringBuilder summary = new StringBuilder();
        summary.append("Translation validation summary: ");
        summary.append("errors=").append(errorCount);
        summary.append(", localesMissingKeys=").append(missingLocales);
        summary.append(", localesWithExtraKeys=").append(extraLocales);
        return summary.toString();
    }

    static TranslationValidationResult empty(Locale defaultLocale) {
        return new TranslationValidationResult(
                defaultLocale,
                Collections.emptySet(),
                Collections.emptyMap(),
                Collections.emptyMap(),
                List.of()
        );
    }

    static TranslationValidationResult of(
            Locale defaultLocale,
            Set<String> defaultKeys,
            Map<Locale, Set<String>> missingKeysByLocale,
            Map<Locale, Set<String>> extraKeysByLocale,
            List<String> errors
    ) {
        return new TranslationValidationResult(
                defaultLocale,
                defaultKeys,
                Collections.unmodifiableMap(new LinkedHashMap<>(missingKeysByLocale)),
                Collections.unmodifiableMap(new LinkedHashMap<>(extraKeysByLocale)),
                List.copyOf(errors)
        );
    }
}
