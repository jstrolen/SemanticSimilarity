package semantic_similarity.word_embedding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Josef Stroleny
 */
public class UnifiedVectorSpace {
    private ELanguage language;
    private Map<String, WordVector> words;
    private int dimension;

    public static UnifiedVectorSpace mergeVectorSpaces(List<UnifiedVectorSpace> unifiedVectorSpaces) {
        if (unifiedVectorSpaces.isEmpty()) return null;

        //check if crosslingual
        ELanguage language = unifiedVectorSpaces.get(0).language;
        if (unifiedVectorSpaces.size() > 1) {
            for (int i = 1; i < unifiedVectorSpaces.size(); i++) {
                if (!unifiedVectorSpaces.get(i).getLanguage().toString().equals(language.toString())) {
                    language = ELanguage.MULTILINGUAL;
                    break;
                }
            }
        }

        //copy word vectors
        UnifiedVectorSpace newUnifiedVectorSpace = new UnifiedVectorSpace(language);
        for (UnifiedVectorSpace unifiedVectorSpace : unifiedVectorSpaces) {
            for (WordVector wordVector : unifiedVectorSpace.getWords().values()) {
                newUnifiedVectorSpace.addWord(wordVector);
            }
        }

        return newUnifiedVectorSpace;
    }

    public UnifiedVectorSpace(ELanguage language) {
        this.language = language;
        this.words = new HashMap<>();
        this.dimension = -1;
    }

    public void addWord(WordVector word) {
        if (dimension == -1) dimension = word.getVector().length;

        if (word.getVector().length == dimension) this.words.put(word.getLanguage().toString() + "_" + word.getWord(), word);
    }

    public boolean removeWord(String word) {
        WordVector v = words.remove(word);

        if (v != null) return true;
        return false;
    }

    public WordVector getWord(String word, ELanguage language) {
        return words.get(language.toString() + "_" + word);
    }

    public WordVector getWord(String word) {
        return words.get(this.language.toString() + "_" + word);
    }

    public List<WordVector> getWords(String word) {
        List<WordVector> vectors = new ArrayList<>();
        for (ELanguage language : ELanguage.values()) {
            WordVector vector = getWord(word, language);
            if (vector != null) vectors.add(vector);
        }
        return vectors;
    }

    public Map<WordVector, Double> getMostSimilarWords(String word, ELanguage language, int count) {
        if (count >= words.size()) count = words.size() - 1;
        Map<WordVector, Double> map = new HashMap<>();
        WordVector wordVector = getWord(word, language);
        if (wordVector == null) {
            System.out.println("null = " + language.toString() + " : " + word);
        }

        //Paralelni pole
        WordVector[] topWords = new WordVector[count];
        double[] topProbabilities = new double[count];

        //Prochazime vsechna slova
        for (WordVector testVector : words.values()) {
            if (wordVector == testVector) continue;

            double similarity = getSimilarity(wordVector, testVector);
            //Nasli jsme vyssi shodu nez je soucasna nejnizsi?
            if (similarity > topProbabilities[0]) {
                int index = count - 1;
                //Hledame kam zaradit nove slovo
                while(true) {
                    if (similarity > topProbabilities[index]) {
                        //Posun slov o pozici nize
                        for (int j = 1; j <= index; j++) {
                            topProbabilities[j - 1] = topProbabilities[j];
                            topWords[j - 1] = topWords[j];
                        }
                        topProbabilities[index] = similarity;
                        topWords[index] = testVector;
                        break;
                    }
                    index--;
                }
            }
        }

        for (int i = 0; i < count; i++) {
            map.put(topWords[i], topProbabilities[i]);
        }

        return map;
    }

    public double getSimilarity(WordVector word1, WordVector word2) {
        double sumWord1 = 0;
        double sumWord2 = 0;
        double sum = 0;

        for (int i = 0; i < word1.getVector().length; i++) {
            sumWord1 += Math.pow(word1.getVector()[i], 2);
            sumWord2 += Math.pow(word2.getVector()[i], 2);
            sum += word1.getVector()[i] * word2.getVector()[i];
        }

        return sum / (Math.sqrt(sumWord1 * sumWord2));
    }

    public Map<String, WordVector> getWords() {
        return words;
    }

    public ELanguage getLanguage() {
        return language;
    }

    public int getSize() {
        return words.size();
    }

    public int getDimension() {
        return dimension;
    }
}
