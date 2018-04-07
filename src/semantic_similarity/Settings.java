package semantic_similarity;

/**
 * @author Josef Stroleny
 */
public class Settings {
    public static final int CORES = Runtime.getRuntime().availableProcessors();
    public static final double EPSILON = 0.00000001;
    public static final int M = 1000000;

    //Paths
    public static final String CORPUS_PATH = "./res/corpus/";
    public static final String EMBEDDING_PATH = "./res/embedding/";
    public static final String VOCABULARY_PATH = "./res/vocabulary/";
    public static final String TESTING_PATH = "./res/testing/";
    public static final String TEMP_PATH = "./res/temp/";

    //Document Settings
    public static final int TOP_WORDS_CUT = 0;
    public static final int MAX_LANGUAGE_VOCABULARY = 250000;

    //  - document parsing
    public static final String DEFAULT_REGEX = "(\\.|\\,|\\?|\\!|\\(|\\)|\\:|\\=|\\|)|"
            + "([^\\s.,?!():=|]+)";

    public static final double MIN_TOKEN_LENGTH = 1;
    public static final boolean SKIP_DIGITS = false;

    //  - vocabulary reduction
    public static final int MIN_OCCURRENCE = 5;

    //dictionary
    public static final boolean UNIQUE_ONLY = false;
    public static final boolean CROSSLINGUAL_MAPPING = false;


    //Word2vec
    /** Velikost skryte vrstvy */
    public static final int HIDDEN_LAYER_SIZE = 300;
    /** Velikost kontextu */
    public static final int WINDOW_SIZE = 5;
    /** Pocet negativnich slov k pozitivnimu */
    public static final int K = 3;
    /** Rychlost uceni */
    public static final double ALPHA = 0.035;
    public static final boolean USE_CBOW = true;

    //Testing
    public static final boolean TRY_OTHER_LANGUAGES = false;
    public static final boolean TRY_BOW = true;
    public static final double DEFAULT_SIMILARITY = 0.5;

    //Random - Translation
    public static final double TRANSLATION_PROBABILITY = 0.5;

    //Sentence-aligned corpus
    public static final double MAX_SENTENCE_RATIO = 1.5;
    public static final int SENTENCE_MIN_LENGTH = 7;
    public static final int SENTENCE_MAX_LENGTH = Integer.MAX_VALUE;
}
