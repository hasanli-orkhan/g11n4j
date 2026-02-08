package info.md7.g11n4j.spring.boot.validation;

import info.md7.g11n4j.core.source.MessageSource;
import info.md7.g11n4j.core.validation.MessageSourceKeyProvider;
import info.md7.g11n4j.core.validation.TranslationValidationResult;
import info.md7.g11n4j.core.validation.TranslationValidator;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Validates message source files for consistency across locales.
 *
 * <p>Checks that all locales have the same keys as the default locale
 * and reports missing keys as warnings during application startup.
 */
public class MessageSourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(MessageSourceValidator.class);

    private final G11nProperties properties;
    private final MessageSource messageSource;

    public MessageSourceValidator(G11nProperties properties, MessageSource messageSource) {
        this.properties = properties;
        this.messageSource = messageSource;
    }

    /**
     * Validate all configured locales against the default locale.
     *
     * @return ValidationResult containing all issues found
     */
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();

        logger.info("Starting message validation for source type: {}", properties.getType());
        logger.info("Default locale: {}", properties.getDefaultLocale());

        if (!(messageSource instanceof MessageSourceKeyProvider keyProvider)) {
            result.addError("MessageSource does not expose translation keys for validation.");
            return result;
        }

        TranslationValidationResult coreResult = TranslationValidator.validate(keyProvider);

        if (!coreResult.getErrors().isEmpty()) {
            coreResult.getErrors().forEach(result::addError);
            return result;
        }

        Set<String> defaultKeys = coreResult.getDefaultKeys();
        if (defaultKeys.isEmpty()) {
            result.addError("Could not load keys for default locale: " + coreResult.getDefaultLocale());
            return result;
        }

        logger.info("Found {} keys in default locale file", defaultKeys.size());

        List<Locale> locales = keyProvider.getSupportedLocales();
        if (locales == null || locales.isEmpty()) {
            result.addWarning("No supported locales configured");
            return result;
        }

        for (Locale locale : locales) {
            if (locale.equals(coreResult.getDefaultLocale())) {
                continue;
            }
            Set<String> missing = coreResult.getMissingKeysByLocale().get(locale);
            if (missing != null && !missing.isEmpty()) {
                result.addMissingKeys(locale, missing);
            }

            Set<String> extra = coreResult.getExtraKeysByLocale().get(locale);
            if (extra != null && !extra.isEmpty()) {
                result.addExtraKeys(locale, extra);
            }

            if ((missing == null || missing.isEmpty()) && (extra == null || extra.isEmpty())) {
                result.addSuccess(locale);
            }
        }

        logger.debug("Validation complete. Errors: {}, Warnings: {}", result.getErrors().size(), result.getWarnings().size());

        return result;
    }
}
