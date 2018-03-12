package semantic_similarity;

import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.document.DocumentHolder;
import semantic_similarity.utils.embedding.FastTextMultilingualUtil;
import semantic_similarity.utils.embedding.FastTextUtil;
import semantic_similarity.utils.embedding.IEmbeddingUtil;
import semantic_similarity.utils.embedding.MyEmbeddingUtil;
import semantic_similarity.utils.parser.DefaultParser;
import semantic_similarity.utils.parser.IParser;
import semantic_similarity.techniques.word2vec.Word2vec;
import semantic_similarity.utils.testing.TestHolder;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static semantic_similarity.Settings.CORPUS_PATH;
import static semantic_similarity.Settings.TESTING_PATH;
import static semantic_similarity.Settings.VOCABULARY_PATH;

/**
 * @author Josef Stroleny
 */
public class Main {
    public static void main(String[] args) {
        /*
        1M, 10M, 20M, 50M
        en - 0.4k, 4k, 8k, 20k
        de - 0.6k, 7k, 13k, 40k
        es - 0.5k, 6k, 17k, 45k
        */

        try {
            //nacteni testu
            TestHolder test = new TestHolder();
            test.load(TESTING_PATH);

            /*
            //nacteni slovniku
            List<ELanguage> excludedLanguages = new ArrayList<>();
            excludedLanguages.add(ELanguage.CZECH); excludedLanguages.add(ELanguage.CHINESE);
            MyDictionary dictionary = new MyDictionary();
            dictionary.load(VOCABULARY_PATH, excludedLanguages);


            //Zamichani korpusu
            DocumentHolder documents = DocumentHolder.load(CORPUS_PATH + "multilingual_unreduced_50M.txt");
            documents.mixDocuments(dictionary, 0.5);
            documents.savePlainText(CORPUS_PATH + "multilingual_unreduced_50M_plaintext_mixed0.5.txt", true);
            */

            IEmbeddingUtil util = new FastTextMultilingualUtil();
            VectorSpace multilingual = util.loadSpace(Settings.EMBEDDING_PATH + "50M_unreduced_300_5e_sg_6-9_mixed0.5.vec", Integer.MAX_VALUE);
            test.testAll(multilingual, true);
            System.out.println();
            test.testAll(multilingual, false);
            System.out.println(multilingual.getSize());

            /*
            DocumentHolder documents = new DocumentHolder();
            IParser parser = new DefaultParser();
            BufferedReader br = new BufferedReader(new FileReader(new File(CORPUS_PATH + "document-aligned/en_full.txt")));
            int count = 0;
            StringBuilder sb = new StringBuilder();
            String line = br.readLine();
            while (line != null && count < 300) {
                if (line.length() > 0) {
                    sb.append(" " + line);
                } else if (line.length() <= 0 && sb.length() > 0) {
                    documents.addDocument(ELanguage.ENGLISH, parser, sb.toString());
                    sb = new StringBuilder();
                    count++;
                }
                line = br.readLine();
            }
            br.close();
            System.out.println(documents.getVocabulary().getTotalCount());
            System.out.println(documents.getVocabulary().size());

            documents.savePlainText(CORPUS_PATH + "test.txt");
            */

            //nejpodobnejsi slova
            Map<String, Double> mostSimilar = multilingual.getMostSimilarWords("en:shark", 10);
            for (Map.Entry<String, Double> entry : MyUtils.sortByValueDescending(mostSimilar).entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            mostSimilar = multilingual.getMostSimilarWords("en:castle", 10);
            for (Map.Entry<String, Double> entry : MyUtils.sortByValueDescending(mostSimilar).entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }
            mostSimilar = multilingual.getMostSimilarWords("en:money", 10);
            for (Map.Entry<String, Double> entry : MyUtils.sortByValueDescending(mostSimilar).entrySet()) {
                System.out.println(entry.getKey() + " = " + entry.getValue());
            }

            /*
            //trenovani
            Word2vec embedding = new Word2vec(documents);
            for (int i = 0; i < 100; i++) {
                embedding.train(1);

                //showTopWords(embedding, "en:west");
                //showTopWords(embedding, "en:food");
                //showTopWords(embedding, "en:product");
                //showTopWords(embedding, "en:science");
                //showTopWords(embedding, "en:internet");

                test.testAll(embedding.toVectorSpace(), true);
                System.out.println();
                test.testAll(embedding.toVectorSpace(), false);
                System.out.println();
                System.out.println();
            }
            */

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void showTopWords(Word2vec embedding, String word) {
        Map<String, Double> map = embedding.getMostSimilarWords(word, 10);

        System.out.print(word + ":   ");
        for (String key : map.keySet()) {
            System.out.print(key + " ");
        }
        System.out.println();
        System.out.println();
    }
}
