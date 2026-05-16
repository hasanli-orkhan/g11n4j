package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.model.SourceType;
import info.md7.g11n4j.core.source.MessageSource;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidationRunner;
import info.md7.g11n4j.spring.boot.validation.MessageSourceValidator;
import org.junit.jupiter.api.Test;
import org.springframework.boot.context.properties.bind.validation.BindValidationException;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.core.NestedExceptionUtils;

import java.util.List;
import java.util.Locale;

import static org.assertj.core.api.Assertions.assertThat;

class G11nAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(G11nAutoConfiguration.class));

    @Test
    void shouldCreateMessageSourceBean() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.base-directory=i18n",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en,ru"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MessageSource.class);
                    assertThat(context.getBean(MessageSource.class)).isNotNull();
                });
    }

    @Test
    void shouldCreateMessageResolverBean() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MessageResolver.class);
                    assertThat(context.getBean(MessageResolver.class)).isNotNull();
                });
    }

    @Test
    void shouldCreateValidatorBeans() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en",
                        "spring.g11n.validation.enabled=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MessageSourceValidator.class);
                    assertThat(context).hasSingleBean(MessageSourceValidationRunner.class);
                });
    }

    @Test
    void shouldNotCreateValidationRunnerWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en",
                        "spring.g11n.validation.enabled=false"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(MessageSourceValidator.class);
                    assertThat(context).doesNotHaveBean(MessageSourceValidationRunner.class);
                });
    }

    @Test
    void shouldNotCreateBeansWhenG11nDisabled() {
        contextRunner
                .withPropertyValues("spring.g11n.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(MessageSource.class);
                    assertThat(context).doesNotHaveBean(MessageResolver.class);
                    assertThat(context).doesNotHaveBean(MessageSourceValidator.class);
                });
    }

    @Test
    void shouldApplyCacheConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en",
                        "spring.g11n.cache.size=2000",
                        "spring.g11n.cache.enable-statistics=true"
                )
                .run(context -> {
                    assertThat(context).hasSingleBean(G11nProperties.class);
                    G11nProperties properties = context.getBean(G11nProperties.class);
                    assertThat(properties.getCache().getSize()).isEqualTo(2000);
                    assertThat(properties.getCache().isEnableStatistics()).isTrue();
                });
    }

    @Test
    void shouldUseDefaultProperties() {
        contextRunner
                .withPropertyValues("spring.g11n.enabled=true")
                .run(context -> {
                    assertThat(context).hasSingleBean(G11nProperties.class);
                    G11nProperties properties = context.getBean(G11nProperties.class);
                    assertThat(properties.getType()).isEqualTo(SourceType.PROPERTIES);
                    assertThat(properties.getBaseDirectory()).isEqualTo("i18n");
                    assertThat(properties.getFileBaseName()).isEqualTo("messages");
                    assertThat(properties.getLocaleSeparator()).isEqualTo("_");
                    assertThat(properties.getFileExtension()).isEqualTo("properties");
                    assertThat(properties.getDefaultLocale()).isEqualTo(Locale.ENGLISH);
                    assertThat(properties.getCache().getSize()).isEqualTo(1000);
                    assertThat(properties.getValidation().isEnabled()).isTrue();
                    assertThat(properties.getValidation().isFailOnError()).isFalse();
                });
    }

    @Test
    void shouldSupportAllSourceTypes() {
        List<SourceType> types = List.of(
                SourceType.PROPERTIES,
                SourceType.YAML,
                SourceType.JSON,
                SourceType.GETTEXT,
                SourceType.XLIFF
        );

        for (SourceType type : types) {
            contextRunner
                    .withPropertyValues(
                            "spring.g11n.enabled=true",
                            "spring.g11n.type=" + type.name(),
                            "spring.g11n.default-locale=en",
                            "spring.g11n.locales=en"
                    )
                    .run(context -> {
                        assertThat(context).hasSingleBean(MessageSource.class);
                        G11nProperties properties = context.getBean(G11nProperties.class);
                        assertThat(properties.getType()).isEqualTo(type);
                    });
        }
    }

    @Test
    void shouldFailFastForInvalidCacheConfiguration() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.cache.size=0"
                )
                .run(context -> {
                    assertThat(context).hasFailed();
                    assertThat(context.getStartupFailure())
                            .hasRootCauseInstanceOf(BindValidationException.class);
                    assertThat(NestedExceptionUtils.getMostSpecificCause(context.getStartupFailure()).getMessage())
                            .contains("cache.size")
                            .contains("must be greater than 0");
                });
    }
}
