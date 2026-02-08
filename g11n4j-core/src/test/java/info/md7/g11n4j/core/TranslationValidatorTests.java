package info.md7.g11n4j.core;

import info.md7.g11n4j.core.validation.TranslationValidationResult;
import info.md7.g11n4j.core.validation.TranslationValidator;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class TranslationValidatorTests {

    @Test
    void whenLocaleIsMissingKeys_thenResultIncludesMissingKeys() {
        Locale defaultLocale = Locale.ENGLISH;
        Locale frenchLocale = Locale.FRENCH;

        Map<Locale, Set<String>> keysByLocale = Map.of(
                defaultLocale, Set.of("a", "b"),
                frenchLocale, Set.of("a")
        );

        TranslationValidationResult result = TranslationValidator.validate(defaultLocale, keysByLocale);

        Assertions.assertTrue(result.hasMissingKeys());
        Assertions.assertTrue(result.getMissingKeysByLocale().get(frenchLocale).contains("b"));
    }
}
