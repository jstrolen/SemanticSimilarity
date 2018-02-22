package semantic_similarity;

import semantic_similarity.io_utils.FastTextUtil;
import semantic_similarity.io_utils.IEmbeddingUtil;
import semantic_similarity.io_utils.IOUtils;
import semantic_similarity.io_utils.MyEmbeddingUtil;
import semantic_similarity.techniques.monolingual_mapping.MultilingualCCA;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.Settings;
import semantic_similarity.vocabulary.VocabularyUtils;
import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.util.Map;

/**
 * @author Josef Stroleny
 */
public class Main {
    public static void main(String[] args) {
        IEmbeddingUtil util = new MyEmbeddingUtil();
        UnifiedVectorSpace multilingual = util.loadSpace(Settings.EMBEDDING_PATH + "numberbatch_multilingual.txt", ELanguage.MULTILINGUAL, Integer.MAX_VALUE);

        Map<WordVector, Double> mostSimilar = multilingual.getMostSimilarWords("hrad", ELanguage.CZECH, 10);
        for (Map.Entry<WordVector, Double> entry : MyUtils.sortByValueDescending(mostSimilar).entrySet()) {
            System.out.println(entry.getKey().getLanguage().toString() + ":" + entry.getKey().getWord() + " = " + entry.getValue());
        }
    }
}
