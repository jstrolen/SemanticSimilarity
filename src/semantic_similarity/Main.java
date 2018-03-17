package semantic_similarity;

import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.document.DocumentHolder;
import semantic_similarity.utils.embedding.*;
import semantic_similarity.utils.testing.TestHolder;

import java.io.IOException;
import java.util.*;

import static semantic_similarity.Settings.*;
import static semantic_similarity.utils.MyUtils.mergeVectorSpaces;
import static semantic_similarity.utils.document.DocumentUtils.loadDocuments;
import static semantic_similarity.utils.document.DocumentUtils.loadSentenceAlignedCorpus;

/**
 * @author Josef Stroleny
 */
public class Main {
    public static void main(String[] args) {
        try {
            List<ELanguage> includedLanguages = new ArrayList<>();
            includedLanguages.add(ELanguage.ENGLISH); includedLanguages.add(ELanguage.GERMAN); includedLanguages.add(ELanguage.SPANISH);

            TestHolder test = new TestHolder();
            test.load(TESTING_PATH);

            MyDictionary dictionary = new MyDictionary();
            dictionary.load(VOCABULARY_PATH, includedLanguages);


            /*
            DocumentHolder documents = new DocumentHolder();
            loadData(documents, includedLanguages, dictionary);
            documents.savePlainText(CORPUS_PATH + "test.txt", true);
            */

            //Zkouska uspesnosti
            //VectorSpace multilingual = new FastTextMultilingualUtil().loadSpace(Settings.EMBEDDING_PATH + "50M_combined_300_5e_sg_6-9_30M+20M.vec", Integer.MAX_VALUE);
            VectorSpace multilingual = VectorSpace.fromDump(TEMP_PATH + "cca+(dictionary+sentence)_dump");
            test.testAll(multilingual);
            System.out.println(multilingual.getSize());

            printMostSimilarWords(multilingual, "en:money", "en:shark", "en:castle");


            /*List<VectorSpace> vs = new ArrayList<>();
            vs.add(new MyEmbeddingUtil().loadSpace(Settings.EMBEDDING_PATH + "fasttext-300k_multilingual.txt", Integer.MAX_VALUE));
            vs.add(multilingual);
            mergeVectorSpaces(vs, true, false);*/

        } catch (Exception e) {
            e.printStackTrace();
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
