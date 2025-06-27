package info.md7.g11n4j.core.render;

import java.util.HashMap;
import java.util.Map;

public class TemplateRenderer {

    private final String template;
    private final Map<String, Object> defaultArgs;

    public TemplateRenderer(String template) {
        this(template, Map.of());
    }

    public TemplateRenderer(String template, Map<String, Object> defaultArgs) {
        this.template = template;
        this.defaultArgs = defaultArgs;
    }

    public String render(Object[] variables) {
        String result = template;
        for (int i = 0; i < variables.length; i++) {
            result = result.replace("{" + i + "}", String.valueOf(variables[i]));
        }
        return result;
    }

    public String render(String key, Object value) {
        return render(Map.of(key, value));
    }

    public String render() {
        return render(defaultArgs);
    }

    public String render(Map<String, Object> args) {
        Map<String, Object> merged = new HashMap<>(defaultArgs);
        merged.putAll(args);
        String result = template;
        for (Map.Entry<String, Object> entry : merged.entrySet()) {
            result = result.replace("{" + entry.getKey() + "}", String.valueOf(entry.getValue()));
        }
        return result;
    }
}
