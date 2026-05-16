package info.md7.g11n4j.core;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.source.YamlMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

public class YamlMessageSourceTests extends AbstractMessageSourceContractTests {

    private static final MessageResolver RESOLVER = createResolver();

    @Override
    protected MessageResolver messageResolver() {
        return RESOLVER;
    }

    @Test
    void whenRenderingBodyWithPluralizationAndContextInEnglish_thenResultIsCorrect() {
        String singleNotificationEnMale = messageResolver().getPlural(
                "push_notification", 1, Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");

        String notificationsEnMale = messageResolver().getPlural(
                "push_notification", 15, Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");

        Assertions.assertEquals("Mr. John Smith, you have 1 new push notification", singleNotificationEnMale);
        Assertions.assertEquals("Mr. John Smith, you have 15 new push notifications", notificationsEnMale);
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
        var messageSource = new YamlMessageSource(
                "i18n-yaml",
                "messages",
                "_",
                "yml",
                Locale.ENGLISH,
                supportedLocales
        );
        return new MessageResolver(messageSource);
    }
}
