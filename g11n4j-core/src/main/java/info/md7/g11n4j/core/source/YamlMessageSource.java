package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.i18n.MessageContext;
import org.yaml.snakeyaml.Yaml;

import java.io.File;
import java.io.InputStream;
import java.util.*;

public class YamlMessageSource implements MessageSource {

    private final Map<Locale, Map<String, String>> messages = new HashMap<>();

    /**
     * Base directory for message files.
     * For example, i18n
     */
    private final String baseDirectory;

    /**
     * Locale separator.
     * For example, "_" - messages_en.yml
     */
    private final String localeSeparator;

    /**
     * Base file name for message files.
     * For example, messages
     */
    private final String fileBaseName;

    /**
     * File extension.
     * For example, yml or yaml
     */
    private final String fileExtension;

    /**
     * Default locale.
     */
    private final Locale defaultLocale;

    /**
     * List of locales the application supports.
     */
    private final List<Locale> supportedLocales;

    public YamlMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        if (!"yaml".equals(fileExtension) && !"yml".equals(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }

        this.baseDirectory = baseDirectory;
        this.fileBaseName = fileBaseName;
        this.localeSeparator = localeSeparator;
        this.fileExtension = fileExtension;
        this.defaultLocale = defaultLocale;
        this.supportedLocales = supportedLocales;
        loadMessages();
    }

    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null) {
            throw new NoSuchMessageException(key, locale);
        }
        if (localeMessages.containsKey(key)) {
            return localeMessages.get(key);
        }
        String baseKey = key + "._base";
        if (localeMessages.containsKey(baseKey)) {
            return localeMessages.get(baseKey);
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

            String specificKey = key + "." + contextKey + "." + contextValue;
            if (localeMessages.containsKey(specificKey)) {
                return localeMessages.get(specificKey);
            }

            String fallbackKey = key + "." + contextKey + ".other";
            if (localeMessages.containsKey(fallbackKey)) {
                return localeMessages.get(fallbackKey);
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


    private void loadMessages() {
        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + File.separator + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    Yaml yaml = new Yaml();
                    Map<String, Object> yamlMap = yaml.load(is);
                    Map<String, String> flatMap = flatten("", yamlMap);
                    messages.put(locale, flatMap);
                }
            } catch (Exception e) {
                throw new RuntimeException("Failed to load: " + filename, e);
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> flatten(String prefix, Map<String, Object> source) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            if (entry.getValue() instanceof Map) {
                result.putAll(flatten(key, (Map<String, Object>) entry.getValue()));
            } else {
                result.put(key, entry.getValue().toString());
            }
        }
        return result;
    }

}
