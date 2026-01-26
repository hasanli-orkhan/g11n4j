package info.md7.g11n4j.core;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.source.GettextMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class GettextMessageSourceTests {

    private static MessageResolver messageResolver;
    private static Locale russianLocale = Locale.forLanguageTag("ru");

    @BeforeAll
    static void setup() {
        var supportedLocales = List.of(Locale.ENGLISH, russianLocale);
        var messageSource = new GettextMessageSource(
                "i18n-gettext", "messages", "_",
                "po", Locale.ENGLISH, supportedLocales
        );
        messageResolver = new MessageResolver(messageSource);
    }

    @Test
    void whenRenderingSubjectWithOneParameter_thenResultIsCorrect() {
        String mailSubjectEn = messageResolver.get("mail.subject", Locale.ENGLISH)
                .render("website", "example.com");

        Assertions.assertEquals("Verification code to confirm your account on the website - example.com", mailSubjectEn);
    }

    @Test
    void whenRenderingGreetingWithContext_thenResultIsCorrect() {
        String maleGreeting = messageResolver.get("greeting", Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");
        String femaleGreeting = messageResolver.get("greeting", Locale.ENGLISH)
                .withContext("gender", "female")
                .render("fullName", "Jane Smith");

        Assertions.assertEquals("Hello, Mr. John Smith", maleGreeting);
        Assertions.assertEquals("Hello, Ms. Jane Smith", femaleGreeting);
    }

    @Test
    void whenRenderingBodyWithMultipleParametersWithMap_thenResultIsCorrect() {
        String randomToken = UUID.randomUUID().toString();
        String mailBodyEn = messageResolver.get("mail.body", Locale.ENGLISH)
                .render(Map.of(
                        "fullName", "John Smith",
                        "code", randomToken
                ));
        String finalMessage = String.format("Dear %s, your verification code is %s.", "John Smith", randomToken);

        Assertions.assertEquals(finalMessage, mailBodyEn);
    }

    @Test
    void whenRenderingBodyWithMultipleParametersWithArray_thenResultIsCorrect() {
        String mailBodyEn = messageResolver.get("mail.footer", Locale.ENGLISH)
                .render(new Object[] { "John Smith", "Example" });

        Assertions.assertEquals("Kindly Regards, John Smith from the Example Team.", mailBodyEn);
    }

    @Test
    void whenRenderingBodyWithPluralizationInEnglish_thenResultIsCorrect() {
        String singleNotificationEn = messageResolver.getPlural(
                "notification.message.count", 1, Locale.ENGLISH, Map.of());
        String notificationsEn = messageResolver.getPlural(
                "notification.message.count", 15, Locale.ENGLISH, Map.of());

        Assertions.assertEquals("You have 1 new notification", singleNotificationEn);
        Assertions.assertEquals("You have 15 new notifications", notificationsEn);
    }

    @Test
    void whenRenderingBodyWithPluralizationInRussian_thenResultIsCorrect() {
        String notification1Ru = messageResolver.getPlural(
                "notification.message.count", 1, russianLocale, Map.of());
        String notification2Ru = messageResolver.getPlural(
                "notification.message.count", 2, russianLocale, Map.of());
        String notification3Ru = messageResolver.getPlural(
                "notification.message.count", 91, russianLocale, Map.of());
        String notification4Ru = messageResolver.getPlural(
                "notification.message.count", 100, russianLocale, Map.of());

        Assertions.assertEquals("У вас 1 непрочитанное уведомление", notification1Ru);
        Assertions.assertEquals("У вас 2 непрочитанных уведомления", notification2Ru);
        Assertions.assertEquals("У вас 91 непрочитанное уведомление", notification3Ru);
        Assertions.assertEquals("У вас 100 непрочитанных уведомлений", notification4Ru);
    }
}
