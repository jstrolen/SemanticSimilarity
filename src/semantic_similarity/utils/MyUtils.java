package semantic_similarity.utils;

import semantic_similarity.VectorSpace;
import semantic_similarity.utils.embedding.MyEmbeddingUtil;
import semantic_similarity.utils.testing.TestHolder;

import java.io.BufferedInputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.text.DecimalFormat;
import java.util.*;
import java.util.stream.Collectors;

import static semantic_similarity.Settings.EMBEDDING_PATH;
import static semantic_similarity.Settings.TEMP_PATH;
import static semantic_similarity.Settings.TESTING_PATH;

/**
 * @author Josef Stroleny
 */
public class MyUtils {
    public static final DecimalFormat three = new DecimalFormat("#0.000");
    public static final Random random = new Random();

    /**
     * https://stackoverflow.com/questions/109383/sort-a-mapkey-value-by-values-java
     */
    public static <K, V extends Comparable<? super V>> Map<K, V> sortByValueDescending(Map<K, V> map) {
        return map.entrySet()
                .stream()
                .sorted(Map.Entry.comparingByValue(Collections.reverseOrder()))
                .collect(Collectors.toMap(
                        Map.Entry::getKey,
                        Map.Entry::getValue,
                        (e1, e2) -> e1,
                        LinkedHashMap::new
                ));
    }

    public static double getSimilarity(float[] vector1, float[] vector2) {
        double sum1 = 0;
        double sum2 = 0;
        double sum = 0;

        for (int i = 0; i < vector1.length; i++) {
            sum1 += Math.pow(vector1[i], 2);
            sum2 += Math.pow(vector2[i], 2);
            sum += vector1[i] * vector2[i];
        }

        return sum / (Math.sqrt(sum1 * sum2));
    }

    public static void shuffle(int[] array) {
        for (int i = array.length - 1; i > 0; i--) {
            int index = random.nextInt(i + 1);
            int a = array[index];
            array[index] = array[i];
            array[i] = a;
        }
    }

    public static int countLines(String filename) {
        try {
            InputStream is = new BufferedInputStream(new FileInputStream(filename));

            byte[] c = new byte[1024];
            int count = 0;
            int readChars;
            boolean empty = true;
            while ((readChars = is.read(c)) != -1) {
                empty = false;
                for (int i = 0; i < readChars; ++i) {
                    if (c[i] == '\n') {
                        count++;
                    }
                }
            }
            is.close();

            return (count == 0 && !empty) ? 1 : count;
        } catch (Exception e) {
            e.printStackTrace();
        }

        return 0;
    }

    public static void mergeVectorSpaces(List<VectorSpace> vectors, boolean dump, boolean save) throws IOException {
        if (vectors == null || vectors.isEmpty()) return;

        for (VectorSpace v : vectors) v.normalize();

        TestHolder test = new TestHolder();
        test.load(TESTING_PATH);

        VectorSpace newVectorSpace = new VectorSpace();
        for (Map.Entry<String, float[]> map : vectors.get(0).getVectorSpace().entrySet()) {
            int dimension = map.getValue().length;
            for (int i = 1; i < vectors.size(); i++) {
                if (dimension != -1) {
                    float[] vector = vectors.get(i).getVector(map.getKey());
                    if (vector == null) dimension = -1;
                    else dimension += vector.length;
                }
            }

            if (dimension == -1) continue;

            float[] newVector = new float[dimension];
            int index = 0;
            for (int i = 0; i < vectors.size(); i++) {
                float[] vector = vectors.get(i).getVector(map.getKey());
                for (float f : vector) {
                    newVector[index++] = f;
                }
            }
            newVectorSpace.addWord(map.getKey(), newVector);
        }

        test.testAll(newVectorSpace);
        System.out.println(newVectorSpace.getSize());

        if (dump) newVectorSpace.dump(TEMP_PATH + "merged");
        if (save) new MyEmbeddingUtil().saveSpace(EMBEDDING_PATH + "merged.txt", newVectorSpace);
    }
}
