package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.cache.MessageCache;
import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMessageSource implements MessageSource {

    private static final String BASE_SUFFIX = "._base";
    private static final String OTHER_SUFFIX = ".other";
    private static final int DEFAULT_CACHE_SIZE = 1000;

    protected final Map<Locale, Map<String, String>> messages = new ConcurrentHashMap<>();
    protected final MessageCache messageCache;

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
     * File extension for message files.
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
        this(baseDirectory, fileBaseName, localeSeparator, fileExtension,
             defaultLocale, supportedLocales, DEFAULT_CACHE_SIZE);
    }

    public AbstractMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        if (baseDirectory == null || baseDirectory.trim().isEmpty()) {
            throw new IllegalArgumentException("baseDirectory cannot be null or empty");
        }
        if (fileBaseName == null || fileBaseName.trim().isEmpty()) {
            throw new IllegalArgumentException("fileBaseName cannot be null or empty");
        }
        if (localeSeparator == null || localeSeparator.trim().isEmpty()) {
            throw new IllegalArgumentException("localeSeparator cannot be null or empty");
        }
        if (fileExtension == null || fileExtension.trim().isEmpty()) {
            throw new IllegalArgumentException("fileExtension cannot be null or empty");
        }
        if (defaultLocale == null) {
            throw new IllegalArgumentException("defaultLocale cannot be null");
        }
        if (supportedLocales == null || supportedLocales.isEmpty()) {
            throw new IllegalArgumentException("supportedLocales cannot be null or empty");
        }

        this.baseDirectory = baseDirectory;
        this.fileBaseName = fileBaseName;
        this.localeSeparator = localeSeparator;
        this.fileExtension = fileExtension;
        this.defaultLocale = defaultLocale;
        this.supportedLocales = supportedLocales;
        this.messageCache = new MessageCache(cacheSize);
        loadMessages();
    }

    protected abstract void loadMessages();

    /**
     * Validate that the key is not null or empty.
     */
    private void validateKey(String key) {
        if (key == null || key.trim().isEmpty()) {
            throw new IllegalArgumentException("key cannot be null or empty");
        }
    }

    /**
     * Validate that the locale is not null.
     */
    private void validateLocale(Locale locale) {
        if (locale == null) {
            throw new IllegalArgumentException("locale cannot be null");
        }
    }

    /**
     * Build a fallback chain for the given locale.
     * For example: en_US → en → default locale
     */
    private List<Locale> buildFallbackChain(Locale locale) {
        List<Locale> chain = new ArrayList<>();
        chain.add(locale);

        if (!locale.getCountry().isEmpty()) {
            Locale languageOnlyLocale = Locale.forLanguageTag(locale.getLanguage());
            if (!chain.contains(languageOnlyLocale)) {
                chain.add(languageOnlyLocale);
            }
        }

        if (!chain.contains(defaultLocale)) {
            chain.add(defaultLocale);
        }

        return chain;
    }

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        validateKey(key);
        validateLocale(locale);

        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, String> localeMessages = messages.get(fallbackLocale);
            if (localeMessages != null) {
                String message = localeMessages.get(key);
                if (message != null) {
                    return message;
                }

                message = localeMessages.get(key + BASE_SUFFIX);
                if (message != null) {
                    return message;
                }
            }
        }

        throw new NoSuchMessageException(key, locale);
    }

    @Override
    public String getMessage(String key, Locale locale, MessageContext context) throws NoSuchMessageException {
        validateKey(key);
        validateLocale(locale);

        if (context == null || context.getContextMap().isEmpty()) {
            return this.getMessage(key, locale);
        }

        String cacheKey = buildCacheKey(key, locale, context);
        String cachedMessage = messageCache.get(cacheKey);
        if (cachedMessage != null) {
            return cachedMessage;
        }

        StringBuilder keyBuilder = new StringBuilder(key.length() + 20);
        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, String> localeMessages = messages.get(fallbackLocale);
            if (localeMessages != null) {
                for (Map.Entry<String, String> contextEntry : context.getContextMap().entrySet()) {
                    String contextKey = contextEntry.getKey();
                    String contextValue = contextEntry.getValue();

                    keyBuilder.setLength(0);
                    keyBuilder.append(key).append('.').append(contextKey).append('.').append(contextValue);
                    String message = localeMessages.get(keyBuilder.toString());
                    if (message != null) {
                        messageCache.put(cacheKey, message);
                        return message;
                    }

                    keyBuilder.setLength(0);
                    keyBuilder.append(key).append('.').append(contextKey).append(OTHER_SUFFIX);
                    message = localeMessages.get(keyBuilder.toString());
                    if (message != null) {
                        messageCache.put(cacheKey, message);
                        return message;
                    }
                }
            }
        }

        String fallbackMessage = this.getMessage(key, locale);
        messageCache.put(cacheKey, fallbackMessage);
        return fallbackMessage;
    }

    private String buildCacheKey(String key, Locale locale, MessageContext context) {
        StringBuilder sb = new StringBuilder(key);
        sb.append(":").append(locale.toLanguageTag());
        for (Map.Entry<String, String> entry : context.getContextMap().entrySet()) {
            sb.append(":").append(entry.getKey()).append("=").append(entry.getValue());
        }
        return sb.toString();
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale) {
        return getPluralForms(keyPrefix, locale, new MessageContext());
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale, MessageContext context) {
        validateKey(keyPrefix);
        validateLocale(locale);

        List<String> potentialPrefixes = new ArrayList<>();
        if (context != null && !context.getContextMap().isEmpty()) {
            Map.Entry<String, String> entry = context.getContextMap().entrySet().iterator().next();
            potentialPrefixes.add(keyPrefix + "." + entry.getKey() + "." + entry.getValue());
        }
        potentialPrefixes.add(keyPrefix + "._base");
        potentialPrefixes.add(keyPrefix);

        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, String> localeMessages = messages.get(fallbackLocale);
            if (localeMessages != null) {
                for (String prefix : potentialPrefixes) {
                    Map<String, String> forms = getPluralFormsForPrefix(localeMessages, prefix);
                    if (!forms.isEmpty()) {
                        return forms;
                    }
                }
            }
        }
        return Collections.emptyMap();
    }

    private Map<String, String> getPluralFormsForPrefix(Map<String, String> allMessages, String prefix) {
        Map<String, String> forms = new HashMap<>();
        String searchPrefix = prefix + ".";
        int prefixLength = searchPrefix.length();

        for (Map.Entry<String, String> entry : allMessages.entrySet()) {
            String key = entry.getKey();
            if (key.startsWith(searchPrefix)) {
                String pluralCategory = key.substring(prefixLength);
                // Check if this is a direct plural form (no nested dots)
                if (pluralCategory.indexOf('.') == -1) {
                    forms.put(pluralCategory, entry.getValue());
                }
            }
        }
        return forms;
    }

    @Override
    public void reload() {
        messages.clear();
        messageCache.clear();
        loadMessages();
    }

    @Override
    public void reload(Locale locale) {
        messages.remove(locale);
        messageCache.clear();
        loadMessages();
    }

}
