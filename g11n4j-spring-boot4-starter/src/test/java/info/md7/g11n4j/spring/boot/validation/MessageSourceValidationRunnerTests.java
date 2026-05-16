package info.md7.g11n4j.spring.boot.validation;

import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.spring.boot.config.G11nProperties;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.boot.ApplicationArguments;

import java.util.List;
import java.util.Locale;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.catchThrowable;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MessageSourceValidationRunnerTests {

    @Mock
    private ApplicationArguments args;

    @Mock
    private MessageSourceValidator validator;

    private G11nProperties properties;
    private MessageSourceValidationRunner runner;

    @BeforeEach
    void setUp() {
        properties = new G11nProperties();
        properties.setType(SourceType.PROPERTIES);
        properties.setDefaultLocale(Locale.ENGLISH);
        properties.setLocales(List.of(Locale.ENGLISH, Locale.GERMAN));
    }

    @Test
    void shouldRunValidationSuccessfully() {
        ValidationResult validResult = ValidationResult.builder()
                .addSuccess(Locale.GERMAN)
                .build();

        when(validator.validate()).thenReturn(validResult);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldNotFailWhenValidationHasWarnings() {
        ValidationResult resultWithWarnings = ValidationResult.builder()
                .addWarning("Some warning")
                .addSuccess(Locale.GERMAN)
                .build();

        when(validator.validate()).thenReturn(resultWithWarnings);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw even with warnings
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldNotFailWhenValidationHasExtraKeys() {
        ValidationResult resultWithExtraKeys = ValidationResult.builder()
                .addExtraKeys(Locale.GERMAN, Set.of("extra.key"))
                .build();

        when(validator.validate()).thenReturn(resultWithExtraKeys);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw for extra keys (they don't make validation invalid)
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldNotFailByDefaultWhenValidationHasMissingKeys() {
        properties.getValidation().setFailOnError(false);

        ValidationResult resultWithMissingKeys = ValidationResult.builder()
                .addMissingKeys(Locale.GERMAN, Set.of("missing.key"))
                .build();

        when(validator.validate()).thenReturn(resultWithMissingKeys);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw when fail-on-error is false
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldFailWhenFailOnErrorEnabledAndValidationFails() {
        properties.getValidation().setFailOnError(true);

        ValidationResult failedResult = ValidationResult.builder()
                .addMissingKeys(Locale.GERMAN, Set.of("missing.key"))
                .build();

        when(validator.validate()).thenReturn(failedResult);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should throw IllegalStateException
        Throwable thrown = catchThrowable(() -> runner.run(args));
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(thrown.getMessage()).contains("Message source validation failed");
    }

    @Test
    void shouldFailWhenFailOnErrorEnabledAndHasErrors() {
        properties.getValidation().setFailOnError(true);

        ValidationResult resultWithErrors = ValidationResult.builder()
                .addError("File not found")
                .build();

        when(validator.validate()).thenReturn(resultWithErrors);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should throw IllegalStateException
        Throwable thrown = catchThrowable(() -> runner.run(args));
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shouldNotFailWhenFailOnErrorDisabled() {
        properties.getValidation().setFailOnError(false);

        ValidationResult failedResult = ValidationResult.builder()
                .addError("Test error")
                .addMissingKeys(Locale.GERMAN, Set.of("missing.key"))
                .build();

        when(validator.validate()).thenReturn(failedResult);

        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw when fail-on-error is false
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldHandleValidatorException() {
        when(validator.validate()).thenThrow(new RuntimeException("Validator error"));

        properties.getValidation().setFailOnError(false);
        runner = new MessageSourceValidationRunner(validator, properties);

        // Should not throw when fail-on-error is false
        assertThat(catchThrowable(() -> runner.run(args))).isNull();
    }

    @Test
    void shouldFailOnValidatorExceptionWhenFailOnErrorEnabled() {
        when(validator.validate()).thenThrow(new RuntimeException("Validator error"));

        properties.getValidation().setFailOnError(true);
        runner = new MessageSourceValidationRunner(validator, properties);

        // Should throw IllegalStateException wrapping the original exception
        Throwable thrown = catchThrowable(() -> runner.run(args));
        assertThat(thrown).isInstanceOf(IllegalStateException.class);
        assertThat(thrown.getCause()).isInstanceOf(RuntimeException.class);
        assertThat(thrown.getCause().getMessage()).contains("Validator error");
    }
}
