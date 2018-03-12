package semantic_similarity.utils.embedding;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

/**
 * @author Josef Stroleny
 */
public class FastTextMultilingualUtil implements IEmbeddingUtil {
    @Override
    public VectorSpace loadSpace(String path, int maxCount) {
        VectorSpace VectorSpace = new VectorSpace();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));

            String[] header = br.readLine().split(" ");
            int wordCount = Math.min(Integer.parseInt(header[0]), maxCount);
            int vectorDimension = Integer.parseInt(header[1]);

            for (int i = 0; i < wordCount; i++) {
                String[] string = br.readLine().split(" ");

                String word = string[0].substring(3);
                ELanguage language = ELanguage.fromString(string[0].substring(0, 2));

                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 1]);
                }

                VectorSpace.addWord(language, word, vector);
            }

            return VectorSpace;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    @Override
    public void saveSpace(String path, VectorSpace vectorSpace) {
        //not used
    }
}