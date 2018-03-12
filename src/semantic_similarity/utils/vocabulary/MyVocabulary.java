package semantic_similarity.utils.vocabulary;

import semantic_similarity.utils.document.MyDocument;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.*;

/**
 * @author Josef Stroleny
 *
 * <id> - <word> mapping + word counts
 */
public class MyVocabulary {
    private InnerVocabulary vocabulary;

    public MyVocabulary() {
        this.vocabulary = new InnerVocabulary();
    }

    public int add(String word) {
        return vocabulary.add(word, 1);
    }

    public int add(String word, int count) {
        return vocabulary.add(word, count);
    }

    public void remove(String word) {
        remove(getKey(word));
    }

    public void remove(int wordKey) {
        vocabulary.addCount(wordKey, -1);
    }

    public int getKey(String word) {
        if (word == null) return -1;
        return vocabulary.getKey(word);
    }

    public String getWord(int key) {
        return vocabulary.getWord(key);
    }

    public int getWordCount(int key) {
        return vocabulary.getCount(key);
    }

    public int getTotalCount() {
        return vocabulary.count;
    }

    public int getWordCount(String word) {
        Integer key = getKey(word);
        return vocabulary.getCount(key);
    }

    public boolean contains(String word) {
        Integer contains = vocabulary.wordToKey.get(word);
        return (contains != null);
    }

    public int size() {
        return vocabulary.wordList.size();
    }

    public void sortWordsByCount() {
        Collections.sort(vocabulary.wordList, new Comparator<String>() {
            @Override
            public int compare(String o1, String o2) {
                int firstWordOccurrence = getWordCount(getKey(o1));
                int secondWordOccurrence = getWordCount(getKey(o2));
                return (firstWordOccurrence > secondWordOccurrence) ? -1 :
                        (firstWordOccurrence < secondWordOccurrence) ? 1 : 0;
            }
        });
    }

    public void save(BufferedWriter bw) throws IOException {
        bw.write(String.valueOf(size()));
        bw.newLine();
        for (int i = 0; i < size(); i++) {
            bw.write(String.valueOf(i) + " ");  //id
            bw.write(getWord(i) + " ");  //word
            bw.write(String.valueOf(getWordCount(i))); //count
            bw.newLine();
        }
        bw.flush();
    }

    public static MyVocabulary load(BufferedReader br) throws IOException {
        MyVocabulary result = new MyVocabulary();

        int size = Integer.valueOf(br.readLine());
        for (int i = 0; i <size; i++) {
            String[] line = br.readLine().split(" ");
            result.add(line[1], Integer.parseInt(line[2]));
        }

        return result;
    }

    public List<String> getWordList() {
        return vocabulary.wordList;
    }

    private class InnerVocabulary {
        List<String> wordList;
        Map<String, Integer> wordToKey;
        Map<Integer, Integer> keyToCount;
        int count;

        public InnerVocabulary() {
            this.wordList = new ArrayList<>();
            this.wordToKey = new HashMap<>();
            this.keyToCount = new HashMap<>();
            this.count = 0;
        }

        int add(String word, int count) {
            int wordKey = getKey(word);
            if (wordKey == -1) {
                wordKey = wordList.size();

                wordList.add(word);
                wordToKey.put(word, wordKey);
            }

            if (count > 0) {
                Integer actualCount = keyToCount.get(wordKey);
                if (actualCount == null) keyToCount.put(wordKey, count);
                else keyToCount.put(wordKey, keyToCount.get(wordKey) + count);
                this.count += count;
            }

            return wordKey;
        }

        int getKey(String word) {
            Integer key = this.wordToKey.get(word);
            return (key != null ? key : -1);
        }

        String getWord(int key) {
            if (key < 0 || key >= wordList.size()) return null;
            return wordList.get(key);
        }

        void addCount(int wordKey, int count) {
            Integer actualCount = keyToCount.get(wordKey);
            if (actualCount == null) keyToCount.put(wordKey, count);
            else keyToCount.put(wordKey, Math.max(keyToCount.get(wordKey) + count, 0));

            this.count += count;
        }

        int getCount(int key) {
            Integer occ = keyToCount.get(key);
            return (occ != null) ? occ : -1;
        }
    }
}
