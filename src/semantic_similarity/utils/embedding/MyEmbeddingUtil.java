package semantic_similarity.utils.embedding;

import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;

import java.io.*;
import java.util.Map;

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
    public VectorSpace loadSpace(String path, int maxCount) {
        VectorSpace VectorSpace = new VectorSpace();
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));
            String[] header = br.readLine().split(" ");

            int wordCount = Math.min(Integer.parseInt(header[0]), maxCount);
            int vectorDimension = Integer.parseInt(header[1]);

            int count = 0;
            String line;
            while ((line = br.readLine()) != null && count < maxCount) {
                String[] string = line.split(" ");
                if (string.length < 2) continue;

                ELanguage wordLanguage = ELanguage.fromString(string[0]);
                String word = string[1];
                float[] vector = new float[vectorDimension];
                for (int vector_i = 0; vector_i < vectorDimension; vector_i++) {
                    vector[vector_i] = Float.parseFloat(string[vector_i + 2]);
                }

                VectorSpace.addWord(wordLanguage, word, vector);
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
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

            bw.write(vectorSpace.getSize() + " " + vectorSpace.getDimension());
            bw.newLine();

            for (Map.Entry<String, float[]> vector : vectorSpace.getVectorSpace().entrySet()) {

                bw.write(vector.getKey().substring(0, 2));
                bw.write(" " + vector.getKey().substring(3));

                for(float f : vector.getValue()) {
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
