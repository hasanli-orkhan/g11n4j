package info.md7.g11n4j.core.i18n;

import info.md7.g11n4j.core.model.ResolvableMessage;
import info.md7.g11n4j.core.model.ResolvablePlural;
import info.md7.g11n4j.core.source.MessageSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class MessageResolver {

    private final MessageSource messageSource;

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    public ResolvableMessage get(String key, Locale locale) {
        return new ResolvableMessage(key, locale, this.messageSource);
    }

    public ResolvablePlural getPlural(String keyPrefix, int count, Locale locale) {
        return new ResolvablePlural(keyPrefix, count, locale, this.messageSource);
    }

    public String getPlural(String keyPrefix, int count, Locale locale, Map<String, Object> args) {
        String category = PluralRuleProvider.selectCategory(locale, count);
        Map<String, String> forms = messageSource.getPluralForms(keyPrefix, locale);

        String template = forms.getOrDefault(category, forms.get("other"));
        if (template == null) return "[[" + keyPrefix + "]]";

        Map<String, Object> merged = new HashMap<>();
        if (args != null && !args.isEmpty()) {
            merged.putAll(args);
        }
        merged.put("count", count);

        return new TemplateRenderer(template).render(merged);
    }
}
