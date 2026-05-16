package info.md7.g11n4j.spring.boot.endpoint;

import info.md7.g11n4j.core.source.MessageSource;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;

import java.util.Locale;

/**
 * Actuator endpoint for reloading message sources.
 *
 * <p>Available operations:
 * <ul>
 *   <li>POST /actuator/g11n-reload - Reload all messages</li>
 *   <li>POST /actuator/g11n-reload/{locale} - Reload messages for specific locale</li>
 * </ul>
 */
@Endpoint(id = "g11n-reload")
public class MessageSourceReloadEndpoint {

    private final MessageSource messageSource;

    public MessageSourceReloadEndpoint(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Reload all message sources.
     *
     * @return Status map indicating success
     */
    @WriteOperation
    public ReloadResponse reloadAll() {
        try {
            messageSource.reload();
            return ReloadResponse.success("All message sources reloaded successfully", null);
        } catch (Exception e) {
            return ReloadResponse.error("Failed to reload message sources: " + e.getMessage(), null);
        }
    }

    /**
     * Reload messages for a specific locale.
     *
     * @param localeStr Locale string (e.g., "en", "ru", "zh")
     * @return Status map indicating success
     */
    @WriteOperation
    public ReloadResponse reloadLocale(@Selector String localeStr) {
        try {
            if (localeStr == null || localeStr.isBlank()) {
                throw new IllegalArgumentException("Locale must not be blank");
            }
            Locale locale = Locale.forLanguageTag(localeStr.replace('_', '-'));
            if (locale.getLanguage().isBlank()) {
                throw new IllegalArgumentException("Invalid locale: " + localeStr);
            }
            messageSource.reload(locale);
            return ReloadResponse.success("Messages reloaded successfully for locale: " + localeStr, localeStr);
        } catch (Exception e) {
            return ReloadResponse.error(
                    "Failed to reload messages for locale " + localeStr + ": " + e.getMessage(),
                    localeStr
            );
        }
    }

    public record ReloadResponse(String status, String message, String locale, long timestamp) {
        static ReloadResponse success(String message, String locale) {
            return new ReloadResponse("success", message, locale, System.currentTimeMillis());
        }

        static ReloadResponse error(String message, String locale) {
            return new ReloadResponse("error", message, locale, System.currentTimeMillis());
        }
    }
}
