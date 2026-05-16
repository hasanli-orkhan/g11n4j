package info.md7.g11n4j.core;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.source.JsonMessageSource;

import java.util.List;
import java.util.Locale;

public class JsonMessageSourceTests extends AbstractMessageSourceContractTests {

    private static final MessageResolver RESOLVER = createResolver();

    @Override
    protected MessageResolver messageResolver() {
        return RESOLVER;
    }

    private static MessageResolver createResolver() {
        var supportedLocales = List.of(
                Locale.ENGLISH,
                US_ENGLISH_LOCALE,
                RUSSIAN_LOCALE,
                CHINESE_LOCALE,
                GERMAN_LOCALE,
                FRENCH_LOCALE
        );
        var messageSource = new JsonMessageSource(
                "i18n-json",
                "messages",
                "_",
                "json",
                Locale.ENGLISH,
                supportedLocales
        );
        return new MessageResolver(messageSource);
    }
}
