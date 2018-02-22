package semantic_similarity.io_utils;

import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.io.*;

/**
 * @author Josef Stroleny
 *
 * https://github.com/facebookresearch/fastText/blob/master/pretrained-vectors.md
 */
public class FastTextUtil implements IEmbeddingUtil {
    @Override
    public UnifiedVectorSpace loadSpace(String path, ELanguage language, int maxCount) {
        UnifiedVectorSpace unifiedVectorSpace = new UnifiedVectorSpace(language);
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String[] header = br.readLine().split(" ");

            int wordCount = Math.min(Integer.parseInt(header[0]), maxCount);
            int vectorDimension = Integer.parseInt(header[1]);

            for (int i = 0; i < wordCount; i++) {
                String[] string = br.readLine().split(" ");

                String word = string[0];
                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 1]);
                }

                WordVector wordVector = new WordVector(language, word, vector);
                unifiedVectorSpace.addWord(wordVector);
            }

            return unifiedVectorSpace;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public void saveSpace(String path, UnifiedVectorSpace unifiedVectorSpace) {

    }
}
