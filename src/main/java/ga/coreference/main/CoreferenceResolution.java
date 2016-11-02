package ga.coreference.main;

import com.sun.xml.internal.ws.policy.privateutil.PolicyUtils;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.*;
import java.util.List;
import java.util.Properties;

public class CoreferenceResolution {

    public static void main(String[] args) {

        CoreferenceResolution resolver = new CoreferenceResolution();
        resolver.startResolution();
    }

    public void startResolution(){
        ClassLoader classLoader = getClass().getClassLoader();

        File f = new File(classLoader.getResource("a8.crf").getFile());
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


        if(document != null){
            System.out.println("Parsed");
           NodeList mainNode = document.getElementsByTagName("COREF");
            System.out.println(mainNode.getLength());

            for (int i = 0; i < mainNode.getLength(); i++) {
                Node n = mainNode.item(i);
                //System.out.println(n.getTextContent());
                //System.out.println(n.getAttributes().getNamedItem("ID"));
            }

        }

        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, ner, parse, dcoref");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation annotatedDoc = null;
        try {
            String fileText = IOUtils.slurpFile(f);
            fileText = fileText.replaceAll("\\<[^\\>]+\\>","");
            System.out.println(fileText);
            annotatedDoc = new Annotation(fileText);
        } catch (IOException e) {
            e.printStackTrace();
        }
        pipeline.annotate(annotatedDoc);
        List<CoreMap> sentences = annotatedDoc.get(CoreAnnotations.SentencesAnnotation.class);
        List<CoreMap> paragraphs = annotatedDoc.get(CoreAnnotations.ParagraphsAnnotation.class);
        System.out.println("Sentences: " + sentences.size());

        for(CoreMap sentence: sentences) {
            // traversing the words in the current sentence
            // a CoreLabel is a CoreMap with additional token-specific methods
//            for (CoreLabel token: sentence.get(CoreAnnotations.TokensAnnotation.class)) {
//                // this is the text of the token
//                String word = token.get(CoreAnnotations.TextAnnotation.class);
//                // this is the POS tag of the token
//                String pos = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
//                // this is the NER label of the token
//                String ne = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
//            }

            // this is the parse tree of the current sentence
            Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);
            List<Tree> children = tree.getChildrenAsList();
            for (Tree child:children) {
                printTree(child);
            }
            //System.out.println(tree);


            // this is the Stanford dependency graph of the current sentence
//            SemanticGraph dependencies = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        }

    }

    private void printTree(Tree tree){
        List<Tree> children = tree.getChildrenAsList();
        for (Tree child: children) {
            child.label();
            child.taggedYield();
            if(child.isLeaf()){
                System.out.println(child);
            }
            else {
                printTree(child);
            }
        }
    }
}
