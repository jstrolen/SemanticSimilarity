package semantic_similarity.utils;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * @author Josef Stroleny
 */
public class MyUtils {
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
}
