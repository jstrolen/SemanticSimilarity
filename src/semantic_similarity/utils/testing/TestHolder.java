package semantic_similarity.utils.testing;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;
import semantic_similarity.utils.MyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.Settings.DEFAULT_SIMILARITY;

/**
 * @author Josef Stroleny
 */
public class TestHolder {
    private List<Test> tests;

    public TestHolder() {
        this.tests = new ArrayList<>();
    }

    public void load(String folder) throws IOException {
        File file = new File(folder);
        for (final File fileEntry : file.listFiles()) {
            if (!fileEntry.isDirectory()) {
                tests.add(Test.loadTest(fileEntry));
            }
        }
    }

    public void testMonolingual(ELanguage language, VectorSpace space, boolean skipOOV) {
        testMultilingual(language, language, space, skipOOV);
    }

    public void testMultilingual(ELanguage language1, ELanguage language2, VectorSpace space, boolean skipOOV) {
        for (Test test : tests) {
            if (!(test.getLanguage1().equals(language1) && test.getLanguage2().equals(language2))
                    && !(test.getLanguage1().equals(language2) && test.getLanguage2().equals(language1))) {
                continue;
            }
            test(test, space, skipOOV);
        }
    }

    public void testAll(VectorSpace space, boolean skipOOV) {
        for (Test test : tests) {
            test(test, space, skipOOV);
        }
    }

    private void test(Test test, VectorSpace space, boolean skipOOV) {
        List<Double> similarities1 = new ArrayList<>();
        List<Double> similarities2 = new ArrayList<>();

        for (Test.WordPair wp: test.getWords()) {
            float[] vector1 = space.getVector(wp.word1);
            float[] vector2 = space.getVector(wp.word2);
            if (vector1 == null || vector2 == null) {
                if (skipOOV) continue;

                similarities2.add(DEFAULT_SIMILARITY);
            }
            else {
                similarities2.add(MyUtils.getSimilarity(vector1, vector2));
            }

            similarities1.add(wp.score);
        }

        double pearson = getPearson(similarities1, similarities2);
        System.out.println(test.getLanguage1().toString() + "-" + test.getLanguage2().toString() +
                "_" + test.getName().substring(0, test.getName().length() - 4) + ": " +
                "r=" + pearson);
    }

    private double getPearson(List<Double> similarities1, List<Double> similarities2) {
        int n = similarities1.size();
        double sumX = 0.0;
        double sumY = 0.0;
        double sumXX = 0.0;
        double sumYY = 0.0;
        double sumXY = 0.0;
        for(int i = 0; i < n; ++i) {
            double x = similarities1.get(i);
            double y = similarities2.get(i);

            sumX += x;
            sumY += y;
            sumXX += x * x;
            sumYY += y * y;
            sumXY += x * y;
        }

        double top = (n * sumXY) - (sumX * sumY);
        double bottom1 = Math.sqrt(n * sumXX - sumX * sumX);
        double bottom2 = Math.sqrt(n * sumYY - sumY * sumY);
        return top / (bottom1 * bottom2);
    }
}
