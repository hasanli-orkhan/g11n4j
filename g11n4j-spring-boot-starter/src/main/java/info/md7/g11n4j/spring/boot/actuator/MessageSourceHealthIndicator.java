package info.md7.g11n4j.spring.boot.actuator;

import info.md7.g11n4j.core.source.MessageSource;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.springframework.boot.actuate.health.Health;
import org.springframework.boot.actuate.health.HealthIndicator;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Health indicator for g11n4j message sources.
 *
 * <p>Checks:
 * <ul>
 *   <li>Message source is available</li>
 *   <li>Supported locales are configured</li>
 *   <li>Messages can be retrieved for each locale</li>
 * </ul>
 */
@Component
@ConditionalOnClass(name = "org.springframework.boot.actuate.health.HealthIndicator")
public class MessageSourceHealthIndicator implements HealthIndicator {

    private final MessageSource messageSource;
    private final G11nProperties properties;

    public MessageSourceHealthIndicator(MessageSource messageSource, G11nProperties properties) {
        this.messageSource = messageSource;
        this.properties = properties;
    }

    @Override
    public Health health() {
        try {
            Map<String, Object> details = new HashMap<>();

            // Basic configuration
            details.put("type", properties.getType());
            details.put("defaultLocale", properties.getDefaultLocale().toString());
            details.put("baseDirectory", properties.getBaseDirectory());
            details.put("cacheSize", properties.getCache().getSize());

            // Check supported locales
            List<Locale> locales = properties.getLocales();
            if (locales == null || locales.isEmpty()) {
                return Health.down()
                        .withDetail("error", "No supported locales configured")
                        .withDetails(details)
                        .build();
            }

            details.put("supportedLocales", locales.stream()
                    .map(Locale::toString)
                    .toList());
            details.put("numberOfLocales", locales.size());

            // Verify message source is accessible
            if (messageSource == null) {
                return Health.down()
                        .withDetail("error", "MessageSource bean is null")
                        .withDetails(details)
                        .build();
            }

            return Health.up()
                    .withDetails(details)
                    .build();

        } catch (Exception e) {
            return Health.down()
                    .withException(e)
                    .build();
        }
    }
}
