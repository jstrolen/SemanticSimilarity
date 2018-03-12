package semantic_similarity.utils.embedding;

import semantic_similarity.VectorSpace;

/**
 * @author Josef Stroleny
 */
public interface IEmbeddingUtil {
    VectorSpace loadSpace(String path, int maxCount);

    void saveSpace(String path, VectorSpace vectorSpace);
}
