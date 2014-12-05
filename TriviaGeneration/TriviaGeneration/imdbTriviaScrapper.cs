﻿using HtmlAgilityPack;
using System;
using System.Collections.Generic;
using System.IO;
using System.Linq;
using System.Text;
using System.Text.RegularExpressions;
using System.Threading;
using System.Threading.Tasks;

namespace TriviaGeneration
{
    class imdbTriviaScrapper
    {
        //static String URLformat = @"http://m.imdb.com/name/nmXXXXXXX/trivia"; // 0006795
        static String AllfileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\selected1000Movies\allTrivia.txt";
        static System.IO.StreamWriter positiveDataFile;

        static void Main(string[] args)
        {
            positiveDataFile = new System.IO.StreamWriter(AllfileName, true);
            List<string> topCelebsList = NameID_Scrapper.GetNameIDsForTop(1000);
            foreach (string s in topCelebsList)
            {
                //String URL = @"http://m.imdb.com" + s + "/trivia";
                String URL = @"http://www.imdb.com" + s + "trivia";
                generateTextFile(URL);
            }
            //Console.ReadLine();
            /*
            for (int i = 9542; i < 1006795; i++)
            {
                String Number = i.ToString("D7");
                String URL = URLformat.Replace("XXXXXXX", Number);

                //new Thread(delegate()
                //{
                generateTextFile(URL);
                //}).Start();
            }
             */
            positiveDataFile.Close();
        }


        static void generateTextFile(String URL)
        {
            HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
            HtmlAgilityPack.HtmlDocument doc = web.Load(URL);
            
            string entityName = "";
            foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//title"))
            {
                entityName += row.InnerText + " ";
            }
            entityName = entityName.Replace(" - Trivia - IMDb", "");
            entityName = entityName.Replace("\n", "");

            if (entityName.Equals("IMDb"))
                return;

            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\selected1000Movies\indivFiles\" + entityName + ".txt";

            //if (File.Exists(entityName))
              //  return;
            
            String Trivias = "";
            try
            {
                foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//div/div[@class=\"sodatext\"]"))
                {
                    String oneRow = row.InnerText.Replace("\n", "");
                    HtmlNode childNode = row.SelectNodes("./../div[@class=\"did-you-know-actions\"]/a").ElementAt(0);
                    String rating = childNode.InnerText;
                    rating = rating.Replace(" found this interesting", "");
                    rating = rating.Replace(" of ", " ");
                    String[] s = rating.Split(new char[] { ' ' });
                    Trivias += oneRow + "\t" + s[0] + "\t" + s[1] + "\t" + s[2] + "\n";
                    //Console.WriteLine("here " + s.Length);
                    //Console.WriteLine(s[1] + " : " + s[2]);
                    positiveDataFile.Write(entityName + "\t" + oneRow + "\t" + s[0] + "\t" + s[1] + "\t" + s[2] + "\n");
                    //positiveDataFile.Flush();
                }
            }
            catch (Exception e)
            {
                Console.ForegroundColor = ConsoleColor.DarkRed;
                Console.BackgroundColor = ConsoleColor.DarkYellow;
                Console.WriteLine("Ignored: " + entityName);
                Console.ForegroundColor = ConsoleColor.White;
                Console.BackgroundColor = ConsoleColor.Black;
            }
            try
            {
                if (!Trivias.Length.Equals(0))
                {
                    System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
                    textFile.Write(Trivias);
                    textFile.Close();
                    Console.WriteLine("done: " + entityName);
                }
            }catch(Exception e){
                Console.ForegroundColor = ConsoleColor.DarkRed;
                Console.BackgroundColor = ConsoleColor.DarkGreen;
                Console.WriteLine("Ignored: " + entityName + " :: " + e.Message);
                Console.ForegroundColor = ConsoleColor.White;
                Console.BackgroundColor = ConsoleColor.Black;
            }
        }
    }
}
