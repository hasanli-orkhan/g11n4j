package info.md7.g11n4j.spring.boot.validation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class ValidationResultTests {

    private ValidationResult result;

    @BeforeEach
    void setUp() {
        result = new ValidationResult();
    }

    @Test
    void shouldStartEmpty() {
        assertThat(result.hasErrors()).isFalse();
        assertThat(result.hasWarnings()).isFalse();
        assertThat(result.hasMissingKeys()).isFalse();
        assertThat(result.hasExtraKeys()).isFalse();
        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldAddErrors() {
        result.addError("Test error");

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).contains("Test error");
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldAddWarnings() {
        result.addWarning("Test warning");

        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.getWarnings()).contains("Test warning");
        assertThat(result.isValid()).isTrue(); // warnings don't affect validity
    }

    @Test
    void shouldAddMissingKeys() {
        Locale locale = Locale.GERMAN;
        Set<String> missingKeys = Set.of("key1", "key2");

        result.addMissingKeys(locale, missingKeys);

        assertThat(result.hasMissingKeys()).isTrue();
        assertThat(result.getMissingKeysByLocale()).containsKey(locale);
        assertThat(result.getMissingKeysByLocale().get(locale)).containsAll(missingKeys);
        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldAddExtraKeys() {
        Locale locale = Locale.FRENCH;
        Set<String> extraKeys = Set.of("extra1", "extra2");

        result.addExtraKeys(locale, extraKeys);

        assertThat(result.hasExtraKeys()).isTrue();
        assertThat(result.getExtraKeysByLocale()).containsKey(locale);
        assertThat(result.getExtraKeysByLocale().get(locale)).containsAll(extraKeys);
        assertThat(result.isValid()).isTrue(); // extra keys don't affect validity
    }

    @Test
    void shouldAddSuccessfulLocales() {
        result.addSuccess(Locale.GERMAN);

        assertThat(result.getSuccessfulLocales()).contains(Locale.GERMAN);
    }

    @Test
    void shouldCountTotalMissingKeys() {
        result.addMissingKeys(Locale.GERMAN, Set.of("key1", "key2"));
        result.addMissingKeys(Locale.FRENCH, Set.of("key3", "key4", "key5"));

        assertThat(result.getTotalMissingKeysCount()).isEqualTo(5);
    }

    @Test
    void shouldCountTotalExtraKeys() {
        result.addExtraKeys(Locale.GERMAN, Set.of("extra1"));
        result.addExtraKeys(Locale.FRENCH, Set.of("extra2", "extra3"));

        assertThat(result.getTotalExtraKeysCount()).isEqualTo(3);
    }

    @Test
    void shouldBeInvalidWhenHasErrors() {
        result.addError("Error");

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldBeInvalidWhenHasMissingKeys() {
        result.addMissingKeys(Locale.GERMAN, Set.of("key1"));

        assertThat(result.isValid()).isFalse();
    }

    @Test
    void shouldBeValidWithOnlyWarnings() {
        result.addWarning("Warning");

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldBeValidWithOnlyExtraKeys() {
        result.addExtraKeys(Locale.GERMAN, Set.of("extra1"));

        assertThat(result.isValid()).isTrue();
    }

    @Test
    void shouldGenerateSummaryWithAllSections() {
        result.addError("Test error");
        result.addWarning("Test warning");
        result.addMissingKeys(Locale.GERMAN, Set.of("key1", "key2"));
        result.addExtraKeys(Locale.FRENCH, Set.of("extra1"));
        result.addSuccess(Locale.ITALIAN);

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
        result.addSuccess(Locale.GERMAN);

        String summary = result.getSummary();

        assertThat(summary).contains("Validation passed");
    }

    @Test
    void shouldLimitKeysInSummary() {
        Set<String> manyKeys = Set.of(
                "key1", "key2", "key3", "key4", "key5",
                "key6", "key7", "key8", "key9", "key10",
                "key11", "key12"
        );
        result.addMissingKeys(Locale.GERMAN, manyKeys);

        String summary = result.getSummary();

        assertThat(summary).contains("12 missing keys");
        assertThat(summary).contains("... and 2 more");
    }

    @Test
    void shouldReturnImmutableCollections() {
        result.addError("Error");
        result.addWarning("Warning");
        result.addMissingKeys(Locale.GERMAN, Set.of("key1"));

        assertThat(result.getErrors()).isInstanceOf(List.class);
        assertThat(result.getWarnings()).isInstanceOf(List.class);
        assertThat(result.getMissingKeysByLocale()).isInstanceOf(java.util.Map.class);

        // Verify immutability
        assertThat(org.assertj.core.api.Assertions.catchThrowable(() ->
                result.getErrors().add("new error")
        )).isInstanceOf(UnsupportedOperationException.class);
    }
}
