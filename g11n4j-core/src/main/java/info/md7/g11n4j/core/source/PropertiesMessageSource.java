package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.NoSuchMessageException;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class PropertiesMessageSource implements MessageSource {

    private final Map<Locale, Map<String, String>> messages = new HashMap<>();

    /**
     * Base directory for message files (e.g., "i18n").
     */
    private final String baseDirectory;

    /**
     * Locale separator (e.g., "_" for "messages_en.properties").
     */
    private final String localeSeparator;

    /**
     * Base file name for message files (e.g., "messages").
     */
    private final String fileBaseName;

    /**
     * File extension (must be "properties").
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

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        if (!"properties".equals(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be 'properties'.");
        }

        this.baseDirectory = baseDirectory;
        this.fileBaseName = fileBaseName;
        this.localeSeparator = localeSeparator;
        this.fileExtension = fileExtension;
        this.supportedLocales = supportedLocales;
        this.defaultLocale = defaultLocale;
        loadMessages();
    }


    @Override
    public String getMessage(String key, Locale locale) throws NoSuchMessageException {
        Map<String, String> localeMessages = messages.getOrDefault(locale, messages.get(defaultLocale));
        if (localeMessages == null || !localeMessages.containsKey(key)) {
            throw new NoSuchMessageException(key, locale);
        }
        return localeMessages.getOrDefault(key, "[[" + key + "]]");
    }

    @Override
    public Map<String, String> getPluralForms(String keyPrefix, Locale locale) {
        Map<String, String> forms = new HashMap<>();
        messages.getOrDefault(locale, Map.of()).forEach((k, v) -> {
            if (k.startsWith(keyPrefix + ".")) {
                forms.put(k.substring(keyPrefix.length() + 1), v);
            }
        });
        return forms;
    }

    private void loadMessages() {
        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + File.separator + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    Properties properties = new Properties();
                    properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                    Map<String, String> stringMap = properties.entrySet().stream()
                            .collect(Collectors.toMap(
                                    e -> String.valueOf(e.getKey()),
                                    e -> String.valueOf(e.getValue())
                            ));
                    messages.put(locale, stringMap);
                }
            } catch (IOException e) {
                throw new RuntimeException("Failed to load properties file: " + filename, e);
            }
        }
    }

}
