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
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

public class CoreferenceResolution {
    private Logger logger;
    private ArrayList<String> originalSentences;

    public static void main(String[] args) {

        String fileName = "a8.crf";
        CoreferenceResolution resolver = new CoreferenceResolution();
        resolver.logger = resolver.getLogger();
        BasicConfigurator.configure();
        resolver.startResolution(fileName);
    }

    public void startResolution(String fileName) {
        ClassLoader classLoader = getClass().getClassLoader();
        File f = new File(classLoader.getResource(fileName).getFile());
        //Get All COREF Tags from file
        NodeList coRefTagList = getAllCoRefTagsInFile(f);
        ArrayList<Tree> listOfParseTrees = getParseTreesForFile(f);
        logger.debug(coRefTagList);
        TreeHelper treeHelper = TreeHelper.getInstance();
        for (int i = 0; i < coRefTagList.getLength(); i++) {
            Tree nodeinTree = null;
            //if(i == 12){
                Node node = coRefTagList.item(i);
                nodeinTree = treeHelper.getTreeForCoRefTag(node, listOfParseTrees);
                getLogger().debug(node.getTextContent());
                if(nodeinTree == null){
                    getLogger().debug("WTF WTF WTF");
                }
                else {
                    getLogger().debug(nodeinTree);
                }
            //}

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
        String[] sentencesInFile = fileText.split("\\.");
        originalSentences = new ArrayList<String>();
        originalSentences.addAll(Arrays.asList(sentencesInFile));

        //fileText = stripCoRefTags(fileText);

        List<CoreMap> sentences = getParsedSentences(fileText);
        logger.debug("Sentences: " + sentences.size());

        int validSentenceCounter = 0;
        ArrayList<Tree> listOfTrees = new ArrayList<Tree>();
        for (CoreMap sentence : sentences) {
            if (sentence.toString().equals(".")) {
                continue;
            }
            validSentenceCounter++;
            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            listOfTrees.add(tree);
            //logger.debug(tree);
            //printTree(tree);
        }
        logger.debug("Valid Sentences Count : " + validSentenceCounter);
        return listOfTrees;
    }

    private List<CoreMap> getParsedSentences(String textToParse){
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
            logger.debug("Parsed");
            listOfTags = document.getElementsByTagName("COREF");
            logger.debug(listOfTags.getLength());
        }
        return listOfTags;
    }

    private void printTree(Tree tree) {
        List<Tree> children = tree.getChildrenAsList();
        for (Tree child : children) {
            String label = child.label().value();
            if (label.equals("NP")) {
                logger.debug("NP: " + child.toString());
            }
            child.taggedYield();
            if (child.isLeaf()) {
                logger.debug("LEAF: " + child);
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
    private Logger getLogger(){
        return Logger.getLogger(CoreferenceResolution.class);
    }

}
