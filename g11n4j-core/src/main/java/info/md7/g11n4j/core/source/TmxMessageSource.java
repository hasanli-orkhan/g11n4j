package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

public class TmxMessageSource implements MessageSource {

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        // todo implement this
        throw new RuntimeException("Not yet implemented.");
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale) {
        return Map.of();
    }
}
