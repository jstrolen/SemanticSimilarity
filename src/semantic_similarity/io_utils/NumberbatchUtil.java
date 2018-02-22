package semantic_similarity.io_utils;

import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.utils.Settings.EMBEDDING_PATH;

/**
 * @author Josef Stroleny
 */
public class NumberbatchUtil implements IEmbeddingUtil {
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

                String info[] = string[0].split("/");
                String wordLanguage = info[2];
                String word = info[3];

                if ((language != ELanguage.MULTILINGUAL) && (!wordLanguage.equals(language.toString()))) continue;

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

    public static void saveAsMultilingual(String name, IEmbeddingUtil outputEmbedding) {
        IEmbeddingUtil embeddingUtil = new NumberbatchUtil();

        List<UnifiedVectorSpace> vectorSpaces = new ArrayList<>();
        vectorSpaces.add(embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", ELanguage.CZECH, Integer.MAX_VALUE));
        vectorSpaces.add(embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", ELanguage.ENGLISH, Integer.MAX_VALUE));
        vectorSpaces.add(embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", ELanguage.GERMAN, Integer.MAX_VALUE));
        vectorSpaces.add(embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", ELanguage.SPANISH, Integer.MAX_VALUE));
        vectorSpaces.add(embeddingUtil.loadSpace(EMBEDDING_PATH + name + ".txt", ELanguage.CHINESE, Integer.MAX_VALUE));

        UnifiedVectorSpace crosslingualNumberbatch = UnifiedVectorSpace.mergeVectorSpaces(vectorSpaces);
        outputEmbedding.saveSpace(EMBEDDING_PATH + outputEmbedding + "_multilingual.txt", crosslingualNumberbatch);
    }
}
