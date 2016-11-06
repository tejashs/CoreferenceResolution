package ga.coreference.main;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Hashtable;
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

    public Tree getTreeForCoRefTag(Node node, ArrayList<Tree> listOfTrees, HashMap<Tree, Tree> coRefPhraseTreeToRootMap) {
        Tree treeToFind = null;
        sentenceRootNodeBeingTraversed = null;
        for (int i = 0; i < listOfTrees.size(); i++) {
            Tree sentence = listOfTrees.get(i);
            sentenceRootNodeBeingTraversed = sentence;
            String sentenceString = getTextValueForTree(sentence, false);
            sentenceString = cleanupSentence(sentenceString);
            treeToFind = getTreeForNode(node, sentence);
            if (treeToFind != null) {
                coRefPhraseTreeToRootMap.put(treeToFind, sentence);
                //TreeTraversalUtility.getCandidateAntecedantList(treeToFind, sentence);
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
        int ballParkIndexToStart = getBallParkIndexToFindLeaf(node, root);
        if(ballParkIndexToStart == -1){
            ballParkIndexToStart = 0;
        }
        int foundIndex = -1;
        for (int i = ballParkIndexToStart; i < leaves.size(); i++) {
            Tree leaf = leaves.get(i);
            String[] leafValues = leaf.label().value().split("\\s");
            String leafValueToCompare = null;
            if(leafValues.length == 1){
                leafValueToCompare = StringUtils.stripNonAlphaNumerics(leafValues[0]);
            }
            else {
                StringBuilder builder = new StringBuilder();
                for (int j = 0; j < leafValues.length ; j++) {
                    builder.append(StringUtils.stripNonAlphaNumerics(leafValues[i]));
                    builder.append(" ");
                }
                leafValueToCompare = builder.toString().trim();
            }
            if(leafValueToCompare.equals(StringUtils.stripNonAlphaNumerics(textToFind))){
                foundIndex = i;
                break;
            }
        }
        if(foundIndex == -1){
            getLogger().info("**********************");
            getLogger().info("INDEX NOT FOUND FOR :");
            getLogger().info(nodeTextValue);
            getLogger().info("**********************");
            return null;
        }

        if(!isNodeTextMultipleWords){
            return getParentForNode(leaves.get(foundIndex));
        }
        else {
            ArrayList<Tree> leavesToSend = new ArrayList<Tree>();
            //leavesToSend.add(0, leaves.get(foundIndex));
            int arrayIndex = 0;
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
                leafText = StringUtils.stripNonAlphaNumerics(leafText);
                if((leafText == null) || (leafText.length() == 0)){
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

    private int getBallParkIndexToFindLeaf(Node node, Tree root) {
        List<Tree> leaves = root.getLeaves();
        int indexToSend = -1;
        String nodeValueToCompare = getNodeValueToCompare(node, true);
        for(int i=0; i<leaves.size(); i++){
            Tree leaf = leaves.get(i);
            if(leaf.label().value().contains(nodeValueToCompare)){
                indexToSend = i;
                break;
            }
        }
        return indexToSend;
    }

    private Tree getCommonParentForLeaves(ArrayList<Tree> leaves, Node node){
        String textToCompare = getNodeValueToCompare(node, false);
        Tree firstLeaf = leaves.get(0);
        Tree parent = getParentForNode(firstLeaf);
        return getParentForText(parent, textToCompare);
    }

    private Tree getParentForText(Tree parent, String textToCompare){
        String parentText = getTextValueForTree(parent, true);
        if(parentText == null){
            return null;
        }
        if(parentText.contains(textToCompare)){
            return parent;
        }
        else {
            parent = getParentForNode(parent);
            return getParentForText(parent, textToCompare);
        }
    }

    private String getTextValueForTree(Tree child, boolean skipCoRefTag) {
        if(child == null){
            return null;
        }
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

    private Logger getLogger(){
        return Logger.getLogger(TreeHelper.class);
    }
    
    public List<Sentence> getSentenceForTree(Tree tree, boolean skipCoRefTag){
    	List<Word> words = tree.yieldWords();
        StringBuilder builder = new StringBuilder();
        if(!skipCoRefTag){
            for (Word w : words) {
                builder.append(w.value().trim());
                builder.append(" ");
            }
            Document doc = new Document(builder.toString().trim());
            return doc.sentences();
        }
        else {
            for (Word w : words) {
                if(w.value().contains("COREF")){
                    continue;
                }
                String word = w.value();
                builder.append(word);
                builder.append(" ");
            }
            Document doc = new Document(builder.toString().trim());
            return doc.sentences();
        }
    }
    
    public String findNERTagForNP(Sentence sentence, Tree NP){
    	List<String> NERtags = sentence.nerTags();
    	List<String> words = sentence.words();
    	List<Word> NPwords = NP.yieldWords();
        StringBuilder builder = new StringBuilder();
        for (Word w : NPwords) {
            if(w.value().contains("COREF")){
                continue;
            }
            String word = w.value();
            builder.append(word);
            builder.append(" ");
        }
        String NPText = builder.toString().trim();
        String[] NPWords = NPText.split("\\s|\\n");
    	
    	Hashtable<String, Integer> NERTagsForNP = new Hashtable<String, Integer>();
    	
    	for(String NPWord: NPWords){
    		int indexOfNPWord = words.indexOf(NPWord);
    		String NERTagForNPWord = NERtags.get(indexOfNPWord);
    		if(NERTagsForNP.containsKey(NERTagForNPWord)){
    			return NERTagForNPWord;
    			
    		}
    		else{
    			NERTagsForNP.put(NERTagForNPWord, 1);
    		}
    	}
    	
    	String w = NPWords[NPWords.length-1];
    	int idx = words.indexOf(w);
    	String tag = NERtags.get(idx);
    	return tag;	
    }
}
