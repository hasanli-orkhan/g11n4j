package info.md7.g11n4j.spring.boot.config;

import info.md7.g11n4j.spring.boot.endpoint.MessageSourceReloadEndpoint;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

import static org.assertj.core.api.Assertions.assertThat;

class G11nActuatorAutoConfigurationTests {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(
                    G11nAutoConfiguration.class,
                    G11nActuatorAutoConfiguration.class
            ));

    @Test
    void shouldCreateActuatorBeansByDefault() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en",
                        "management.endpoints.web.exposure.include=g11n-reload,health"
                )
                .run(context -> {
                    assertThat(context).hasBean("g11nHealthIndicator");
                    assertThat(context).hasSingleBean(MessageSourceReloadEndpoint.class);
                });
    }

    @Test
    void shouldNotCreateHealthIndicatorWhenDisabled() {
        contextRunner
                .withPropertyValues(
                        "spring.g11n.enabled=true",
                        "spring.g11n.type=PROPERTIES",
                        "spring.g11n.default-locale=en",
                        "spring.g11n.locales=en",
                        "management.health.g11n.enabled=false",
                        "management.endpoints.web.exposure.include=g11n-reload,health"
                )
                .run(context -> assertThat(context).doesNotHaveBean("g11nHealthIndicator"));
    }

    @Test
    void shouldNotCreateActuatorBeansWhenG11nIsDisabled() {
        contextRunner
                .withPropertyValues("spring.g11n.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean("g11nHealthIndicator");
                    assertThat(context).doesNotHaveBean(MessageSourceReloadEndpoint.class);
                });
    }
}
