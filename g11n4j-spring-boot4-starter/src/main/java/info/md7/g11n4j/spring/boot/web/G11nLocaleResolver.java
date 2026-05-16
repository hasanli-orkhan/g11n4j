package info.md7.g11n4j.spring.boot.web;

import info.md7.g11n4j.spring.boot.config.G11nProperties;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/**
 * Custom LocaleResolver that integrates with g11n4j configuration.
 *
 * <p>Resolution order:
 * <ol>
 *   <li>Query parameter (e.g., ?locale=en)</li>
 *   <li>Accept-Language header</li>
 *   <li>Default locale from configuration</li>
 * </ol>
 */
public class G11nLocaleResolver implements LocaleResolver {

    private static final String LOCALE_PARAM = "locale";

    private final G11nProperties properties;

    public G11nLocaleResolver(G11nProperties properties) {
        this.properties = properties;
    }

    @Override
    public Locale resolveLocale(HttpServletRequest request) {
        // 1. Check query parameter
        String localeParam = request.getParameter(LOCALE_PARAM);
        if (localeParam != null && !localeParam.isEmpty()) {
            Locale resolved = findBestSupportedLocale(parseLocale(localeParam));
            if (resolved != null) {
                return resolved;
            }
        }

        // 2. Check Accept-Language header
        Locale headerLocale = request.getLocale();
        if (headerLocale != null) {
            Locale resolved = findBestSupportedLocale(headerLocale);
            if (resolved != null) {
                return resolved;
            }
        }

        // 3. Fallback to default locale
        return properties.getDefaultLocale();
    }

    @Override
    public void setLocale(HttpServletRequest request, HttpServletResponse response, Locale locale) {
        // Not supported - locale resolution is request-based only
        throw new UnsupportedOperationException(
                "G11nLocaleResolver does not support setLocale. Use query parameters or Accept-Language header."
        );
    }

    /**
     * Parse locale string, handling both underscore and hyphen separators.
     *
     * @param localeStr Locale string (e.g., "en_US", "en-US", "en")
     * @return Parsed Locale
     */
    private Locale parseLocale(String localeStr) {
        return Locale.forLanguageTag(localeStr.replace('_', '-'));
    }

    /**
     * Check if the given locale is in the list of supported locales.
     *
     * @param locale Locale to check
     * @return true if supported, false otherwise
     */
    private Locale findBestSupportedLocale(Locale locale) {
        if (locale == null) {
            return null;
        }
        List<Locale> supportedLocales = properties.getLocales();
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            return locale;
        }

        Locale exactMatch = supportedLocales.stream()
                .filter(supported -> supported.toLanguageTag().equalsIgnoreCase(locale.toLanguageTag()))
                .findFirst()
                .orElse(null);
        if (exactMatch != null) {
            return exactMatch;
        }

        return supportedLocales.stream()
                .filter(supported -> supported.getLanguage().equalsIgnoreCase(locale.getLanguage()))
                .min(Comparator.comparing(supported -> supported.getCountry().isEmpty() ? 1 : 0))
                .orElse(null);
    }
}
