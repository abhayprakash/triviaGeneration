/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.EntityLinker.STOPWORDS;
import static featureExtractor.comparativePOS.bw;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

/**
 *
 * @author Abhay Prakash
 */
public class contradictory_words {
    public static void main(String[] args) throws IOException
    {
        String list = "but,rather,contrast,although,still,instead,unlike,whereas,yet,conversel,despite,otherwise,contrary,however,spite,besides,even,regardless,nonetheless";
        List<String> CONTRA_WORDS = Arrays.asList(list.split(","));
        
        String resultComp = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\RANK\\judgedWiki\\res_contra.txt";
        FileWriter fw = new FileWriter(resultComp);
        BufferedWriter bw = new BufferedWriter(fw);
        
        String inputFilePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\RANK\\judgedWiki\\trivia.txt";
        FileReader inputFile = new FileReader(inputFilePath);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String input;
        int lineNum = 0;
        while((input = bufferReader.readLine()) != null)
        {
            lineNum++;
            String[] words = input.split(" ");
            int count = 0;
            for(String w: words)
            {
                if(CONTRA_WORDS.contains(w))
                    count++;
            }
            bw.write(count + "\n");
            System.out.println(count);
        }
        bw.close();
    }
}
