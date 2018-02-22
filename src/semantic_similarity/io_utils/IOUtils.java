package semantic_similarity.io_utils;

import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.utils.Settings.EMBEDDING_PATH;
import static semantic_similarity.utils.Settings.TEMP_PATH;
import static semantic_similarity.utils.Settings.VOCABULARY_PATH;

/**
 * @author Josef Stroleny
 */
public class IOUtils {
    /**
     * Zredukuje pocet embeddingu a ulozi do souboru
     */
    public static void reduceEmbeddingCount(IEmbeddingUtil inputEmbedding, String inputVectorName, IEmbeddingUtil outputEmbedding, String vectorName, int count) {
        UnifiedVectorSpace czechVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_cs.txt", ELanguage.CZECH, count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_cs.txt", czechVectors);

        UnifiedVectorSpace englishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_en.txt", ELanguage.ENGLISH, count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_en.txt", englishVectors);

        UnifiedVectorSpace germanVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_de.txt", ELanguage.GERMAN, count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_de.txt", germanVectors);

        UnifiedVectorSpace spanishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_es.txt", ELanguage.SPANISH, count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_es.txt", spanishVectors);

        UnifiedVectorSpace chineseVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_zh.txt", ELanguage.CHINESE, count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_zh.txt", chineseVectors);
    }

    /**
     * Ulozi slova z danych embedingu do samostatnych souboru
     * Odstrani kratka slova a fraze
     */
    public static void getWordsFromEmbeddings(IEmbeddingUtil embedding, String vectorName) {
        UnifiedVectorSpace czechVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_cs.txt", ELanguage.CZECH, Integer.MAX_VALUE);
        getOneLanguageWords(czechVectors, TEMP_PATH + "words-cs.txt");

        UnifiedVectorSpace englishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_en.txt", ELanguage.ENGLISH, Integer.MAX_VALUE);
        getOneLanguageWords(englishVectors, TEMP_PATH + "words-en.txt");

        UnifiedVectorSpace germanVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_de.txt", ELanguage.GERMAN, Integer.MAX_VALUE);
        getOneLanguageWords(germanVectors, TEMP_PATH + "words-de.txt");

        UnifiedVectorSpace spanishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_es.txt", ELanguage.SPANISH, Integer.MAX_VALUE);
        getOneLanguageWords(spanishVectors, TEMP_PATH + "words-es.txt");

        UnifiedVectorSpace chineseVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_zh.txt", ELanguage.CHINESE, Integer.MAX_VALUE);
        getOneLanguageWords(chineseVectors, TEMP_PATH + "words-zh.txt");
    }

    private static void getOneLanguageWords(UnifiedVectorSpace unifiedVectorSpace, String target) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(target)));
            for (WordVector v : unifiedVectorSpace.getWords().values()) {

                //skip short words (except chinese) and phrases
                if ((v.getLanguage() != ELanguage.CHINESE && v.getWord().length() < 3) || v.getWord().contains("_")) continue;

                bw.write(v.getWord());
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
