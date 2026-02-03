package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.model.SourceType;

import java.io.InputStream;
import java.util.*;

public class GettextMessageSource extends AbstractMessageSource {

    public GettextMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        if (!SourceType.GETTEXT.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be one of: " + SourceType.GETTEXT.getExtensions());
        }
    }

    public GettextMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        if (!SourceType.GETTEXT.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be one of: " + SourceType.GETTEXT.getExtensions());
        }
    }

    @Override
    protected void loadMessages() {
        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + "/" + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    Map<String, String> flatMap = parseGettextFile(is);
                    messages.put(locale, flatMap);
                }
            } catch (Exception e) {
                throw new MessageLoadException(filename, e);
            }
        }
    }

    private Map<String, String> parseGettextFile(InputStream is) {
        Map<String, String> result = new LinkedHashMap<>();

        try (Scanner scanner = new Scanner(is, "UTF-8")) {
            String currentMsgid = null;
            StringBuilder msgidBuilder = new StringBuilder();
            StringBuilder msgstrBuilder = new StringBuilder();
            boolean readingMsgid = false;
            boolean readingMsgstr = false;

            while (scanner.hasNextLine()) {
                String line = scanner.nextLine();
                String trimmed = line.trim();

                // Skip comments
                if (trimmed.startsWith("#")) {
                    continue;
                }

                // Empty line or new entry - save previous entry if exists
                if (trimmed.isEmpty() || trimmed.startsWith("msgid ")) {
                    if (readingMsgstr && currentMsgid != null && !currentMsgid.isEmpty()) {
                        String msgstr = msgstrBuilder.toString();
                        if (!msgstr.isEmpty()) {
                            result.put(currentMsgid, msgstr);
                        }
                    }

                    if (trimmed.isEmpty()) {
                        readingMsgid = false;
                        readingMsgstr = false;
                        currentMsgid = null;
                        continue;
                    }
                }

                if (trimmed.startsWith("msgid ")) {
                    // Start new msgid
                    msgidBuilder = new StringBuilder();
                    msgidBuilder.append(extractQuotedString(trimmed.substring(6)));
                    readingMsgid = true;
                    readingMsgstr = false;
                    currentMsgid = null;

                } else if (trimmed.startsWith("msgstr ")) {
                    // Finalize msgid and start msgstr
                    currentMsgid = msgidBuilder.toString();
                    msgstrBuilder = new StringBuilder();
                    msgstrBuilder.append(extractQuotedString(trimmed.substring(7)));
                    readingMsgid = false;
                    readingMsgstr = true;

                } else if (trimmed.startsWith("\"")) {
                    // Continuation line
                    String continuation = extractQuotedString(trimmed);
                    if (readingMsgid) {
                        msgidBuilder.append(continuation);
                    } else if (readingMsgstr) {
                        msgstrBuilder.append(continuation);
                    }
                }
            }

            // Save last entry if exists
            if (readingMsgstr && currentMsgid != null && !currentMsgid.isEmpty()) {
                String msgstr = msgstrBuilder.toString();
                if (!msgstr.isEmpty()) {
                    result.put(currentMsgid, msgstr);
                }
            }

        } catch (Exception e) {
            throw new MessageLoadException("Failed to parse gettext file", e);
        }

        return result;
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
}
