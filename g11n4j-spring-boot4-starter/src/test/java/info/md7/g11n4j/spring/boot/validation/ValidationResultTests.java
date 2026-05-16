package info.md7.g11n4j.spring.boot.validation;

import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationResultTests {

    @Test
    void shouldStartEmpty() {
        ValidationResult result = ValidationResult.builder().build();

        assertThat(result.hasErrors()).isFalse();
        assertThat(result.hasWarnings()).isFalse();
        assertThat(result.hasMissingKeys()).isFalse();
        assertThat(result.hasExtraKeys()).isFalse();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldAddErrors() {
        ValidationResult result = ValidationResult.builder()
                .addError("Test error")
                .build();

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("Test error");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldAddWarnings() {
        ValidationResult result = ValidationResult.builder()
                .addWarning("Test warning")
                .build();

        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.getWarnings()).contains("Test warning");
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldAddMissingKeys() {
        Locale locale = Locale.GERMAN;
        Set<String> missingKeys = Set.of("key1", "key2");
        ValidationResult result = ValidationResult.builder()
                .addMissingKeys(locale, missingKeys)
                .build();

        assertThat(result.hasMissingKeys()).isTrue();
        assertThat(result.getMissingKeysByLocale()).containsKey(locale);
        assertThat(result.getMissingKeysByLocale().get(locale)).containsAll(missingKeys);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldAddExtraKeys() {
        Locale locale = Locale.FRENCH;
        Set<String> extraKeys = Set.of("extra1", "extra2");
        ValidationResult result = ValidationResult.builder()
                .addExtraKeys(locale, extraKeys)
                .build();

        assertThat(result.hasExtraKeys()).isTrue();
        assertThat(result.getExtraKeysByLocale()).containsKey(locale);
        assertThat(result.getExtraKeysByLocale().get(locale)).containsAll(extraKeys);
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldAddSuccessfulLocales() {
        ValidationResult result = ValidationResult.builder()
                .addSuccess(Locale.GERMAN)
                .build();

        assertThat(result.getSuccessfulLocales()).contains(Locale.GERMAN);
    }

    @Test
    void shouldCountTotalMissingKeys() {
        ValidationResult result = ValidationResult.builder()
                .addMissingKeys(Locale.GERMAN, Set.of("key1", "key2"))
                .addMissingKeys(Locale.FRENCH, Set.of("key3", "key4", "key5"))
                .build();

        assertThat(result.getTotalMissingKeysCount()).isEqualTo(5);
    }

    @Test
    void shouldCountTotalExtraKeys() {
        ValidationResult result = ValidationResult.builder()
                .addExtraKeys(Locale.GERMAN, Set.of("extra1"))
                .addExtraKeys(Locale.FRENCH, Set.of("extra2", "extra3"))
                .build();

        assertThat(result.getTotalExtraKeysCount()).isEqualTo(3);
    }

    @Test
    void shouldGenerateSummaryWithAllSections() {
        ValidationResult result = ValidationResult.builder()
                .addError("Test error")
                .addWarning("Test warning")
                .addMissingKeys(Locale.GERMAN, Set.of("key1", "key2"))
                .addExtraKeys(Locale.FRENCH, Set.of("extra1"))
                .addSuccess(Locale.ITALIAN)
                .build();

        String summary = result.getSummary();

        assertThat(summary).contains("Message Source Validation Results");
        assertThat(summary).contains("ERRORS");
        assertThat(summary).contains("WARNINGS");
        assertThat(summary).contains("MISSING KEYS");
        assertThat(summary).contains("EXTRA KEYS");
        assertThat(summary).contains("VALID LOCALES");
        assertThat(summary).contains("Validation failed");
    }

    @Test
    void shouldGenerateSuccessSummary() {
        ValidationResult result = ValidationResult.builder()
                .addSuccess(Locale.GERMAN)
                .build();

        assertThat(result.getSummary()).contains("Validation passed");
    }

    @Test
    void shouldLimitKeysInSummary() {
        Set<String> manyKeys = Set.of(
                "key1", "key2", "key3", "key4", "key5",
                "key6", "key7", "key8", "key9", "key10",
                "key11", "key12"
        );
        ValidationResult result = ValidationResult.builder()
                .addMissingKeys(Locale.GERMAN, manyKeys)
                .build();

        String summary = result.getSummary();

        assertThat(summary).contains("12 missing keys");
        assertThat(summary).contains("... and 2 more");
    }

    @Test
    void shouldReturnImmutableCollections() {
        ValidationResult result = ValidationResult.builder()
                .addError("Error")
                .addWarning("Warning")
                .addMissingKeys(Locale.GERMAN, Set.of("key1"))
                .build();

        assertThat(result.getErrors()).isInstanceOf(List.class);
        assertThat(result.getWarnings()).isInstanceOf(List.class);
        assertThat(result.getMissingKeysByLocale()).isInstanceOf(java.util.Map.class);

        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                result.getErrors().add("new error")
        )).isInstanceOf(UnsupportedOperationException.class);
    }
}
