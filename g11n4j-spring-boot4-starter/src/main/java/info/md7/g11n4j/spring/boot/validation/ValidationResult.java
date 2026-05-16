package info.md7.g11n4j.spring.boot.validation;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * Immutable result of message source validation.
 */
public final class ValidationResult {

    private final List<String> errors;
    private final List<String> warnings;
    private final Map<Locale, Set<String>> missingKeysByLocale;
    private final Map<Locale, Set<String>> extraKeysByLocale;
    private final Set<Locale> successfulLocales;

    private ValidationResult(Builder builder) {
        this.errors = List.copyOf(builder.errors);
        this.warnings = List.copyOf(builder.warnings);
        this.missingKeysByLocale = deepImmutableCopy(builder.missingKeysByLocale);
        this.extraKeysByLocale = deepImmutableCopy(builder.extraKeysByLocale);
        this.successfulLocales = Collections.unmodifiableSet(new LinkedHashSet<>(builder.successfulLocales));
    }

    public static Builder builder() {
        return new Builder();
    }

    private static Map<Locale, Set<String>> deepImmutableCopy(Map<Locale, Set<String>> source) {
        Map<Locale, Set<String>> result = new LinkedHashMap<>();
        source.forEach((locale, keys) -> result.put(locale, Collections.unmodifiableSet(new LinkedHashSet<>(keys))));
        return Collections.unmodifiableMap(result);
    }

    public boolean hasErrors() {
        return !errors.isEmpty();
    }

    public boolean hasWarnings() {
        return !warnings.isEmpty();
    }

    public boolean hasMissingKeys() {
        return !missingKeysByLocale.isEmpty();
    }

    public boolean hasExtraKeys() {
        return !extraKeysByLocale.isEmpty();
    }

    public boolean isValid() {
        return errors.isEmpty() && missingKeysByLocale.isEmpty();
    }

    public List<String> getErrors() {
        return errors;
    }

    public List<String> getWarnings() {
        return warnings;
    }

    public Map<Locale, Set<String>> getMissingKeysByLocale() {
        return missingKeysByLocale;
    }

    public Map<Locale, Set<String>> getExtraKeysByLocale() {
        return extraKeysByLocale;
    }

    public Set<Locale> getSuccessfulLocales() {
        return successfulLocales;
    }

    public int getTotalMissingKeysCount() {
        return missingKeysByLocale.values().stream().mapToInt(Set::size).sum();
    }

    public int getTotalExtraKeysCount() {
        return extraKeysByLocale.values().stream().mapToInt(Set::size).sum();
    }

    /**
     * Get a formatted summary of the validation results.
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("Message Source Validation Results\n");
        sb.append("========================================\n");

        if (!errors.isEmpty()) {
            sb.append("\nERRORS:\n");
            errors.forEach(e -> sb.append("  - ").append(e).append("\n"));
        }

        if (!warnings.isEmpty()) {
            sb.append("\nWARNINGS:\n");
            warnings.forEach(w -> sb.append("  - ").append(w).append("\n"));
        }

        if (!missingKeysByLocale.isEmpty()) {
            sb.append("\nMISSING KEYS:\n");
            missingKeysByLocale.forEach((locale, keys) -> {
                sb.append("  Locale '").append(locale).append("': ").append(keys.size()).append(" missing keys\n");
                keys.stream().limit(10).forEach(key -> sb.append("    - ").append(key).append("\n"));
                if (keys.size() > 10) {
                    sb.append("    ... and ").append(keys.size() - 10).append(" more\n");
                }
            });
        }

        if (!extraKeysByLocale.isEmpty()) {
            sb.append("\nEXTRA KEYS (not in default locale):\n");
            extraKeysByLocale.forEach((locale, keys) -> {
                sb.append("  Locale '").append(locale).append("': ").append(keys.size()).append(" extra keys\n");
                keys.stream().limit(10).forEach(key -> sb.append("    - ").append(key).append("\n"));
                if (keys.size() > 10) {
                    sb.append("    ... and ").append(keys.size() - 10).append(" more\n");
                }
            });
        }

        if (!successfulLocales.isEmpty()) {
            sb.append("\nVALID LOCALES:\n");
            successfulLocales.forEach(locale -> sb.append("  - ").append(locale).append("\n"));
        }

        sb.append("\n========================================\n");
        if (isValid()) {
            sb.append("Validation passed\n");
        } else {
            sb.append("Validation failed\n");
        }
        sb.append("========================================\n");

        return sb.toString();
    }

    public static final class Builder {
        private final List<String> errors = new ArrayList<>();
        private final List<String> warnings = new ArrayList<>();
        private final Map<Locale, Set<String>> missingKeysByLocale = new LinkedHashMap<>();
        private final Map<Locale, Set<String>> extraKeysByLocale = new LinkedHashMap<>();
        private final Set<Locale> successfulLocales = new LinkedHashSet<>();

        public Builder addError(String error) {
            errors.add(error);
            return this;
        }

        public Builder addWarning(String warning) {
            warnings.add(warning);
            return this;
        }

        public Builder addMissingKeys(Locale locale, Set<String> keys) {
            missingKeysByLocale.put(locale, new LinkedHashSet<>(keys));
            return this;
        }

        public Builder addExtraKeys(Locale locale, Set<String> keys) {
            extraKeysByLocale.put(locale, new LinkedHashSet<>(keys));
            return this;
        }

        public Builder addSuccess(Locale locale) {
            successfulLocales.add(locale);
            return this;
        }

        public ValidationResult build() {
            return new ValidationResult(this);
        }
    }
}
