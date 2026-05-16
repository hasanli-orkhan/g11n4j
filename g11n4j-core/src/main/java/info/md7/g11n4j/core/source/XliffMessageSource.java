package info.md7.g11n4j.core.source;

import info.md7.g11n4j.core.model.SourceType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import javax.xml.XMLConstants;
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
        validateSupportedExtension(SourceType.XLIFF, fileExtension);
    }

    public XliffMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales,
            int cacheSize
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales, cacheSize);
        validateSupportedExtension(SourceType.XLIFF, fileExtension);
    }

    @Override
    protected Map<String, String> parseMessageFile(InputStream is) throws Exception {
        DocumentBuilderFactory factory = createSecureDocumentBuilderFactory();
        DocumentBuilder builder = factory.newDocumentBuilder();
        Document doc = builder.parse(is);
        return parseXliffDocument(doc);
    }

    private DocumentBuilderFactory createSecureDocumentBuilderFactory() throws Exception {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        factory.setNamespaceAware(true);
        factory.setXIncludeAware(false);
        factory.setExpandEntityReferences(false);
        factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
        factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
        factory.setFeature("http://xml.org/sax/features/external-general-entities", false);
        factory.setFeature("http://xml.org/sax/features/external-parameter-entities", false);
        factory.setFeature("http://apache.org/xml/features/nonvalidating/load-external-dtd", false);
        try {
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_DTD, "");
            factory.setAttribute(XMLConstants.ACCESS_EXTERNAL_SCHEMA, "");
        } catch (IllegalArgumentException ignored) {
            // Not supported by some parsers/JDK combinations.
        }
        return factory;
    }

    private Map<String, String> parseXliffDocument(Document doc) {
        Map<String, String> result = new LinkedHashMap<>();

        NodeList fileNodes = doc.getElementsByTagName("file");
        for (int i = 0; i < fileNodes.getLength(); i++) {
            Element fileElement = (Element) fileNodes.item(i);

            NodeList transUnits = fileElement.getElementsByTagName("trans-unit");
            for (int j = 0; j < transUnits.getLength(); j++) {
                Element transUnit = (Element) transUnits.item(j);
                String id = transUnit.getAttribute("id");

                if (id != null && !id.isEmpty()) {
                    NodeList targetNodes = transUnit.getElementsByTagName("target");
                    String translation = null;

                    if (targetNodes.getLength() > 0) {
                        translation = targetNodes.item(0).getTextContent();
                    } else {
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
