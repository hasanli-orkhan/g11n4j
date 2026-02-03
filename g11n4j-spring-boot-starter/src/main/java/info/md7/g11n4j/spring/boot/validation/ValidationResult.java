package info.md7.g11n4j.spring.boot.validation;

import java.util.*;

/**
 * Result of message source validation.
 *
 * <p>Contains errors, warnings, and missing/extra keys per locale.
 */
public class ValidationResult {

    private final List<String> errors = new ArrayList<>();
    private final List<String> warnings = new ArrayList<>();
    private final Map<Locale, Set<String>> missingKeysByLocale = new HashMap<>();
    private final Map<Locale, Set<String>> extraKeysByLocale = new HashMap<>();
    private final Set<Locale> successfulLocales = new HashSet<>();

    public void addError(String error) {
        errors.add(error);
    }

    public void addWarning(String warning) {
        warnings.add(warning);
    }

    public void addMissingKeys(Locale locale, Set<String> keys) {
        missingKeysByLocale.put(locale, new HashSet<>(keys));
    }

    public void addExtraKeys(Locale locale, Set<String> keys) {
        extraKeysByLocale.put(locale, new HashSet<>(keys));
    }

    public void addSuccess(Locale locale) {
        successfulLocales.add(locale);
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
        return Collections.unmodifiableList(errors);
    }

    public List<String> getWarnings() {
        return Collections.unmodifiableList(warnings);
    }

    public Map<Locale, Set<String>> getMissingKeysByLocale() {
        return Collections.unmodifiableMap(missingKeysByLocale);
    }

    public Map<Locale, Set<String>> getExtraKeysByLocale() {
        return Collections.unmodifiableMap(extraKeysByLocale);
    }

    public Set<Locale> getSuccessfulLocales() {
        return Collections.unmodifiableSet(successfulLocales);
    }

    public int getTotalMissingKeysCount() {
        return missingKeysByLocale.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    public int getTotalExtraKeysCount() {
        return extraKeysByLocale.values().stream()
                .mapToInt(Set::size)
                .sum();
    }

    /**
     * Get a formatted summary of the validation results.
     *
     * @return Multi-line summary string
     */
    public String getSummary() {
        StringBuilder sb = new StringBuilder();
        sb.append("\n========================================\n");
        sb.append("Message Source Validation Results\n");
        sb.append("========================================\n");

        // Errors
        if (!errors.isEmpty()) {
            sb.append("\n❌ ERRORS:\n");
            errors.forEach(e -> sb.append("  - ").append(e).append("\n"));
        }

        // Warnings
        if (!warnings.isEmpty()) {
            sb.append("\n⚠️  WARNINGS:\n");
            warnings.forEach(w -> sb.append("  - ").append(w).append("\n"));
        }

        // Missing keys
        if (!missingKeysByLocale.isEmpty()) {
            sb.append("\n🔍 MISSING KEYS:\n");
            missingKeysByLocale.forEach((locale, keys) -> {
                sb.append("  Locale '").append(locale).append("': ").append(keys.size()).append(" missing keys\n");
                keys.stream().limit(10).forEach(key -> sb.append("    - ").append(key).append("\n"));
                if (keys.size() > 10) {
                    sb.append("    ... and ").append(keys.size() - 10).append(" more\n");
                }
            });
        }

        // Extra keys
        if (!extraKeysByLocale.isEmpty()) {
            sb.append("\n➕ EXTRA KEYS (not in default locale):\n");
            extraKeysByLocale.forEach((locale, keys) -> {
                sb.append("  Locale '").append(locale).append("': ").append(keys.size()).append(" extra keys\n");
                keys.stream().limit(10).forEach(key -> sb.append("    - ").append(key).append("\n"));
                if (keys.size() > 10) {
                    sb.append("    ... and ").append(keys.size() - 10).append(" more\n");
                }
            });
        }

        // Successful locales
        if (!successfulLocales.isEmpty()) {
            sb.append("\n✅ VALID LOCALES:\n");
            successfulLocales.forEach(locale -> sb.append("  - ").append(locale).append("\n"));
        }

        sb.append("\n========================================\n");
        if (isValid()) {
            sb.append("✅ Validation passed\n");
        } else {
            sb.append("❌ Validation failed\n");
        }
        sb.append("========================================\n");

        return sb.toString();
    }
}
