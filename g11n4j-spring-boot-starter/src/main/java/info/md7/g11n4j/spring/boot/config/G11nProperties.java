package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.model.SourceType;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.validation.annotation.Validated;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Configuration properties for g11n4j internationalization support.
 *
 * <p>Example configuration:
 * <pre>
 * spring:
 *   g11n:
 *     enabled: true
 *     type: YAML
 *     base-directory: i18n
 *     default-locale: en
 *     locales:
 *       - en
 *       - ru
 *       - zh
 *     cache:
 *       size: 2000
 * </pre>
 */
@Validated
@ConfigurationProperties(prefix = "spring.g11n")
public class G11nProperties {

    /**
     * Enable/disable g11n4j auto-configuration. Default: true
     */
    private boolean enabled = true;

    /**
     * Type of message source (YAML, PROPERTIES, JSON, GETTEXT, XLIFF).
     */
    @NotNull
    private SourceType type = SourceType.PROPERTIES;

    /**
     * Base directory for message files. Default: i18n
     */
    @NotBlank
    private String baseDirectory = "i18n";

    /**
     * Sub-paths under {@link #baseDirectory} whose message files are merged on top of the base
     * directory's files. For each locale the best-matching file from the base directory is loaded
     * first, then each entry in this list (in declared order) is merged over it, so later entries
     * override earlier ones on key conflict (base directory has the lowest priority). Default: empty.
     */
    private List<String> additionalDirectories = new ArrayList<>();

    /**
     * Base name for message files. Default: messages
     */
    @NotBlank
    private String fileBaseName = "messages";

    /**
     * Locale separator in filenames. Default: _
     */
    @NotBlank
    private String localeSeparator = "_";

    /**
     * File extension for message files. Default: properties
     */
    @NotBlank
    private String fileExtension = "properties";

    /**
     * Default locale for fallback. Default: en
     */
    @NotNull
    private Locale defaultLocale = Locale.ENGLISH;

    /**
     * List of supported locales.
     */
    private List<Locale> locales;

    /**
     * Cache configuration properties.
     */
    @Valid
    @NotNull
    private CacheProperties cache = new CacheProperties();

    /**
     * Validation configuration properties.
     */
    @Valid
    @NotNull
    private ValidationProperties validation = new ValidationProperties();

    /**
     * Cache configuration for message storage.
     */
    public static class CacheProperties {

        /**
         * Maximum number of cached messages. Default: 1000
         */
        @Positive
        private int size = 1000;

        /**
         * Enable cache statistics collection. Default: false
         */
        private boolean enableStatistics = false;

        public int getSize() {
            return size;
        }

        public void setSize(int size) {
            this.size = size;
        }

        public boolean isEnableStatistics() {
            return enableStatistics;
        }

        public void setEnableStatistics(boolean enableStatistics) {
            this.enableStatistics = enableStatistics;
        }
    }

    // Getters and Setters

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public List<String> getAdditionalDirectories() {
        return additionalDirectories;
    }

    public void setAdditionalDirectories(List<String> additionalDirectories) {
        this.additionalDirectories = additionalDirectories;
    }

    public String getFileBaseName() {
        return fileBaseName;
    }

    public void setFileBaseName(String fileBaseName) {
        this.fileBaseName = fileBaseName;
    }

    public String getLocaleSeparator() {
        return localeSeparator;
    }

    public void setLocaleSeparator(String localeSeparator) {
        this.localeSeparator = localeSeparator;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }

    public CacheProperties getCache() {
        return cache;
    }

    public void setCache(CacheProperties cache) {
        this.cache = cache;
    }

    public ValidationProperties getValidation() {
        return validation;
    }

    public void setValidation(ValidationProperties validation) {
        this.validation = validation;
    }

    /**
     * Validation configuration for message source consistency checks.
     */
    public static class ValidationProperties {

        /**
         * Enable/disable validation on startup. Default: true
         */
        private boolean enabled = true;

        /**
         * Fail application startup if validation fails. Default: false
         */
        private boolean failOnError = false;

        public boolean isEnabled() {
            return enabled;
        }

        public void setEnabled(boolean enabled) {
            this.enabled = enabled;
        }

        public boolean isFailOnError() {
            return failOnError;
        }

        public void setFailOnError(boolean failOnError) {
            this.failOnError = failOnError;
        }
    }
}
