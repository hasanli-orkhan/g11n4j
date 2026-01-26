package info.md7.g11n4j.cli.validator;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;

public class GettextMessageValidator implements MessageValidator {

    private final String basePath;
    private final String fileBaseName = "messages";
    private final String localeSeparator = "_";
    private final String fileExtension = "po";

    public GettextMessageValidator(String basePath) {
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
        Set<String> keys = new HashSet<>();

        try (FileInputStream is = new FileInputStream(filePath.toFile());
             Scanner scanner = new Scanner(is, "UTF-8")) {

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine().trim();

                // Parse msgid lines to extract keys
                if (line.startsWith("msgid ")) {
                    String msgid = extractQuotedString(line.substring(6));

                    // Read continuation lines
                    while (scanner.hasNextLine()) {
                        String nextLine = scanner.nextLine().trim();
                        if (nextLine.startsWith("\"")) {
                            msgid += extractQuotedString(nextLine);
                        } else {
                            // Not a continuation, push back and break
                            break;
                        }
                    }

                    // Add non-empty msgids (skip header entry with empty msgid)
                    if (!msgid.isEmpty()) {
                        keys.add(msgid);
                    }
                }
            }

            return keys;

        } catch (FileNotFoundException e) {
            return Collections.emptySet();
        } catch (IOException e) {
            System.err.println("Error loading or parsing file for locale '" + locale + "': " + e.getMessage());
            return Collections.emptySet();
        }
    }

    private String extractQuotedString(String line) {
        line = line.trim();
        if (line.startsWith("\"") && line.endsWith("\"")) {
            // Remove quotes and unescape
            return line.substring(1, line.length() - 1)
                    .replace("\\n", "\n")
                    .replace("\\t", "\t")
                    .replace("\\\"", "\"")
                    .replace("\\\\", "\\");
        }
        return line;
    }

    private String getFileName(String locale) {
        return fileBaseName + localeSeparator + locale + "." + fileExtension;
    }
}
