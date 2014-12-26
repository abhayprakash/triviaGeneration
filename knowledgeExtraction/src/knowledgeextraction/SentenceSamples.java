/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knowledgeextraction;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import opennlp.tools.sentdetect.SentenceDetectorME;
import opennlp.tools.sentdetect.SentenceModel;
import static test_SENTENCE_DETECT.Main.sentence_detect_model;

/**
 *
 * @author Abhay Prakash
 */
public class SentenceSamples {
    public static String sentence_detect_model="C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Code\\knowledgeExtraction\\src\\models\\en-sent.zip";
    public static void main(String[] args) throws FileNotFoundException, IOException {
        InputStream is = new FileInputStream(sentence_detect_model);
        SentenceModel model = new SentenceModel(is);
        SentenceDetectorME sdetector = new SentenceDetectorME(model);
        
        File writeFile = new File("C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\dataForCF.txt");
        writeFile.createNewFile();
        FileWriter writer = new FileWriter(writeFile); 

        String folderPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\top1000Movies\\input\\";
        File[] files = new File(folderPath).listFiles();
        for (File file : files) {
            if(file.isFile()){
                System.out.println("File: " + file.getName());
                
                FileReader inputFile = new FileReader(folderPath + file.getName());
                BufferedReader bufferReader = new BufferedReader(inputFile);
                String input = bufferReader.readLine();
                System.out.println("Here: " + input);
                bufferReader.close();
                String sentences[] = sdetector.sentDetect(input);
                for(int i=0;i<sentences.length;i++){
                    String name = file.getName().replace("_", " ");
                    //writer.write(name.replace(".txt", ": ") + sentences[i] + "\n");
                    writer.write(sentences[i] + "\n");
                }
            }
            writer.flush();
        }
        writer.close();
    }
}
