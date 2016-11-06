package ga.coreference.main;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tejas on 03/11/16.
 */
public class TreeHelper {

    private static TreeHelper INSTANCE = new TreeHelper();
    private Tree sentenceRootNodeBeingTraversed;

    private TreeHelper() {

    }

    public static TreeHelper getInstance() {
        return INSTANCE;
    }

    public Tree getTreeForCoRefTag(Node node, ArrayList<Tree> listOfTrees) {
        Tree treeToFind = null;
        sentenceRootNodeBeingTraversed = null;
        for (int i = 0; i < listOfTrees.size(); i++) {
            Tree sentence = listOfTrees.get(i);
            sentenceRootNodeBeingTraversed = sentence;
            String sentenceString = getTextValueForTree(sentence, false);
            sentenceString = cleanupSentence(sentenceString);
            treeToFind = getTreeForNode(node, sentence);
            if (treeToFind != null) {
                break;
            }
            else {
                continue;
            }
        }
        return treeToFind;
    }

    private Tree getTreeForNode(Node node, Tree sentence) {
        for (Tree child : sentence.getChildrenAsList()) {
            if (POSUtility.checkIfTagIsNounRelated(child.label().value())) {
                //String textValue = child.toString();
                //TODO IGNORE CASE????
                String textValueOfChild;
                String nodeValueToCompare = "";
                textValueOfChild = getTextValueForTree(child, false);
                nodeValueToCompare = getNodeValueToCompare(node, true);

                if (textValueOfChild.contains(nodeValueToCompare)) {
                    Tree nodeToReturn = narrowDownOnLeafNode(node, sentenceRootNodeBeingTraversed);
                    return nodeToReturn;
                }
            } else if (!POSUtility.isTerminalTag(child.label().value())) {
                Tree tree = getTreeForNode(node, child);
                if(tree != null){
                    return tree;
                }
            }
        }
        return null;
    }

    private Tree narrowDownOnLeafNode(Node node, Tree root){
        String nodeTextValue = node.getTextContent();
        String[] textArray = nodeTextValue.split("\\s|\\n");
        String textToFind = textArray[0];
        boolean isNodeTextMultipleWords = false;
        if(textArray.length > 1){
            //Node text contains more than one word
            isNodeTextMultipleWords = true;
        }

        List<Tree> leaves = root.getLeaves();
        int foundIndex = -1;
        for (int i = 0; i < leaves.size(); i++) {
            Tree leaf = leaves.get(i);
            if(leaf.label().value().equals(StringUtils.stripNonAlphaNumerics(textToFind))){
                foundIndex = i;
                break;
            }
        }
        if(foundIndex == -1){
            Logger.getRootLogger().info("**********************");
            Logger.getRootLogger().info("INDEX NOT FOUND FOR :");
            Logger.getRootLogger().info(nodeTextValue);
            Logger.getRootLogger().info("**********************");
            return null;
        }

        if(!isNodeTextMultipleWords){
            return getParentForNode(leaves.get(foundIndex));
        }
        else {
            ArrayList<Tree> leavesToSend = new ArrayList<Tree>();
            leavesToSend.add(0, leaves.get(foundIndex));
            int arrayIndex = 1;
            int tempIndex = 0;
            for (Tree leaf : leaves) {
                if(tempIndex != foundIndex){
                    tempIndex++;
                    //So that we can start comparison directly from found index
                    continue;
                }
                if(arrayIndex == textArray.length){
                    break;
                }
                String leafText = leaf.label().value();
                if(!StringUtils.isAlphanumeric(leafText)){
                    continue;
                }
                if(StringUtils.stripNonAlphaNumerics(textArray[arrayIndex]).equals(leafText)){
                    leavesToSend.add(arrayIndex, leaf);
                    arrayIndex++;
                }
            }
            return getCommonParentForLeaves(leavesToSend, node);
        }
    }

    private Tree getCommonParentForLeaves(ArrayList<Tree> leaves, Node node){
        String textToCompare = getNodeValueToCompare(node, false);
        Tree firstLeaf = leaves.get(0);
        Tree parent = getParentForNode(firstLeaf);
        return getParentForText(parent, textToCompare);
    }

    private Tree getParentForText(Tree parent, String textToCompare){
        String parentText = getTextValueForTree(parent, true);
        if(parentText.contains(textToCompare)){
            return parent;
        }
        else {
            parent = getParentForNode(parent);
            return getParentForText(parent, textToCompare);
        }
    }

    private String getTextValueForTree(Tree child, boolean skipCoRefTag) {
        List<Word> words = child.yieldWords();
        StringBuilder builder = new StringBuilder();
        if(!skipCoRefTag){
            //This is for finding valid CoRef ID. So anything would work as long as it has ID.
            for (Word w : words) {
                builder.append(w.value().trim());
                builder.append(" ");
            }
            return builder.toString().trim();
        }
        else {
            for (Word w : words) {
                if(w.value().contains("COREF")){
                    continue;
                }
                String word = StringUtils.stripNonAlphaNumerics(w.value());
                builder.append(word);
                if(word != null && word.length() > 0){
                    builder.append(" ");
                }
            }
            return builder.toString().trim();
        }
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
            String nodeTextValue = node.getTextContent();
            String[] textArray = nodeTextValue.split("\\s|\\n");
            for (int i = 0; i < textArray.length ; i++) {
                sb.append(StringUtils.stripNonAlphaNumerics(textArray[i]));
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

    private String cleanupSentence(String sentence) {
        sentence = sentence.replaceAll("\\.", " ");
        sentence = sentence.replaceAll("\\n", " ");
        return sentence.trim();
    }
    private Tree getParentForNode(Tree node){
        return node.parent(sentenceRootNodeBeingTraversed);
    }
}
