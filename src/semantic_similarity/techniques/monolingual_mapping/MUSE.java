package semantic_similarity.techniques.monolingual_mapping;

import semantic_similarity.utils.embedding.FastTextUtil;
import semantic_similarity.utils.embedding.IEmbeddingUtil;
import semantic_similarity.utils.embedding.MyEmbeddingUtil;
import semantic_similarity.VectorSpace;

import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.Settings.EMBEDDING_PATH;

/**
 * @author Josef Stroleny
 */
public class MUSE {
    /**
     * Postup pro MUSE
     */
    public MUSE() {
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
        List<VectorSpace> commonSpaces = new ArrayList<>();
        //cs
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_cs.txt", Integer.MAX_VALUE));
        //de
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_de.txt", Integer.MAX_VALUE));
        //en
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_en.txt", Integer.MAX_VALUE));
        //es
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_es.txt", Integer.MAX_VALUE));
        //zh
        commonSpaces.add(source_util.loadSpace(EMBEDDING_PATH + name + "_zh.txt", Integer.MAX_VALUE));

        //merge spaces
        VectorSpace multilingualEmbeddings = VectorSpace.mergeVectorSpaces(commonSpaces);
        target_util.saveSpace(EMBEDDING_PATH + name + "_multilingual.txt", multilingualEmbeddings);
    }
}
