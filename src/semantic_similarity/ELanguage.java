package semantic_similarity;

/**
 * @author Josef Stroleny
 */
public enum ELanguage {
    CZECH ("cs"),
    ENGLISH ("en"),
    GERMAN ("de"),
    SPANISH ("es"),
    CHINESE ("zh");

    private final String language;

    ELanguage(final String language) {
        this.language = language;
    }

    public static ELanguage fromString(String text) {
        if (text == null) return null;

        String substring = text.substring(0, 2);
        for (ELanguage lang : ELanguage.values()) {
            if (lang.toString().equalsIgnoreCase(substring)) {
                return lang;
            }
        }
        return null;
    }

    @Override
    public String toString() {
        return language;
    }
}
