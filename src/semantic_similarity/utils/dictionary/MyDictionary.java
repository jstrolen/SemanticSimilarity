package semantic_similarity.utils.dictionary;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.testing.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

/**
 * @author Josef Stroleny
 */
public class MyDictionary {
    private Map<String, List<String>> dictionaryTable;

    public MyDictionary() {
        this.dictionaryTable = new HashMap<>();
    }

    public void load(String folder, List<ELanguage> excludedLanguages) {
        try {
            File file = new File(folder);
            for (final File fileEntry : file.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    String languages = fileEntry.getName().split("_")[1];
                    ELanguage language1 = ELanguage.fromString(languages.substring(0, 2));
                    ELanguage language2 = ELanguage.fromString(languages.substring(3, 5));

                    if (excludedLanguages.contains(language1) || excludedLanguages.contains(language2)) {
                        continue;
                    }

                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line;
                    while ((line = br.readLine()) != null){
                        String[] splitLine = line.split("\\s+");
                        if (splitLine.length != 2) continue;

                        String word1 = language1.toString() + ":" + splitLine[0].toLowerCase();
                        String word2 = language2.toString() + ":" + splitLine[1].toLowerCase();

                        putWord(word1, word2);
                        putWord(word2, word1);
                    }

                    br.close();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public boolean contains(String word) {
        return dictionaryTable.containsKey(word);
    }

    public String getRandom(String word) {
        List<String> list = dictionaryTable.get(word);
        if (list == null) return null;

        return list.get(MyUtils.random.nextInt(list.size()));
    }

    private void putWord(String word1, String word2) {
        if (!dictionaryTable.containsKey(word1)) dictionaryTable.put(word1, new ArrayList<>());
        dictionaryTable.get(word1).add(word2);
    }
}
