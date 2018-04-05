package semantic_similarity;

import semantic_similarity.techniques.collocation.Collocation;
import semantic_similarity.techniques.monolingual_mapping.MultilingualCCA;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.document.DocumentHolder;
import semantic_similarity.utils.embedding.*;
import semantic_similarity.utils.parser.IParser;
import semantic_similarity.utils.parser.PlaintextParser;
import semantic_similarity.utils.testing.TestHolder;

import java.io.*;
import java.util.*;

import static semantic_similarity.Settings.*;
import static semantic_similarity.utils.MyUtils.mergeVectorSpaces;
import static semantic_similarity.utils.MyUtils.random;
import static semantic_similarity.utils.document.DocumentUtils.loadDocuments;
import static semantic_similarity.utils.document.DocumentUtils.loadSentenceAlignedCorpus;

/**
 * @author Josef Stroleny
 */
public class Main {
    public static void main(String[] args) {
        try {
            TestHolder test = new TestHolder();
            test.load(TESTING_PATH);

            /*MyDictionary dictionary = new MyDictionary();
            dictionary.load(VOCABULARY_PATH);*/

            /*
            DocumentHolder documents = new DocumentHolder();
            loadData(documents, includedLanguages, dictionary);
            documents.savePlainText(CORPUS_PATH + "test.txt", true);
            */

            //Zkouska uspesnosti
            VectorSpace multilingual = new MyEmbeddingUtil().loadSpace(EMBEDDING_PATH + "fasttext-300k_multilingual.txt", Integer.MAX_VALUE);
            System.out.println(multilingual.getSize());
            //printMostSimilarWords(multilingual, "cs:peníze", "en:shark", "cs:hrad");
            test.testAll(multilingual);
            //interactive(multilingual);


            /*List<VectorSpace> vs = new ArrayList<>();
            vs.add(new MyEmbeddingUtil().loadSpace(Settings.EMBEDDING_PATH + "fasttext-300k_multilingual.txt", Integer.MAX_VALUE));
            vs.add(multilingual);
            mergeVectorSpaces(vs, true, false);*/

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void work(VectorSpace multilingual, String language) throws IOException {
        BufferedWriter bw = new BufferedWriter(new FileWriter(new File(EMBEDDING_PATH + "fasttext-uni_" + language + ".txt")));
        bw.write("0 300");
        bw.newLine();

        BufferedReader br = new BufferedReader(new FileReader(new File(EMBEDDING_PATH + "fasttext_" + language + ".txt")));
        br.readLine();
        String line;
        while ((line = br.readLine()) != null) {
            String[] split = line.split(" ");
            if (split.length < 1) continue;

            if (multilingual.getVector(language + ":" + split[0]) != null) {
                bw.write(line);
                bw.newLine();
            }
        }
        bw.flush();
    }

    private static void interactive(VectorSpace space) {
        System.out.println("ready: ");
        Scanner sc = new Scanner(System.in);
        String line;
        while ((line = sc.nextLine()) != null) {
            List<String> list = Arrays.asList(line.split(" "));

            Map<String, Double> map = space.getMostSimilarWords(list, 10);
            if (map == null || map.isEmpty()) continue;
            Iterator<Map.Entry<String, Double>> iterator1 = MyUtils.sortByValueDescending(map).entrySet().iterator();
            while (iterator1.hasNext()) {
                Map.Entry<String, Double> entry1 = iterator1.next();
                System.out.println(entry1.getKey() + " : " + entry1.getValue());
            }
            System.out.println();

        }
    }

    public static void loadData(DocumentHolder documents, List<ELanguage> includedLanguages, MyDictionary dictionary) throws IOException {
        loadDocuments(documents, includedLanguages, 30*M);
        documents.mixDocuments(dictionary, 1.0);

        loadSentenceAlignedCorpus(documents, 20*M);

        Collections.shuffle(documents.getDocuments());
    }

    public static void printMostSimilarWords(VectorSpace space, String word1, String word2, String word3) {
        int mostSimilarCount = 10;
        Iterator<Map.Entry<String, Double>> iterator1 = MyUtils.sortByValueDescending(space.getMostSimilarWords(word1, mostSimilarCount)).entrySet().iterator();
        Iterator<Map.Entry<String, Double>> iterator2 = MyUtils.sortByValueDescending(space.getMostSimilarWords(word2, mostSimilarCount)).entrySet().iterator();
        Iterator<Map.Entry<String, Double>> iterator3 = MyUtils.sortByValueDescending(space.getMostSimilarWords(word3, mostSimilarCount)).entrySet().iterator();

        System.out.println(word1 + " | | " + word2 + " | | " + word3 + " | | ");
        System.out.println("--- | --- | --- | --- | --- | ---");
        System.out.println("Slovo | Podobnost | Slovo | Podobnost | Slovo | Podobnost");
        System.out.println("--- | --- | --- | --- | --- | ---");

        while (iterator1.hasNext()) {
            Map.Entry<String, Double> entry1 = iterator1.next();
            Map.Entry<String, Double> entry2 = iterator2.next();
            Map.Entry<String, Double> entry3 = iterator3.next();
            System.out.println(entry1.getKey() + " | " + entry1.getValue() +
                    " | " + entry2.getKey() + " | " + entry2.getValue() +
                    " | " + entry3.getKey() + " | " + entry3.getValue());
        }
    }
}
