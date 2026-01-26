package info.md7.g11n4j.core.model;

import com.ibm.icu.text.PluralRules;
import info.md7.g11n4j.core.i18n.MessageContext;
import info.md7.g11n4j.core.i18n.TemplateRenderer;
import info.md7.g11n4j.core.source.MessageSource;

import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class ResolvablePlural {

    private final String keyPrefix;
    private final int count;
    private final Locale locale;
    private final MessageSource messageSource;
    private final MessageContext context;

    public ResolvablePlural(String keyPrefix, int count, Locale locale, MessageSource messageSource) {
        this(keyPrefix, count, locale, messageSource, new MessageContext());
    }

    private ResolvablePlural(String keyPrefix, int count, Locale locale, MessageSource messageSource, MessageContext context) {
        this.keyPrefix = keyPrefix;
        this.count = count;
        this.locale = locale;
        this.messageSource = messageSource;
        this.context = context;
    }

    public ResolvablePlural withContext(String key, String value) {
        MessageContext newContext = new MessageContext();
        for (Map.Entry<String, String> entry : this.context.getContextMap().entrySet()) {
            newContext.set(entry.getKey(), entry.getValue());
        }
        newContext.set(key, value);
        return new ResolvablePlural(this.keyPrefix, this.count, this.locale, this.messageSource, newContext);
    }

    public String render(Object... variables) {
        String template = messageSource.getMessage(keyPrefix, locale, context);
        TemplateRenderer renderer = new TemplateRenderer(template);
        return renderer.render(variables);
    }

    public String render(String key, Object value) {
        return render(Map.of(key, value));
    }

    public String render(Map<String, Object> args) {
        Map<String, String> forms = messageSource.getPluralForms(keyPrefix, locale, context);
        PluralRules rules = PluralRules.forLocale(locale);
        String category = rules.select(count);
        String template = forms.getOrDefault(category, forms.get("other"));
        if (template == null) {
            return "[[" + keyPrefix + "]]"; // Fallback if no forms are found
        }
        Map<String, Object> mergedArgs = new HashMap<>(args);
        mergedArgs.put("count", count);
        TemplateRenderer renderer = new TemplateRenderer(template);

        return renderer.render(mergedArgs);
    }

    public String render() {
        return render(Map.of());
    }
}
