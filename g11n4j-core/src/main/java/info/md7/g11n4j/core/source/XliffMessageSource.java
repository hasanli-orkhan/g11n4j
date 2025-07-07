package info.md7.g11n4j.core.source;

import java.util.List;
import java.util.Locale;

public class XliffMessageSource extends AbstractMessageSource {

    public XliffMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
    }

    @Override
    protected void loadMessages() {
        // todo implement JSON message source
        throw new RuntimeException("Not yet implemented.");
    }
}
