package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.core.source.MessageSource;
import info.md7.g11n4j.core.source.PropertiesMessageSource;
import info.md7.g11n4j.core.source.YamlMessageSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class G11nAutoConfiguration {

    @Autowired
    private G11nProperties properties;

    @ConditionalOnMissingBean
    @Bean(name = "g11n4jMessageSource")
    public MessageSource messageSource() {
        final SourceType sourceType = properties.getType();
        return switch (sourceType) {
            case YAML -> new YamlMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    properties.getLocales()
            );
            case PROPERTIES -> new PropertiesMessageSource(
                    properties.getBaseDirectory(),
                    properties.getFileBaseName(),
                    properties.getLocaleSeparator(),
                    properties.getFileExtension(),
                    properties.getDefaultLocale(),
                    properties.getLocales()
            );
            // todo add more types
            default -> throw new IllegalArgumentException("Unsupported message source type: " + sourceType);
        };
    }

    @ConditionalOnMissingBean
    @Bean
    public MessageResolver messageResolver() {
        return new MessageResolver(messageSource());
    }

}
