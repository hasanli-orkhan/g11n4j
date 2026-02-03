package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.exception.MessageLoadException;
import info.md7.g11n4j.core.model.SourceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.InputStream;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class XliffMessageSource extends AbstractMessageSource {

    public XliffMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
        if (!SourceType.XLIFF.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be one of: " + SourceType.XLIFF.getExtensions());
        }
    }

    public XliffMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        if (!SourceType.XLIFF.getExtensions().contains(fileExtension)) {
            throw new IllegalArgumentException("Unsupported file extension: " + fileExtension + ". Must be one of: " + SourceType.XLIFF.getExtensions());
        }
    }

    @Override
    protected void loadMessages() {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);

        for (Locale locale : supportedLocales) {
            String filename = baseDirectory + "/" + fileBaseName + localeSeparator + locale.getLanguage() + "." + fileExtension;
            try (InputStream is = getClass().getClassLoader().getResourceAsStream(filename)) {
                if (is != null) {
                    DocumentBuilder builder = factory.newDocumentBuilder();
                    Document doc = builder.parse(is);
                    Map<String, String> flatMap = parseXliffDocument(doc);
                    messages.put(locale, flatMap);
                }
            } catch (Exception e) {
                throw new MessageLoadException(filename, e);
            }
        }
    }

    private Map<String, String> parseXliffDocument(Document doc) {
        Map<String, String> result = new LinkedHashMap<>();

        // XLIFF can have multiple file elements
        NodeList fileNodes = doc.getElementsByTagName("file");
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element fileElement = (Element) fileNodes.item(i);

            // Get all trans-unit elements
            NodeList transUnits = fileElement.getElementsByTagName("trans-unit");
            for (int j = 0; j < transUnits.getLength(); j++) {
                Element transUnit = (Element) transUnits.item(j);
                String id = transUnit.getAttribute("id");

                if (id != null && !id.isEmpty()) {
                    // Get target element (translated text), fallback to source if target doesn't exist
                    NodeList targetNodes = transUnit.getElementsByTagName("target");
                    String translation = null;

                    if (targetNodes.getLength() > 0) {
                        translation = targetNodes.item(0).getTextContent();
                    } else {
                        // Fallback to source if target is not present
                        NodeList sourceNodes = transUnit.getElementsByTagName("source");
                        if (sourceNodes.getLength() > 0) {
                            translation = sourceNodes.item(0).getTextContent();
                        }
                    }

                    if (translation != null && !translation.isEmpty()) {
                        result.put(id, translation);
                    }
                }
            }
        }

        return result;
    }
}
