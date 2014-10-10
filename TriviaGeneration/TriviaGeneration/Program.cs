using System;
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
        static string fileName = @"C:\Users\Abhay Prakash\Workspace\trivia\SachinCode.txt";

        static void Main(string[] args)
        {
            string url = @"http://en.wikipedia.org/wiki/Sachin_Tendulkar";
            string text = getText(url);
            System.IO.StreamWriter textFile = new System.IO.StreamWriter(fileName);
            textFile.Write(text);
            textFile.Close();
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
