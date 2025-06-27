package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;

import java.util.Locale;
import java.util.Map;

public interface MessageSource {

    String getMessage(String key, Locale locale) throws NoSuchMessageException;
    Map<String, String> getPluralForms(String keyPrefix, Locale locale);
}
