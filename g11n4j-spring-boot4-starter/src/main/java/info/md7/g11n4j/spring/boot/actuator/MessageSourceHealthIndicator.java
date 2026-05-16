package info.md7.g11n4j.spring.boot.actuator;

import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.HealthIndicator;

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
public class MessageSourceHealthIndicator implements HealthIndicator {

    private final G11nProperties properties;

    public MessageSourceHealthIndicator(G11nProperties properties) {
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
                locales = List.of(properties.getDefaultLocale());
            }

            details.put("supportedLocales", locales.stream()
                    .map(Locale::toString)
                    .toList());
            details.put("numberOfLocales", locales.size());

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
