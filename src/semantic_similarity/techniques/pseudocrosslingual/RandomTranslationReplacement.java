package semantic_similarity.techniques.pseudocrosslingual;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.document.DocumentHolder;

import java.util.ArrayList;
import java.util.List;

import static semantic_similarity.Settings.CORPUS_PATH;
import static semantic_similarity.Settings.VOCABULARY_PATH;

/**
 * @author Josef Stroleny
 */
public class RandomTranslationReplacement {
    public RandomTranslationReplacement() {
        //nacteni slovniku
        List<ELanguage> excludedLanguages = new ArrayList<>();
        excludedLanguages.add(ELanguage.CZECH); excludedLanguages.add(ELanguage.CHINESE);
        MyDictionary dictionary = new MyDictionary();
        dictionary.load(VOCABULARY_PATH, excludedLanguages);

        //Zamichani korpusu
        DocumentHolder documents = DocumentHolder.load(CORPUS_PATH + "multilingual_unreduced_50M.txt");
        documents.mixDocuments(dictionary, 0.5);
        documents.savePlainText(CORPUS_PATH + "multilingual_unreduced_50M_plaintext_mixed0.5.txt", true);

        //trenovani fasttext
        //fasttext.exe skipgram -input test.txt -output test -minn 6 -maxn 9 -dim 300 -thread 4 -epoch 5
    }
}
