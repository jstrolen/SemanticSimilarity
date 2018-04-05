package semantic_similarity.utils.dictionary;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.testing.Test;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.*;

import static semantic_similarity.Settings.CROSSLINGUAL_MAPPING;
import static semantic_similarity.Settings.UNIQUE_ONLY;

/**
 * @author Josef Stroleny
 */
public class MyDictionary {
    private Map<String, List<String>> dictionaryTable;

    public MyDictionary() {
        this.dictionaryTable = new HashMap<>();
    }

    public void load(String folder) {
        try {
            File file = new File(folder);
            for (final File fileEntry : file.listFiles()) {
                if (!fileEntry.isDirectory()) {
                    Map<String, Integer> sourceTarget = new HashMap<>();
                    Map<String, Integer> targetSource = new HashMap<>();
                    if(UNIQUE_ONLY) {
                        getOccurrences(sourceTarget, targetSource, fileEntry);
                    }

                    String languages = fileEntry.getName().split("_")[1];
                    ELanguage language1 = ELanguage.fromString(languages.substring(0, 2));
                    ELanguage language2 = ELanguage.fromString(languages.substring(3, 5));

                    BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                    String line;
                    while ((line = br.readLine()) != null){
                        String[] splitLine = line.split("\\s+");
                        if (splitLine.length != 2) continue;

                        String w1 = splitLine[0].toLowerCase();
                        String w2 = splitLine[1].toLowerCase();

                        if (UNIQUE_ONLY && ((sourceTarget.get(w1) != null && sourceTarget.get(w1) > 1) || (targetSource.get(w2) != null && targetSource.get(w2) > 1))) continue;

                        String word1 = language1.toString() + ":" + w1;
                        String word2 = language2.toString() + ":" + w2;

                        if (language1 != ELanguage.ENGLISH && language2 != ELanguage.ENGLISH) {
                            putWord(word1, word2, dictionaryTable);
                            putWord(word2, word1, dictionaryTable);

                            continue;
                        } else if (language1 == ELanguage.ENGLISH && language2 == ELanguage.ENGLISH) {
                            continue;
                        } else if (language2 == ELanguage.ENGLISH) {
                            String hlp = word1;
                            word1 = word2;
                            word2 = hlp;
                        }

                        putWord(word1, word2, dictionaryTable);
                        if (!CROSSLINGUAL_MAPPING) putWord(word2, word1, dictionaryTable);
                    }
                    br.close();
                }
            }

            if (CROSSLINGUAL_MAPPING) {
                Map<String, List<String>> hlp = new HashMap<>();

                for (Map.Entry<String, List<String>> pair : dictionaryTable.entrySet()) {
                    for (String s1 : pair.getValue()) {
                        putWord(s1, pair.getKey(), hlp);

                        for (String s2 : pair.getValue()) {
                            if (s1.equals(s2)) continue;

                            putWord(s1, s2, hlp);
                        }
                    }
                }

                dictionaryTable.putAll(hlp);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void getOccurrences(Map<String, Integer> sourceTarget, Map<String, Integer> targetSource, File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        String line;
        while ((line = br.readLine()) != null){
            String[] splitLine = line.split("\\s+");
            if (splitLine.length != 2) continue;

            String word1 = splitLine[0].toLowerCase();
            String word2 = splitLine[1].toLowerCase();

            if (!sourceTarget.containsKey(word1)) sourceTarget.put(word1, 1);
            else sourceTarget.put(word1, sourceTarget.get(word1) + 1);

            if (!targetSource.containsKey(word2)) targetSource.put(word2, 1);
            else targetSource.put(word2, targetSource.get(word2) + 1);
        }
        br.close();
    }

    public boolean contains(String word) {
        return dictionaryTable.containsKey(word);
    }

    public String getRandom(String word) {
        List<String> list = dictionaryTable.get(word);
        if (list == null) return null;

        return list.get(MyUtils.random.nextInt(list.size()));
    }

    private void putWord(String word1, String word2, Map<String, List<String>> map) {
        if (!map.containsKey(word1)) map.put(word1, new ArrayList<>());
        if (map.get(word1).contains(word2)) return;

        map.get(word1).add(word2);
    }

    public Map<String, List<String>> getDictionaryTable() {
        return dictionaryTable;
    }
}
