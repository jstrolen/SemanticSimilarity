package semantic_similarity.utils.testing;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;
import semantic_similarity.utils.MyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.Settings.DEFAULT_SIMILARITY;
import static semantic_similarity.Settings.SKIP_PHRASES;
import static semantic_similarity.Settings.TRY_OTHER_LANGUAGES;
import static semantic_similarity.utils.MyUtils.three;

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

    public void testAll(VectorSpace space) {
        List<Double> result1 = new ArrayList<>();
        List<Double> result2 = new ArrayList<>();

        for (Test test : tests) {
            result1.add(test(test, space, true));
            result2.add(test(test, space, false));
        }
        double mean1 = 0;
        double mean2 = 0;
        for (int i = 0; i < result1.size(); i++) {
            mean1 += result1.get(i);
            mean2 += result2.get(i);
        }
        mean1 /= result1.size();
        mean2 /= result2.size();

        for (int i = 0; i < tests.size(); i++) {
            Test test = tests.get(i);
            System.out.print(test.getLanguage1().toString() + "-" + test.getLanguage2().toString() +
                    "_" + test.getName().substring(0, test.getName().length() - 4) + ": ");

            System.out.println(three.format(result1.get(i)) + " (" + three.format(result2.get(i)) + ") ");
        }
        System.out.println("Prumer: " + three.format(mean1) + " (" + three.format(mean2) + ") ");
    }

    private double test(Test test, VectorSpace space, boolean skipOOV) {
        List<Double> similarities1 = new ArrayList<>();
        List<Double> similarities2 = new ArrayList<>();

        for (Test.WordPair wp: test.getWords()) {
            if (SKIP_PHRASES && (wp.word1.contains("_") || wp.word2.contains("_"))) continue;

            float[] vector1 = space.getVector(wp.word1);
            float[] vector2 = space.getVector(wp.word2);

            if ((vector1 == null || vector2 == null) && skipOOV) continue;

            if (vector1 == null && TRY_OTHER_LANGUAGES) {
                for (int i = 0; i < 5; i++) {
                    vector1 = getOtherLanguage(wp.word1, i, space);
                    if (vector1 != null) break;
                }
            }
            if (vector2 == null && TRY_OTHER_LANGUAGES) {
                for (int i = 0; i < 5; i++) {
                    vector2 = getOtherLanguage(wp.word2, i, space);
                    if (vector2 != null) break;
                }
            }

            if (vector1 == null || vector2 == null) {
                similarities2.add(DEFAULT_SIMILARITY);
            }
            else {
                similarities2.add(MyUtils.getSimilarity(vector1, vector2));
            }

            similarities1.add(wp.score);
        }

        return getPearson(similarities1, similarities2);
    }

    private float[] getOtherLanguage(String originalWord, int languageOrder, VectorSpace space) {
        ELanguage language;
        switch (languageOrder) {
            case 0: {
                language = ELanguage.ENGLISH;
                break;
            }
            case 1: {
                language = ELanguage.GERMAN;
                break;
            }
            case 2: {
                language = ELanguage.SPANISH;
                break;
            }
            case 3: {
                language = ELanguage.CZECH;
                break;
            }
            case 4: {
                language = ELanguage.CHINESE;
                break;
            }
            default: language =  ELanguage.ENGLISH;
        }

        String newWord = language.toString() + ":" + originalWord.substring(3);
        return space.getVector(newWord);
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
