package info.md7.g11n4j.core;

import info.md7.g11n4j.core.source.PropertiesMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.io.InputStream;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

class MessageSourceReloadBehaviorTests {

    @Test
    void reloadLocale_shouldReloadOnlyRequestedLocale() {
        AtomicInteger parseCalls = new AtomicInteger();
        PropertiesMessageSource messageSource = new PropertiesMessageSource(
                "i18n-properties",
                "messages",
                "_",
                "properties",
                Locale.ENGLISH,
                List.of(Locale.ENGLISH, Locale.forLanguageTag("ru"))
        ) {
            @Override
            protected Map<String, String> parseMessageFile(InputStream is) throws java.io.IOException {
                parseCalls.incrementAndGet();
                return super.parseMessageFile(is);
            }
        };

        Assertions.assertEquals(2, parseCalls.get(), "initial load should parse one file per locale");

        messageSource.reload(Locale.ENGLISH);
        Assertions.assertEquals(3, parseCalls.get(), "locale reload should parse only one file");

        messageSource.reload();
        Assertions.assertEquals(5, parseCalls.get(), "full reload should parse every locale file again");
    }
}
