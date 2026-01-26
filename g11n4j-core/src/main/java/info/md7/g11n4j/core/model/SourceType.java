package info.md7.g11n4j.core.model;

import java.util.List;

public enum SourceType {

    YAML(List.of("yml", "yaml")),
    PROPERTIES(List.of("properties")),
    GETTEXT(List.of("pot", "po", "mo")),
    JSON(List.of("json"));
    // todo add missing source types (XLIFF, TMX, RDBMS, NoSQL)

    private final List<String> extensions;

    SourceType(List<String> extensions) {
        this.extensions = extensions;
    }

    public List<String> getExtensions() {
        return extensions;
    }

}
