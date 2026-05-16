package info.md7.g11n4j.core;

import info.md7.g11n4j.core.i18n.MessageResolver;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

abstract class AbstractMessageSourceContractTests {

    protected static final Locale RUSSIAN_LOCALE = Locale.forLanguageTag("ru");
    protected static final Locale CHINESE_LOCALE = Locale.forLanguageTag("zh");
    protected static final Locale GERMAN_LOCALE = Locale.forLanguageTag("de");
    protected static final Locale FRENCH_LOCALE = Locale.forLanguageTag("fr");
    protected static final Locale US_ENGLISH_LOCALE = Locale.forLanguageTag("en-US");

    protected abstract MessageResolver messageResolver();

    @Test
    void whenRenderingSubjectWithOneParameter_thenResultIsCorrect() {
        String mailSubjectEn = messageResolver().get("mail.subject", Locale.ENGLISH)
                .render("website", "example.com");

        Assertions.assertEquals("Verification code to confirm your account on the website - example.com", mailSubjectEn);
    }

    @Test
    void whenRenderingGreetingWithContext_thenResultIsCorrect() {
        String maleGreeting = messageResolver().get("greeting", Locale.ENGLISH)
                .withContext("gender", "male")
                .render("fullName", "John Smith");
        String femaleGreeting = messageResolver().get("greeting", Locale.ENGLISH)
                .withContext("gender", "female")
                .render("fullName", "Jane Smith");

        Assertions.assertEquals("Hello, Mr. John Smith", maleGreeting);
        Assertions.assertEquals("Hello, Ms. Jane Smith", femaleGreeting);
    }

    @Test
    void whenRenderingBodyWithMultipleParametersWithMap_thenResultIsCorrect() {
        String randomToken = UUID.randomUUID().toString();
        String mailBodyEn = messageResolver().get("mail.body", Locale.ENGLISH)
                .render(Map.of(
                        "fullName", "John Smith",
                        "code", randomToken
                ));
        String finalMessage = String.format("Dear %s, your verification code is %s.", "John Smith", randomToken);

        Assertions.assertEquals(finalMessage, mailBodyEn);
    }

    @Test
    void whenRenderingBodyWithMultipleParametersWithArray_thenResultIsCorrect() {
        String mailBodyEn = messageResolver().get("mail.footer", Locale.ENGLISH)
                .render(new Object[]{"John Smith", "Example"});

        Assertions.assertEquals("Kindly Regards, John Smith from the Example Team.", mailBodyEn);
    }

    @Test
    void whenRenderingBodyWithPluralizationInEnglish_thenResultIsCorrect() {
        String singleNotificationEn = messageResolver().getPlural(
                "notification.message.count", 1, Locale.ENGLISH, Map.of());
        String notificationsEn = messageResolver().getPlural(
                "notification.message.count", 15, Locale.ENGLISH, Map.of());

        Assertions.assertEquals("You have 1 new notification", singleNotificationEn);
        Assertions.assertEquals("You have 15 new notifications", notificationsEn);
    }

    @Test
    void whenRenderingPluralWithIndexedArgs_thenResultIsCorrect() {
        String singleItem = messageResolver().getPlural("items.count", 1, Locale.ENGLISH)
                .render(1);
        String multipleItems = messageResolver().getPlural("items.count", 3, Locale.ENGLISH)
                .render(3);

        Assertions.assertEquals("Item 1", singleItem);
        Assertions.assertEquals("Items 3", multipleItems);
    }

    @Test
    void whenRenderingWithRegionSpecificLocale_thenRegionFileIsUsed() {
        String mailSubjectUs = messageResolver().get("mail.subject", US_ENGLISH_LOCALE)
                .render("website", "example.com");

        Assertions.assertEquals("US verification code for example.com", mailSubjectUs);
    }

    @Test
    void whenRegionFileMissing_thenLanguageFallbackIsUsed() {
        Locale ukEnglish = Locale.forLanguageTag("en-GB");
        String mailSubjectUk = messageResolver().get("mail.subject", ukEnglish)
                .render("website", "example.com");

        Assertions.assertEquals("Verification code to confirm your account on the website - example.com", mailSubjectUk);
    }

    @Test
    void whenPluralArgsAreNull_thenResultIsCorrect() {
        String singleNotificationEn = messageResolver().getPlural(
                "notification.message.count", 1, Locale.ENGLISH, null);

        Assertions.assertEquals("You have 1 new notification", singleNotificationEn);
    }

    @Test
    void whenRenderingBodyWithPluralizationInRussian_thenResultIsCorrect() {
        String notification1Ru = messageResolver().getPlural(
                "notification.message.count", 1, RUSSIAN_LOCALE, Map.of());
        String notification2Ru = messageResolver().getPlural(
                "notification.message.count", 2, RUSSIAN_LOCALE, Map.of());
        String notification3Ru = messageResolver().getPlural(
                "notification.message.count", 91, RUSSIAN_LOCALE, Map.of());
        String notification4Ru = messageResolver().getPlural(
                "notification.message.count", 100, RUSSIAN_LOCALE, Map.of());

        Assertions.assertEquals("У вас 1 непрочитанное уведомление", notification1Ru);
        Assertions.assertEquals("У вас 2 непрочитанных уведомления", notification2Ru);
        Assertions.assertEquals("У вас 91 непрочитанное уведомление", notification3Ru);
        Assertions.assertEquals("У вас 100 непрочитанных уведомлений", notification4Ru);
    }

    @Test
    void whenRenderingInChinese_thenResultIsCorrect() {
        String mailSubjectZh = messageResolver().get("mail.subject", CHINESE_LOCALE)
                .render("website", "example.com");
        String greetingZh = messageResolver().get("greeting", CHINESE_LOCALE)
                .withContext("gender", "male")
                .render("fullName", "李明");
        String notificationZh = messageResolver().getPlural(
                "notification.message.count", 5, CHINESE_LOCALE, Map.of());

        Assertions.assertEquals("确认您在网站上的帐户的验证码 - example.com", mailSubjectZh);
        Assertions.assertEquals("您好，李明 先生", greetingZh);
        Assertions.assertEquals("您有 5 条新通知", notificationZh);
    }

    @Test
    void whenRenderingInGerman_thenResultIsCorrect() {
        String mailSubjectDe = messageResolver().get("mail.subject", GERMAN_LOCALE)
                .render("website", "beispiel.de");
        String greetingDe = messageResolver().get("greeting", GERMAN_LOCALE)
                .withContext("gender", "female")
                .render("fullName", "Schmidt");
        String notificationSingleDe = messageResolver().getPlural(
                "notification.message.count", 1, GERMAN_LOCALE, Map.of());
        String notificationPluralDe = messageResolver().getPlural(
                "notification.message.count", 5, GERMAN_LOCALE, Map.of());

        Assertions.assertEquals("Bestätigungscode zur Bestätigung Ihres Kontos auf der Website - beispiel.de", mailSubjectDe);
        Assertions.assertEquals("Hallo, Frau Schmidt", greetingDe);
        Assertions.assertEquals("Sie haben 1 neue Benachrichtigung", notificationSingleDe);
        Assertions.assertEquals("Sie haben 5 neue Benachrichtigungen", notificationPluralDe);
    }

    @Test
    void whenRenderingInFrench_thenResultIsCorrect() {
        String mailBodyFr = messageResolver().get("mail.body", FRENCH_LOCALE)
                .render(Map.of("fullName", "Dupont", "code", "ABC123"));
        String greetingFr = messageResolver().get("greeting", FRENCH_LOCALE)
                .withContext("gender", "male")
                .render("fullName", "Martin");
        String notificationSingleFr = messageResolver().getPlural(
                "notification.message.count", 1, FRENCH_LOCALE, Map.of());
        String notificationPluralFr = messageResolver().getPlural(
                "notification.message.count", 10, FRENCH_LOCALE, Map.of());

        Assertions.assertEquals("Cher(ère) Dupont, votre code de vérification est ABC123.", mailBodyFr);
        Assertions.assertEquals("Bonjour, M. Martin", greetingFr);
        Assertions.assertEquals("Vous avez 1 nouvelle notification", notificationSingleFr);
        Assertions.assertEquals("Vous avez 10 nouvelles notifications", notificationPluralFr);
    }
}
