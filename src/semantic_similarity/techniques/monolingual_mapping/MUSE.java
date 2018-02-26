package semantic_similarity.techniques.monolingual_mapping;

import semantic_similarity.io_utils.FastTextUtil;
import semantic_similarity.io_utils.IEmbeddingUtil;
import semantic_similarity.io_utils.IOUtils;
import semantic_similarity.io_utils.MyEmbeddingUtil;
import semantic_similarity.word_embedding.ELanguage;
import semantic_similarity.word_embedding.UnifiedVectorSpace;

import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.utils.Settings.EMBEDDING_PATH;

/**
 * @author Josef Stroleny
 */
public class MUSE {
    /**
     * Postup pro MUSE
     */
    public static void MUSE() {
        //
        //rucne domapovat cinstinu do eng prostoru

        //Ulozi jazyky do jednoho souboru
        saveToSingleFile("muse");
    }

    /**
     * Ulozi fasttext embeddingy z vice souboru do jednoho jako multilingual
     * @param name Nazev embeddingu
     */
    private static void saveToSingleFile(String name) {
        IEmbeddingUtil source_util = new FastTextUtil();
        IEmbeddingUtil target_util = new MyEmbeddingUtil();
        List<UnifiedVectorSpace> commonSpaces = new ArrayList<>();
        //cs
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_cs.txt", ELanguage.CZECH, Integer.MAX_VALUE));
        //de
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_de.txt", ELanguage.GERMAN, Integer.MAX_VALUE));
        //en
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_en.txt", ELanguage.ENGLISH, Integer.MAX_VALUE));
        //es
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_es.txt", ELanguage.SPANISH, Integer.MAX_VALUE));
        //zh
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_zh.txt", ELanguage.CHINESE, Integer.MAX_VALUE));

        //merge spaces
        UnifiedVectorSpace multilingualEmbeddings = UnifiedVectorSpace.mergeVectorSpaces(commonSpaces);
        target_util.saveSpace(EMBEDDING_PATH + name + "_multilingual.txt", multilingualEmbeddings);
    }
}
