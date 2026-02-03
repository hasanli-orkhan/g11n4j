package info.md7.g11n4j.spring.boot.validation;

import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Validates message source files for consistency across locales.
 *
 * <p>Checks that all locales have the same keys as the default locale
 * and reports missing keys as warnings during application startup.
 */
public class MessageSourceValidator {

    private static final Logger logger = LoggerFactory.getLogger(MessageSourceValidator.class);

    private final G11nProperties properties;
    private final PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();

    public MessageSourceValidator(G11nProperties properties) {
        this.properties = properties;
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

        // Load keys from default locale
        Set<String> defaultKeys = loadKeysForLocale(properties.getDefaultLocale());
        if (defaultKeys.isEmpty()) {
            result.addError("Could not load keys for default locale: " + properties.getDefaultLocale());
            return result;
        }

        logger.info("Found {} keys in default locale file", defaultKeys.size());

        // Validate each supported locale
        List<Locale> locales = properties.getLocales();
        if (locales == null || locales.isEmpty()) {
            result.addWarning("No supported locales configured");
            return result;
        }

        logger.debug("Validating {} locales", locales.size());
        for (Locale locale : locales) {
            if (locale.equals(properties.getDefaultLocale())) {
                logger.debug("Skipping default locale: {}", locale);
                continue; // Skip default locale
            }

            validateLocale(locale, defaultKeys, result);
        }
        logger.debug("Validation complete. Errors: {}, Warnings: {}", result.getErrors().size(), result.getWarnings().size());

        return result;
    }

    private void validateLocale(Locale locale, Set<String> defaultKeys, ValidationResult result) {
        logger.debug("Validating locale: {}", locale);

        String filename = getFileName(locale);
        Set<String> localeKeys = loadKeysForLocale(locale);
        logger.debug("File: {}, Loaded {} keys for locale {}", filename, localeKeys.size(), locale);

        if (localeKeys.isEmpty()) {
            // Check if file exists to determine if it's an error or warning
            boolean exists = fileExists(filename);
            logger.debug("File {} exists: {}", filename, exists);
            
            if (!exists) {
                logger.debug("Adding error for missing file: {}", locale);
                result.addError("File not found for locale: " + locale);
            } else {
                logger.debug("Adding warning for empty file: {}", locale);
                result.addWarning("No keys found for locale: " + locale);
            }
            return;
        }

        // Find missing keys
        Set<String> missingKeys = new HashSet<>(defaultKeys);
        missingKeys.removeAll(localeKeys);

        if (!missingKeys.isEmpty()) {
            result.addMissingKeys(locale, missingKeys);
        }

        // Find extra keys (keys in locale but not in default)
        Set<String> extraKeys = new HashSet<>(localeKeys);
        extraKeys.removeAll(defaultKeys);

        if (!extraKeys.isEmpty()) {
            result.addExtraKeys(locale, extraKeys);
        }

        if (missingKeys.isEmpty() && extraKeys.isEmpty()) {
            result.addSuccess(locale);
        }
    }

    private Set<String> loadKeysForLocale(Locale locale) {
        String filename = getFileName(locale);

        try {
            String pattern = "classpath:" + filename;
            Resource[] resources = resolver.getResources(pattern);

            if (resources.length == 0) {
                return Collections.emptySet();
            }

            Resource resource = Arrays.stream(resources)
                    .filter(Resource::exists)
                    .findFirst()
                    .orElse(null);
            if (resource == null) {
                return Collections.emptySet();
            }

            try (InputStream is = resource.getInputStream()) {
                return extractKeys(is);
            }
        } catch (IOException e) {
            logger.debug("Could not load file for locale {}: {}", locale, e.getMessage());
            return Collections.emptySet();
        }
    }

    private Set<String> extractKeys(InputStream is) throws IOException {
        Set<String> keys = new HashSet<>();

        switch (properties.getType()) {
            case PROPERTIES -> {
                Properties props = new Properties();
                props.load(is);
                keys.addAll(props.stringPropertyNames());
            }
            case YAML -> {
                // For YAML, we'd need to parse and flatten the structure
                // For now, skip detailed validation for YAML/JSON/GETTEXT/XLIFF
                // as they require format-specific parsing
                logger.debug("Detailed validation for YAML format not yet implemented");
            }
            case JSON -> {
                logger.debug("Detailed validation for JSON format not yet implemented");
            }
            case GETTEXT -> {
                logger.debug("Detailed validation for GETTEXT format not yet implemented");
            }
            case XLIFF -> {
                logger.debug("Detailed validation for XLIFF format not yet implemented");
            }
        }

        return keys;
    }

    private boolean fileExists(String filename) {
        try {
            String pattern = "classpath:" + filename;
            Resource[] resources = resolver.getResources(pattern);
            if (resources.length == 0) {
                return false;
            }
            return Arrays.stream(resources).anyMatch(Resource::exists);
        } catch (IOException e) {
            return false;
        }
    }

    private String getFileName(Locale locale) {
        return properties.getBaseDirectory() + "/" +
               properties.getFileBaseName() +
               properties.getLocaleSeparator() +
               locale.getLanguage() + "." +
               properties.getFileExtension();
    }
}
