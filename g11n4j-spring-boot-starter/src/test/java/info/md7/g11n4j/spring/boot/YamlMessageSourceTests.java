package info.md7.g11n4j.spring.boot;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Locale;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest(classes = G11n4jApplication.class)
@ExtendWith(MockitoExtension.class)
@ActiveProfiles("yaml")
public class YamlMessageSourceTests {

    @Autowired
    private MessageResolver messageResolver;

    @Autowired
    private G11nProperties g11nProperties;

    @Test
    void propertiesAreLoadedFromCustomFile() {
        assertThat(g11nProperties.getBaseDirectory()).isEqualTo("classpath:i18n-yaml");
        assertThat(g11nProperties.getType()).isEqualTo(SourceType.YAML);
    }

    @Test
    void givenSingleParam_WhenRenderingSubject_thenResultIsCorrect() {
        String mailSubjectEn = messageResolver.get("mail.subject", Locale.ENGLISH)
                .render("website", "example.com");

        assertThat("Verification code to confirm your account on the website - example.com").isEqualTo(mailSubjectEn);
    }

    @Test
    void givenMapOfParams_whenRenderingBody_thenResultIsCorrect() {
        String randomToken = UUID.randomUUID().toString();
        String mailBodyEn = messageResolver.get("mail.body", Locale.ENGLISH)
                .render(Map.of(
                        "fullName", "John Smith",
                        "code", randomToken
                ));
        String finalMessage = String.format("Dear %s, your verification code is %s.", "John Smith", randomToken);

        assertThat(mailBodyEn).isEqualTo(finalMessage);
    }

    @Test
    void givenArrayOfParams_whenRenderingBody_thenResultIsCorrect() {
        String mailBodyEn = messageResolver.get("mail.footer", Locale.ENGLISH)
                .render(new Object[] { "John Smith", "Example" });

        assertThat(mailBodyEn).isEqualTo("Kindly Regards, John Smith from the Example Team.");
    }

    @Test
    void givenPluralizedParams_whenRenderingBodyInEnglish_thenResultIsCorrect() {
        String singleNotificationEn = messageResolver.getPlural(
                "notification.message.count", 1, Locale.ENGLISH, Map.of());
        String notificationsEn = messageResolver.getPlural(
                "notification.message.count", 15, Locale.ENGLISH, Map.of());

        assertThat(singleNotificationEn).isEqualTo("You have 1 new notification");
        assertThat(notificationsEn).isEqualTo("You have 15 new notifications");
    }

    @Test
    void givenPluralizedParams_whenRenderingBodyInRussian_thenResultIsCorrect() {
        Locale russianLocale = Locale.forLanguageTag("ru");
        String notification1Ru = messageResolver.getPlural(
                "notification.message.count", 1, russianLocale, Map.of());
        String notification2Ru = messageResolver.getPlural(
                "notification.message.count", 2, russianLocale, Map.of());
        String notification91Ru = messageResolver.getPlural(
                "notification.message.count", 91, russianLocale, Map.of());
        String notification100Ru = messageResolver.getPlural(
                "notification.message.count", 100, russianLocale, Map.of());

        assertThat(notification1Ru).isEqualTo("У вас 1 непрочитанное уведомление");
        assertThat(notification2Ru).isEqualTo("У вас 2 непрочитанных уведомления");
        assertThat(notification91Ru).isEqualTo("У вас 91 непрочитанное уведомление");
        assertThat(notification100Ru).isEqualTo("У вас 100 непрочитанных уведомлений");
    }

}
