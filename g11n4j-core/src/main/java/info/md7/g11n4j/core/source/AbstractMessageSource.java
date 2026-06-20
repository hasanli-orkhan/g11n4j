package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.cache.MessageCache;
import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.core.validation.MessageSourceKeyProvider;

import java.io.InputStream;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public abstract class AbstractMessageSource implements MessageSource, MessageSourceKeyProvider {

    private static final String BASE_SUFFIX = "._base";
    private static final String OTHER_SUFFIX = ".other";
    private static final int DEFAULT_CACHE_SIZE = 1000;
    private static final String CLASSPATH_PREFIX = "classpath:";

    private final Map<Locale, List<Locale>> fallbackChainCache = new ConcurrentHashMap<>();
    protected final MessageCache messageCache;
    private volatile Map<Locale, Map<String, String>> messagesByLocale = Map.of();
    private volatile Map<Locale, Map<String, Map<String, String>>> pluralFormsIndexByLocale = Map.of();

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

        this.baseDirectory = normalizeBaseDirectory(baseDirectory);
        this.fileBaseName = fileBaseName;
        this.localeSeparator = localeSeparator;
        this.fileExtension = fileExtension;
        this.defaultLocale = defaultLocale;
        this.supportedLocales = List.copyOf(supportedLocales);
        this.messageCache = new MessageCache(cacheSize);
        reloadAllMessages();
    }

    private String normalizeBaseDirectory(String configuredBaseDirectory) {
        String normalized = configuredBaseDirectory.trim();

        if (normalized.startsWith(CLASSPATH_PREFIX)) {
            normalized = normalized.substring(CLASSPATH_PREFIX.length());
        }
        while (normalized.startsWith("/")) {
            normalized = normalized.substring(1);
        }
        while (normalized.endsWith("/")) {
            normalized = normalized.substring(0, normalized.length() - 1);
        }

        if (normalized.isEmpty()) {
            throw new IllegalArgumentException("baseDirectory cannot be blank after normalization");
        }
        return normalized;
    }

    protected abstract Map<String, String> parseMessageFile(InputStream is) throws Exception;

    protected final void validateSupportedExtension(SourceType sourceType, String extension) {
        if (sourceType == null) {
            throw new IllegalArgumentException("sourceType cannot be null");
        }
        if (extension == null || extension.trim().isEmpty()) {
            throw new IllegalArgumentException("fileExtension cannot be null or empty");
        }
        if (!sourceType.getExtensions().contains(extension)) {
            throw new IllegalArgumentException(
                    "Unsupported file extension: " + extension + ". Must be one of: " + sourceType.getExtensions()
            );
        }
    }

    @SuppressWarnings("unchecked")
    protected final Map<String, String> flattenNestedMap(Map<String, Object> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        Map<String, String> result = new LinkedHashMap<>();
        flattenInto("", source, result);
        return result;
    }

    @SuppressWarnings("unchecked")
    private void flattenInto(String prefix, Map<String, Object> source, Map<String, String> target) {
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map<?, ?> nestedMap) {
                flattenInto(key, (Map<String, Object>) nestedMap, target);
            } else if (value != null) {
                target.put(key, value.toString());
            }
        }
    }

    @Override
    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    @Override
    public List<Locale> getSupportedLocales() {
        return Collections.unmodifiableList(supportedLocales);
    }

    @Override
    public Map<Locale, Set<String>> getKeysByLocale() {
        Map<Locale, Set<String>> keysByLocale = new LinkedHashMap<>();
        for (Map.Entry<Locale, Map<String, String>> entry : messagesByLocale.entrySet()) {
            keysByLocale.put(entry.getKey(), Set.copyOf(entry.getValue().keySet()));
        }
        return Collections.unmodifiableMap(keysByLocale);
    }

    protected List<String> buildCandidateFilenames(Locale locale) {
        String language = locale.getLanguage();
        String country = locale.getCountry();

        List<String> candidates = new ArrayList<>(3);
        if (language != null && !language.isEmpty()) {
            if (country != null && !country.isEmpty()) {
                candidates.add(baseDirectory + "/" + fileBaseName + localeSeparator + language + localeSeparator + country + "." + fileExtension);
            }
            candidates.add(baseDirectory + "/" + fileBaseName + localeSeparator + language + "." + fileExtension);
        }
        String bareFilename = baseDirectory + "/" + fileBaseName + "." + fileExtension;
        if (!candidates.contains(bareFilename)) {
            candidates.add(bareFilename);
        }
        return Collections.unmodifiableList(candidates);
    }

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
        return fallbackChainCache.computeIfAbsent(locale, key -> {
            List<Locale> chain = new ArrayList<>();
            chain.add(key);

            if (!key.getCountry().isEmpty()) {
                Locale languageOnlyLocale = Locale.forLanguageTag(key.getLanguage());
                if (!chain.contains(languageOnlyLocale)) {
                    chain.add(languageOnlyLocale);
                }
            }

            if (!chain.contains(defaultLocale)) {
                chain.add(defaultLocale);
            }

            return Collections.unmodifiableList(chain);
        });
    }

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        validateKey(key);
        validateLocale(locale);

        Map<Locale, Map<String, String>> currentMessages = messagesByLocale;
        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, String> localeMessages = currentMessages.get(fallbackLocale);
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

        if (context == null || context.getContextMapView().isEmpty()) {
            return getMessage(key, locale);
        }

        String cacheKey = buildCacheKey(key, locale, context);
        String cachedMessage = messageCache.get(cacheKey);
        if (cachedMessage != null) {
            return cachedMessage;
        }

        StringBuilder keyBuilder = new StringBuilder(key.length() + 20);
        Map<Locale, Map<String, String>> currentMessages = messagesByLocale;
        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, String> localeMessages = currentMessages.get(fallbackLocale);
            if (localeMessages != null) {
                for (Map.Entry<String, String> contextEntry : context.getContextMapView().entrySet()) {
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

        String fallbackMessage = getMessage(key, locale);
        messageCache.put(cacheKey, fallbackMessage);
        return fallbackMessage;
    }

    private String buildCacheKey(String key, Locale locale, MessageContext context) {
        StringBuilder sb = new StringBuilder(key);
        sb.append(":").append(locale.toLanguageTag());
        Map<String, String> contextMap = context.getContextMapView();
        List<String> keys = new ArrayList<>(contextMap.keySet());
        Collections.sort(keys);
        for (String keyName : keys) {
            sb.append(":").append(keyName).append("=").append(contextMap.get(keyName));
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

        List<String> potentialPrefixes = new ArrayList<>(3);
        if (context != null && !context.getContextMapView().isEmpty()) {
            Map.Entry<String, String> entry = context.getContextMapView().entrySet().iterator().next();
            potentialPrefixes.add(keyPrefix + "." + entry.getKey() + "." + entry.getValue());
        }
        potentialPrefixes.add(keyPrefix + "._base");
        potentialPrefixes.add(keyPrefix);

        Map<Locale, Map<String, Map<String, String>>> currentPluralIndex = pluralFormsIndexByLocale;
        for (Locale fallbackLocale : buildFallbackChain(locale)) {
            Map<String, Map<String, String>> localePluralForms = currentPluralIndex.get(fallbackLocale);
            if (localePluralForms == null || localePluralForms.isEmpty()) {
                continue;
            }
            for (String prefix : potentialPrefixes) {
                Map<String, String> forms = localePluralForms.get(prefix);
                if (forms != null && !forms.isEmpty()) {
                    return forms;
                }
            }
        }
        return Collections.emptyMap();
    }

    @Override
    public void reload() {
        reloadAllMessages();
        messageCache.clear();
        fallbackChainCache.clear();
    }

    @Override
    public void reload(Locale locale) {
        validateLocale(locale);
        reloadSingleLocale(locale);
        messageCache.clear();
        fallbackChainCache.clear();
    }

    private void reloadAllMessages() {
        Map<Locale, Map<String, String>> reloadedMessages = new LinkedHashMap<>();
        Map<Locale, Map<String, Map<String, String>>> reloadedPluralIndex = new LinkedHashMap<>();

        for (Locale locale : supportedLocales) {
            Optional<Map<String, String>> loadedMessages = tryLoadLocaleMessages(locale);
            if (loadedMessages.isPresent()) {
                Map<String, String> localeMessages = loadedMessages.get();
                reloadedMessages.put(locale, localeMessages);
                reloadedPluralIndex.put(locale, buildPluralFormsIndex(localeMessages));
            }
        }

        messagesByLocale = Map.copyOf(reloadedMessages);
        pluralFormsIndexByLocale = Map.copyOf(reloadedPluralIndex);
    }

    private void reloadSingleLocale(Locale locale) {
        Map<Locale, Map<String, String>> updatedMessages = new LinkedHashMap<>(messagesByLocale);
        Map<Locale, Map<String, Map<String, String>>> updatedPluralIndex = new LinkedHashMap<>(pluralFormsIndexByLocale);

        Optional<Map<String, String>> loadedMessages = tryLoadLocaleMessages(locale);
        if (loadedMessages.isPresent()) {
            Map<String, String> localeMessages = loadedMessages.get();
            updatedMessages.put(locale, localeMessages);
            updatedPluralIndex.put(locale, buildPluralFormsIndex(localeMessages));
        } else {
            updatedMessages.remove(locale);
            updatedPluralIndex.remove(locale);
        }

        messagesByLocale = Map.copyOf(updatedMessages);
        pluralFormsIndexByLocale = Map.copyOf(updatedPluralIndex);
    }

    private Optional<Map<String, String>> tryLoadLocaleMessages(Locale locale) {
        for (String filename : buildCandidateFilenames(locale)) {
            try (InputStream is = openResource(filename)) {
                if (is == null) {
                    continue;
                }
                Map<String, String> parsed = parseMessageFile(is);
                return Optional.of(toImmutableMap(parsed));
            } catch (Exception e) {
                throw new MessageLoadException(filename, e);
            }
        }
        return Optional.empty();
    }

    private InputStream openResource(String filename) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        if (classLoader == null) {
            classLoader = getClass().getClassLoader();
        }
        return classLoader.getResourceAsStream(filename);
    }

    private Map<String, Map<String, String>> buildPluralFormsIndex(Map<String, String> localeMessages) {
        if (localeMessages == null || localeMessages.isEmpty()) {
            return Map.of();
        }

        Map<String, Map<String, String>> index = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : localeMessages.entrySet()) {
            String fullKey = entry.getKey();
            int lastDot = fullKey.lastIndexOf('.');
            if (lastDot <= 0 || lastDot == fullKey.length() - 1) {
                continue;
            }

            String prefix = fullKey.substring(0, lastDot);
            String category = fullKey.substring(lastDot + 1);
            index.computeIfAbsent(prefix, ignored -> new LinkedHashMap<>())
                    .put(category, entry.getValue());
        }

        Map<String, Map<String, String>> immutableIndex = new LinkedHashMap<>();
        for (Map.Entry<String, Map<String, String>> entry : index.entrySet()) {
            immutableIndex.put(entry.getKey(), toImmutableMap(entry.getValue()));
        }
        return Map.copyOf(immutableIndex);
    }

    private Map<String, String> toImmutableMap(Map<String, String> source) {
        if (source == null || source.isEmpty()) {
            return Map.of();
        }
        return Map.copyOf(new LinkedHashMap<>(source));
    }

}
