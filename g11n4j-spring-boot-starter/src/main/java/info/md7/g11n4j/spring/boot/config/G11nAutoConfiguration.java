package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.core.source.*;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidationRunner;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

@Configuration
@ConditionalOnProperty(prefix = "spring.g11n", name = "enabled", havingValue = "true", matchIfMissing = true)
@EnableConfigurationProperties(G11nProperties.class)
public class G11nAutoConfiguration {

    @Autowired
    private G11nProperties properties;

    @ConditionalOnMissingBean
    @Bean(name = "g11n4jMessageSource")
    public MessageSource messageSource() {
        final SourceType sourceType = properties.getType();
        final int cacheSize = properties.getCache().getSize();
        
        // Ensure locales is not null or empty
        List<Locale> locales = properties.getLocales();
        if (locales == null || locales.isEmpty()) {
            locales = List.of(properties.getDefaultLocale());
        }

        return switch (sourceType) {
            case YAML -> new YamlMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    locales,
                    cacheSize
            );
            case PROPERTIES -> new PropertiesMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    locales,
                    cacheSize
            );
            case JSON -> new JsonMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    locales,
                    cacheSize
            );
            case GETTEXT -> new GettextMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    locales,
                    cacheSize
            );
            case XLIFF -> new XliffMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    locales,
                    cacheSize
            );
            default -> throw new IllegalArgumentException("Unsupported message source type: " + sourceType);
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

}
