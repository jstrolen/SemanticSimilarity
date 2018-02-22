package semantic_similarity.io_utils;

import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;

/**
 * @author Josef Stroleny
 */
public interface IEmbeddingUtil {
    UnifiedVectorSpace loadSpace(String path, ELanguage language, int maxCount);

    void saveSpace(String path, UnifiedVectorSpace unifiedVectorSpace);
}
