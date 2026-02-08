package info.md7.g11n4j.core.validation;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public final class TranslationValidator {

    private TranslationValidator() {
    }

    public static TranslationValidationResult validate(MessageSourceKeyProvider provider) {
        if (provider == null) {
            return TranslationValidationResult.empty(Locale.getDefault());
        }
        return validate(provider.getDefaultLocale(), provider.getKeysByLocale(), provider.getSupportedLocales());
    }

    public static TranslationValidationResult validate(
            Locale defaultLocale,
            Map<Locale, Set<String>> keysByLocale,
            Collection<Locale> localesToCompare
    ) {
        if (defaultLocale == null) {
            defaultLocale = Locale.getDefault();
        }
        if (keysByLocale == null || keysByLocale.isEmpty()) {
            return TranslationValidationResult.of(
                    defaultLocale,
                    Set.of(),
                    Map.of(),
                    Map.of(),
                    List.of("No translation keys provided for validation.")
            );
        }

        Set<String> defaultKeys = keysByLocale.getOrDefault(defaultLocale, Set.of());
        List<String> errors = new ArrayList<>();
        if (defaultKeys.isEmpty()) {
            errors.add("Default locale has no translation keys: " + defaultLocale);
        }

        Map<Locale, Set<String>> missingByLocale = new LinkedHashMap<>();
        Map<Locale, Set<String>> extraByLocale = new LinkedHashMap<>();

        Collection<Locale> locales = (localesToCompare == null || localesToCompare.isEmpty())
                ? keysByLocale.keySet()
                : localesToCompare;

        for (Locale locale : locales) {
            if (locale == null || locale.equals(defaultLocale)) {
                continue;
            }
            Set<String> localeKeys = keysByLocale.getOrDefault(locale, Set.of());

            if (!defaultKeys.isEmpty()) {
                Set<String> missing = new HashSet<>(defaultKeys);
                missing.removeAll(localeKeys);
                if (!missing.isEmpty()) {
                    missingByLocale.put(locale, Set.copyOf(missing));
                }
            }

            if (!localeKeys.isEmpty()) {
                Set<String> extra = new HashSet<>(localeKeys);
                extra.removeAll(defaultKeys);
                if (!extra.isEmpty()) {
                    extraByLocale.put(locale, Set.copyOf(extra));
                }
            }
        }

        return TranslationValidationResult.of(
                defaultLocale,
                Set.copyOf(defaultKeys),
                missingByLocale,
                extraByLocale,
                errors
        );
    }

    public static TranslationValidationResult validate(
            Locale defaultLocale,
            Map<Locale, Set<String>> keysByLocale
    ) {
        return validate(defaultLocale, keysByLocale, keysByLocale != null ? keysByLocale.keySet() : List.of());
    }
}
