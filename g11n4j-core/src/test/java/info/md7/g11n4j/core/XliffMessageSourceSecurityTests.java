package info.md7.g11n4j.core;

import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.source.XliffMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

class XliffMessageSourceSecurityTests {

    @Test
    void constructor_shouldRejectXxePayloads() {
        Assertions.assertThrows(MessageLoadException.class, () -> new XliffMessageSource(
                "i18n-xliff-malicious",
                "messages",
                "_",
                "xlf",
                Locale.ENGLISH,
                List.of(Locale.ENGLISH)
        ));
    }
}
