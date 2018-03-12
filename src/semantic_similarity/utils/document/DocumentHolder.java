package semantic_similarity.utils.document;

import semantic_similarity.Settings;
import semantic_similarity.utils.dictionary.MyDictionary;
import semantic_similarity.utils.parser.IParser;
import semantic_similarity.utils.vocabulary.MyVocabulary;
import semantic_similarity.ELanguage;

import java.io.*;
import java.util.*;

import static semantic_similarity.Settings.MIN_OCCURRENCE;

/**
 * @author Josef Stroleny
 */
public class DocumentHolder {
    private List<MyDocument> documents;
    private MyVocabulary vocabulary;

    public DocumentHolder() {
        this.documents = new ArrayList<>();
        this.vocabulary = new MyVocabulary();
    }

    public DocumentHolder(MyVocabulary vocabulary, List<MyDocument> documents) {
        this.documents = documents;
        this.vocabulary = vocabulary;
    }

    public void addDocument(MyDocument document) {
        this.documents.add(document);
    }

    public void addDocument(ELanguage language, IParser parser, String text) {
        this.documents.add(new MyDocument(language, parser, text, vocabulary));
    }

    public void addDocument(ELanguage language1, IParser parser1, String text1,
                            ELanguage language2, IParser parser2, String text2) {
        this.documents.add(new MyDocument(language1, parser1, text1, language2, parser2, text2, vocabulary));
    }

    public void reduceVocabulary() {
        Map<ELanguage, MyVocabulary> vocabularyMap = new HashMap<>();

        for (int j = 0; j < vocabulary.size(); j++) {
            String word = vocabulary.getWordList().get(j);
            int occurrence = vocabulary.getWordCount(word);

            ELanguage wordLanguage = ELanguage.fromString(word);
            if (wordLanguage == null) continue;

            if (!vocabularyMap.containsKey(wordLanguage)) vocabularyMap.put(wordLanguage, new MyVocabulary());
            MyVocabulary languageVocabulary = vocabularyMap.get(wordLanguage);

            languageVocabulary.add(word, occurrence);
        }

        for (MyVocabulary vocabulary : vocabularyMap.values()) {
            vocabulary.sortWordsByCount();
        }

        MyVocabulary newVocabulary = new MyVocabulary();
        for (MyVocabulary vocabulary : vocabularyMap.values()) {
            int startIndex = Settings.TOP_WORDS_CUT;
            int usedWords = 0;
            for (int i = startIndex; i < vocabulary.size(); i++) {
                if (usedWords >= Settings.MAX_LANGUAGE_VOCABULARY) break;

                String token = vocabulary.getWordList().get(i);
                int occurrence = vocabulary.getWordCount(token);
                if (occurrence < MIN_OCCURRENCE) break;

                newVocabulary.add(token, occurrence);
                usedWords++;
            }
        }

        for (int i = 0; i < getDocumentCount(); i++) {
            documents.get(i).changeVocabulary(vocabulary, newVocabulary);
        }

        this.vocabulary = newVocabulary;
        System.gc();
    }

    public void mixDocuments(MyDictionary dictionary, double probability) {
        Collections.shuffle(this.documents);
        for (MyDocument doc : documents) {
            for (int i = 0; i <  doc.getDocumentSize(); i++) {
                String word = vocabulary.getWord(doc.getTokenAt(i));
                if (dictionary.contains(word) && Math.random() < probability) {
                    int wordID = vocabulary.add(dictionary.getRandom(word));
                    doc.getTokens()[i] = wordID;
                    vocabulary.remove(word);
                }
            }
        }

        //should remove 0 occurrences form vocabulary + sort vocabulary and update document wordIDs
    }

    public void save(String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

            //save vocabulary
            vocabulary.save(bw);

            //save docs
            bw.write(String.valueOf(documents.size()));
            bw.newLine();
            for (MyDocument doc : documents) {
                doc.save(bw);
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void savePlainText(String path, boolean skipNull) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

            for (MyDocument doc : documents) {
                doc.savePlainText(bw, vocabulary, skipNull);
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static DocumentHolder load(String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));

            MyVocabulary vocab = MyVocabulary.load(br);

            int documentCount = Integer.parseInt(br.readLine());
            List<MyDocument> documents = new ArrayList<>();
            for (int i = 0; i < documentCount; i++) {
                documents.add(MyDocument.load(br, vocab));
            }

            return new DocumentHolder(vocab, documents);

        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public int getDocumentCount() {
        return documents.size();
    }

    public int getVocabularySize() {
        return vocabulary.size();
    }

    public List<MyDocument> getDocuments() {
        return documents;
    }

    public MyVocabulary getVocabulary() {
        return vocabulary;
    }
}
