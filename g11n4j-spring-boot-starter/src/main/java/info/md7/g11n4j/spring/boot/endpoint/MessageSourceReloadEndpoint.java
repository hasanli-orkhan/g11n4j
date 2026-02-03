package info.md7.g11n4j.spring.boot.endpoint;

import info.md7.g11n4j.core.source.MessageSource;
import org.springframework.boot.actuate.endpoint.annotation.Endpoint;
import org.springframework.boot.actuate.endpoint.annotation.WriteOperation;
import org.springframework.boot.actuate.endpoint.annotation.Selector;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

/**
 * Actuator endpoint for reloading message sources.
 *
 * <p>Available operations:
 * <ul>
 *   <li>POST /actuator/g11n-reload - Reload all messages</li>
 *   <li>POST /actuator/g11n-reload/{locale} - Reload messages for specific locale</li>
 * </ul>
 */
@Component
@Endpoint(id = "g11n-reload")
@ConditionalOnClass(name = "org.springframework.boot.actuate.endpoint.annotation.Endpoint")
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
    public Map<String, Object> reloadAll() {
        try {
            messageSource.reload();
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "All message sources reloaded successfully");
            result.put("timestamp", System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Failed to reload message sources: " + e.getMessage());
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }

    /**
     * Reload messages for a specific locale.
     *
     * @param localeStr Locale string (e.g., "en", "ru", "zh")
     * @return Status map indicating success
     */
    @WriteOperation
    public Map<String, Object> reloadLocale(@Selector String localeStr) {
        try {
            Locale locale = Locale.forLanguageTag(localeStr.replace('_', '-'));
            messageSource.reload(locale);
            Map<String, Object> result = new HashMap<>();
            result.put("status", "success");
            result.put("message", "Messages reloaded successfully for locale: " + localeStr);
            result.put("locale", localeStr);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        } catch (Exception e) {
            Map<String, Object> result = new HashMap<>();
            result.put("status", "error");
            result.put("message", "Failed to reload messages for locale " + localeStr + ": " + e.getMessage());
            result.put("locale", localeStr);
            result.put("timestamp", System.currentTimeMillis());
            return result;
        }
    }
}
