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
        this(key, locale, messageSource, new MessageContext());
    }

    private ResolvableMessage(String key, Locale locale, MessageSource messageSource, MessageContext context) {
        this.key = key;
        this.locale = locale;
        this.messageSource = messageSource;
        this.context = context;
    }

    public ResolvableMessage withContext(String key, String value) {
        MessageContext newContext = new MessageContext();
        for (Map.Entry<String, String> entry : this.context.getContextMapView().entrySet()) {
            newContext.set(entry.getKey(), entry.getValue());
        }
        newContext.set(key, value);
        return new ResolvableMessage(this.key, this.locale, this.messageSource, newContext);
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
