package semantic_similarity.io_utils;

import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.io.*;

/**
 * @author Josef Stroleny
 *
 * format:
 * <count> <dimension>
 * <language_1> <word_1> <dim_11> <dim_12> ...
 * <language_2> <word_2> <dim_21> <dim_22> ...
 * <language_3> <word_3> <dim_31> <dim_32> ...
 * .
 * .
 * .
 *
 */
public class MyEmbeddingUtil implements IEmbeddingUtil {
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

                String wordLanguage = string[0];
                if ((language != ELanguage.MULTILINGUAL) && (!wordLanguage.equals(language.toString()))) continue;

                String word = string[1];
                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 2]);
                }

                WordVector wordVector = new WordVector(ELanguage.fromString(wordLanguage), word, vector);
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
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

            bw.write(unifiedVectorSpace.getSize() + " " + unifiedVectorSpace.getDimension());
            bw.newLine();

            for (WordVector vector : unifiedVectorSpace.getWords().values()) {
                bw.write(vector.getLanguage().toString());
                bw.write(" " + vector.getWord());

                for(float f : vector.getVector()) {
                    bw.write(" " + String.valueOf(f));
                }

                bw.newLine();
            }

            bw.flush();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
