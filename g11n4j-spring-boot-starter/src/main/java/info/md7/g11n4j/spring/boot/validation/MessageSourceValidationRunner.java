package info.md7.g11n4j.spring.boot.validation;

import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;

/**
 * Application runner that validates message sources on startup.
 *
 * <p>Runs after the application context is loaded and logs any
 * inconsistencies found in the message files.
 */
public class MessageSourceValidationRunner implements ApplicationRunner {

    private static final Logger logger = LoggerFactory.getLogger(MessageSourceValidationRunner.class);

    private final MessageSourceValidator validator;
    private final G11nProperties properties;

    public MessageSourceValidationRunner(MessageSourceValidator validator, G11nProperties properties) {
        this.validator = validator;
        this.properties = properties;
    }

    @Override
    public void run(ApplicationArguments args) {
        logger.info("Running message source validation...");

        try {
            ValidationResult result = validator.validate();

            // Log the summary
            String summary = result.getSummary();

            if (result.hasErrors()) {
                logger.error(summary);
            } else if (result.hasMissingKeys() || result.hasWarnings()) {
                logger.warn(summary);
            } else {
                logger.info(summary);
            }

            // Log individual issues for better visibility
            if (result.hasErrors()) {
                result.getErrors().forEach(error ->
                    logger.error("Validation error: {}", error)
                );
            }

            if (result.hasMissingKeys()) {
                result.getMissingKeysByLocale().forEach((locale, keys) ->
                    logger.warn("Locale '{}' is missing {} keys from default locale", locale, keys.size())
                );
            }

            if (result.hasExtraKeys()) {
                result.getExtraKeysByLocale().forEach((locale, keys) ->
                    logger.warn("Locale '{}' has {} extra keys not in default locale", locale, keys.size())
                );
            }

            // Fail fast if configured
            if (properties.getValidation().isFailOnError() && !result.isValid()) {
                throw new IllegalStateException(
                    "Message source validation failed. Set 'spring.g11n.validation.fail-on-error=false' to ignore."
                );
            }

        } catch (IllegalStateException e) {
            throw e; // Rethrow to fail startup
        } catch (Exception e) {
            logger.error("Failed to validate message sources", e);
            if (properties.getValidation().isFailOnError()) {
                throw new IllegalStateException("Message source validation failed", e);
            }
        }
    }
}
