package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.source.MessageSource;
import info.md7.g11n4j.spring.boot.actuator.MessageSourceHealthIndicator;
import info.md7.g11n4j.spring.boot.endpoint.MessageSourceReloadEndpoint;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.autoconfigure.endpoint.condition.ConditionalOnAvailableEndpoint;
import org.springframework.boot.health.autoconfigure.contributor.ConditionalOnEnabledHealthIndicator;
import org.springframework.boot.health.contributor.HealthIndicator;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;

@AutoConfiguration(after = G11nAutoConfiguration.class)
@ConditionalOnBean(MessageSource.class)
@ConditionalOnProperty(prefix = "spring.g11n", name = "enabled", havingValue = "true", matchIfMissing = true)
public class G11nActuatorAutoConfiguration {

    @Bean
    @ConditionalOnClass(HealthIndicator.class)
    @ConditionalOnEnabledHealthIndicator("g11n")
    @ConditionalOnMissingBean(name = "g11nHealthIndicator")
    public HealthIndicator g11nHealthIndicator(G11nProperties properties) {
        return new MessageSourceHealthIndicator(properties);
    }

    @Bean
    @ConditionalOnClass(Endpoint.class)
    @ConditionalOnAvailableEndpoint(endpoint = MessageSourceReloadEndpoint.class)
    @ConditionalOnMissingBean
    public MessageSourceReloadEndpoint g11nReloadEndpoint(MessageSource messageSource) {
        return new MessageSourceReloadEndpoint(messageSource);
    }
}
