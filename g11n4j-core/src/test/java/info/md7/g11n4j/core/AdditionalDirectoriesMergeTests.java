package info.md7.g11n4j.core;

import info.md7.g11n4j.core.exception.NoSuchMessageException;
import info.md7.g11n4j.core.source.PropertiesMessageSource;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Locale;

class AdditionalDirectoriesMergeTests {

    @Test
    void whenAdditionalDirectoriesConfigured_thenMessagesAreMergedWithOverridePriority() {
        PropertiesMessageSource messageSource = new PropertiesMessageSource(
                "i18n-merge",
                "messages",
                "_",
                "properties",
                Locale.ENGLISH,
                List.of(Locale.ENGLISH),
                List.of("module-a", "module-b")
        );

        // Base-only key is present.
        Assertions.assertEquals("BaseValue", messageSource.getMessage("base.only", Locale.ENGLISH));

        // Keys from each sub-path are merged in.
        Assertions.assertEquals("AValue", messageSource.getMessage("module.a", Locale.ENGLISH));
        Assertions.assertEquals("BValue", messageSource.getMessage("module.b", Locale.ENGLISH));

        // Last sub-path in declared order wins the conflict: base < module-a < module-b.
        Assertions.assertEquals("fromModuleB", messageSource.getMessage("shared", Locale.ENGLISH));
    }

    @Test
    void whenAdditionalDirectoriesEmpty_thenOnlyBaseKeysArePresent() {
        PropertiesMessageSource messageSource = new PropertiesMessageSource(
                "i18n-merge",
                "messages",
                "_",
                "properties",
                Locale.ENGLISH,
                List.of(Locale.ENGLISH),
                List.of()
        );

        // Base keys remain resolvable.
        Assertions.assertEquals("BaseValue", messageSource.getMessage("base.only", Locale.ENGLISH));
        Assertions.assertEquals("fromBase", messageSource.getMessage("shared", Locale.ENGLISH));

        // Sub-path keys are absent when no additional directories are configured.
        Assertions.assertThrows(
                NoSuchMessageException.class,
                () -> messageSource.getMessage("module.a", Locale.ENGLISH)
        );
        Assertions.assertThrows(
                NoSuchMessageException.class,
                () -> messageSource.getMessage("module.b", Locale.ENGLISH)
        );
    }
}
