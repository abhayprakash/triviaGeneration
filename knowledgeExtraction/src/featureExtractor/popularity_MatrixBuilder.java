/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.popularityMeasure.In_entities;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;

/**
 *
 * @author Abhay Prakash
 */
public class popularity_MatrixBuilder {
    public static void main(String[] args) throws FileNotFoundException, IOException{
        String popularityFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\Expanded_popularity.txt";
        FileReader inputFile = new FileReader(popularityFile);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String line;
        line = bufferReader.readLine();
        HashMap<Integer, HashMap<String, Integer>> countPop = new HashMap<>();
        String[] colnames = {"low_pop", "mid_pop", "high_pop", "very_high_pop"};
        int maxId = -1;
        while((line = bufferReader.readLine()) != null)
        {
            String[] row = line.split("\t");
            System.out.println(row[0]);
            int id = Integer.parseInt(row[0]);
            if(!countPop.containsKey(id))
                countPop.put(id, new HashMap<>());
            if(!countPop.get(id).containsKey(row[1]))
                countPop.get(id).put(row[1], 0);
            countPop.get(id).put(row[1], countPop.get(id).get(row[1]) + 1 );
            maxId = Math.max(maxId, id);
        }
        
        String popMatrix = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\Expanded_popularity_matrix.txt";
        FileWriter fw = new FileWriter(popMatrix);
        BufferedWriter bw = new BufferedWriter(fw);
        
        for(int i = 0; i <= maxId; i++)
        {
            for(String popId : colnames)
            {
                try{
                    bw.write(countPop.get(i).get(popId) + "\t");
                }
                catch(Exception e)
                {
                    bw.write(Integer.toString(0) + "\t");
                }
            }
            bw.write("\n");
        }
        bw.close();
    }
}
