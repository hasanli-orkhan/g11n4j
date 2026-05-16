package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.core.source.*;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidationRunner;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.util.List;
import java.util.Locale;

@AutoConfiguration
@ConditionalOnProperty(prefix = "spring.g11n", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(G11nProperties.class)
public class G11nAutoConfiguration {

    private final G11nProperties properties;

    public G11nAutoConfiguration(G11nProperties properties) {
        this.properties = properties;
    }

    @ConditionalOnMissingBean
    @Bean(name = "g11n4jMessageSource")
    public MessageSource messageSource() {
        final SourceType sourceType = properties.getType();
        final int cacheSize = properties.getCache().getSize();
        List<Locale> locales = resolveLocales();
        String baseDirectory = properties.getBaseDirectory();
        String fileBaseName = properties.getFileBaseName();
        String localeSeparator = properties.getLocaleSeparator();
        String fileExtension = resolveFileExtension(sourceType);
        Locale defaultLocale = properties.getDefaultLocale();

        return switch (sourceType) {
            case YAML -> new YamlMessageSource(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, locales, cacheSize);
            case PROPERTIES -> new PropertiesMessageSource(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, locales, cacheSize);
            case JSON -> new JsonMessageSource(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, locales, cacheSize);
            case GETTEXT -> new GettextMessageSource(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, locales, cacheSize);
            case XLIFF -> new XliffMessageSource(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, locales, cacheSize);
        };
    }

    @ConditionalOnMissingBean
    @Bean
    public MessageResolver messageResolver(MessageSource g11n4jMessageSource) {
        return new MessageResolver(g11n4jMessageSource);
    }

    @ConditionalOnMissingBean
    @Bean
    public MessageSourceValidator messageSourceValidator(MessageSource g11n4jMessageSource) {
        return new MessageSourceValidator(properties, g11n4jMessageSource);
    }

    @ConditionalOnProperty(prefix = "spring.g11n.validation", name = "enabled", havingValue = "true", matchIfMissing = true)
    @ConditionalOnMissingBean
    @Bean
    public MessageSourceValidationRunner messageSourceValidationRunner(MessageSourceValidator validator) {
        return new MessageSourceValidationRunner(validator, properties);
    }

    private List<Locale> resolveLocales() {
        List<Locale> locales = properties.getLocales();
        if (locales == null || locales.isEmpty()) {
            return List.of(properties.getDefaultLocale());
        }
        return locales;
    }

    private String resolveFileExtension(SourceType sourceType) {
        String configuredExtension = properties.getFileExtension();
        if (sourceType != SourceType.PROPERTIES && "properties".equals(configuredExtension)) {
            return sourceType.getExtensions().get(0);
        }
        return configuredExtension;
    }
}
