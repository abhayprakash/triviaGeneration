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
        static String URLformat = @"http://m.imdb.com/name/nmXXXXXXX/trivia"; // 0006795
        static String AllfileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\imdbTrivia\allTrivia.txt";
        static System.IO.StreamWriter positiveDataFile;

        static void Main(string[] args)
        {
            positiveDataFile = new System.IO.StreamWriter(AllfileName);
            for (int i = 1; i < 1006795; i++)
            {
                String Number = i.ToString("D7");
                String URL = URLformat.Replace("XXXXXXX", Number);

                //new Thread(delegate()
                //{
                    generateTextFile(URL);
                //}).Start();
            }
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
            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\imdbTrivia\" + entityName + ".txt";
            
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
                Console.WriteLine("Ignored: " + entityName);
            }
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            textFile.Write(Trivias);
            textFile.Close();
            Console.WriteLine("done: " + entityName);
        }
    }
}
