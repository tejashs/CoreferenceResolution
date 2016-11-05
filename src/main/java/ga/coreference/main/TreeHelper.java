package ga.coreference.main;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashSet;
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

    private String getTextValueForTree(Tree child, boolean skipCoRefTag) {
        List<Word> words = child.yieldWords();
        StringBuilder builder = new StringBuilder();
        for (Word w : words) {
            if(skipCoRefTag){
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
            sb.append(node.getTextContent().replaceAll("\\n", " "));
        }
        return sb.toString();
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

        if(!isNodeTextMultipleWords){
            return getParentForNode(leaves.get(foundIndex));
        }
        else {
            ArrayList<Tree> leavesToSend = new ArrayList<Tree>();
            leavesToSend.add(0, leaves.get(foundIndex));

            for (int i = 1; i < textArray.length ; i++) {
                Tree leaf = leaves.get(foundIndex + i);
                String leafText = leaf.label().value();
                if(!StringUtils.isAlphanumeric(leafText)){
                    continue;
                }
                if(StringUtils.stripNonAlphaNumerics(textArray[i]).equals(leafText)){
                    leavesToSend.add(i, leaf);
                }
            }
            //return getParentForLeaves(leavesToSend, new HashSet<Tree>());
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


//    private Tree getParentForLeaves(ArrayList<Tree> leaves, HashSet<Tree> parentSet){
//        if(parentSet.size() == 0){
//            for (Tree leaf: leaves) {
//                Tree parent = leaf.parent(sentenceRootNodeBeingTraversed);
//                parentSet.add(parent);
//            }
//        }
//
//        if(parentSet.size() == 1){
//            //Return the common parent
//            for (Tree leafToReturn: parentSet) {
//                return leafToReturn;
//            }
//        }
//        else {
//            ArrayList<Tree> children = new ArrayList<Tree>();
//            children.addAll(parentSet);
//            return getParentForLeaves(children, new HashSet<Tree>());
//        }
//        return null;
//    }

    private Tree getParentForNode(Tree node){
        return node.parent(sentenceRootNodeBeingTraversed);
    }
}
