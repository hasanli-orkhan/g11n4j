package info.md7.g11n4j.spring.boot.validation;

import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceValidatorTests {

    private G11nProperties properties;
    private MessageSourceValidator validator;

    @BeforeEach
    void setUp() {
        properties = new G11nProperties();
        properties.setType(SourceType.PROPERTIES);
        properties.setBaseDirectory("i18n-validation");
        properties.setFileBaseName("messages");
        properties.setLocaleSeparator("_");
        properties.setFileExtension("properties");
        properties.setDefaultLocale(Locale.ENGLISH);
    }

    @Test
    void shouldDetectMissingKeys() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("ru")));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.hasMissingKeys()).isTrue();
        assertThat(result.getMissingKeysByLocale()).containsKey(Locale.forLanguageTag("ru"));
        assertThat(result.getMissingKeysByLocale().get(Locale.forLanguageTag("ru")))
                .contains("user.greeting", "user.logout");
    }

    @Test
    void shouldDetectExtraKeys() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("ru")));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.hasExtraKeys()).isTrue();
        assertThat(result.getExtraKeysByLocale()).containsKey(Locale.forLanguageTag("ru"));
        assertThat(result.getExtraKeysByLocale().get(Locale.forLanguageTag("ru")))
                .contains("extra.key");
    }

    @Test
    void shouldValidateSuccessfullyForCompleteLocale() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.GERMAN));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.getSuccessfulLocales()).contains(Locale.GERMAN);
        assertThat(result.getMissingKeysByLocale()).doesNotContainKey(Locale.GERMAN);
        assertThat(result.getExtraKeysByLocale()).doesNotContainKey(Locale.GERMAN);
    }

    @Test
    void shouldReportErrorForMissingFile() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.FRENCH));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.hasErrors()).isTrue();
        assertThat(result.getErrors()).anyMatch(error -> error.contains("fr"));
    }

    @Test
    void shouldSkipDefaultLocaleInValidation() {
        properties.setLocales(List.of(Locale.ENGLISH));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        // Only default locale, nothing to validate
        assertThat(result.isValid()).isTrue();
        assertThat(result.getMissingKeysByLocale()).isEmpty();
        assertThat(result.getSuccessfulLocales()).isEmpty();
    }

    @Test
    void shouldHandleEmptyLocalesList() {
        properties.setLocales(List.of());
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.hasWarnings()).isTrue();
        assertThat(result.getWarnings()).anyMatch(w -> w.contains("No supported locales"));
    }

    @Test
    void shouldCountTotalMissingAndExtraKeys() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("ru")));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();

        assertThat(result.getTotalMissingKeysCount()).isEqualTo(2); // user.greeting, user.logout
        assertThat(result.getTotalExtraKeysCount()).isEqualTo(1); // extra.key
    }

    @Test
    void shouldGenerateFormattedSummary() {
        properties.setLocales(List.of(Locale.ENGLISH, Locale.forLanguageTag("ru"), Locale.GERMAN));
        validator = new MessageSourceValidator(properties);

        ValidationResult result = validator.validate();
        String summary = result.getSummary();

        assertThat(summary).contains("Message Source Validation Results");
        assertThat(summary).contains("MISSING KEYS");
        assertThat(summary).contains("EXTRA KEYS");
        assertThat(summary).contains("VALID LOCALES");
        assertThat(summary).contains("de"); // German is valid
    }
}
