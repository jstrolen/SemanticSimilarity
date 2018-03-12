package semantic_similarity.utils.embedding;

import semantic_similarity.VectorSpace;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;

import static semantic_similarity.Settings.EMBEDDING_PATH;
import static semantic_similarity.Settings.TEMP_PATH;

/**
 * @author Josef Stroleny
 */
public class EmbeddingUtils {
    /**
     * Zredukuje pocet embeddingu a ulozi do souboru
     */
    public static void reduceEmbeddingCount(IEmbeddingUtil inputEmbedding, String inputVectorName, IEmbeddingUtil outputEmbedding, String vectorName, int count) {
        VectorSpace czechVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_cs.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_cs.txt", czechVectors);

        VectorSpace englishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_en.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_en.txt", englishVectors);

        VectorSpace germanVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_de.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_de.txt", germanVectors);

        VectorSpace spanishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_es.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_es.txt", spanishVectors);

        VectorSpace chineseVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_zh.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_zh.txt", chineseVectors);
    }

    /**
     * Ulozi slova z danych embedingu do samostatnych souboru
     * Odstrani kratka slova a fraze
     */
    public static void printWordsFromEmbeddings(IEmbeddingUtil embedding, String vectorName) {
        VectorSpace czechVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_cs.txt", Integer.MAX_VALUE);
        printWordsFromEmbedding(czechVectors, TEMP_PATH + "words-cs.txt");

        VectorSpace englishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_en.txt", Integer.MAX_VALUE);
        printWordsFromEmbedding(englishVectors, TEMP_PATH + "words-en.txt");

        VectorSpace germanVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_de.txt", Integer.MAX_VALUE);
        printWordsFromEmbedding(germanVectors, TEMP_PATH + "words-de.txt");

        VectorSpace spanishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_es.txt", Integer.MAX_VALUE);
        printWordsFromEmbedding(spanishVectors, TEMP_PATH + "words-es.txt");

        VectorSpace chineseVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_zh.txt", Integer.MAX_VALUE);
        printWordsFromEmbedding(chineseVectors, TEMP_PATH + "words-zh.txt");
    }

    private static void printWordsFromEmbedding(VectorSpace vectorSpace, String target) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(target)));
            for (String s : vectorSpace.getVectorSpace().keySet()) {
                bw.write(s.substring(3));
                bw.newLine();
            }
            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
