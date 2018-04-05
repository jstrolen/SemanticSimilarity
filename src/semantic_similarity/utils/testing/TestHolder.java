package semantic_similarity.utils.testing;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;
import semantic_similarity.utils.MyUtils;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.Settings.*;
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

        for (int i = 0; i < tests.size(); i++) {
            Test test = tests.get(i);
            result1.add(test(test, space, false));
            result2.add(test(test, space, true));
        }
        double mean1 = 0;
        double mean2 = 0;
        for (int i = 0; i < result1.size(); i++) {
            mean1 += result1.get(i);
            mean2 += result2.get(i);
        }
        mean1 /= result1.size();
        mean2 /= result2.size();

        /*for (int i = 0; i < tests.size(); i++) {
            Test test = tests.get(i);
            System.out.print(test.getLanguage1().toString() + "-" + test.getLanguage2().toString() +
                    "_" + test.getName().substring(0, test.getName().length() - 4) + ": ");

            System.out.println(three.format(result1.get(i)) + " (" + three.format(result2.get(i)) + ") ");
        }*/

        System.out.print("");
        System.out.print("||");

        soutTest(ELanguage.ENGLISH, ELanguage.ENGLISH, result1, result2);
        System.out.print("|");
        soutTest(ELanguage.GERMAN, ELanguage.GERMAN, result1, result2);
        System.out.print("|");
        soutTest(ELanguage.SPANISH, ELanguage.SPANISH, result1, result2);
        System.out.print("||");

        soutTest(ELanguage.GERMAN, ELanguage.SPANISH, result1, result2);
        System.out.print("|");
        soutTest(ELanguage.ENGLISH, ELanguage.GERMAN, result1, result2);
        System.out.print("|");
        soutTest(ELanguage.ENGLISH, ELanguage.SPANISH, result1, result2);
        System.out.print("||");

        System.out.println(three.format(mean1) + " (" + three.format(mean2) + ") ");
    }

    private void soutTest(ELanguage language1, ELanguage language2, List<Double> result1, List<Double> result2) {
        for (int i = 0; i < tests.size(); i++) {
            Test t = tests.get(i);
            if ((t.getLanguage1() == language1 && t.getLanguage2() == language2) ||
                    t.getLanguage1() == language2 && t.getLanguage2() == language1) {
                System.out.print(three.format(result1.get(i)) + " (" + three.format(result2.get(i)) + ") ");
            }
        }
    }

    private double test(Test test, VectorSpace space, boolean skipOOV) {
        double sum = 0.0;
        int count = 0;

        List<Double> similarities1 = new ArrayList<>();
        List<Double> similarities2 = new ArrayList<>();

        for (Test.WordPair wp: test.getWords()) {
            float[] vector1 = space.getVector(wp.word1);
            float[] vector2 = space.getVector(wp.word2);

            if ((vector1 == null || vector2 == null) && skipOOV) continue;

            if (vector1 == null && TRY_OTHER_LANGUAGES) {
                vector1 = getOtherLanguages(wp.word1, space);
            }
            if (vector1 == null && wp.word1.contains("_") && TRY_BOW) {
                vector1 = tryBagOfWords(wp.word1, space);
            }

            if (vector2 == null && TRY_OTHER_LANGUAGES) {
                vector2 = getOtherLanguages(wp.word2, space);
            }
            if (vector2 == null && wp.word2.contains("_") && TRY_BOW) {
                vector2 = tryBagOfWords(wp.word2, space);
            }

            if (vector1 == null || vector2 == null) {
                similarities1.add(wp.score);

                if (count < 5) similarities2.add(DEFAULT_SIMILARITY);
                else similarities2.add(sum / count);
            }
            else {
                similarities1.add(wp.score);

                double sim = MyUtils.getSimilarity(vector1, vector2);
                sum += sim;
                count++;
                similarities2.add(sim);
            }
        }

        return getPearson(similarities1, similarities2);
    }

    private float[] tryBagOfWords(String word, VectorSpace space) {
        float[] result = null;

        String language = word.substring(0, 3);
        String[] split = word.split("_");
        for (int i = 0; i < split.length; i++) {
            String s;
            if (i == 0) s = split[i];
            else  s = language + split[i];

            float[] hlp = space.getVector(s);
            if (hlp == null && TRY_OTHER_LANGUAGES) hlp = getOtherLanguages(s, space);


            if (hlp != null) {
                if (result == null) {
                    result = new float[hlp.length];
                    for (int j = 0; j < result.length; j++) {
                        result[j] = hlp[j];
                    }
                } else {
                    for (int j = 0; j < result.length; j++) {
                        result[j] += hlp[j];
                    }
                }
            }
        }

        return result;
    }

    private float[] getOtherLanguages(String word, VectorSpace space) {
        float[] result = null;
        for (int i = 0; i < 5; i++) {
            result = getOtherLanguage(word, i, space);
            if (result != null) return result;
        }

        return result;
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
