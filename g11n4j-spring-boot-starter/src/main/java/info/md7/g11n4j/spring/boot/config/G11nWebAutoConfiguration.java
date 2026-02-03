package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.spring.boot.web.G11nLocaleResolver;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.autoconfigure.condition.ConditionalOnWebApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.web.servlet.LocaleResolver;

/**
 * Auto-configuration for g11n4j web integration.
 *
 * <p>Provides:
 * <ul>
 *   <li>Custom LocaleResolver that integrates with g11n4j configuration</li>
 * </ul>
 */
@AutoConfiguration
@ConditionalOnWebApplication
@ConditionalOnClass(name = "org.springframework.web.servlet.LocaleResolver")
@ConditionalOnProperty(prefix = "spring.g11n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class G11nWebAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean(LocaleResolver.class)
    public LocaleResolver localeResolver(G11nProperties properties) {
        return new G11nLocaleResolver(properties);
    }
}
