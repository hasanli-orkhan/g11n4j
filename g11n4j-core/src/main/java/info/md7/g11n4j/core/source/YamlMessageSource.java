package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.model.SourceType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YamlMessageSource extends AbstractMessageSource {

    public YamlMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        if (!SourceType.YAML.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }
    }

    public YamlMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        if (!SourceType.YAML.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension);
        }
    }

    @Override
    protected void loadMessages() {
        final Yaml yaml = new Yaml();
        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + "/" + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    Map<String, Object> yamlMap = yaml.load(is);
                    if (yamlMap != null) {
                        Map<String, String> flatMap = flatten("", yamlMap);
                        messages.put(locale, flatMap);
                    }
                }
            } catch (Exception e) {
                throw new MessageLoadException(filename, e);
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
