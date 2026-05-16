package info.md7.g11n4j.spring.boot.actuator;

import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.junit.jupiter.api.Test;
import org.springframework.boot.health.contributor.Health;
import org.springframework.boot.health.contributor.Status;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class MessageSourceHealthIndicatorTests {

    @Test
    void shouldReportConfiguredLocalesAsUp() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.ENGLISH, Locale.forLanguageTag("ru-RU")));
        MessageSourceHealthIndicator indicator = new MessageSourceHealthIndicator(properties);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("numberOfLocales", 2);
        Object supportedLocales = health.getDetails().get("supportedLocales");
        assertThat(supportedLocales).isInstanceOf(List.class);
        assertThat(supportedLocales).isEqualTo(List.of("en", "ru_RU"));
    }

    @Test
    void shouldFallbackToDefaultLocaleWhenSupportedLocalesAreMissing() {
        G11nProperties properties = createProperties(Locale.forLanguageTag("en-US"), null);
        MessageSourceHealthIndicator indicator = new MessageSourceHealthIndicator(properties);

        Health health = indicator.health();

        assertThat(health.getStatus()).isEqualTo(Status.UP);
        assertThat(health.getDetails()).containsEntry("numberOfLocales", 1);
        Object supportedLocales = health.getDetails().get("supportedLocales");
        assertThat(supportedLocales).isInstanceOf(List.class);
        assertThat(supportedLocales).isEqualTo(List.of("en_US"));
    }

    private G11nProperties createProperties(Locale defaultLocale, List<Locale> locales) {
        G11nProperties properties = new G11nProperties();
        properties.setDefaultLocale(defaultLocale);
        properties.setLocales(locales);
        properties.setType(SourceType.PROPERTIES);
        properties.setBaseDirectory("i18n");
        properties.getCache().setSize(1000);
        return properties;
    }
}
