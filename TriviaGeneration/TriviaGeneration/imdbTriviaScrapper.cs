using HtmlAgilityPack;
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
        static String AllfileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\top1000Celebs\allTrivia.txt";
        static System.IO.StreamWriter positiveDataFile;

        static void Main(string[] args)
        {
            positiveDataFile = new System.IO.StreamWriter(AllfileName, true);
            List<string> topCelebsList = NameID_Scrapper.GetNameIDsForTop(1000);
            foreach (string s in topCelebsList)
            {
                String URL = @"http://m.imdb.com" + s + "/trivia";
                generateTextFile(URL);
            }
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

            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\IMDb\top1000Celebs\indivFiles\" + entityName + ".txt";

            //if (File.Exists(entityName))
              //  return;
            
            String Trivias = "";
            try
            {
                foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//p"))
                {
                    String oneRow = row.InnerText.Replace("\n", "") + "\n";
                    Trivias += oneRow;
                    positiveDataFile.Write(entityName + ": " + oneRow);
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
                Console.WriteLine("Ignored: " + entityName);
                Console.ForegroundColor = ConsoleColor.White;
                Console.BackgroundColor = ConsoleColor.Black;
            }
        }
    }
}
