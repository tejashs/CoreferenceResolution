package ga.coreference.main;

import edu.stanford.nlp.trees.Tree;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tejas on 05/11/16.
 */
public class TreeTraversalUtility {

    public static ArrayList<Tree> getCandidateAntecedantList(Tree nounPhraseNodeToStart, Tree sentenceNode){
//        Tree someNode = nounPhraseNodeToStart.parent(sentenceNode).parent(sentenceNode).parent(sentenceNode).getChild(0).getChild(0);
//        List<Tree> list = sentenceNode.pathNodeToNode(nounPhraseNodeToStart, someNode);
//        Tree npParentTree = getNPOrSParent(nounPhraseNodeToStart, sentenceNode);
//        ArrayList<Tree> list2 = new ArrayList<Tree>();
//        list2.add(npParentTree);
//        List<Tree> listOfNPTrees = getNPOrSNodes(list2);
//        System.out.println(listOfNPTrees.size());
//        listOfNPTrees.remove(nounPhraseNodeToStart);
//        System.out.println(listOfNPTrees.size());


        //List<Tree> listOfTreesUptil = getNodesInTreeUptilNode(nounPhraseNodeToStart.getLeaves().get(nounPhraseNodeToStart.getLeaves().size()-1).parent(sentenceNode), sentenceNode);
        //ArrayList<Tree> terminalNPNodes = getTerminalNPNodes(sentenceNode, new ArrayList<Tree>());
       // print(terminalNPNodes);
        return null;
    }

    public static void print(ArrayList<Tree> nodes){
        Logger.getRootLogger().debug("#### PRINTING LIST OF NODES");
        for (Tree node: nodes) {
            Logger.getRootLogger().debug(node.toString());
        }
    }

    private static Tree getNPOrSParent(Tree markedNode, Tree rootNode){
        Tree parent = markedNode.parent(rootNode).parent(rootNode);
        while((!parent.label().value().equals("NP")) && (!parent.label().value().equals("S"))){
            return getNPOrSParent(parent, rootNode);
        }
        return parent;
    }

    private static ArrayList<Tree> getStuff(Tree markedNode, Tree rootNode){
        //TODO
        ArrayList<Tree> allNPSNodes = new ArrayList<Tree>();
        while ((markedNode.parent(rootNode) != null) || !markedNode.parent(rootNode).equals(rootNode)){
            Tree parent = markedNode.parent(rootNode);
            ArrayList<Tree> npNodes = getNPOrSNodes(getNodesInTreeUptilNode(parent, rootNode));
            markedNode = parent;
        }
        return  allNPSNodes;
    }



    public static ArrayList<Tree> getNodesInTreeUptilNode(Tree markedNode, Tree rootNode){
        //WORKING
        List<Tree> children = markedNode.parent(rootNode).getChildrenAsList();
        int indexOfMarkedNode = -1;
        for (int i = 0; i < children.size() ; i++) {
            Tree tree = children.get(i);
            if(tree.equals(markedNode)){
                indexOfMarkedNode = i;
                break;
            }
        }
        ArrayList<Tree> listToReturn = new ArrayList<Tree>();
        if(indexOfMarkedNode != -1){
            listToReturn.addAll(children.subList(0,indexOfMarkedNode));
        }
        return  listToReturn;
    }

    private static ArrayList<Tree> getNPOrSNodes(ArrayList<Tree> nodeList){
        ArrayList<Tree> listToReturn = new ArrayList<Tree>();
        for (Tree tree: nodeList) {
            ArrayList<Tree> list = new ArrayList<Tree>();
            getNPOrSNodes(tree, list);
            listToReturn.addAll(list);
        }
        return listToReturn;
    }

    private static void getNPOrSNodes(Tree tree, ArrayList<Tree> listToReturn){
        for (Tree child: tree.getChildrenAsList()) {
            if(!child.isLeaf()){
                String tag = child.label().value();
                if(tag.equals("NP")){
                    listToReturn.add(child);
                }
                if(!POSUtility.isTerminalTag(tag)){
                    getNPOrSNodes(child, listToReturn);
                }
            }
        }
    }

    public static ArrayList<Tree> getTerminalNPNodes(Tree node, ArrayList<Tree> terminalNPNodesToReturn){
        List<Tree> children = node.getChildrenAsList();
        for (Tree child: children) {
            if(doesNodeContainsNonTerminals(child)){
                getTerminalNPNodes(child, terminalNPNodesToReturn);
            }
            else {
                if(child.label().value().equals("NP")){
                    terminalNPNodesToReturn.add(child);
                }

            }
        }
        return terminalNPNodesToReturn;
    }

    private static boolean doesNodeContainsNonTerminals(Tree node) {
        for (Tree child: node.getChildrenAsList()) {
            if(!child.isLeaf()){
                if(!POSUtility.isTerminalTag(child.label().value())){
                    return true;
                }
            }
        }
        return false;
    }

}
