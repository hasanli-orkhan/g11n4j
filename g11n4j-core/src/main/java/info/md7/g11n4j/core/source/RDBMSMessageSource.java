package info.md7.g11n4j.core.source;

import java.util.List;
import java.util.Locale;

public class RDBMSMessageSource extends AbstractMessageSource {

    public RDBMSMessageSource(
            String baseDirectory, String fileBaseName,
            String localeSeparator, String fileExtension,
            Locale defaultLocale, List<Locale> supportedLocales
    ) {
        super(baseDirectory, fileBaseName, localeSeparator, fileExtension, defaultLocale, supportedLocales);
    }

    @Override
    protected void loadMessages() {
        // todo implement RDBMS message source
        throw new RuntimeException("Not yet implemented.");
    }
}
