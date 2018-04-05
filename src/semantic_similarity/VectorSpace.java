package semantic_similarity;

import semantic_similarity.techniques.word2vec.Word2vec;
import semantic_similarity.utils.MyUtils;

import java.io.*;
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

    public void normalize() {
        for (Map.Entry<String, float[]> pair : words.entrySet()) {
            //mean mormalization
            float sum = 0;
            for (float f : pair.getValue()) {
                sum += f;
            }
            sum /= pair.getValue().length;
            for (int i = 0; i < pair.getValue().length; i++) {
                pair.getValue()[i] -= sum;
            }
        }
    }

    public Map<String, Double> getMostSimilarWords(List<String> words, int count) {
        if (words == null || words.isEmpty()) return null;

        float[] hlpVector = null;
        int index = 0;
        boolean plus = true;
        while (hlpVector == null && index < words.size()) {
            String word = words.get(index++);

            char first = word.charAt(0);
            if (first == '-') {
                word = word.substring(1);
                plus = false;
            } else {
                plus = true;
            }

            hlpVector = getVector(word);
        }
        if (hlpVector == null) return null;
        float[] resultVector = new float[hlpVector.length];
        for (int i = 0; i < hlpVector.length; i++) {
            if (plus) resultVector[i] += hlpVector[i];
            else resultVector[i] -= hlpVector[i];
        }

        while (index < words.size()) {
            String word = words.get(index++);

            char first = word.charAt(0);
            if (first == '-') {
                word = word.substring(1);
                plus = false;
            } else {
                plus = true;
            }

            hlpVector = getVector(word);
            if (hlpVector != null) {
                for (int i = 0; i < hlpVector.length; i++) {
                    if (plus) resultVector[i] += hlpVector[i];
                    else resultVector[i] -= hlpVector[i];
                }
            }
        }

        return getMostSimilarWords(resultVector, count);
    }

    public Map<String, Double> getMostSimilarWords(String word, int count) {
        return getMostSimilarWords(getVector(word), count);
    }

    public Map<String, Double> getMostSimilarWords(float[] wordVector, int count) {
        if (count >= words.size()) count = words.size() - 1;
        Map<String, Double> map = new HashMap<>();
        if (wordVector == null) {
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

    public void dump(String path) {
        try {
            BufferedWriter bw_word = new BufferedWriter(new FileWriter(new File(path + ".word.txt")));
            BufferedWriter bw_vec = new BufferedWriter(new FileWriter(new File(path + ".vec.txt")));
            for (Map.Entry<String, float[]> pair : words.entrySet()) {
                bw_word.write(pair.getKey());
                bw_word.newLine();

                for (int i = 0; i < pair.getValue().length; i++) {
                    bw_vec.write(pair.getValue()[i] + " ");
                }
                bw_vec.newLine();
            }
            bw_word.close();
            bw_vec.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static VectorSpace fromDump(String path) {
        VectorSpace vc = new VectorSpace();
        try {
            BufferedReader br_word = new BufferedReader(new FileReader(new File(path + ".word.txt")));
            BufferedReader br_vec = new BufferedReader(new FileReader(new File(path + ".vec.txt")));
            String line_word;
            while ((line_word = br_word.readLine()) != null) {
                String[] vectorWord = br_vec.readLine().split(" ");
                float[] vector = new float[vectorWord.length];
                for (int i = 0; i < vectorWord.length; i++) {
                    vector[i] = Float.parseFloat(vectorWord[i]);
                }

                vc.addWord(line_word, vector);
            }
            br_word.close();
            br_vec.close();
        } catch (Exception e) {
            e.printStackTrace();
        }

        return vc;
    }
}
