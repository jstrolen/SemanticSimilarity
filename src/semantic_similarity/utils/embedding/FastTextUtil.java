package semantic_similarity.utils.embedding;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;

import java.io.*;

/**
 * @author Josef Stroleny
 *
 * https://github.com/facebookresearch/fastText/blob/master/pretrained-vectors.md
 */
public class FastTextUtil implements IEmbeddingUtil {
    @Override
    public VectorSpace loadSpace(String path, int maxCount) {
        VectorSpace VectorSpace = new VectorSpace();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));

            ELanguage language = ELanguage.fromString(path.substring(Math.max(path.length() - 6, 0), Math.max(path.length() - 6, 0) + 2));
            String[] header = br.readLine().split(" ");

            int wordCount = Math.min(Integer.parseInt(header[0]), maxCount);
            int vectorDimension = Integer.parseInt(header[1]);

            int count = 0;
            String line;
            while ((line = br.readLine()) != null && count < maxCount) {
                String[] string = line.split(" ");
                if (string.length < 2) continue;

                String word = string[0];
                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 1]);
                }

                VectorSpace.addWord(language, word, vector);
                count++;
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
