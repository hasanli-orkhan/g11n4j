package info.md7.g11n4j.spring.boot.web;

import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockHttpServletRequest;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class G11nLocaleResolverTests {

    @Test
    void shouldPreferQueryParamOverHeaderLocale() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.ENGLISH, Locale.forLanguageTag("ru-RU")));
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("locale", "ru_RU");
        request.setPreferredLocales(List.of(Locale.ENGLISH));

        Locale resolved = resolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.forLanguageTag("ru-RU"));
    }

    @Test
    void shouldUseLanguageMatchWhenExactLocaleIsMissing() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.forLanguageTag("en-US"), Locale.FRENCH));
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("locale", "en-GB");

        Locale resolved = resolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.forLanguageTag("en-US"));
    }

    @Test
    void shouldUseHeaderLocaleWhenQueryParamIsUnsupported() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.ENGLISH, Locale.forLanguageTag("fr-FR")));
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setParameter("locale", "de-DE");
        request.setPreferredLocales(List.of(Locale.ENGLISH));

        Locale resolved = resolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void shouldFallbackToDefaultLocaleWhenNoMatchExists() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.forLanguageTag("fr-FR")));
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.GERMAN));

        Locale resolved = resolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.ENGLISH);
    }

    @Test
    void shouldUseRequestedLocaleWhenSupportedLocalesAreNotConfigured() {
        G11nProperties properties = createProperties(Locale.ENGLISH, null);
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);
        MockHttpServletRequest request = new MockHttpServletRequest();
        request.setPreferredLocales(List.of(Locale.GERMAN));

        Locale resolved = resolver.resolveLocale(request);

        assertThat(resolved).isEqualTo(Locale.GERMAN);
    }

    @Test
    void setLocaleShouldThrowUnsupportedOperationException() {
        G11nProperties properties = createProperties(Locale.ENGLISH, List.of(Locale.ENGLISH));
        G11nLocaleResolver resolver = new G11nLocaleResolver(properties);

        assertThatThrownBy(() -> resolver.setLocale(new MockHttpServletRequest(), null, Locale.GERMAN))
                .isInstanceOf(UnsupportedOperationException.class)
                .hasMessageContaining("does not support setLocale");
    }

    private G11nProperties createProperties(Locale defaultLocale, List<Locale> locales) {
        G11nProperties properties = new G11nProperties();
        properties.setDefaultLocale(defaultLocale);
        properties.setLocales(locales);
        return properties;
    }
}
