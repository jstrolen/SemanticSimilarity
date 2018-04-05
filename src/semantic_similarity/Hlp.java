package semantic_similarity;

import java.io.*;

import static semantic_similarity.Settings.CORPUS_PATH;

/**
 * @author Josef Stroleny
 */
public class Hlp {
    private void hlp0() throws IOException {
        File file = new File(CORPUS_PATH + "document-aligned/de");
        for (final File fileEntry : file.listFiles()) {
            if (!fileEntry.isDirectory()) {
                BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileEntry.getAbsolutePath() + "2")));

                String pom = "";
                String line;
                while ((line = br.readLine()) != null) {
                    if (line.length() <= 0) continue;
                    if (line.length() > 4 && line.charAt(0) == '[' && line.charAt(1) == '[') continue;


                    if (Character.isUpperCase(line.charAt(0))) {
                        if (!pom.isEmpty()) {
                            bw.write(pom);
                            bw.newLine();
                        }
                        pom = line;
                    } else {
                        pom = pom + " " + line;
                    }
                }
                bw.flush();

                br.close();
                bw.close();
            }
        }
    }


    private void hlp1() throws IOException {
        File file = new File(CORPUS_PATH + "document-aligned/de");
        for (final File fileEntry : file.listFiles()) {
            if (!fileEntry.isDirectory()) {
                BufferedReader br = new BufferedReader(new FileReader(fileEntry));
                BufferedWriter bw = new BufferedWriter(new FileWriter(new File(fileEntry.getAbsolutePath() + "2")));

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.split(" ").length < 5) continue;
                    bw.write(line);
                    bw.newLine();
                }

                bw.flush();

                br.close();
                bw.close();
            }
        }
    }

    private void hlp2() throws IOException {
        BufferedReader br_1 = new BufferedReader(new FileReader(new File(CORPUS_PATH +
                "sentence-aligned/cs-en/Europarl.cs-en.cs")));
        BufferedReader br_2 = new BufferedReader(new FileReader(new File(CORPUS_PATH +
                "sentence-aligned/cs-en/Europarl.cs-en.en")));

        BufferedWriter bw_1 = new BufferedWriter(new FileWriter(new File(CORPUS_PATH +
                "sentence-aligned/cs-en/Europarl.cs-en_new.cs")));
        BufferedWriter bw_2 = new BufferedWriter(new FileWriter(new File(CORPUS_PATH +
                "sentence-aligned/cs-en/Europarl.cs-en_new.en")));

        String line1;
        String line2;
        while ((line1 = br_1.readLine()) != null) {
            line2 = br_2.readLine();


            if (line1.split(" ").length < 5 || line2.split(" ").length < 5) continue;

            bw_1.write(line1);
            bw_1.newLine();
            bw_2.write(line2);
            bw_2.newLine();
        }
        bw_1.flush();
        bw_2.flush();

        br_1.close();
        br_2.close();
        bw_1.close();
        bw_2.close();
    }
}
