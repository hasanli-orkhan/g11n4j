package info.md7.g11n4j.core.i18n;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class MessageContext {

    private final Map<String, String> context = new HashMap<>();

    public static MessageContext of(String key, String value) {
        MessageContext ctx = new MessageContext();
        ctx.set(key, value);
        return ctx;
    }

    public void set(String key, String value) {
        context.put(key, value);
    }

    public Map<String, String> getContextMapView() {
        return Collections.unmodifiableMap(context);
    }
}
