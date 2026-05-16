package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.model.SourceType;
import org.yaml.snakeyaml.Yaml;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class YamlMessageSource extends AbstractMessageSource {

    private static final Yaml YAML = new Yaml();

    public YamlMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        validateSupportedExtension(SourceType.YAML, fileExtension);
    }

    public YamlMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        validateSupportedExtension(SourceType.YAML, fileExtension);
    }

    @Override
    protected Map<String, String> parseMessageFile(InputStream is) {
        Map<String, Object> yamlMap = YAML.load(is);
        return flattenNestedMap(yamlMap);
    }

}
