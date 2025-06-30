package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;

import java.util.Locale;
import java.util.Map;

public interface MessageSource {

    String getMessage(String key, Locale locale) throws NoSuchMessageException;

    String getMessage(String key, Locale locale, MessageContext context) throws NoSuchMessageException;

    Map<String, String> getPluralForms(String keyPrefix, Locale locale);

    Map<String, String> getPluralForms(String keyPrefix, Locale locale, MessageContext context);
}
