/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package tablebuilder;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.AbstractMap;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.w3c.dom.events.Event;
import org.yaml.snakeyaml.Yaml;

/**
 *
 * @author Abhay Prakash
 */
public class TableBuilder {

    /**
     * @param args the command line arguments
     */
    static String folderPath = "E:\\Cricket\\odis\\";
    static String resultFile = "E:\\Cricket\\odisResult\\table.txt";
    static BufferedWriter bw;
    
    public static void main(String[] args) throws IOException {
        File dir = new File(folderPath);
        File[] directoryListing = dir.listFiles();
        
        File file = new File(resultFile);
        // if file doesnt exists, then create it
        if (file.exists()) {
                file.delete();
        }
        file.createNewFile();
        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        bw = new BufferedWriter(fw);
        
        bw.write("matchID\tcity\tdates\tmatch_type\twinner\tteam1\tteam2\ttossWinner\ttossDecision\tumpires\tvenue\tplayer_of_match\tScore_1\tScore_2\tScore_Total\tExtra_1\tExtrac_2\tExtra_Total\tFours_1\tFours_2\tFours_Total\tSix_1\tSix_2\tSix_Total\tWicket_1\tWicket_2\tWicket_Total\tlbw_1\tlbw_2\tlbw_total\t_caught_1\tcaught_2\tcaught_total\tbowled_1\tbowled_2\tbowled_total\n");//\ttotalRuns", "total4", "total6"};
        
        for (File child : directoryListing) {
            String inputFile = child.getName();
            if(!inputFile.equals("README.txt"))
                MakeEntry(inputFile);
            //break;
        }
        bw.close();
        for(String s: table.keySet())
            System.out.print(s + " ");
    }
    
    static String readFile(String path, Charset encoding) throws IOException 
    {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
    static void MakeEntry(String fileName) throws IOException
    {
        String printRow = fileName.replace(".yaml", "");
        System.out.println(printRow);
        Yaml yaml = new Yaml();
        String fileContent = readFile(folderPath+fileName, StandardCharsets.UTF_8);
        Map<String, Object> obj = (Map<String, Object>) yaml.load(fileContent);
        //System.out.println("HERE: " + ((Map<String, Object>)((Map<String, Object>)(((ArrayList<Object>)(obj.get("innings"))).get(0))).get("1st innings")).get("team"));
        
        String firstToBat = (String)((Map<String, Object>)((Map<String, Object>)(((ArrayList<Object>)(obj.get("innings"))).get(0))).get("1st innings")).get("team");
        
        for (Map.Entry<String, Object> entry : obj.entrySet())
        {
            if(entry.getKey().equals("info"))
            {
                printRow += GetBasicValues(entry, firstToBat);
            }
            
            if(entry.getKey().equals("innings"))
            {
                printRow += GetRuns(entry);
                printRow += GetExtras(entry);
                printRow += Get4s(entry);
                printRow += Get6s(entry);
                printRow += GetWickets(entry);
                printRow += GetTypeOfOut(entry, "wicket", "kind", "lbw");
                printRow += GetTypeOfOut(entry, "wicket", "kind", "caught");
                printRow += GetTypeOfOut(entry, "wicket", "kind", "bowled");
            }
            
        }
        bw.write(printRow + "\n");
    }
    
    static String GetWickets(Map.Entry<String, Object> entry)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        if(((Map<String, Object>)ball).containsKey("wicket"))
                            sumInning ++;
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static String GetRuns(Map.Entry<String, Object> entry)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        sumInning += (Integer)(((Map<String, Object>)(((Map<String, Object>)ball).get("runs"))).get("total"));
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static String GetTypeOfOut(Map.Entry<String, Object> entry, String first, String second, String third)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        if(((Map<String, Object>)ball).containsKey(first))
                        if(((Map<String, Object>)(((Map<String, Object>)ball).get(first))).containsKey(second))
                        if(((Map<String, Object>)(((Map<String, Object>)ball).get(first))).get(second).equals(third))
                            sumInning ++;
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static String GetExtras(Map.Entry<String, Object> entry)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        sumInning += (Integer)(((Map<String, Object>)(((Map<String, Object>)ball).get("runs"))).get("extras"));
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static String Get4s(Map.Entry<String, Object> entry)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        int temp = (Integer)(((Map<String, Object>)(((Map<String, Object>)ball).get("runs"))).get("batsman"));
                        if(temp == 4)
                            sumInning ++;
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static String Get6s(Map.Entry<String, Object> entry)
    {
        String printRow = "";
        ArrayList<Object> eObj = (ArrayList<Object>) entry.getValue();
        //System.out.println(eObj);
        int sum = 0;
        for(Object obj: eObj)
        {
            int sumInning = 0;
            //System.out.println(obj);
            for(Object inning : ((Map<String, Object>)obj).values())
            {
                for(Object deliveries : (ArrayList<Object>)((Map<String, Object>)inning).get("deliveries"))
                {
                    for(Object ball: ((Map<String, Object>)deliveries).values())
                    {
                        int temp = (Integer)(((Map<String, Object>)(((Map<String, Object>)ball).get("runs"))).get("batsman"));
                        if(temp == 6)
                            sumInning ++;
                    }
                }
            }
            printRow += "\t" + sumInning;
            sum += sumInning;
        }
        //System.out.println(sum);
        printRow += "\t" + sum;
        return printRow;
    }
    
    static Map<String, Integer> table = new HashMap<String, Integer>();
    static String GetBasicValues(Map.Entry<String, Object> entry, String firstToBat)
    {
        String printRow = "";
        Map<String, Object> eObj = (Map<String, Object>) entry.getValue();
        if(eObj.containsKey("city"))
            printRow += "\t" + eObj.get("city");
        else
            printRow += "\t" + "NA";

        SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy");

        if(eObj.containsKey("dates"))
            printRow += "\t" + sdf.format((Date)(((ArrayList<Date>)eObj.get("dates")).get(0)));
        else
            printRow += "\t" + "NA";

        if(eObj.containsKey("match_type"))
            printRow += "\t" + eObj.get("match_type");
        else
            printRow += "\t" + "NA";

        if(eObj.containsKey("outcome"))
        {
            if(((Map<String, Object>)eObj.get("outcome")).containsKey("winner"))
                printRow += "\t" + ((Map<String, Object>)eObj.get("outcome")).get("winner");
            else
                printRow += "\t" + "NA";
        }
        else
            printRow += "\t" + "NA";

        if(eObj.containsKey("teams"))
        {
            if(((ArrayList<String>)eObj.get("teams")).get(0).equals(firstToBat))
                printRow += "\t" + ((ArrayList<String>)eObj.get("teams")).get(0) + "\t" + ((ArrayList<String>)eObj.get("teams")).get(1);
            else
                printRow += "\t" + ((ArrayList<String>)eObj.get("teams")).get(1) + "\t" + ((ArrayList<String>)eObj.get("teams")).get(0);
        }
        else
            printRow += "\t" + "NA" + "\t" + "NA";

        if(eObj.containsKey("toss"))
        {
            if(((Map<String, Object>)eObj.get("toss")).containsKey("winner"))
                printRow += "\t" + ((Map<String, Object>)eObj.get("toss")).get("winner");
            else
                printRow += "\t" + "NA";

            if(((Map<String, Object>)eObj.get("toss")).containsKey("decision"))
                printRow += "\t" + ((Map<String, Object>)eObj.get("toss")).get("decision");
            else
                printRow += "\t" + "NA";
        }
        else
            printRow += "\t" + "NA" + "\t" + "\n";

        if(eObj.containsKey("umpires"))
        {
            String value = ((ArrayList<String>)eObj.get("umpires")).get(0);
            for(int i = 1; i < ((ArrayList<String>)eObj.get("umpires")).size(); i++)
                value += "," + ((ArrayList<String>)eObj.get("umpires")).get(i);
            printRow += "\t" + value;
        }
        else
            printRow += "\t" + "NA";

        if(eObj.containsKey("venue"))
            printRow += "\t" + eObj.get("venue");
        else
            printRow += "\t" + "NA";

        if(eObj.containsKey("player_of_match"))
        {
            String value = ((ArrayList<String>)eObj.get("player_of_match")).get(0);
            for(int i = 1; i < ((ArrayList<String>)eObj.get("player_of_match")).size(); i++)
                value += "," + ((ArrayList<String>)eObj.get("player_of_match")).get(i);
            printRow += "\t" + value;
        }
        else
            printRow += "\t" + "NA";
        
        
        for(String s: eObj.keySet())
        {
            if(table.containsKey(s))
                continue;
            //System.out.print(s + ",");
            table.put(s, 1);
        }
        
        return printRow;
    }
}
