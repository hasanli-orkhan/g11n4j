package info.md7.g11n4j.cli.validator;

public class MessageValidatorFactory {

    public static MessageValidator create(String extension, String basePath) {
        return switch (extension.toLowerCase()) {
            case "properties" -> new PropertiesMessageValidator(basePath);
            case "yml", "yaml" -> new YamlMessageValidator(basePath);
            default -> throw new IllegalArgumentException("Unsupported file extension '" + extension + "'.");
        };
    }
}
