package semantic_similarity.techniques.monolingual_mapping;

import semantic_similarity.io_utils.FastTextUtil;
import semantic_similarity.io_utils.IEmbeddingUtil;
import semantic_similarity.io_utils.IOUtils;
import semantic_similarity.io_utils.MyEmbeddingUtil;
import semantic_similarity.vocabulary.VocabularyUtils;
import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;
import semantic_similarity.word_embedding.WordVector;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.utils.Settings.EMBEDDING_PATH;
import static semantic_similarity.utils.Settings.TEMP_PATH;

/**
 * @author Josef Stroleny
 */
public class MultilingualCCA {
    /**
     * Postup pro Multilingual CCA
     */
    public static void multilingualCCA() {
        //Redukujeme pocet slov z fasttext na top 200 000
        IOUtils.reduceEmbeddingCount(new FastTextUtil(), "fasttext", new MyEmbeddingUtil(), "fasttext-200k", 200000);

        /*
        //puvodni verze s manualnimi preklady

        //Ulozime do samostatnych souboru slova z embeddingu
        IOUtils.getWordsFromEmbeddings(new MyEmbeddingUtil(), "fasttext-30k");

        //RUCNE - Vytvorime preklady slov do anglictiny (en == cilovy jazyk)
        //manually create translations

        //Ze slov a jejich prekladu vytvorime prekladovy slovnik
        VocabularyUtils.createAllTranslationWordPairs();
        */

        //Za slova ve slovniku dosadime vektory
        VocabularyUtils.createAllTranslationVectorPairs(new MyEmbeddingUtil(), "fasttext-200k");

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
        List<UnifiedVectorSpace> commonSpaces = new ArrayList<>();
        //cs-en
        UnifiedVectorSpace source = util.loadSpace(EMBEDDING_PATH + name + "_cs.txt", ELanguage.CZECH, Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-cs-en.txt"));
        //de-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_de.txt", ELanguage.GERMAN, Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-de-en.txt"));
        //es-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_es.txt", ELanguage.SPANISH, Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-es-en.txt"));
        //zh-en
        source = util.loadSpace(EMBEDDING_PATH + name + "_zh.txt", ELanguage.CHINESE, Integer.MAX_VALUE);
        commonSpaces.add(transformLanguageToCommonSpace(source, TEMP_PATH + "cca-zh-en.txt"));
        //en
        commonSpaces.add(util.loadSpace(EMBEDDING_PATH + name + "_en.txt", ELanguage.ENGLISH, Integer.MAX_VALUE));

        //merge spaces
        UnifiedVectorSpace multilingualEmbeddings = UnifiedVectorSpace.mergeVectorSpaces(commonSpaces);
        util.saveSpace(EMBEDDING_PATH + name + "_multilingual.txt", multilingualEmbeddings);
    }

    /**
     * Transformuje jeden jazyk do spolecneho prostoru tim, ze ho vynasobi s matici z CCA ( lang_vec * [A * inv(B)] )
     * @param sourceSpace Puvodni monolingualni prostor
     * @param multiplyMatrixPath Cesta k transformacni matici (ctvercova matice, dimenze odpovida poctu priznaku v monolingualnim prostoru)
     * @return Puvodni jazyk transformovany do multilingualniho prostoru
     */
    private static UnifiedVectorSpace transformLanguageToCommonSpace(UnifiedVectorSpace sourceSpace, String multiplyMatrixPath) {
        UnifiedVectorSpace targetSpace = new UnifiedVectorSpace(ELanguage.MULTILINGUAL);

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

            for (WordVector sourceWordVector : sourceSpace.getWords().values()) {
                float[] targetVector = new float[dimension];
                for (int i = 0; i < dimension; i++) {
                    float sum = 0.0f;
                    for (int j = 0; j < dimension; j++) {
                        sum += sourceWordVector.getVector()[j] * multiplyMatrix[i][j];
                    }
                    targetVector[i] = sum;
                }

                WordVector targetWordVector = new WordVector(sourceWordVector.getLanguage(), sourceWordVector.getWord(), targetVector);
                targetSpace.addWord(targetWordVector);
            }

        } catch (Exception e) {
            e.printStackTrace();
        }

        return targetSpace;
    }
}
