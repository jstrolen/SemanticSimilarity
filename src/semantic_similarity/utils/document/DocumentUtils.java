package semantic_similarity.utils.document;

import semantic_similarity.ELanguage;
import semantic_similarity.utils.parser.DefaultParser;
import semantic_similarity.utils.parser.IParser;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.List;

import static semantic_similarity.Settings.CORPUS_PATH;

/**
 * @author Josef Stroleny
 */
public class DocumentUtils {
    public static void loadDocuments(DocumentHolder documents, List<ELanguage> languages, int loadWords) {
        try {
            IParser parser = new DefaultParser();

            for (ELanguage language : languages) {
                int originalWords = documents.getVocabulary().getTotalCount();

                BufferedReader br = new BufferedReader(new FileReader(new File(CORPUS_PATH + "document-aligned/" + language.toString() + "_full.txt")));
                StringBuilder sb = new StringBuilder();
                String line;
                while (((line = br.readLine()) != null) && (documents.getVocabulary().getTotalCount() - originalWords < loadWords)) {
                    if (line.length() > 0) {
                        sb.append(" " + line);
                    } else if (line.length() <= 0 && sb.length() > 0) {
                        documents.addDocument(language, parser, sb.toString());
                        sb = new StringBuilder();
                    }
                }
                br.close();
            }

            documents.savePlainText(CORPUS_PATH + "test.txt", true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void loadSentenceAlignedCorpus(DocumentHolder documents, int totalWordCount) {
        try {
            loadSentenceAlignedData(documents, CORPUS_PATH + "sentence-aligned/OpenSubtitles2018.de-en",
                    ELanguage.GERMAN, ELanguage.ENGLISH, totalWordCount);
            loadSentenceAlignedData(documents, CORPUS_PATH + "sentence-aligned/OpenSubtitles2018.de-es",
                    ELanguage.GERMAN, ELanguage.SPANISH, totalWordCount);
            loadSentenceAlignedData(documents, CORPUS_PATH + "sentence-aligned/OpenSubtitles2018.en-es",
                    ELanguage.ENGLISH, ELanguage.SPANISH, totalWordCount);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static void loadSentenceAlignedData(DocumentHolder documents, String basePath,
                                                ELanguage language1, ELanguage language2, int loadWords) throws IOException {
        IParser parser = new DefaultParser();
        String path1 = basePath + "." + language1.toString();
        String path2 = basePath + "." + language2.toString();

        int originalWords = documents.getVocabulary().getTotalCount();

        BufferedReader br1 = new BufferedReader(new FileReader(new File(path1)));
        BufferedReader br2 = new BufferedReader(new FileReader(new File(path2)));

        String line1;
        while ((line1 = br1.readLine()) != null && (documents.getVocabulary().getTotalCount() - originalWords < loadWords)) {
            String line2 = br2.readLine();
            documents.addDocument(language1, parser, line1, language2, parser, line2);
        }
        br1.close();
        br2.close();
    }
}
