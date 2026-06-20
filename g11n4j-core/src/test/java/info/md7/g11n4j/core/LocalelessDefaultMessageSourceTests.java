package info.md7.g11n4j.core;

import info.md7.g11n4j.core.source.PropertiesMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

class LocalelessDefaultMessageSourceTests {

    private static final PropertiesMessageSource MESSAGE_SOURCE = createMessageSource();

    private static PropertiesMessageSource createMessageSource() {
        var supportedLocales = List.of(
                Locale.ENGLISH,
                Locale.GERMAN,
                Locale.FRENCH,
                Locale.ITALIAN
        );
        return new PropertiesMessageSource(
                "classpath:i18n-properties-bare",
                "messages",
                "_",
                "properties",
                Locale.ENGLISH,
                supportedLocales
        );
    }

    @Test
    void whenLocaleHasNoSuffixedFile_thenBareDefaultFileIsUsed() {
        Assertions.assertEquals(
                "Hello from default file",
                MESSAGE_SOURCE.getMessage("greeting", Locale.FRENCH)
        );
        Assertions.assertEquals(
                "Hello from default file",
                MESSAGE_SOURCE.getMessage("greeting", Locale.ITALIAN)
        );
    }

    @Test
    void whenSuffixedFileExists_thenItTakesPriorityOverBareFile() {
        Assertions.assertEquals(
                "Hallo",
                MESSAGE_SOURCE.getMessage("greeting", Locale.GERMAN)
        );
    }

    @Test
    void whenDefaultLocaleHasNoSuffixedFile_thenBareFileIsResolved() {
        Assertions.assertEquals(
                "Hello from default file",
                MESSAGE_SOURCE.getMessage("greeting", Locale.ENGLISH)
        );
    }
}
