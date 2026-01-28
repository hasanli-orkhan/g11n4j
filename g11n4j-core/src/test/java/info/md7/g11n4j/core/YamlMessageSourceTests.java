package info.md7.g11n4j.core;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.source.YamlMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

public class YamlMessageSourceTests {

    private static MessageResolver messageResolver;
    private static Locale russianLocale = Locale.forLanguageTag("ru");
    private static Locale chineseLocale = Locale.forLanguageTag("zh");
    private static Locale germanLocale = Locale.forLanguageTag("de");
    private static Locale frenchLocale = Locale.forLanguageTag("fr");

    @BeforeAll
    static void setup() {
        var supportedLocales = List.of(Locale.ENGLISH, russianLocale, chineseLocale, germanLocale, frenchLocale);
        var messageSource = new YamlMessageSource(
                "i18n-yaml", "messages", "_",
                "yml", Locale.ENGLISH, supportedLocales
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

    @Test
    void whenRenderingBodyWithPluralizationAndContextInEnglish_thenResultIsCorrect() {
        String singleNotificationEnMale = messageResolver.getPlural(
                "push_notification", 1, Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");

        String notificationsEnMale = messageResolver.getPlural(
                "push_notification", 15, Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");

        Assertions.assertEquals("Mr. John Smith, you have 1 new push notification", singleNotificationEnMale);
        Assertions.assertEquals("Mr. John Smith, you have 15 new push notifications", notificationsEnMale);
    }

    @Test
    void whenRenderingInChinese_thenResultIsCorrect() {
        String mailSubjectZh = messageResolver.get("mail.subject", chineseLocale)
                .render("website", "example.com");
        String greetingZh = messageResolver.get("greeting", chineseLocale)
                .withContext("gender", "male")
                .render("fullName", "李明");
        String notificationZh = messageResolver.getPlural(
                "notification.message.count", 5, chineseLocale, Map.of());

        Assertions.assertEquals("确认您在网站上的帐户的验证码 - example.com", mailSubjectZh);
        Assertions.assertEquals("您好，李明 先生", greetingZh);
        Assertions.assertEquals("您有 5 条新通知", notificationZh);
    }

    @Test
    void whenRenderingInGerman_thenResultIsCorrect() {
        String mailSubjectDe = messageResolver.get("mail.subject", germanLocale)
                .render("website", "beispiel.de");
        String greetingDe = messageResolver.get("greeting", germanLocale)
                .withContext("gender", "female")
                .render("fullName", "Schmidt");
        String notificationSingleDe = messageResolver.getPlural(
                "notification.message.count", 1, germanLocale, Map.of());
        String notificationPluralDe = messageResolver.getPlural(
                "notification.message.count", 5, germanLocale, Map.of());

        Assertions.assertEquals("Bestätigungscode zur Bestätigung Ihres Kontos auf der Website - beispiel.de", mailSubjectDe);
        Assertions.assertEquals("Hallo, Frau Schmidt", greetingDe);
        Assertions.assertEquals("Sie haben 1 neue Benachrichtigung", notificationSingleDe);
        Assertions.assertEquals("Sie haben 5 neue Benachrichtigungen", notificationPluralDe);
    }

    @Test
    void whenRenderingInFrench_thenResultIsCorrect() {
        String mailBodyFr = messageResolver.get("mail.body", frenchLocale)
                .render(Map.of("fullName", "Dupont", "code", "ABC123"));
        String greetingFr = messageResolver.get("greeting", frenchLocale)
                .withContext("gender", "male")
                .render("fullName", "Martin");
        String notificationSingleFr = messageResolver.getPlural(
                "notification.message.count", 1, frenchLocale, Map.of());
        String notificationPluralFr = messageResolver.getPlural(
                "notification.message.count", 10, frenchLocale, Map.of());

        Assertions.assertEquals("Cher(ère) Dupont, votre code de vérification est ABC123.", mailBodyFr);
        Assertions.assertEquals("Bonjour, M. Martin", greetingFr);
        Assertions.assertEquals("Vous avez 1 nouvelle notification", notificationSingleFr);
        Assertions.assertEquals("Vous avez 10 nouvelles notifications", notificationPluralFr);
    }

}
