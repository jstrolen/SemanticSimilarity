package semantic_similarity.techniques.monolingual_mapping;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.embedding.FastTextUtil;
import semantic_similarity.utils.embedding.IEmbeddingUtil;
import semantic_similarity.utils.embedding.MyEmbeddingUtil;
import semantic_similarity.VectorSpace;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static semantic_similarity.Settings.EMBEDDING_PATH;
import static semantic_similarity.Settings.TEMP_PATH;
import static semantic_similarity.Settings.VOCABULARY_PATH;

/**
 * @author Josef Stroleny
 */
public class MultilingualCCA {
    /**
     * Postup pro Multilingual CCA
     */
    public MultilingualCCA() {
        //Redukujeme pocet slov z fasttext na top 300 000
        MultilingualCCA.reduceEmbeddingCount(new FastTextUtil(), "fasttext", new MyEmbeddingUtil(), "fasttext-300k", 300000);

        //Za slova ve slovniku dosadime vektory
        MultilingualCCA.createAllTranslationVectorPairs(new MyEmbeddingUtil(), "fasttext-300k");

        //Najdeme transformacni matici CCA
        //get CCA vectors - matlab script

        //Transformuje jazyky do spolecneho prostoru
        MultilingualCCA.transformToCommonSpace("fasttext-300k");
    }

    /**
     * Zredukuje pocet embeddingu a ulozi do souboru
     */
    public static void reduceEmbeddingCount(IEmbeddingUtil inputEmbedding, String inputVectorName, IEmbeddingUtil outputEmbedding, String vectorName, int count) {
        VectorSpace czechVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_cs.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_cs.txt", czechVectors);

        VectorSpace englishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_en.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_en.txt", englishVectors);

        VectorSpace germanVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_de.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_de.txt", germanVectors);

        VectorSpace spanishVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_es.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_es.txt", spanishVectors);

        VectorSpace chineseVectors = inputEmbedding.loadSpace(EMBEDDING_PATH + inputVectorName + "_zh.txt", count);
        outputEmbedding.saveSpace(EMBEDDING_PATH + vectorName + "_zh.txt", chineseVectors);
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
                TEMP_PATH + "vectors-cs-en_cs.txt", TEMP_PATH + "vectors-cs-en_en.txt",
                ELanguage.CZECH, ELanguage.ENGLISH);

        createTranslationVectorPairs(germanVectors, englishVectors, VOCABULARY_PATH + "vocabulary_de-en.txt",
                TEMP_PATH + "vectors-de-en_de.txt", TEMP_PATH + "vectors-de-en_en.txt",
                ELanguage.GERMAN, ELanguage.ENGLISH);

        createTranslationVectorPairs(spanishVectors, englishVectors, VOCABULARY_PATH + "vocabulary_es-en.txt",
                TEMP_PATH + "vectors-es-en_es.txt", TEMP_PATH + "vectors-es-en_en.txt",
                ELanguage.SPANISH, ELanguage.ENGLISH);

        createTranslationVectorPairs(chineseVectors, englishVectors, VOCABULARY_PATH + "vocabulary_zh-en.txt",
                TEMP_PATH + "vectors-zh-en_zh.txt", TEMP_PATH + "vectors-zh-en_en.txt",
                ELanguage.CHINESE, ELanguage.ENGLISH);
    }

    private static void createTranslationVectorPairs(VectorSpace sourceVectors, VectorSpace targetVectors,
                                                     String vocabularyPairs, String sourceOutput, String targetOutput,
                                                     ELanguage sourceLanguage, ELanguage targetLanguage) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(vocabularyPairs)));
            BufferedWriter bw_source = new BufferedWriter(new FileWriter(new File(sourceOutput)));
            BufferedWriter bw_target = new BufferedWriter(new FileWriter(new File(targetOutput)));
            String line;
            while ((line = br.readLine()) != null) {
                String pair[] = line.split("\\s");
                if (pair.length != 2) {
                    pair = line.split("\\t");
                    if (pair.length != 2) continue;
                }

                String source = sourceLanguage.toString() + ":" + pair[0];
                String target = targetLanguage.toString() + ":" + pair[1];
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

    /**
     * Transformuje vsechny jazyky do spolecneho prostoru
     * @param name Nazev embeddingu
     */
    public static void transformToCommonSpace(String name) {
        IEmbeddingUtil util = new MyEmbeddingUtil();
        List<VectorSpace> commonSpaces = new ArrayList<>();
        //cs-en
        VectorSpace source = util.loadSpace(EMBEDDING_PATH + name + "_cs.txt", Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-cs-en.txt"));
        //de-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_de.txt", Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-de-en.txt"));
        //es-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_es.txt", Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-es-en.txt"));
        //zh-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_zh.txt", Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-zh-en.txt"));
        //en
        commonSpaces.add(util.loadSpace(EMBEDDING_PATH + name + "_en.txt", Integer.MAX_VALUE));

        //merge spaces
        VectorSpace multilingualEmbeddings = VectorSpace.mergeVectorSpaces(commonSpaces);
        util.saveSpace(EMBEDDING_PATH + name + "_multilingual.txt", multilingualEmbeddings);
    }

    /**
     * Transformuje jeden jazyk do spolecneho prostoru tim, ze ho vynasobi s matici z CCA ( lang_vec * [A * inv(B)] )
     * @param sourceSpace Puvodni monolingualni prostor
     * @param multiplyMatrixPath Cesta k transformacni matici (ctvercova matice, dimenze odpovida poctu priznaku v monolingualnim prostoru)
     * @return Puvodni jazyk transformovany do multilingualniho prostoru
     */
    private static VectorSpace transformLanguageToCommonSpace(VectorSpace sourceSpace, String multiplyMatrixPath) {
        VectorSpace targetSpace = new VectorSpace();

        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(multiplyMatrixPath)));
            //load multiply matrix
            int dimension = sourceSpace.getDimension();
            float[][] multiplyMatrix = new float[dimension][dimension];
            for(int i = 0; i < dimension; i++) {
                String[] values = br.readLine().split(" ");
                for (int j = 0; j < dimension; j++) {
                    multiplyMatrix[i][j] = Float.valueOf(values[j]);
                }
            }

            for (Map.Entry<String, float[]> sourceWordVector : sourceSpace.getVectorSpace().entrySet()) {
                float[] targetVector = new float[dimension];
                for (int i = 0; i < dimension; i++) {
                    float sum = 0.0f;
                    for (int j = 0; j < dimension; j++) {
                        sum += sourceWordVector.getValue()[j] * multiplyMatrix[i][j];
                    }
                    targetVector[i] = sum;
                }

                targetSpace.addWord(sourceWordVector.getKey(), targetVector);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetSpace;
    }
}
