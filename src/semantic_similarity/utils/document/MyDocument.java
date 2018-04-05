package semantic_similarity.utils.document;

import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.parser.IParser;
import semantic_similarity.utils.vocabulary.MyVocabulary;
import semantic_similarity.ELanguage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;

import static semantic_similarity.Settings.MAX_SENTENCE_RATIO;
import static semantic_similarity.Settings.SENTENCE_MAX_LENGTH;
import static semantic_similarity.Settings.SENTENCE_MIN_LENGTH;

/**
 * @author Josef Stroleny
 */
public class MyDocument {
    private int[] tokens;

    private boolean multilingual;

    public MyDocument(ELanguage language, IParser parser, String text, MyVocabulary vocabulary) {
        this.tokens = parser.parse(language, text, vocabulary, null);
        this.multilingual = false;
    }

    public static MyDocument create(ELanguage language1, IParser parser1, String text1,
                      ELanguage language2, IParser parser2, String text2,
                      MyVocabulary vocabulary) {
        int[] tokens1 = parser1.parse(language1, text1, vocabulary, null);
        int[] tokens2 = parser2.parse(language2, text2, vocabulary, null);

        int longer[] = tokens1;
        int shorter[] = tokens2;
        if (tokens2.length > tokens1.length) {
            longer = tokens2;
            shorter = tokens1;
        }

        double ratio = (double) longer.length / shorter.length;
        if (shorter.length < SENTENCE_MIN_LENGTH || ratio > MAX_SENTENCE_RATIO || longer.length > SENTENCE_MAX_LENGTH) {
            for (int i : longer) vocabulary.remove(i);
            for (int i : shorter) vocabulary.remove(i);

            return null;
        }

        //propleteme vety
        int[] mergedArray = new int[longer.length + shorter.length];
        double sum = 0;
        int indexLonger = 0;
        int indexShorter = 0;
        while (indexLonger + indexShorter < longer.length + shorter.length) {
            if ((sum < ratio) && (indexLonger < longer.length)) {
                mergedArray[indexLonger + indexShorter] = longer[indexLonger];
                indexLonger++;
                sum += 1;
            } else {
                mergedArray[indexLonger + indexShorter] = shorter[indexShorter];
                indexShorter++;
                sum -= ratio;
            }
        }

        return new MyDocument(mergedArray, vocabulary);
    }

    public MyDocument(int[] tokens, MyVocabulary vocabulary) {
        this.tokens = tokens;

        int position = 0;
        ELanguage lang = ELanguage.fromString(vocabulary.getWord(tokens[position++]));
        while (lang == null && position < tokens.length) {
            lang = ELanguage.fromString(vocabulary.getWord(tokens[position++]));
        }

        for (int i = position; i < tokens.length; i++) {
            ELanguage newLang = ELanguage.fromString(vocabulary.getWord(tokens[i]));
            if (newLang == null) continue;
            if (!lang.equals(newLang)) {
                this.multilingual = true;
                return;
            }
        }
        this.multilingual = false;
    }

    public void changeVocabulary(MyVocabulary originalVocabulary, MyVocabulary newVocabulary) {
        for (int i = 0; i < tokens.length; i++) {
            String token = originalVocabulary.getWord(tokens[i]);
            int value = newVocabulary.getKey(token);
            tokens[i] = value;
        }
    }

    public void save(BufferedWriter bw) throws IOException {
        bw.write(String.valueOf(getDocumentSize()));
        bw.newLine();

        for (int i = 0; i < tokens.length; i++) {
            bw.write(tokens[i] >> 16);
            bw.write(tokens[i]);
        }

        bw.newLine();
        bw.flush();
    }

    public void savePlainText(BufferedWriter bw, MyVocabulary vocabulary, boolean skipNull) throws IOException {
        boolean first = true;

        for (int i = 0; i < tokens.length; i++) {
            String word = vocabulary.getWord(tokens[i]);
            if (skipNull && word == null) continue;

            if (first) {
                bw.write(word);
                first = false;
            }
             else {
                bw.write(" " + word);
            }
        }

        bw.newLine();
        bw.flush();
    }

    public static MyDocument load(BufferedReader br, MyVocabulary hlpVocabulary) throws IOException {
        int size = Integer.parseInt(br.readLine());

        int[] hlp = new int[size];
        for (int i = 0; i < size; i++) {
            int a = br.read();
            int b = br.read();
            hlp[i] = (a << 16) | (b);
        }
        br.readLine();

        return new MyDocument(hlp, hlpVocabulary);
    }

    public int[] getTokens() {
        return tokens;
    }

    public int getTokenAt(int position) {
        return tokens[position];
    }

    public int getDocumentSize() {
        return tokens.length;
    }

    public boolean isMultilingual() {
        return multilingual;
    }
}
