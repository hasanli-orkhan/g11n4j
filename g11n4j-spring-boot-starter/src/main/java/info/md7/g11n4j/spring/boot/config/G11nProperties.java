package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.model.SourceType;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.Locale;

@Configuration
@ConfigurationProperties(prefix = "spring.g11n")
public class G11nProperties {

    private SourceType type = SourceType.PROPERTIES;
    private String baseDirectory = "i18n";
    private String fileBaseName = "messages";
    private String localeSeparator = "_";
    private String fileExtension = "properties";
    private Locale defaultLocale = Locale.ENGLISH;
    private List<Locale> locales;

    public SourceType getType() {
        return type;
    }

    public void setType(SourceType type) {
        this.type = type;
    }

    public String getBaseDirectory() {
        return baseDirectory;
    }

    public void setBaseDirectory(String baseDirectory) {
        this.baseDirectory = baseDirectory;
    }

    public String getFileBaseName() {
        return fileBaseName;
    }

    public void setFileBaseName(String fileBaseName) {
        this.fileBaseName = fileBaseName;
    }

    public String getLocaleSeparator() {
        return localeSeparator;
    }

    public void setLocaleSeparator(String localeSeparator) {
        this.localeSeparator = localeSeparator;
    }

    public String getFileExtension() {
        return fileExtension;
    }

    public void setFileExtension(String fileExtension) {
        this.fileExtension = fileExtension;
    }

    public Locale getDefaultLocale() {
        return defaultLocale;
    }

    public void setDefaultLocale(Locale defaultLocale) {
        this.defaultLocale = defaultLocale;
    }

    public List<Locale> getLocales() {
        return locales;
    }

    public void setLocales(List<Locale> locales) {
        this.locales = locales;
    }
}
