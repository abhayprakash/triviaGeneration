/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

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
public class uniqueness_words {
    static String folderPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\MORE_DATA\\test_";
    static String resultComp = folderPath + "INT_test_uniqueness.txt";
    static String inputFilePath = folderPath + "trivia.txt";
    
    public static void main(String[] args) throws IOException
    {
        String list = "only,alone,single,one,unique,solo,solitary,individual,matchless,peerless,unparalleled,exclusive";
        List<String> CONTRA_WORDS = Arrays.asList(list.split(","));
        
        FileWriter fw = new FileWriter(resultComp);
        BufferedWriter bw = new BufferedWriter(fw);
        
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
