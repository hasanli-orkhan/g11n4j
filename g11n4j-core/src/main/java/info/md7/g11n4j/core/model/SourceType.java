package info.md7.g11n4j.core.model;

import java.util.List;

public enum SourceType {

    YAML(List.of("yml", "yaml")),
    PROPERTIES(List.of("properties")),
    GETTEXT(List.of("pot", "po", "mo")),
    JSON(List.of("json")),
    XLIFF(List.of("xlf", "xliff"));

    private final List<String> extensions;

    SourceType(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getExtensions() {
        return extensions;
    }

}
