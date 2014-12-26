/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knowledgeextraction;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;

/**
 *
 * @author Abhay Prakash
 */
public class movieNameToID {
    static String preURL = "http://www.omdbapi.com/?t=";
    public static void main(String[] args) throws IOException
    {
        BufferedReader br = null;
        File file = new File("C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\selected1000Movies\\list.txt");

        // if file doesnt exists, then create it
        if (!file.exists()) {
                file.createNewFile();
        }

        FileWriter fw = new FileWriter(file.getAbsoluteFile());
        BufferedWriter bw = new BufferedWriter(fw);

        try {

                String sCurrentLine;

                br = new BufferedReader(new FileReader("C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\IMDb\\selected1000Movies\\MoviesSelected.txt"));

                while ((sCurrentLine = br.readLine()) != null) {
                        //System.out.println(sCurrentLine);
                    String name = sCurrentLine;
                    sCurrentLine = sCurrentLine.replace(" ", "%20");
                    String URL = preURL + sCurrentLine;
                    try{
                        String page = getText(URL);
                        int i = page.indexOf("imdbID");
                        String titleId = page.substring(i + 9, i + 18);
                        /************/
                        bw.write("/title/" + titleId + "/\t" + name + "\n");
                        System.out.println("Done: " + name);
                        /************/
                    }catch(Exception e)
                    {
                        System.out.println("Ignored: " + name);
                    }
                }

        } catch (IOException e) {
                e.printStackTrace();
        } finally {
                try {
                        if (br != null)br.close();
                } catch (IOException ex) {
                        ex.printStackTrace();
                }
        }
        bw.close();
    }
    static String getText(String URL)
    {
        URL url;
        InputStream is = null;
        BufferedReader br;
        String line;
        String result = "";

        try {
            url = new URL(URL);
            is = url.openStream();  // throws an IOException
            br = new BufferedReader(new InputStreamReader(is));//http://www.omdbapi.com/?t=Like%20Water%20for%20Chocolate

            while ((line = br.readLine()) != null) {
                //System.out.println(line);
                result += line;
            }
        } catch (MalformedURLException mue) {
             mue.printStackTrace();
        } catch (IOException ioe) {
             ioe.printStackTrace();
        } finally {
            try {
                if (is != null) is.close();
            } catch (IOException ioe) {
                // nothing to see here
            }
        }
        return result;
    }
}
