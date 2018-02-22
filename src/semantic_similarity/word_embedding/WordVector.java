package semantic_similarity.word_embedding;

/**
 * @author Josef Stroleny
 */
public class WordVector {
    private ELanguage language;
    private String word;
    private float[] vector;

    public WordVector(ELanguage language, String word, float[] vector) {
        this.language = language;
        this.word = word;
        this.vector = vector;
    }

    public ELanguage getLanguage() {
        return language;
    }

    public float[] getVector() {
        return vector;
    }

    public String getWord() {
        return word;
    }
}
