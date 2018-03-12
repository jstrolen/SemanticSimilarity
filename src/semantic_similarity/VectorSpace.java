package semantic_similarity;

import semantic_similarity.techniques.word2vec.Word2vec;
import semantic_similarity.utils.MyUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author Josef Stroleny
 */
public class VectorSpace {
    private Map<String, float[]> words;

    public VectorSpace() {
        this.words = new HashMap<>();
    }

    public static VectorSpace mergeVectorSpaces(List<VectorSpace> vectorSpaces) {
        VectorSpace newVectorSpace = new VectorSpace();

        for (VectorSpace vectorSpace : vectorSpaces) {
            for (Map.Entry<String, float[]> vector : vectorSpace.getVectorSpace().entrySet()) {
                newVectorSpace.addWord(vector.getKey(), vector.getValue());
            }
        }

        return newVectorSpace;
    }

    public void addWord(ELanguage language, String word, float[] vector) {
        if (language == null) {
            return;
        }

        addWord(language.toString() + ":" + word, vector);
    }

    public void addWord(String word, float[] vector) {
        int ourDimension = getDimension();
        if (ourDimension != -1 && ourDimension != vector.length) {
            System.out.println("Error dimension");
            return;
        }

        this.words.put(word, vector);
    }

    public float[] getVector(ELanguage language, String word) {
        return getVector(language.toString() + ":" + word);
    }

    public float[] getVector(String word) {
        return words.get(word);
    }

    public Map<String, Double> getMostSimilarWords(ELanguage language, String word, int count) {
        return getMostSimilarWords(language.toString() + ":" + word, count);
    }

    public Map<String, Double> getMostSimilarWords(String word, int count) {
        if (count >= words.size()) count = words.size() - 1;
        Map<String, Double> map = new HashMap<>();
        float[] wordVector = getVector(word);
        if (wordVector == null) {
            System.out.println(word + " is null");
            return null;
        }

        //Paralelni pole
        String[] topWords = new String[count];
        double[] topProbabilities = new double[count];

        //Prochazime vsechna slova
        for (Map.Entry<String, float[]> testVector : words.entrySet()) {
            if (wordVector == testVector.getValue()) continue;

            double similarity = MyUtils.getSimilarity(wordVector, testVector.getValue());
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
                        topWords[index] = testVector.getKey();
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

    public Map<String, float[]> getVectorSpace() {
        return words;
    }

    public int getSize() {
        return words.size();
    }

    public int getDimension() {
        if (!words.isEmpty()) {
            return words.values().iterator().next().length;
        }

        return -1;
    }
}
