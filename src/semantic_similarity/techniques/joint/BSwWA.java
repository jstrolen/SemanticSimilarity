package semantic_similarity.techniques.joint;

import semantic_similarity.utils.document.DocumentHolder;

import java.util.Collections;

import static semantic_similarity.Settings.CORPUS_PATH;
import static semantic_similarity.Settings.M;
import static semantic_similarity.utils.document.DocumentUtils.loadSentenceAlignedCorpus;

/**
 * @author Josef Stroleny
 */
public class BSwWA {
    public BSwWA() {
        //Vytvoreni korpusu
        DocumentHolder documents = new DocumentHolder();
        loadSentenceAlignedCorpus(documents, 50*M);
        Collections.shuffle(documents.getDocuments());
        documents.savePlainText(CORPUS_PATH + "multilingual_sentence_50M_plaintext.txt", true);

        //trenovani
        //fasttext.exe skipgram -input multilingual_sentence_50M_plaintext.txt -output test -minn 6 -maxn 9 -dim 300 -thread 4 -epoch 5
    }
}
