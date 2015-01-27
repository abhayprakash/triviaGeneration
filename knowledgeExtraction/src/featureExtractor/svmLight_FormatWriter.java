/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.EntityLinker.Out_subjWords;
import static featureExtractor.NLPFeatures.inputFilePath;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

/**
 *
 * @author Abhay Prakash
 */
public class svmLight_FormatWriter {
    static String folderPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\RANK\\";
    static String infilePath = folderPath + "train_Features.txt";
    static String outfilePath = folderPath + "train_Features_svmLight.txt";
    
    public static void main(String[] args) throws FileNotFoundException, IOException
    {
        FileReader inputFile = new FileReader(infilePath);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        FileWriter fw = new FileWriter(outfilePath);
        BufferedWriter bw = new BufferedWriter(fw);
        
        String input;
        int lineNum = 0;
        input = bufferReader.readLine();
        String[] featureNames = input.split("\t");
        int id = 0, movie_position = 0, rank_position = 0;
        
        for(String f: featureNames)
        {
            id++;
            if(f.equals("MOVIE"))
            {
                movie_position = id;
            }
            else if(f.equals("train_validate_codes"))
            {
                rank_position = id;
            }
            
            if(movie_position != 0 && rank_position != 0)
                break;
        }
        
        HashMap<String, Integer> movie_to_id = new HashMap<>();
        int next_Movie = 1;
        while((input = bufferReader.readLine()) != null)
        {
            String values[] = input.split("\t");
            int MOVIE = 1, RANK = 1;
            
            HashMap<Integer, Double> storedValue = new HashMap<>();
            
            int curr_id = 0, feature_id = 0;
            for(String v: values)
            {
                curr_id++;
                if(curr_id == movie_position)
                {
                    if(movie_to_id.containsKey(v))
                        MOVIE = movie_to_id.get(v);
                    else
                    {
                        MOVIE = next_Movie;
                        movie_to_id.put(v, next_Movie);
                        next_Movie++;
                    }
                }
                else if(curr_id == rank_position)
                {
                    RANK = Integer.parseInt(v);
                }
                else
                {
                    feature_id++;
                    storedValue.put(feature_id, Double.parseDouble(v));
                }
            }
            
            bw.write(RANK + " qid:" + MOVIE);
            
            SortedSet<Integer> keys = new TreeSet<Integer>(storedValue.keySet());
            for (Integer key : keys) { 
               Double value = storedValue.get(key);
               if(value != 0)
                  bw.write(" " + key + ":" + value);
            }
            bw.write("\n");
            System.out.println("Done: " + lineNum);
            lineNum++;
        }
        bw.close();
        main22();
    }
    
    public static void main22() throws FileNotFoundException, IOException
    {
        infilePath = folderPath + "test_Features.txt";
        FileReader inputFile = new FileReader(infilePath);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        outfilePath = folderPath + "test_Features_svmLight.txt";
        FileWriter fw = new FileWriter(outfilePath);
        BufferedWriter bw = new BufferedWriter(fw);
        
        String input;
        int lineNum = 0;
        input = bufferReader.readLine();
        String[] featureNames = input.split("\t");
        int id = 0, movie_position = 0, rank_position = 0;
        
        for(String f: featureNames)
        {
            id++;
            if(f.equals("MOVIE"))
            {
                movie_position = id;
            }
            else if(f.equals("train_validate_codes"))
            {
                rank_position = id;
            }
            
            if(movie_position != 0 && rank_position != 0)
                break;
        }
        
        HashMap<String, Integer> movie_to_id = new HashMap<>();
        int next_Movie = 1;
        while((input = bufferReader.readLine()) != null)
        {
            String values[] = input.split("\t");
            int MOVIE = 1, RANK = 1;
            
            HashMap<Integer, Double> storedValue = new HashMap<>();
            
            int curr_id = 0, feature_id = 0;
            for(String v: values)
            {
                curr_id++;
                if(curr_id == movie_position)
                {
                    if(movie_to_id.containsKey(v))
                        MOVIE = movie_to_id.get(v);
                    else
                    {
                        MOVIE = next_Movie;
                        movie_to_id.put(v, next_Movie);
                        next_Movie++;
                    }
                }
                else if(curr_id == rank_position)
                {
                    RANK = Integer.parseInt(v);
                }
                else
                {
                    feature_id++;
                    storedValue.put(feature_id, Double.parseDouble(v));
                }
            }
            
            bw.write(RANK + " qid:" + MOVIE);
            
            SortedSet<Integer> keys = new TreeSet<Integer>(storedValue.keySet());
            for (Integer key : keys) { 
               Double value = storedValue.get(key);
               if(value != 0)
                  bw.write(" " + key + ":" + value);
            }
            bw.write("\n");
            System.out.println("Done: " + lineNum);
            lineNum++;
        }
        bw.close();
    }
}
