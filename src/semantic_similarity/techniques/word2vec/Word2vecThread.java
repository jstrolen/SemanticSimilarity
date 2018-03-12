package semantic_similarity.techniques.word2vec;

import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.document.MyDocument;

import java.util.HashSet;
import java.util.Set;

import static semantic_similarity.Settings.*;

/**
 * @author Josef Stroleny
 */
public class Word2vecThread implements Runnable {
    private Word2vec word2vec;
    private Set<Integer> skipSet;

    private double defaultAlpha;
    private boolean useCbow;
    private int cores;

    private double error;
    private int count;

    public Word2vecThread(Word2vec word2vec, double defaultAlpha, boolean useCbow, int cores) {
        this.word2vec = word2vec;

        this.defaultAlpha = defaultAlpha;
        this.useCbow = useCbow;
        this.cores = cores;

        this.error = 0;
        this.count = 0;
    }

    @Override
    public void run() {
        int totalDocuments = word2vec.documents.getDocumentCount() / cores;
        for (int doc = 0; doc < totalDocuments; doc++) {
            int randomDocumentIndex = MyUtils.random.nextInt(word2vec.documents.getDocumentCount());

            MyDocument document = word2vec.documents.getDocuments().get(randomDocumentIndex);
            skipSet = new HashSet<>();
            for (int i = 0; i < document.getDocumentSize(); i++) {
                if (word2vec.skip(document.getTokenAt(i))) skipSet.add(i);
            }

            double alpha = defaultAlpha;//Math.max(defaultAlpha * 0.0001,
                    //defaultAlpha * (1.0 - ((double) doc / totalDocuments)));
            for (int wordIndex = 0; wordIndex < document.getDocumentSize(); wordIndex++) {
                if (useCbow) processWordCBOW(document, wordIndex, alpha);
                else processWordSkipGram(document, wordIndex, alpha);
            }
        }

        word2vec.signalError(error, count);
    }

    private void processWordSkipGram(MyDocument document, int wordIndex, double alpha) {
        if (skipSet.contains(wordIndex)) return;

        int target = document.getTokenAt(wordIndex);
        if (target == -1) return;

        int startIndex = 0;
        int endIndex = document.getDocumentSize() - 1;
        if (!document.isMultilingual()) {
            startIndex = Math.max(wordIndex - WINDOW_SIZE, 0);
            endIndex = Math.min(wordIndex + WINDOW_SIZE, document.getDocumentSize() - 1);
        }

        for (int contextIndex = startIndex; contextIndex <= endIndex; contextIndex++) {
            if (contextIndex == 0 || skipSet.contains(contextIndex)) continue;
            int context = document.getTokenAt(contextIndex);
            if (context == -1) continue;

            float[] errorSum = new float[word2vec.inputMatrix[0].length];

            /* Pozitivni sample */
            double sigma = fastSigmoid(multiply(word2vec.inputMatrix, target, word2vec.outputMatrix, context));
            error += -Math.log(Math.max(sigma, EPSILON));

            double delta = alpha * (sigma - 1.0);    //double delta = defaultAlpha * (sigma - (1.0 / (Math.abs(wordIndex - relativeIndex) + 1)));
            for (int i = 0; i < errorSum.length; i++) {
                errorSum[i] += delta * word2vec.outputMatrix[context][i];
                word2vec.outputMatrix[context][i] -= delta * word2vec.inputMatrix[target][i];
            }

            /* Negativni samply */
            for (int i = 0; i < K; i++) {
                //nahodne slovo
                int negative = word2vec.getNegativeSample();

                sigma = fastSigmoid(- multiply(word2vec.inputMatrix, target, word2vec.outputMatrix, negative));
                error += -Math.log(Math.max(sigma, EPSILON));

                delta = alpha * (1.0 - sigma);
                for (int j = 0; j < errorSum.length; j++) {
                    errorSum[j] += delta * word2vec.outputMatrix[negative][j];
                    word2vec.outputMatrix[negative][j] -= delta * word2vec.inputMatrix[target][j];
                }
            }

            //vstupni vrstva
            for (int i = 0; i < errorSum.length; i++) {
                word2vec.inputMatrix[target][i] -= errorSum[i];
            }

            count++;
        }
    }

    private void processWordCBOW(MyDocument document, int wordIndex, double alpha) {
        if (skipSet.contains(wordIndex)) return;

        int target = document.getTokenAt(wordIndex);
        if (target == -1) return;

        int startIndex = 0;
        int endIndex = document.getDocumentSize() - 1;
        if (!document.isMultilingual()) {
            startIndex = Math.max(wordIndex - WINDOW_SIZE, 0);
            endIndex = Math.min(wordIndex + WINDOW_SIZE, document.getDocumentSize() - 1);
        }

        //prumer z kontextu
        float[] contextMean = new float[word2vec.inputMatrix[0].length];
        int contextSize = 0;
        for (int contextIndex = startIndex; contextIndex <= endIndex; contextIndex++) {
            if (contextIndex == 0 || skipSet.contains(contextIndex)) continue;
            int context = document.getTokenAt(contextIndex);
            if (context == -1) continue;

            for (int i = 0; i < contextMean.length; i++) {
                contextMean[i] += word2vec.inputMatrix[context][i];
            }
            contextSize++;
        }
        if (contextSize <= 0) return;
        for (int i = 0; i < contextMean.length; i++) {
            contextMean[i] /= contextSize;
        }

        float[] errorSum = new float[word2vec.outputMatrix[0].length];

        /* Pozitivni sample */
        double sigma = sigmoid(multiply(word2vec.outputMatrix, target, contextMean));
        error += -Math.log(Math.max(sigma, EPSILON));

        double delta = alpha * (sigma - 1.0);
        for (int i = 0; i < errorSum.length; i++) {
            errorSum[i] += delta * word2vec.outputMatrix[target][i];
            word2vec.outputMatrix[target][i] -= delta * contextMean[i];
        }

        /* Negativni samply */
        for (int i = 0; i < K; i++) {
            //nahodne slovo
            int negative = word2vec.getNegativeSample();

            sigma = sigmoid(- multiply(word2vec.outputMatrix, negative, contextMean));
            error += -Math.log(Math.max(sigma, EPSILON));

            delta = alpha * (1.0 - sigma);
            for (int j = 0; j < errorSum.length; j++) {
                errorSum[j] += delta * word2vec.outputMatrix[negative][j];
                word2vec.outputMatrix[negative][j] -= delta * contextMean[i];
            }
        }

        //vstupni vrstva
        for (int contextIndex = startIndex; contextIndex <= endIndex; contextIndex++) {
            if (contextIndex == 0 || skipSet.contains(contextIndex)) continue;
            int context = document.getTokenAt(contextIndex);
            if (context == -1) continue;

            for (int i = 0; i < errorSum.length; i++) {
                word2vec.inputMatrix[context][i] -= errorSum[i];
            }
        }

        count++;
    }

    private static double multiply(float[][] matrix1, int row1, float[][] matrix2, int row2) {
        double sum = 0;
        for (int i = 0; i < matrix1[row1].length; i++) {
            sum += matrix1[row1][i] * matrix2[row2][i];
        }
        return sum;
    }

    private static double multiply(float[][] matrix, int row, float[] vector) {
        double sum = 0;
        for (int i = 0; i < matrix[row].length; i++) {
            sum += matrix[row][i] * vector[i];
        }
        return sum;
    }

    private static double sigmoid(double x) {
        return 1.0 / (1.0 + Math.exp(-x));
    }

    private static double fastSigmoid(double x) {
        return x / (1.0 + Math.abs(x));
    }
}
