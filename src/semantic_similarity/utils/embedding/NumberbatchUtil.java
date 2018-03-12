package semantic_similarity.utils.embedding;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;

import static semantic_similarity.Settings.EMBEDDING_PATH;

/**
 * @author Josef Stroleny
 */
public class NumberbatchUtil implements IEmbeddingUtil {
    @Override
    public VectorSpace loadSpace(String path, int maxCount) {
        VectorSpace vectorSpace = new VectorSpace();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));

            String[] header = br.readLine().split(" ");

            int wordCount = Math.min(Integer.parseInt(header[0]), maxCount);
            int vectorDimension = Integer.parseInt(header[1]);

            for (int i = 0; i < wordCount; i++) {
                String[] string = br.readLine().split(" ");

                String info[] = string[0].split("/");

                ELanguage wordLanguage = ELanguage.fromString(info[2]);
                String word = info[3];

                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 1]);
                }

                vectorSpace.addWord(wordLanguage, word, vector);
            }

            return vectorSpace;

        } catch (Exception e) {
            e.printStackTrace();

        }
        return null;
    }

    @Override
    public void saveSpace(String path, VectorSpace vectorSpace) {
        //not used
    }

    public static void saveAsMultilingual(String name, IEmbeddingUtil outputEmbedding) {
        IEmbeddingUtil embeddingUtil = new NumberbatchUtil();
        VectorSpace vectorSpace = embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", Integer.MAX_VALUE);
        outputEmbedding.saveSpace(EMBEDDING_PATH + outputEmbedding + "_multilingual.txt", vectorSpace);
    }
}
