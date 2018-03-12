package semantic_similarity.utils.parser;

import semantic_similarity.utils.vocabulary.MyVocabulary;
import semantic_similarity.ELanguage;

/**
 * @author Josef Stroleny
 */
public interface IParser {
    int[] parse(ELanguage language, String text, MyVocabulary vocabulary, int[] tokens);
}
