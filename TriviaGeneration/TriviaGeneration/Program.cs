using System;
using System.IO;
using System.Collections.Generic;
using System.Linq;
using System.Net;
using System.Text;
using System.Threading.Tasks;
using System.Diagnostics;
using System.Text.RegularExpressions;
using HtmlAgilityPack;

namespace TriviaGeneration
{
    class Program
    {
        static System.IO.StreamWriter positiveDataFile;
        static void Main(string[] args)
        {
            string allfileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\countryTrivia.txt";
            positiveDataFile = new System.IO.StreamWriter(allfileName, true);

            /*
            urls.Add(@"http://en.wikipedia.org/wiki/Sachin_Tendulkar");
            */
            
            //urls.Add(@"http://en.wikipedia.org/wiki/Interstellar_%28film%29");
            //urls.Add(@"http://www.sciencekids.co.nz/sciencefacts/countries/brazil.html");
            
            String fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\countriesAvailable.txt";
            StreamReader r = new StreamReader(fileName);
            string line;
            while ((line = r.ReadLine()) != null)
            {
                String[] s = line.Split(new char[] { '\t' });
                String url = "http://www.sciencekids.co.nz/sciencefacts/countries/"+s[0];
                String countryName = s[1];
                generateTextFile(url, countryName);
            }
            
            //generateTextFile(urls);
            positiveDataFile.Flush();
            positiveDataFile.Close();
            Console.ReadLine();
        }
        
        static void generateTextFile(List<string> urls)
        {
            foreach (string url in urls)
            {
                string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\brazil.txt";

                if (File.Exists(fileName))
                    continue;

                string text = getText(url);
                System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
                textFile.Write(text);
                textFile.Close();
                Console.WriteLine("done: " + url);
            }
        }

        static void generateTextFile(String url, String countryName)
        {
            string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\Country\"+countryName+".txt";

            if (File.Exists(fileName))
                return;

            string text = getText(url, countryName);
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            textFile.Write(text);
            textFile.Close();
            Console.WriteLine("done: " + url);
        }

        static string getText(string url, string cn = "")
        {
            HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
            HtmlAgilityPack.HtmlDocument doc = web.Load(url);

            string text="";
            try
            {
                foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//li/p"))
                {
                    text += row.InnerText + "\n";
                    positiveDataFile.WriteLine(cn + "\t" + row.InnerText);
                }
            }
            catch (Exception e)
            {
                Console.WriteLine("ERROR: " + url + "\n" + e.ToString());
            }

            string pattern = "citation needed";
            string replacement = "0";
            Regex rgx1 = new Regex(pattern);
            text = rgx1.Replace(text, replacement);

            pattern = "[[0-9]*]";
            replacement = "";
            Regex rgx = new Regex(pattern);
            text = rgx.Replace(text, replacement);

            return text;
        }
    }
}
