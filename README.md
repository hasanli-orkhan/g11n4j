# g11n4j

Lightweight internationalization library for Java and Spring Boot with:
- locale fallback (`en_US -> en -> default`)
- ICU4J plural rules
- named and indexed placeholders (`{name}`, `{0}`)
- context-aware resolution (for example `gender.male` vs `gender.female`)
- runtime reload support

## Modules

- `g11n4j-core` - Core Java library
- `g11n4j-spring-boot-starter` - Starter for Spring Boot 3.x
- `g11n4j-spring-boot4-starter` - Starter for Spring Boot 4.x

## Requirements

- Java 21+
- Maven 3.9+

## Supported message formats

- `YAML` (`.yml`, `.yaml`)
- `Properties` (`.properties`, UTF-8 supported)
- `JSON` (`.json`)
- `Gettext` (`.po`)
- `XLIFF` (`.xlf`, `.xliff`)

## Core usage (plain Java)

```xml
<dependency>
  <groupId>info.md7.g11n4j</groupId>
  <artifactId>g11n4j-core</artifactId>
  <version>1.0.0</version>
</dependency>
```

```java
import info.md7.g11n4j.core.i18n.MessageResolver;
import info.md7.g11n4j.core.source.YamlMessageSource;

import java.util.List;
import java.util.Locale;

var source = new YamlMessageSource(
        "i18n-yaml",    // or classpath:i18n-yaml
        "messages",     // messages_en.yml, messages_ru.yml, ...
        "_",
        "yml",
        Locale.ENGLISH,
        List.of(Locale.ENGLISH, Locale.forLanguageTag("ru"), Locale.forLanguageTag("de")),
        1000
);

var resolver = new MessageResolver(source);

String greeting = resolver.get("greeting", Locale.ENGLISH)
        .withContext("gender", "male")
        .render("fullName", "John Smith");

String notifications = resolver.getPlural("notification.message.count", 15, Locale.ENGLISH)
        .render();
```

## Message conventions

- Naming: `${fileBaseName}_${locale}.${extension}` (for example `messages_en.yml`)
- Locale chain: region -> language -> default locale
- Context keys:
  - specific: `greeting.gender.male`
  - fallback: `greeting.gender.other`
  - base: `greeting._base`

Plural keys use CLDR categories:
- `zero`, `one`, `two`, `few`, `many`, `other`

Example:

```yaml
notification:
  message:
    count:
      one: "You have {count} new notification"
      other: "You have {count} new notifications"
```

## Spring Boot integration

### Dependency

For Spring Boot 3.x:

```xml
<dependency>
  <groupId>info.md7.g11n4j</groupId>
  <artifactId>g11n4j-spring-boot-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

For Spring Boot 4.x:

```xml
<dependency>
  <groupId>info.md7.g11n4j</groupId>
  <artifactId>g11n4j-spring-boot4-starter</artifactId>
  <version>1.0.0</version>
</dependency>
```

### Configuration

```yaml
spring:
  g11n:
    enabled: true
    type: YAML                # YAML | PROPERTIES | JSON | GETTEXT | XLIFF
    base-directory: classpath:i18n-yaml
    file-base-name: messages
    file-extension: yml
    locale-separator: _
    default-locale: en
    locales: [en, ru, de]
    cache:
      size: 2000
      enable-statistics: true
    validation:
      enabled: true
      fail-on-error: false
```

### Spring Boot configuration reference

All supported starter parameters:

| Property | Type | Default | Description |
| --- | --- | --- | --- |
| `spring.g11n.enabled` | `boolean` | `true` | Master switch for g11n4j auto-configuration. |
| `spring.g11n.type` | `enum` | `PROPERTIES` | Message source type: `YAML`, `PROPERTIES`, `JSON`, `GETTEXT`, `XLIFF`. |
| `spring.g11n.base-directory` | `String` | `i18n` | Base directory for translation files (`classpath:` prefix is supported). |
| `spring.g11n.file-base-name` | `String` | `messages` | Base filename used before locale and extension. |
| `spring.g11n.file-extension` | `String` | `properties` | Translation file extension. Must match selected type. If type is not `PROPERTIES` and this remains `properties`, starter auto-selects the first extension for that type. |
| `spring.g11n.locale-separator` | `String` | `_` | Separator in filenames, for example `messages_en.properties`. |
| `spring.g11n.default-locale` | `Locale` | `en` | Default fallback locale. |
| `spring.g11n.locales` | `List<Locale>` | `[default-locale]` | Supported locales list. If empty, only default locale is used. |
| `spring.g11n.cache.size` | `int` | `1000` | Maximum cache entries for resolved messages (must be positive). |
| `spring.g11n.cache.enable-statistics` | `boolean` | `false` | Enables cache statistics collection. |
| `spring.g11n.validation.enabled` | `boolean` | `true` | Enables startup translation validation. |
| `spring.g11n.validation.fail-on-error` | `boolean` | `false` | If `true`, startup fails on validation errors; otherwise logs and continues. |

### Use in application code

```java
import info.md7.g11n4j.core.i18n.MessageResolver;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
public class UserMessageService {

    private final MessageResolver messageResolver;

    public UserMessageService(MessageResolver messageResolver) {
        this.messageResolver = messageResolver;
    }

    public String welcome(String fullName, Locale locale) {
        return messageResolver.get("greeting", locale)
                .withContext("gender", "male")
                .render("fullName", fullName);
    }
}
```

## Actuator support

When actuator is on classpath and endpoint exposure is enabled:

- Health indicator: `g11n`
- Reload endpoint id: `g11n-reload`
  - reload all
  - reload a specific locale

Typical setup (Boot 3.x):

```properties
management.endpoints.web.exposure.include=health,g11n-reload
```

Typical setup (Boot 4.x):

```properties
management.endpoints.web.exposure.include=health,g11nreload
```

## Web locale resolution

With Web MVC starter present, a `LocaleResolver` is auto-configured.

Resolution order:
1. `?locale=...` query parameter
2. `Accept-Language` header
3. `spring.g11n.default-locale`

## Properties files and UTF-8

`.properties` files are read as UTF-8. Save translation files as UTF-8 in your IDE/editor.

In IntelliJ IDEA:
- `Settings/Preferences -> Editor -> File Encodings`
- set properties encoding to UTF-8

![properties file encoding](properties-encoding.png "properties file encoding")

## Build and test

```bash
mvn clean test
```
