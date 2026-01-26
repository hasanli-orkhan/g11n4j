package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.model.SourceType;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

public class PropertiesMessageSource extends AbstractMessageSource {

    public PropertiesMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        if (!SourceType.PROPERTIES.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be 'properties'.");
        }
    }

    @Override
    protected void loadMessages() {
        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + "/" + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    Properties properties = new Properties();
                    properties.load(new InputStreamReader(is, StandardCharsets.UTF_8));
                    Map<String, String> flatMap = new HashMap<>();
                    for (Map.Entry<Object, Object> entry : properties.entrySet()) {
                        flatMap.put(String.valueOf(entry.getKey()), String.valueOf(entry.getValue()));
                    }
                    messages.put(locale, flatMap);
                }
            } catch (IOException e) {
                throw new MessageLoadException(filename, e);
            }
        }
    }

}
