package ga.coreference.main;

import com.sun.org.apache.bcel.internal.generic.INSTANCEOF;
import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import javafx.geometry.Pos;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tejas on 03/11/16.
 */
public class TreeHelper {

    private static TreeHelper INSTANCE = new TreeHelper();

    private TreeHelper() {

    }

    public static TreeHelper getInstance() {
        return INSTANCE;
    }

    public Tree getTreeForCoRefTag(Node node, ArrayList<Tree> listOfTrees) {
        for (int i = 0; i < listOfTrees.size(); i++) {
            Tree sentence = listOfTrees.get(i);
            String sentenceString = getTextValueForTree(sentence, false);
            sentenceString = cleanupSentence(sentenceString);
            Tree tree;
            tree = getTreeForNode(node, sentence, false);
            if (tree != null) {
                Logger.getRootLogger().debug(tree);
            }

        }
        return null;
    }

    private Tree getTreeForNode(Node node, Tree sentence, boolean isIDFound) {
        for (Tree child : sentence.getChildrenAsList()) {
            if (POSUtility.checkIfTagIsNounRelated(child.label().value())) {
                //String textValue = child.toString();
                //TODO IGNORE CASE????
                String textValue;

                String nodeValueToCompare = "";
                if(isIDFound){
                    textValue = getTextValueForTree(child, true);
                    nodeValueToCompare = getNodeValueToCompare(node, false);
                }
                else {
                    textValue = getTextValueForTree(child, false);
                    nodeValueToCompare = getNodeValueToCompare(node, true);
                }
                if (nodeValueToCompare.equals(textValue)) {
                    return child;
                }
                if (textValue.contains(nodeValueToCompare)) {
                    Tree nodeToReturn = getTreeForNode(node, child, true);
                    return nodeToReturn;
                }
            } else if (!POSUtility.isTerminalTag(child.label().value())) {
                Tree tree = getTreeForNode(node, child, isIDFound);
                return tree;
            }
        }
        return null;
    }

    private String getTextValueForTree(Tree child, boolean skipTag) {
        List<Word> words = child.yieldWords();
        StringBuilder builder = new StringBuilder();
        for (Word w : words) {
            if(skipTag){
                if(w.value().contains("COREF")){
                    continue;
                }
            }
            builder.append(w.value().trim());
            builder.append(" ");
        }
        return builder.toString().trim();
    }

    private String cleanupSentence(String sentence) {
        sentence = sentence.replaceAll("\\.", " ");
        sentence = sentence.replaceAll("\\n", " ");
        return sentence.trim();
    }

    private String getNodeValueToCompare(Node node, boolean shouldCompareID) {
        StringBuilder sb = new StringBuilder();
        if(shouldCompareID) {
            sb.append(node.getAttributes().item(0).getNodeName());
            sb.append("=");
            sb.append("\"");
            sb.append(node.getAttributes().item(0).getNodeValue());
            sb.append("\"");
        }
        else {
            sb.append(node.getTextContent());
        }
        return sb.toString();
    }
}
