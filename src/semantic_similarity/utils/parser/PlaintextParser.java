package semantic_similarity.utils.parser;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.vocabulary.MyVocabulary;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;

/**
 * @author Josef Stroleny
 */
public class PlaintextParser implements IParser {
    @Override
    public int[] parse(ELanguage language, String text, MyVocabulary vocabulary, int[] tokens) {
        List<Integer> hlp = new ArrayList<>();

        String[] words = text.split(" ");
        for (String word : words) {
            if (ELanguage.fromString(word) == null) continue;

            int wordKey = vocabulary.add(word);
            hlp.add(wordKey);
        }

        int originalLength = 0;
        if (tokens != null) originalLength = tokens.length;

        int[] result = new int[originalLength + hlp.size()];
        if (originalLength > 0) System.arraycopy(tokens, 0, result, 0, originalLength);
        for (int i = 0; i < hlp.size(); i++) {
            result[i + originalLength] = hlp.get(i);
        }

        return result;
    }
}
