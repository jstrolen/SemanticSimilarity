package semantic_similarity.techniques.monolingual_mapping;

import semantic_similarity.utils.embedding.EmbeddingUtils;
import semantic_similarity.utils.embedding.FastTextUtil;
import semantic_similarity.utils.embedding.IEmbeddingUtil;
import semantic_similarity.utils.embedding.MyEmbeddingUtil;
import semantic_similarity.utils.dictionary.DictionaryUtils;
import semantic_similarity.VectorSpace;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static semantic_similarity.Settings.EMBEDDING_PATH;
import static semantic_similarity.Settings.TEMP_PATH;

/**
 * @author Josef Stroleny
 */
public class MultilingualCCA {
    /**
     * Postup pro Multilingual CCA
     */
    public static void multilingualCCA() {
        //Redukujeme pocet slov z fasttext na top 200 000
        EmbeddingUtils.reduceEmbeddingCount(new FastTextUtil(), "fasttext", new MyEmbeddingUtil(), "fasttext-200k", 200000);

        /*
        //puvodni verze s manualnimi preklady

        //Ulozime do samostatnych souboru slova z embeddingu
        EmbeddingUtils.printWordsFromEmbeddings(new MyEmbeddingUtil(), "fasttext-30k");

        //RUCNE - Vytvorime preklady slov do anglictiny (en == cilovy jazyk)
        //manually create translations

        //Ze slov a jejich prekladu vytvorime prekladovy slovnik
        DictionaryUtils.createAllTranslationWordPairs();
        */

        //Za slova ve slovniku dosadime vektory
        DictionaryUtils.createAllTranslationVectorPairs(new MyEmbeddingUtil(), "fasttext-200k");

        //Najdeme transformacni matici CCA
        //get CCA vectors - matlab script

        //Transformuje jazyky do spolecneho prostoru
        MultilingualCCA.transformToCommonSpace("fasttext-200k");
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
