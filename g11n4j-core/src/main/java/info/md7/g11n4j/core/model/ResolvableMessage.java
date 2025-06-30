package info.md7.g11n4j.core.model;

import info.md7.g11n4j.core.i18n.MessageContext;
import info.md7.g11n4j.core.i18n.TemplateRenderer;
import info.md7.g11n4j.core.source.MessageSource;

import java.util.Locale;
import java.util.Map;

public class ResolvableMessage {

    private final String key;
    private final Locale locale;
    private final MessageSource messageSource;
    private final MessageContext context;

    public ResolvableMessage(String key, Locale locale, MessageSource messageSource) {
        this.key = key;
        this.locale = locale;
        this.messageSource = messageSource;
        this.context = new MessageContext();
    }

    public ResolvableMessage withContext(String key, String value) {
        this.context.set(key, value);
        return this;
    }

    public String render(Map<String, Object> args) {
        String template = messageSource.getMessage(key, locale, context);
        TemplateRenderer renderer = new TemplateRenderer(template);
        return renderer.render(args);
    }

    public String render(Object... variables) {
        String template = messageSource.getMessage(key, locale, context);
        TemplateRenderer renderer = new TemplateRenderer(template);
        return renderer.render(variables);
    }

    public String render(String key, Object value) {
        return render(Map.of(key, value));
    }

    public String render() {
        return render(Map.of());
    }

    @Override
    public String toString() {
        try {
            return render();
        } catch (Exception e) {
            return "[[" + key + "]]";
        }
    }
}
