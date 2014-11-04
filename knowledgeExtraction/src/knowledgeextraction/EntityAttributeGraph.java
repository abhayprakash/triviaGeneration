/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package knowledgeextraction;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.process.CoreLabelTokenFactory;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.Tokenizer;
import edu.stanford.nlp.process.TokenizerFactory;
import edu.stanford.nlp.trees.GrammaticalStructure;
import edu.stanford.nlp.trees.GrammaticalStructureFactory;
import edu.stanford.nlp.trees.PennTreebankLanguagePack;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreebankLanguagePack;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import static knowledgeextraction.usingWordnet.filePath;

/**
 *
 * @author Abhay Prakash
 */
public class EntityAttributeGraph {
    
    static String filePath = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Data\\test.txt";
    static String graphFile = "C:\\Users\\Abhay Prakash\\Workspace\\trivia\\Results\\GraphFile_NLP.txt";
    
    private final static String PCG_MODEL = "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz";
   
    private final TokenizerFactory<CoreLabel> tokenizerFactory = PTBTokenizer.factory(new CoreLabelTokenFactory(), "invertible=true");
    private final static LexicalizedParser parser = LexicalizedParser.loadModel(PCG_MODEL);

    public Tree parse(String str) {
        List<CoreLabel> tokens = tokenize(str);
        Tree tree = parser.apply(tokens);
        return tree;
    }

    private List<CoreLabel> tokenize(String str) {
        Tokenizer<CoreLabel> tokenizer = tokenizerFactory.getTokenizer(new StringReader(str));
        return tokenizer.tokenize();
    }

    public static void main(String[] args) throws IOException {
        // TODO code application logic here
        
        BufferedReader reader = new BufferedReader(new FileReader(filePath));
        String text = reader.readLine();
        
        Annotation document = new Annotation(text);
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit");//, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        pipeline.annotate(document);
        List<CoreMap> sentences = document.get(CoreAnnotations.SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            String input = sentence.toString();
            System.out.println(input);
            Tree tree = new EntityAttributeGraph().parse(input);
            System.out.println("tree: " + tree.toString());
            
            // Get dependency tree
            TreebankLanguagePack tlp = new PennTreebankLanguagePack();
            GrammaticalStructureFactory gsf = tlp.grammaticalStructureFactory();
            GrammaticalStructure gs = gsf.newGrammaticalStructure(tree);
            Collection<TypedDependency> td = gs.typedDependenciesCollapsed();
            //System.out.println(td);

            Object[] list = td.toArray();
            //System.out.println(list.length);
            PrintBestPath(list);
            //System.out.println();
        }
    }
    
    static HashMap<String, HashMap<String, List<String> > > GetTree(Object[] list)
    {
        HashMap<String, HashMap<String, List<String> > > tree = new HashMap<>();
        TypedDependency typedDependency;
        for (Object object : list) {
            typedDependency = (TypedDependency) object;
            String line = typedDependency.toString();
            int i = 0;
            char[] str = line.toCharArray();
            String reln = "", first = "", second = "";
            for(;i<str.length; i++){
                if(str[i] == '(')
                    break;
                reln += str[i];
            }
            
            for(i++;i<str.length; i++){
                if(str[i] == '-')
                    break;
                first += str[i];
            }
            
            for(i++;i<str.length; i++){
                if(str[i] == '-')
                    break;
                second += str[i];
            }
            if(tree.containsKey(first) == false){
                tree.put(first, new HashMap<String, List<String> >());
            }
            
            if(tree.get(first).containsKey(reln) == false){
                tree.get(first).put(reln, new ArrayList<String>());
            }
            
            tree.get(first).get(reln).add(second);
        }
        return tree;
    }
    
    static HashMap<String, Integer> GetPriorityReln()
    {
        HashMap<String, Integer> toret = new HashMap<>();
        toret.put("root", 1);
        toret.put("dobj", 1);
        toret.put("amod", 1);
        toret.put("xcomp", 1);
        toret.put("neg", 1);
        toret.put("nn", 1);
        toret.put("num", 1);
        toret.put("number", 1);
        return toret;
    }
    
    static void CleanAndCollapse(HashMap<String, HashMap<String, List<String> > > Tree)
    {
        HashMap<String, Integer> PriorityReln = GetPriorityReln();
        Pattern specialReln = Pattern.compile("prep_(.*)");
        //Remove Non Prior Nodes
        List<String> NodeToRemove = new ArrayList<>();
        for(String key: Tree.keySet()){
            List<String> RelToRemove = new ArrayList<>();
            for(String cKey: Tree.get(key).keySet()){
                if(PriorityReln.containsKey(cKey)){
                    continue;
                }
                Matcher m = specialReln.matcher(cKey);
                if (m.find()) {
                    continue;
                }
                
                for(String children: Tree.get(key).get(cKey)){
                    RelToRemove.add(children);
                }
            }
            for(String s: RelToRemove){
                Tree.get(key).remove(s);
            }
        }
        for(String s: NodeToRemove){
            Tree.remove(s);
        }        
    }
    
    static void TraverseAndPrintBestPath(HashMap<String, HashMap<String, List<String> > > Tree)
    {
        
    }
    
    static void PrintBestPath(Object[] list){
        HashMap<String, HashMap<String, List<String> > > Tree = GetTree(list); // Node[first][Relation] = List<Second>
        CleanAndCollapse(Tree);
        TraverseAndPrintBestPath(Tree);
    }
}