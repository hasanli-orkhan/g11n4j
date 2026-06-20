package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.model.SourceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Properties;

public class PropertiesMessageSource extends AbstractMessageSource {

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        validateSupportedExtension(SourceType.PROPERTIES, fileExtension);
    }

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        validateSupportedExtension(SourceType.PROPERTIES, fileExtension);
    }

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            List<String> additionalDirectories
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, additionalDirectories);
        validateSupportedExtension(SourceType.PROPERTIES, fileExtension);
    }

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            List<String> additionalDirectories,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, additionalDirectories, cacheSize);
        validateSupportedExtension(SourceType.PROPERTIES, fileExtension);
    }

    @Override
    protected Map<String, String> parseMessageFile(InputStream is) throws IOException {
        Properties properties = new Properties();
        properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
        Map<String, String> flatMap = new HashMap<>();
        for (Map.Entry<Object, Object> entry : properties.entrySet()) {
            flatMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
        }
        return flatMap;
    }

}
