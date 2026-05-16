package info.md7.g11n4j.spring.boot.endpoint;

import info.md7.g11n4j.core.source.MessageSource;
import org.junit.jupiter.api.Test;

import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

class MessageSourceReloadEndpointTests {

    @Test
    void shouldReloadAllMessages() {
        MessageSource messageSource = mock(MessageSource.class);
        MessageSourceReloadEndpoint endpoint = new MessageSourceReloadEndpoint(messageSource);

        MessageSourceReloadEndpoint.ReloadResponse response = endpoint.reloadAll();

        verify(messageSource).reload();
        assertThat(response.status()).isEqualTo("success");
        assertThat(response.locale()).isNull();
    }

    @Test
    void shouldReturnErrorWhenReloadAllFails() {
        MessageSource messageSource = mock(MessageSource.class);
        doThrow(new IllegalStateException("boom")).when(messageSource).reload();
        MessageSourceReloadEndpoint endpoint = new MessageSourceReloadEndpoint(messageSource);

        MessageSourceReloadEndpoint.ReloadResponse response = endpoint.reloadAll();

        assertThat(response.status()).isEqualTo("error");
        assertThat(response.message()).contains("boom");
        assertThat(response.locale()).isNull();
    }

    @Test
    void shouldReloadLocaleFromUnderscoreTag() {
        MessageSource messageSource = mock(MessageSource.class);
        MessageSourceReloadEndpoint endpoint = new MessageSourceReloadEndpoint(messageSource);

        MessageSourceReloadEndpoint.ReloadResponse response = endpoint.reloadLocale("en_US");

        verify(messageSource).reload(Locale.forLanguageTag("en-US"));
        assertThat(response.status()).isEqualTo("success");
        assertThat(response.locale()).isEqualTo("en_US");
    }

    @Test
    void shouldReturnErrorForBlankLocale() {
        MessageSource messageSource = mock(MessageSource.class);
        MessageSourceReloadEndpoint endpoint = new MessageSourceReloadEndpoint(messageSource);

        MessageSourceReloadEndpoint.ReloadResponse response = endpoint.reloadLocale(" ");

        assertThat(response.status()).isEqualTo("error");
        assertThat(response.message()).contains("Locale must not be blank");
    }
}
