package semantic_similarity.techniques.word2vec;

import semantic_similarity.VectorSpace;
import semantic_similarity.utils.MyUtils;
import semantic_similarity.utils.document.DocumentHolder;

import java.io.*;
import java.util.*;

import static semantic_similarity.utils.MyUtils.random;
import static semantic_similarity.Settings.*;

/**
 * @author Josef Stroleny
 */
public class Word2vec {
    DocumentHolder documents;

    //vocabulary×dimension
    float[][] inputMatrix;
    //vocabulary×dimension
    float[][] outputMatrix;

    private int[] negativeSamplingTable;

    private int iteration;

    private double error;
    private int count;

    public Word2vec(DocumentHolder documents) {
        this.documents = documents;

        inputMatrix = new float[documents.getVocabularySize()][HIDDEN_LAYER_SIZE];
        for (int i = 0; i < inputMatrix.length; i++) {
            for (int j = 0; j < inputMatrix[i].length; j++) {
                inputMatrix[i][j] = (random.nextFloat() - 0.5f) / HIDDEN_LAYER_SIZE;
            }
        }

        outputMatrix = new float[documents.getVocabularySize()][HIDDEN_LAYER_SIZE];
        for (int i = 0; i < outputMatrix.length; i++) {
            for (int j = 0; j < outputMatrix[i].length; j++) {
                outputMatrix[i][j] = 0;
            }
        }

        this.negativeSamplingTable = new int[100000000]; //1e8
        double sum = 0.0;
        for (String s : documents.getVocabulary().getWordList()) {
            sum += Math.pow(documents.getVocabulary().getWordCount(s), 0.75);
        }

        double probability = 0.0;
        int tableIndex = 0;
        for (String s : documents.getVocabulary().getWordList()) {
            probability += Math.pow(documents.getVocabulary().getWordCount(s), 0.75) / sum;

            while ((tableIndex < negativeSamplingTable.length) && (((double) tableIndex / negativeSamplingTable.length) < probability)) {
                negativeSamplingTable[tableIndex] = documents.getVocabulary().getKey(s);
                tableIndex++;
            }
        }


        this.iteration = 0;
    }

    public Word2vec(DocumentHolder documents, float[][] inputMatrix, float[][] outputMatrix, int iteration) {
        this.documents = documents;
        this.inputMatrix = inputMatrix;
        this.outputMatrix = outputMatrix;
        this.iteration = iteration;
    }

    public void train(int iterations) {
        for (int i = 0; i < iterations; i++) {
            error = 0;
            count = 0;
            iteration++;

            List<Thread> threads = new ArrayList<>();
            for (int thread = 0; thread < CORES; thread++) {
                Thread thr = new Thread(new Word2vecThread(this, ALPHA, USE_CBOW, CORES));
                threads.add(thr);
                thr.start();
            }

            for (Thread t : threads) {
                try {
                    t.join();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            System.out.println("Iteration " + iteration + " done; Cost: " + (error / count));
        }
    }

    protected synchronized void signalError(double error, int count) {
        this.error += error;
        this.count += count;
    }

    public Map<String, Double> getMostSimilarWords(String word, int count) {
        Map<String, Double> map = new HashMap<>();
        //Paralelni pole
        String[] topWords = new String[count];
        double[] topProbabilities = new double[count];

        //Prochazime vsechna slova
        for (int i = 0; i < documents.getVocabularySize(); i++) {
            String testWord = documents.getVocabulary().getWord(i);
            if (testWord.equals(word)) continue;

            double similarity = getSimilarity(word, testWord);
            //Nasli jsme vyssi shodu nez je soucasna nejnizsi?
            if (similarity > topProbabilities[0]) {
                int index = count - 1;
                //Hledame kam zaradit nove slovo
                while(true) {
                    if (similarity > topProbabilities[index]) {
                        //Posun slov o pozici nize
                        for (int j = 1; j <= index; j++) {
                            topProbabilities[j - 1] = topProbabilities[j];
                            topWords[j - 1] = topWords[j];
                        }
                        topProbabilities[index] = similarity;
                        topWords[index] = testWord;
                        break;
                    }
                    index--;
                }
            }
        }

        for (int i = 0; i < count; i++) {
            map.put(topWords[i], topProbabilities[i]);
        }

        return map;
    }

    public double getSimilarity(String word1, String word2) {
        int index1 = documents.getVocabulary().getKey(word1);
        int index2 = documents.getVocabulary().getKey(word2);
        if (index1 == -1 || index2 == -1) return -1;

        return MyUtils.getSimilarity(inputMatrix[index1], inputMatrix[index2]);
    }

    public VectorSpace toVectorSpace() {
        VectorSpace vectorSpace = new VectorSpace();
        for (int i = 0; i < inputMatrix.length; i++) {
            vectorSpace.addWord(documents.getVocabulary().getWord(i), inputMatrix[i]);
        }

        return vectorSpace;
    }

    public void save(String path) {
        try {
            BufferedWriter bw = new BufferedWriter(new FileWriter(new File(path)));

            //save iteration, vocabSize and dimension
            bw.write(String.valueOf(iteration) + " ");
            bw.write(String.valueOf(documents.getVocabularySize()));
            bw.write(String.valueOf(inputMatrix[0].length));
            bw.newLine();
            bw.flush();

            //save matrices
            for (int i = 0; i < inputMatrix.length; i++) {
                for (int j = 0; j < inputMatrix[i].length; j++) {
                    bw.write(String.valueOf(inputMatrix[i][j]) + " ");
                }
                bw.newLine();
            }
            bw.flush();

            for (int i = 0; i < outputMatrix.length; i++) {
                for (int j = 0; j < outputMatrix[i].length; j++) {
                    bw.write(String.valueOf(outputMatrix[i][j]) + " ");
                }
                bw.newLine();
            }

            bw.flush();
            bw.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Word2vec load(DocumentHolder documentHolder, String path) {
        try {
            BufferedReader br = new BufferedReader(new FileReader(new File(path)));

            String[] info = br.readLine().split(" ");
            int iteration = Integer.parseInt(info[0]);
            int vocabularySize = Integer.parseInt(info[1]);
            int dimension = Integer.parseInt(info[2]);

            float[][] inputMatrix = new float[vocabularySize][dimension];
            for (int i = 0; i < inputMatrix.length; i++) {
                String[] values = br.readLine().split(" ");
                for (int j = 0; j < inputMatrix[i].length; j++) {
                    inputMatrix[i][j] = Float.parseFloat(values[j]);
                }
            }

            float[][] outputMatrix = new float[vocabularySize][dimension];
            for (int i = 0; i < outputMatrix.length; i++) {
                String[] values = br.readLine().split(" ");
                for (int j = 0; j < outputMatrix[i].length; j++) {
                    outputMatrix[i][j] = Float.parseFloat(values[j]);
                }
            }
            br.close();

            return new Word2vec(documentHolder, inputMatrix, outputMatrix, iteration);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean skip(int token) {
        double z = documents.getVocabulary().getWordCount(token) / documents.getVocabulary().getTotalCount();
        double p = (Math.sqrt(z / 0.001) + 1) * (0.001 / z);
        return Math.random() >= p;
    }

    public int getNegativeSample() {
        return random.nextInt(documents.getVocabularySize());
    }
}
