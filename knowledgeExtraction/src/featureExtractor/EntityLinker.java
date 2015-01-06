/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package featureExtractor;

import static featureExtractor.NLPFeatures.resultFile_Root;
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

/**
 *
 * @author Abhay Prakash
 */
public class EntityLinker {
    static String folderPath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\anotherSelected5k\\entityLinking\\";
    static String In_rootWords = folderPath + "INT_D_rootWord.txt";
    static String Out_rootWords = folderPath + "PRO_rootWord.txt";
    static String In_subjWords = folderPath + "INT_D_subjectWords.txt";
    static String Out_subjWords = folderPath + "PRO_subjWord.txt";
    static String In_underRootWords = folderPath + "INT_D_underRootWords.txt";
    static String Out_underRootWors = folderPath + "PRO_underRootWords.txt";
    
    static String In_entityDictionary = folderPath + "entityLinks.txt";
    static String In_movieID_Trivia = folderPath + "movieID_Trivia.txt";
    static String Out_allEntitiesPresent = folderPath + "PRO_allLinkedEntities.txt";
    
    static BufferedWriter bw_root, bw_subj, bw_underRoot, bw_allEntities;
    
    static HashMap<String, HashMap<String, List<String>>> dict = new HashMap<String, HashMap<String, List<String>>>();
    static List<String> movieIDs = new ArrayList<String>();
    
    static List<String> STOPWORDS = new ArrayList<String>();
    
    public static void main(String[] args) throws IOException
    {
        String list = "a,able,about,across,after,all,almost,also,am,among,an,and,any,are,as,at,be,because,been,but,by,can,cannot,could,dear,did,do,does,either,else,ever,every,for,from,get,got,had,has,have,he,her,hers,him,his,how,however,i,if,in,into,is,it,its,just,least,let,like,likely,may,me,might,most,must,my,neither,no,nor,not,of,off,often,on,only,or,other,our,own,rather,said,say,says,she,should,since,so,some,than,that,the,their,them,then,there,these,they,this,tis,to,too,twas,us,wants,was,we,were,what,when,where,which,while,who,whom,why,will,with,would,yet,you,your";
        STOPWORDS = Arrays.asList(list.split(","));
        
        ReadDictionary();
        ReadIDs_and_processAllPresentLinkedEntities();
        Process_RootWords();
        Process_subjWords();
        Process_underRootWords();
    }
    
    static void ReadDictionary() throws FileNotFoundException, IOException
    {
        FileReader inputFile = new FileReader(In_entityDictionary);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        String line;
        int lineNum = 0;
        while((line = bufferReader.readLine()) != null)
        {
            String[] row = line.split("\t");
            row[0] = row[0].trim();
            //System.out.println(lineNum + " : " + row.length + " : " + row[0] + row[1] + row[2]);
            dict.putIfAbsent(row[0], new HashMap<>());
            dict.get(row[0]).putIfAbsent(row[1], new ArrayList<>());
            dict.get(row[0]).get(row[1]).add(row[2]);
            lineNum++;
        }
        System.out.println("Read " + lineNum + " lines");
    }
    
    static void ReadIDs_and_processAllPresentLinkedEntities() throws FileNotFoundException, IOException
    {
        FileReader inputFile = new FileReader(In_movieID_Trivia);
        BufferedReader bufferReader = new BufferedReader(inputFile);
        
        FileWriter fw = new FileWriter(Out_allEntitiesPresent);
        bw_allEntities = new BufferedWriter(fw);
        
        String line;
        int lineNum = 0;
        while((line = bufferReader.readLine()) != null)
        {
            lineNum++;
            String[] row = line.split("\t");
            row[0] = row[0].trim();
            movieIDs.add(row[0]);
            if(row.length > 2)
                System.out.println("TAKE NOTE OF TRIVIA NUMBER: " + lineNum);
            ProcessSingleLine_AllEnitiesPresent(row[0],row[1]);
        }
        System.out.println("found all linkable entities, number of lines: " + lineNum);
        bw_allEntities.flush();
        bw_allEntities.close();
    }
    
    static void ProcessSingleLine_AllEnitiesPresent(String movieID,String Trivia) throws IOException
    {
        Trivia = Trivia.trim().toLowerCase();
        String words[] = Trivia.split(" ");
        //System.out.println("Trivia: " + Trivia);
                        
        for(String entity_X: dict.get(movieID).keySet())
        {//continue if found i.e. break inner for loops
            //System.out.println("entity type: " + entity_X);
            for(String candidate : dict.get(movieID).get(entity_X))
            {
                candidate = candidate.toLowerCase();
                //System.out.println("candidate: " + candidate);
                Boolean toBreak = false;
                for(String triviaWord : words) // Yes, I know that same word can be linked to different entity_type, denoting their presence - any ways we need to give benfit of doubt and the more awesome thing is if a person is director as well as producer, somehow this signal too will get captured some how
                {
                    //System.out.println("Trivia Word: " + triviaWord);
                    if(Arrays.asList(candidate.split(" ")).contains(triviaWord) && !STOPWORDS.contains(triviaWord))
                    {
                        //System.out.println("Matched: " + triviaWord + " :: " + entity_X + " :: " + candidate);
                        bw_allEntities.write(entity_X + " ");
                        toBreak = true;
                        break;
                    }
                }
                if(toBreak)
                    break;
            }
        }
        bw_allEntities.write("\n");
    }
    
    static void Process_RootWords()
    {
        
    }
    
    static void Process_subjWords()
    {
        
    }
    
    static void Process_underRootWords()
    {
        
    }
}
