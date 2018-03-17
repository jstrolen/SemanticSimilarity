package semantic_similarity.techniques.joint;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.document.DocumentHolder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static semantic_similarity.Settings.CORPUS_PATH;
import static semantic_similarity.Settings.M;
import static semantic_similarity.Settings.VOCABULARY_PATH;
import static semantic_similarity.utils.document.DocumentUtils.loadDocuments;
import static semantic_similarity.utils.document.DocumentUtils.loadSentenceAlignedCorpus;

/**
 * @author Josef Stroleny
 */
public class Combination {
    public Combination() {
        //Pouzite jazyky
        List<ELanguage> includedLanguages = new ArrayList<>();
        includedLanguages.add(ELanguage.ENGLISH); includedLanguages.add(ELanguage.GERMAN); includedLanguages.add(ELanguage.SPANISH);

        //Nacteni slovniku
        MyDictionary dictionary = new MyDictionary();
        dictionary.load(VOCABULARY_PATH, includedLanguages);

        //Vytvoreni korpusu
        DocumentHolder documents = new DocumentHolder();
        loadDocuments(documents, includedLanguages, 30*M);
        documents.mixDocuments(dictionary, 1.0);
        loadSentenceAlignedCorpus(documents, 20*M);
        Collections.shuffle(documents.getDocuments());
        documents.savePlainText(CORPUS_PATH + "multilingual_combined_50M_plaintext.txt", true);

        //trenovani fasttext
        //fasttext.exe skipgram -input multilingual_combined_50M_plaintext.txt -output test -minn 6 -maxn 9 -dim 300 -thread 4 -epoch 5
    }
}
