package info.md7.g11n4j.core.source;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import info.md7.g11n4j.core.model.SourceType;

import java.io.InputStream;
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
        validateSupportedExtension(SourceType.JSON, fileExtension);
    }

    public JsonMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        validateSupportedExtension(SourceType.JSON, fileExtension);
    }

    public JsonMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            List<String> additionalDirectories
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, additionalDirectories);
        validateSupportedExtension(SourceType.JSON, fileExtension);
    }

    public JsonMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            List<String> additionalDirectories,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, additionalDirectories, cacheSize);
        validateSupportedExtension(SourceType.JSON, fileExtension);
    }

    @Override
    protected Map<String, String> parseMessageFile(InputStream is) throws Exception {
        Map<String, Object> jsonMap = OBJECT_MAPPER.readValue(is, new TypeReference<Map<String, Object>>() {});
        return flattenNestedMap(jsonMap);
    }
}
