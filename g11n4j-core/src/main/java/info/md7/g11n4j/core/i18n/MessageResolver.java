package info.md7.g11n4j.core.i18n;

import com.ibm.icu.text.PluralRules;
import info.md7.g11n4j.core.model.ResolvableMessage;
import info.md7.g11n4j.core.model.ResolvablePlural;
import info.md7.g11n4j.core.source.MessageSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MessageResolver {

    private final MessageSource messageSource;
    private final Map<Locale, PluralRules> pluralRulesCache = new ConcurrentHashMap<>();

    public MessageResolver(MessageSource messageSource) {
        this.messageSource = messageSource;
    }

    /**
     * Get cached PluralRules for the given locale.
     * Creates and caches the PluralRules instance if not already cached.
     */
    private PluralRules getPluralRules(Locale locale) {
        return pluralRulesCache.computeIfAbsent(locale, PluralRules::forLocale);
    }

    public ResolvableMessage get(String key, Locale locale) {
        return new ResolvableMessage(key, locale, this.messageSource);
    }

    public ResolvablePlural getPlural(String keyPrefix, int count, Locale locale) {
        return new ResolvablePlural(keyPrefix, count, locale, this.messageSource);
    }

    public String getPlural(String keyPrefix, int count, Locale locale, Map<String, Object> args) {
        PluralRules rules = getPluralRules(locale);
        String category = rules.select(count);
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
