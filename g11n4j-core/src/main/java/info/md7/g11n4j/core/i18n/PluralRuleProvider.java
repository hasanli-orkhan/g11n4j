package info.md7.g11n4j.core.i18n;

import com.ibm.icu.text.PluralRules;

import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class PluralRuleProvider {

    private static final Map<Locale, PluralRules> PLURAL_RULES_CACHE = new ConcurrentHashMap<>();

    private PluralRuleProvider() {
    }

    public static String selectCategory(Locale locale, int count) {
        PluralRules rules = PLURAL_RULES_CACHE.computeIfAbsent(locale, PluralRules::forLocale);
        return rules.select(count);
    }
}
