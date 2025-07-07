package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;

import java.util.*;

public abstract class AbstractMessageSource implements MessageSource {

    protected final Map<Locale, Map<String, String>> messages = new HashMap<>();

    /**
     * Base directory for message files (e.g., "i18n").
     */
    protected final String baseDirectory;

    /**
     * Locale separator (e.g., "_" for "messages_en.properties").
     */
    protected final String localeSeparator;

    /**
     * Base file name for message files (e.g., "messages").
     */
    protected final String fileBaseName;

    /**
     * File extension (must be "properties").
     */
    protected final String fileExtension;

    /**
     * Default locale.
     */
    protected final Locale defaultLocale;

    /**
     * List of locales the application supports.
     */
    protected final List<Locale> supportedLocales;

    public AbstractMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        this.baseDirectory = baseDirectory;
        this.fileBaseName = fileBaseName;
        this.localeSeparator = localeSeparator;
        this.fileExtension = fileExtension;
        this.defaultLocale = defaultLocale;
        this.supportedLocales = supportedLocales;
        loadMessages();
    }

    protected abstract void loadMessages();

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null) {
            throw new NoSuchMessageException(key, locale);
        }
        String message = localeMessages.get(key);
        if (message != null) {
            return message;
        }
        message = localeMessages.get(key + "._base");
        if (message != null) {
            return message;
        }

        throw new NoSuchMessageException(key, locale);
    }

    @Override
    public String getMessage(String key, Locale locale, MessageContext context) throws NoSuchMessageException {
        if (context == null || context.getContextMap().isEmpty()) {
            return this.getMessage(key, locale);
        }

        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null) {
            throw new NoSuchMessageException(key, locale);
        }
        for (Map.Entry<String, String> contextEntry : context.getContextMap().entrySet()) {
            String contextKey = contextEntry.getKey();
            String contextValue = contextEntry.getValue();

            String message = localeMessages.get(key + "." + contextKey + "." + contextValue);
            if (message != null) {
                return message;
            }

            message = localeMessages.get(key + "." + contextKey + ".other");
            if (message != null) {
                return message;
            }
        }

        return this.getMessage(key, locale);
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale) {
        return getPluralForms(keyPrefix, locale, new MessageContext());
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale, MessageContext context) {
        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null) {
            return Collections.emptyMap();
        }
        List<String> potentialPrefixes = new ArrayList<>();
        if (context != null && !context.getContextMap().isEmpty()) {
            Map.Entry<String, String> entry = context.getContextMap().entrySet().iterator().next();
            potentialPrefixes.add(keyPrefix + "." + entry.getKey() + "." + entry.getValue());
        }
        potentialPrefixes.add(keyPrefix + "._base");
        potentialPrefixes.add(keyPrefix);
        for (String prefix : potentialPrefixes) {
            Map<String, String> forms = getPluralFormsForPrefix(localeMessages, prefix);
            if (!forms.isEmpty()) {
                return forms;
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, String> getPluralFormsForPrefix(Map<String, String> allMessages, String prefix) {
        Map<String, String> forms = new HashMap<>();
        String searchPrefix = prefix + ".";
        for (Map.Entry<String, String> entry : allMessages.entrySet()) {
            if (entry.getKey().startsWith(searchPrefix)) {
                String pluralCategory = entry.getKey().substring(searchPrefix.length());
                if (!pluralCategory.contains(".")) {
                    forms.put(pluralCategory, entry.getValue());
                }
            }
        }
        return forms;
    }

}
