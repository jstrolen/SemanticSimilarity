package semantic_similarity.word_embedding;

/**
 * @author Josef Stroleny
 */
public enum ELanguage {
    MULTILINGUAL ("multi"),

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
        for (ELanguage lang : ELanguage.values()) {
            if (lang.toString().equalsIgnoreCase(text)) {
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
