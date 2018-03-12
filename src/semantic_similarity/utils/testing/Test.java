package semantic_similarity.utils.testing;

import semantic_similarity.ELanguage;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author Josef Stroleny
 */
public class Test {
    private String name;
    private ELanguage language1;
    private ELanguage language2;
    private List<WordPair> words;

    public Test(String name, ELanguage language1, ELanguage language2) {
        this.name = name;
        this.language1 = language1;
        this.language2 = language2;
        this.words = new ArrayList<>();
    }

    public static Test loadTest(File file) throws IOException {
        String[] info = file.getName().split("_");
        String[] langs = info[0].split("-");
        ELanguage language1 = ELanguage.fromString(langs[0]);
        ELanguage language2 = language1;
        if (langs.length > 1) {
            language2 = ELanguage.fromString(langs[1]);
        }

        Test test = new Test(info[1], language1, language2);

        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null) {
            String[] splitLine = line.split("\\t");
            if (splitLine.length != 3) {
                splitLine = line.split(" ");
                if (splitLine.length != 3) {
                    continue;
                }
            }

            String word1 = language1.toString() + ":" + splitLine[0].replaceAll(" ", "_").toLowerCase();
            String word2 = language2.toString() + ":" + splitLine[1].replaceAll(" ", "_").toLowerCase();
            double score = Double.parseDouble(splitLine[2]);
            test.putWord(word1, word2, score);
        }
        br.close();

        return test;
    }

    public void putWord(String word1, String word2, double score) {
        this.words.add(new WordPair(word1, word2, score));
    }

    public String getName() {
        return name;
    }

    public ELanguage getLanguage1() {
        return language1;
    }

    public ELanguage getLanguage2() {
        return language2;
    }

    public List<WordPair> getWords() {
        return words;
    }

    public class WordPair {
        String word1;
        String word2;
        double score;

        public WordPair(String word1, String word2, double score) {
            this.word1 = word1;
            this.word2 = word2;
            this.score = score;
        }
    }
}
