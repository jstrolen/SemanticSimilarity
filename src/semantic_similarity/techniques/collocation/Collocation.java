package semantic_similarity.techniques.collocation;

import java.io.*;
import java.util.*;

/**
 * @author Josef Stroleny
 */
public class Collocation {
    private Map<String, Map<String, Integer>> collocationMap;
    private Map<String, Integer> wordMap;
    private int totalWords;

    private Map<String, Set<String>> finalized;

    public Collocation() {
        this.collocationMap = new HashMap<>();
        this.wordMap = new HashMap<>();
        this.totalWords = 0;

        this.finalized = new HashMap<>();
    }

    public void learn(String file) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(file)));

            int x = 0;
            String line;
            while ((line = br.readLine()) != null && x < 500000) {
                String[] split = line.split(" ");
                if (split.length <= 0) continue;

                for (int i = 0; i < split.length - 1; i++) {
                    addToMap(split[i], split[i + 1]);

                    Integer count = wordMap.get(split[i]);
                    if (count == null) count = 0;
                    wordMap.put(split[i], count + 1);
                    totalWords++;
                }

                Integer count = wordMap.get(split[split.length - 1]);
                if (count == null) count = 0;
                wordMap.put(split[split.length - 1], count + 1);
                totalWords++;

                x++;
            }

            br.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void finalize(int min_count, double threshold) {
        for (Map.Entry<String, Map<String, Integer>> pair : collocationMap.entrySet()) {
            for (Map.Entry<String, Integer> word : pair.getValue().entrySet()) {
                double score = ((double) word.getValue() - min_count) /
                        (Math.pow(wordMap.get(pair.getKey()), 1.50) * Math.pow(wordMap.get(word.getKey()), 1.50)) * totalWords;
                if (score > threshold) addToFinalized(pair.getKey(), word.getKey());
            }
        }
    }

    private void addToFinalized(String key1, String key2) {
        if (finalized.get(key1) == null) finalized.put(key1, new HashSet<>());
        Set<String> s = finalized.get(key1);

        s.add(key2);
    }

    private void addToMap(String s1, String s2) {
        if (collocationMap.get(s1) == null) collocationMap.put(s1, new HashMap<>());
        Map<String, Integer> m = collocationMap.get(s1);

        Integer i = m.get(s2);
        if (i == null) m.put(s2, 1);
        else m.put(s2, i + 1);
    }

    public void resave(File file) throws IOException {
        BufferedReader br = new BufferedReader(new FileReader(file));
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(file.getAbsolutePath() + "_collocation.txt")));

        String line;
        while((line = br.readLine()) != null) {
            String[] splitted = line.split(" ");
            List<String> newLine = new ArrayList<>();

            if (splitted.length < 1) continue;

            String last = splitted[0];
            String hlp = last;
            for (int i = 1; i < splitted.length; i++) {
                Set<String> set = finalized.get(last);
                if (set == null || !set.contains(splitted[i])) {
                    newLine.add(hlp);
                    hlp = splitted[i];
                } else {
                    hlp = hlp + "_" + splitted[i];
                }
                last = splitted[i];
            }
            newLine.add(hlp);

            if (!newLine.isEmpty()) bw.write(newLine.get(0));
            for (int i = 1; i < newLine.size(); i++) {
                bw.write(" " + newLine.get(i));
            }

            bw.newLine();
        }


        br.close();
        bw.close();
    }

    public void print() {
        for (Map.Entry<String, Set<String>> pair : finalized.entrySet()) {
            Iterator<String> i = pair.getValue().iterator();
            System.out.println(pair.getKey() + " " + i.next());
        }
    }
}
