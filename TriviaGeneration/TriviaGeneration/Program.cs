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
        static void Main2(string[] args)
        {
            List<string> urls = new List<string>();
            /*
            urls.Add(@"http://en.wikipedia.org/wiki/Sachin_Tendulkar");
            urls.Add(@"http://en.wikipedia.org/wiki/Virender_Sehwag");
            urls.Add(@"http://en.wikipedia.org/wiki/Sourav_Ganguly");
            urls.Add(@"http://en.wikipedia.org/wiki/Rahul_Dravid");
            urls.Add(@"http://en.wikipedia.org/wiki/Mahendra_Singh_Dhoni");
            urls.Add(@"http://en.wikipedia.org/wiki/V._V._S._Laxman");
            urls.Add(@"http://en.wikipedia.org/wiki/Yuvraj_Singh");
            urls.Add(@"http://en.wikipedia.org/wiki/Virat_Kohli");
            urls.Add(@"http://en.wikipedia.org/wiki/Gautam_Gambhir");
            urls.Add(@"http://en.wikipedia.org/wiki/Suresh_Raina");
            urls.Add(@"http://en.wikipedia.org/wiki/Irfan_Pathan");
            urls.Add(@"http://en.wikipedia.org/wiki/Mohammad_Azharuddin");
            urls.Add(@"http://en.wikipedia.org/wiki/Amitabh_Bachchan");
            */
            /*
            urls.Add(@"http://en.wikipedia.org/wiki/Will_Smith");
            urls.Add(@"http://en.wikipedia.org/wiki/Tom_Hanks");
            urls.Add(@"http://en.wikipedia.org/wiki/Bradley_Cooper");
            urls.Add(@"http://en.wikipedia.org/wiki/Tom_Cruise");
            urls.Add(@"http://en.wikipedia.org/wiki/Jude_Law");
            urls.Add(@"http://en.wikipedia.org/wiki/Robert_Pattinson");
            urls.Add(@"http://en.wikipedia.org/wiki/Matt_Damon");
            urls.Add(@"http://en.wikipedia.org/wiki/Brad_Pitt");
            urls.Add(@"http://en.wikipedia.org/wiki/Johnny_Depp");
            urls.Add(@"http://en.wikipedia.org/wiki/Leonardo_DiCaprio");
            */

            generateTextFile(urls);
            //GenerateSentencesWithEntityName(urls);
        }

        
        static void generateTextFile(List<string> urls)
        {
            foreach (string url in urls)
            {
                string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\Data\wikiText\HollywoodActors\" + url.Remove(0, 29) + ".txt";

                if (File.Exists(fileName))
                    continue;

                string text = getText(url);
                System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
                textFile.Write(text);
                textFile.Close();
                Console.WriteLine("done: " + url);
            }
        }

        static string getText(string url)
        {
            HtmlAgilityPack.HtmlWeb web = new HtmlWeb();
            HtmlAgilityPack.HtmlDocument doc = web.Load(url);

            string text="";

            foreach (HtmlNode row in doc.DocumentNode.SelectNodes("//p"))
            {
                text += row.InnerText + " ";
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
