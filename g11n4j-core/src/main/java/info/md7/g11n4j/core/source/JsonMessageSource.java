package info.md7.g11n4j.core.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.model.SourceType;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class JsonMessageSource extends AbstractMessageSource {

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    public JsonMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        if (!SourceType.JSON.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be 'json'.");
        }
    }

    public JsonMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        if (!SourceType.JSON.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be 'json'.");
        }
    }

    @Override
    protected void loadMessages() {
        for (Locale locale : supportedLocales) {
            for (String filename : buildCandidateFilenames(locale)) {
                try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                    if (is != null) {
                        Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(is, new TypeReference<Map<String, Object>>() {});
                        if (jsonMap != null) {
                            Map<String, String> flatMap = flatten("", jsonMap);
                            messages.put(locale, flatMap);
                        }
                        break;
                    }
                } catch (Exception e) {
                    throw new MessageLoadException(filename, e);
                }
            }
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, String> flatten(String prefix, Map<String, Object> source) {
        Map<String, String> result = new LinkedHashMap<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                result.putAll(flatten(key, (Map<String, Object>) value));
            } else if (value != null) {
                result.put(key, value.toString());
            }
        }
        return result;
    }
}
