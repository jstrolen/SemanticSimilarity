package semantic_similarity.utils.parser;

import semantic_similarity.Settings;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.vocabulary.MyVocabulary;
import semantic_similarity.ELanguage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Josef Stroleny
 */
public class DefaultParser implements IParser {
    private static Pattern pattern = Pattern.compile(Settings.DEFAULT_REGEX);

    @Override
    public int[] parse(ELanguage language, String text, MyVocabulary vocabulary, int[] tokens) {
        List<Integer> hlp = new ArrayList<>();

        text = normalize(text);
        Matcher matcher = pattern.matcher(text);
        while (matcher.find()) {
            String preToken = matcher.group();
            String token = processToken(preToken);
            if (token == null) hlp.add(-1);
            else {
                int wordKey = vocabulary.add(language.toString() + ":" + token);
                hlp.add(wordKey);
            }
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

    private String normalize(String text) {
        text = text.toLowerCase();
        text = text.replaceAll("[\\t\\n\\r]+"," ");

        return text;
    }

    private String processToken(String preToken) {
        if (preToken.length() < Settings.MIN_TOKEN_LENGTH) return null;
        if (Settings.SKIP_DIGITS && containsDigit(preToken)) return null;

        //stemmer?

        return preToken;
    }

    private boolean containsDigit(String s) {
        for (int i = 0; i < s.length(); i++) {
            if (Character.isDigit(s.charAt(i))) return true;
        }
        return false;
    }
}
