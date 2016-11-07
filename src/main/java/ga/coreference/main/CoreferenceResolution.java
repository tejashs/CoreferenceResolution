package ga.coreference.main;

import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Array;
import java.util.*;

public class CoreferenceResolution {
    private ArrayList<Tree> coRefNodeTrees = new ArrayList<Tree>();
    private ArrayList<Tree> sentenceParsedTrees = new ArrayList<Tree>();
    private HashMap<Tree, Tree> coRefPhraseTreeToSentenceMap = new HashMap<Tree, Tree>();
    private HashMap<Tree, ArrayList<Tree>> sentenceToNPTerminalMap = new HashMap<Tree, ArrayList<Tree>>();
    private HashMap<Tree, ArrayList<CandidateNP>> coRefToCandidateNPMap = new HashMap<Tree, ArrayList<CandidateNP>>();

    public static void main(String[] args) {

        String fileName = "b2.crf";
        CoreferenceResolution resolver = new CoreferenceResolution();
        BasicConfigurator.configure();
        resolver.parseInputFiles(fileName);
    }

    public CoreferenceResolution(){
        coRefPhraseTreeToSentenceMap = new HashMap<Tree, Tree>();
    }

    public void parseInputFiles(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(fileName).getFile());
        //Get All COREF Tags from file
        NodeList coRefTagList = getAllCoRefTagsInFile(f);
        sentenceParsedTrees = getParseTreesForFile(f);
        TreeHelper treeHelper = TreeHelper.getInstance();
        for (int i = 0; i < coRefTagList.getLength(); i++) {
            Tree nodeinTree = null;
            Node node = coRefTagList.item(i);
            //if(i == 12)
            nodeinTree = treeHelper.getTreeForCoRefTag(node, sentenceParsedTrees, coRefPhraseTreeToSentenceMap);
            if(nodeinTree != null){
                coRefNodeTrees.add(nodeinTree);
            }

            //######### DONT REMOVE UNTIL LAST
//            getLogger().debug(node.getTextContent());
//            if (nodeinTree == null) {
//                getLogger().debug("WTF WTF WTF");
//            } else {
//                getLogger().debug(nodeinTree);
//            }
        }
        startResolution();
    }

    private void startResolution(){
        for (Tree sentenceNode: sentenceParsedTrees) {
            ArrayList<Tree> terminalNPNodes = new ArrayList<Tree>();
            TreeTraversalUtility.getTerminalNPNodes(sentenceNode, terminalNPNodes);
            if(!sentenceToNPTerminalMap.containsKey(sentenceNode)){
                sentenceToNPTerminalMap.put(sentenceNode, terminalNPNodes);
            }
        }

        for (Tree coRefNode: coRefNodeTrees) {
            Tree sentence = coRefPhraseTreeToSentenceMap.get(coRefNode);
            ArrayList<Tree> npNodesList = sentenceToNPTerminalMap.get(sentence);
            ArrayList<CandidateNP> candidateNPs = getNpNodesUptilNodeInSentence(coRefNode, npNodesList, sentence);
//            getLogger().debug("#######");
//            getLogger().debug(coRefNode.toString());
//            getLogger().debug("Candidate Size of Sentence : " + candidateNPs.size());
//            getLogger().debug("Sentence : " + sentence.toString());
//            getLogger().debug("Printing NP Nodes uptil Node :" + coRefNode.toString());
//            getLogger().debug("-----");
            getAllNPsFromPreviousSentencesInDecreasingOrder(sentence, candidateNPs);
//            getLogger().debug("Candidate Size of All previous Sentences : " + candidateNPs.size());
            coRefToCandidateNPMap.put(coRefNode, candidateNPs);
        }

        CandidateEvaluator evaluator = new CandidateEvaluator(sentenceParsedTrees, coRefPhraseTreeToSentenceMap, sentenceToNPTerminalMap, coRefToCandidateNPMap);
        evaluator.evaluateCandidateNPsForCoRefs();


    }


    private ArrayList<CandidateNP> getNpNodesUptilNodeInSentence(Tree node, ArrayList<Tree> npNpdesList, Tree sentence){
        ArrayList<CandidateNP> listToReturn = new ArrayList<CandidateNP>();
        int nodeNumber = node.nodeNumber(sentence);
//        getLogger().debug("NODE NUMBER : " + nodeNumber);
        for (int i = 0; i < npNpdesList.size(); i++) {
            Tree n = npNpdesList.get(i);
            int nNodeNumber = n.nodeNumber(sentence);
            if(nNodeNumber < nodeNumber){
                listToReturn.add(new CandidateNP(n, sentence));
//                getLogger().debug("N-Node Num: " + nNodeNumber);
//                getLogger().debug(n.toString());
            }
        }
        return listToReturn;
    }

    private void getAllNPsFromPreviousSentencesInDecreasingOrder(Tree sentence, ArrayList<CandidateNP> candidateNPs){
        int sentenceIndex = sentenceParsedTrees.indexOf(sentence);
        for(int i=sentenceIndex-1; i>=0; i--){
            Tree sentenceNode = sentenceParsedTrees.get(i);
            ArrayList<Tree> terminalNPNodes = new ArrayList<Tree>();
            TreeTraversalUtility.getTerminalNPNodes(sentenceNode, terminalNPNodes);
            ArrayList<CandidateNP> candidates = CandidateNP.getCandidateNPFromTree(terminalNPNodes, sentenceNode);
            candidateNPs.addAll(candidates);
        }

    }

    private ArrayList<Tree> getParseTreesForFile(File file) {
        String fileText = "";
        try {
            fileText = IOUtils.slurpFile(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        fileText = fileText.replaceAll("\\n\\n", ".\n\n");
        fileText = fileText.replaceAll("\\n-", ".\n\n");
        String[] sentencesInFile = fileText.split("\\.");

        List<CoreMap> sentences = getParsedSentences(fileText);
        getLogger().debug("Parsed Sentences Count: " + sentences.size());

        int validSentenceCounter = 0;
        ArrayList<Tree> listOfTrees = new ArrayList<Tree>();
        for (CoreMap sentence : sentences) {
            if (sentence.toString().equals(".")) {
                continue;
            }
            validSentenceCounter++;
            //getLogger().debug(sentence.toString());
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            listOfTrees.add(tree);
        }
        getLogger().debug("Valid Sentences Count : " + validSentenceCounter);
        return listOfTrees;
    }

    private List<CoreMap> getParsedSentences(String textToParse) {
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotatedDoc = null;
        annotatedDoc = new Annotation(textToParse);
        pipeline.annotate(annotatedDoc);
        List<CoreMap> sentences = annotatedDoc.get(CoreAnnotations.SentencesAnnotation.class);
        return sentences;
    }

    private NodeList getAllCoRefTagsInFile(File f) {
        DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
        Document document = null;
        try {
            document = factory.newDocumentBuilder().parse(f);
        } catch (SAXException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ParserConfigurationException e) {
            e.printStackTrace();
        }
        NodeList listOfTags = null;
        if (document != null) {
            listOfTags = document.getElementsByTagName("COREF");
            getLogger().debug("Number of CoRef Tags : " + listOfTags.getLength());
        }
        return listOfTags;
    }

    private void printTree(Tree tree) {
        List<Tree> children = tree.getChildrenAsList();
        for (Tree child : children) {
            String label = child.label().value();
            if (label.equals("NP")) {
                getLogger().debug("NP: " + child.toString());
            }
            child.taggedYield();
            if (child.isLeaf()) {
                getLogger().debug("LEAF: " + child);
            } else {
                printTree(child);
            }
        }
    }

    private String getOriginalSentenceForCoRefTag(String coRefTag, ArrayList<String> listOfSentences) {
        for (String sentence : listOfSentences) {
            if (sentence.contains(coRefTag)) {
                return sentence;
            }
        }
        return null;
    }

    private Logger getLogger() {
        return Logger.getLogger(CoreferenceResolution.class);
    }

    private void print(Object obj){
        if(obj == null){
            return;
        }
        if(obj instanceof ArrayList){
            ArrayList list = (ArrayList)obj;
            for (int i = 0; i < list.size(); i++) {
                Tree t = (Tree)list.get(i);
                getLogger().debug(t.toString());
            }
        }
        else if(obj instanceof Tree){
            getLogger().debug(((Tree)obj).toString());
        }
        else {
            getLogger().debug(obj.toString());
        }

    }

}
