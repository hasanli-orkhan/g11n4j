package info.md7.g11n4j.core.i18n;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class TemplateRenderer {

    private static final Pattern PLACEHOLDER_PATTERN = Pattern.compile("\\{([^}]+)}");

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
        Map<String, Object> indexedArgs = new HashMap<>();
        for (int i = 0; i < variables.length; i++) {
            indexedArgs.put(String.valueOf(i), variables[i]);
        }
        return render(indexedArgs);
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

        // Sort placeholders by length in descending order to avoid overlapping issues
        // e.g., replace {count} before {c} to prevent {c} from partially matching {count}
        List<String> sortedKeys = new ArrayList<>(merged.keySet());
        sortedKeys.sort((a, b) -> Integer.compare(b.length(), a.length()));

        StringBuffer result = new StringBuffer();
        Matcher matcher = PLACEHOLDER_PATTERN.matcher(template);

        while (matcher.find()) {
            String placeholder = matcher.group(1);
            Object value = merged.get(placeholder);
            String replacement = value != null ? String.valueOf(value) : matcher.group(0);
            matcher.appendReplacement(result, Matcher.quoteReplacement(replacement));
        }
        matcher.appendTail(result);

        return result.toString();
    }
}
