package info.md7.g11n4j.cli.validator;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class JsonMessageValidator implements MessageValidator {

    private final String basePath;
    private final String fileBaseName = "messages";
    private final String localeSeparator = "_";
    private final String fileExtension = "json";

    public JsonMessageValidator(String basePath) {
        this.basePath = basePath;
    }

    @Override
    public boolean validate(String defaultLocale, List<String> compareLocales) {
        System.out.println("Starting validation...");
        System.out.println("Base Path: " + basePath);
        System.out.println("Default Locale: " + defaultLocale);
        System.out.println("Comparing against: " + compareLocales);
        System.out.println("---");

        Set<String> defaultKeys = loadKeysForLocale(defaultLocale);
        if (defaultKeys.isEmpty()) {
            System.err.println("Could not load keys for the default locale '" + defaultLocale + "'. Please check the path and file format.");
            return false;
        }

        System.out.println("Found " + defaultKeys.size() + " keys in the default locale file ('" + defaultLocale + "').");
        boolean allValid = true;

        for (String compareLocale : compareLocales) {
            System.out.println("\nValidating locale: '" + compareLocale + "'...");
            Set<String> compareKeys = loadKeysForLocale(compareLocale);

            if (compareKeys.isEmpty() && !Paths.get(basePath, getFileName(compareLocale)).toFile().exists()) {
                System.err.println("ERROR: File for locale '" + compareLocale + "' not found.");
                allValid = false;
                continue;
            }

            Set<String> missingKeys = defaultKeys.stream()
                    .filter(key -> !compareKeys.contains(key))
                    .collect(Collectors.toSet());

            if (missingKeys.isEmpty()) {
                System.out.println("✅ OK: Locale '" + compareLocale + "' is consistent with the default locale.");
            } else {
                allValid = false;
                System.err.println("❌ FAILED: Locale '" + compareLocale + "' is missing " + missingKeys.size() + " keys:");
                missingKeys.forEach(key -> System.err.println("  - " + key));
            }
        }

        System.out.println("\n---");
        if (allValid) {
            System.out.println("Validation finished successfully. All locales are consistent.");
        } else {
            System.err.println("Validation finished with errors. Some locales have missing keys.");
        }
        return allValid;
    }

    private Set<String> loadKeysForLocale(String locale) {
        Path filePath = Paths.get(basePath, getFileName(locale));
        ObjectMapper objectMapper = new ObjectMapper();

        try (FileInputStream is = new FileInputStream(filePath.toFile())) {
            Map<String, Object> jsonMap = objectMapper.readValue(is, new TypeReference<Map<String, Object>>() {});
            if (jsonMap != null) {
                return flattenKeys("", jsonMap);
            }
            return Collections.emptySet();
        } catch (FileNotFoundException e) {
            return Collections.emptySet();
        } catch (IOException e) {
            System.err.println("Error loading or parsing file for locale '" + locale + "': " + e.getMessage());
            return Collections.emptySet();
        }
    }

    @SuppressWarnings("unchecked")
    private Set<String> flattenKeys(String prefix, Map<String, Object> source) {
        Set<String> keys = new HashSet<>();
        for (Map.Entry<String, Object> entry : source.entrySet()) {
            String key = prefix.isEmpty() ? entry.getKey() : prefix + "." + entry.getKey();
            Object value = entry.getValue();
            if (value instanceof Map) {
                keys.addAll(flattenKeys(key, (Map<String, Object>) value));
            } else {
                keys.add(key);
            }
        }
        return keys;
    }

    private String getFileName(String locale) {
        return fileBaseName + localeSeparator + locale + "." + fileExtension;
    }
}
