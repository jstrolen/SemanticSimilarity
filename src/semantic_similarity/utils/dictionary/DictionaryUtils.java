package semantic_similarity.utils.dictionary;

import semantic_similarity.utils.embedding.IEmbeddingUtil;
import semantic_similarity.ELanguage;
import semantic_similarity.VectorSpace;

import java.io.*;

import static semantic_similarity.Settings.EMBEDDING_PATH;
import static semantic_similarity.Settings.TEMP_PATH;
import static semantic_similarity.Settings.VOCABULARY_PATH;

/**
 * @author Josef Stroleny
 */
public class DictionaryUtils {
    /**
     * Vezme slova ze zdrojového jazyka, jejich překlady do cílového jazyka (angličtina),
     * odstraní víceslovná slova a členy a uloží výstup jako překladový slovník
     */
    public static void createAllTranslationWordPairs() {
        createTranslationWordPairs(TEMP_PATH + "words-cs.txt", TEMP_PATH + "words-cs_en-translation.txt",
                VOCABULARY_PATH + "manual-vocabulary_cs-en.txt");

        createTranslationWordPairs(TEMP_PATH + "words-de.txt", TEMP_PATH + "words-de_en-translation.txt",
                VOCABULARY_PATH + "manual-vocabulary_de-en.txt");

        createTranslationWordPairs(TEMP_PATH + "words-es.txt", TEMP_PATH + "words-es_en-translation.txt",
                VOCABULARY_PATH + "manual-vocabulary_es-en.txt");

        createTranslationWordPairs(TEMP_PATH + "words-zh.txt", TEMP_PATH + "words-zh_en-translation.txt",
                VOCABULARY_PATH + "manual-vocabulary_zh-en.txt");
    }

    /**
     * Vezme natrenovane monolingualni jazykove modely a prekladovy slovnik a ulozi do dvou souboru vektory zdrojoveho a ciloveho jazyka
     */
    public static void createAllTranslationVectorPairs(IEmbeddingUtil embedding, String vectorName) {
        VectorSpace englishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_en.txt", Integer.MAX_VALUE);
        VectorSpace czechVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_cs.txt", Integer.MAX_VALUE);
        VectorSpace germanVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_de.txt", Integer.MAX_VALUE);
        VectorSpace spanishVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_es.txt", Integer.MAX_VALUE);
        VectorSpace chineseVectors = embedding.loadSpace(EMBEDDING_PATH + vectorName + "_zh.txt", Integer.MAX_VALUE);

        createTranslationVectorPairs(czechVectors, englishVectors, VOCABULARY_PATH + "vocabulary_cs-en.txt",
                TEMP_PATH + "vectors-cs-en_cs.txt", TEMP_PATH + "vectors-cs-en_en.txt");

        createTranslationVectorPairs(germanVectors, englishVectors, VOCABULARY_PATH + "vocabulary_de-en.txt",
                TEMP_PATH + "vectors-de-en_de.txt", TEMP_PATH + "vectors-de-en_en.txt");

        createTranslationVectorPairs(spanishVectors, englishVectors, VOCABULARY_PATH + "vocabulary_es-en.txt",
                TEMP_PATH + "vectors-es-en_es.txt", TEMP_PATH + "vectors-es-en_en.txt");

        createTranslationVectorPairs(chineseVectors, englishVectors, VOCABULARY_PATH + "vocabulary_zh-en.txt",
                TEMP_PATH + "vectors-zh-en_zh.txt", TEMP_PATH + "vectors-zh-en_en.txt");
    }

    private static void createTranslationWordPairs(String sourcePath, String targetPath, String outputPath) {
        try {
            BufferedReader br_source = new BufferedReader(new FileReader(new File(sourcePath)));
            BufferedReader br_target = new BufferedReader(new FileReader(new File(targetPath)));
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(outputPath)));

            String source;
            while ((source = br_source.readLine()) != null) {
                String target = br_target.readLine();

                String[] parts = target.split(" ");
                //fraze nezapisujeme
                if (parts.length > 2) continue;
                //odstranime cleny
                if (parts.length == 2) {
                    if (!parts[0].equals("a") && !parts[0].equals("an") && !parts[0].equals("the")) continue;
                    else target = parts[1];
                }

                bw.write(source + " " + target);
                bw.newLine();
            }

            bw.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void createTranslationVectorPairs(VectorSpace sourceVectors, VectorSpace targetVectors,
                                                     String vocabularyPairs, String sourceOutput, String targetOutput) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(vocabularyPairs)));
            BufferedWriter bw_source = new BufferedWriter(new FileWriter(new File(sourceOutput)));
            BufferedWriter bw_target = new BufferedWriter(new FileWriter(new File(targetOutput)));
            String line;
            while ((line = br.readLine()) != null) {
                String pair[] = line.split("\\s");
                if (pair.length != 2) continue;

                String source = pair[0];
                String target = pair[1];
                float[] sourceVector = sourceVectors.getVector(source);
                float[] targetVector = targetVectors.getVector(target);
                if (sourceVector == null || targetVector == null) continue;

                for (int i = 0; i < sourceVector.length; i++) {
                    bw_source.write(String.valueOf(sourceVector[i]) + " ");
                }
                bw_source.newLine();

                for (int i = 0; i < targetVector.length; i++) {
                    bw_target.write(String.valueOf(targetVector[i]) + " ");
                }
                bw_target.newLine();
            }

            bw_source.flush();
            bw_target.flush();

        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
