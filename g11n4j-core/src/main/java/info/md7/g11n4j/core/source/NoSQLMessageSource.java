package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;

import java.util.Locale;
import java.util.Map;

public class NoSQLMessageSource implements MessageSource {

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        // todo implement this
        throw new RuntimeException("Not yet implemented.");
    }

    @Override
    public String getMessage(String key, Locale locale, MessageContext context) throws NoSuchMessageException {
        return "";
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale, MessageContext context) {
        return Map.of();
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale) {
        return Map.of();
    }
}
