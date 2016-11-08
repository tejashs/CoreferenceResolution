package ga.coreference.main;

import edu.stanford.nlp.trees.Tree;
import org.apache.log4j.Logger;
import org.w3c.dom.Node;

import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by tejas on 06/11/16.
 */
public class CandidateEvaluator {
    private ArrayList<Tree> coRefNodes;
    private ArrayList<Tree> sentenceParsedTrees;
    private HashMap<Tree, Tree> coRefPhraseTreeToSentenceMap;
    private HashMap<Tree, ArrayList<Tree>> sentenceToNPTerminalMap = new HashMap<Tree, ArrayList<Tree>>();
    private HashMap<Tree, ArrayList<CandidateNP>> coRefToCandidateNPMap = new HashMap<Tree, ArrayList<CandidateNP>>();
    private HashMap<Tree, ArrayList<CandidateNP>> coRefToSuccessCandidateMap = new HashMap<Tree, ArrayList<CandidateNP>>();
    private HashMap<Tree, Node> corefTreetoCorefNode = new HashMap<Tree, Node>();
    private String fileName;
    public CandidateEvaluator(ArrayList<Tree> coRefNodes, ArrayList<Tree> sentenceParsedTrees, HashMap<Tree, Tree> coRefPhraseTreeToSentenceMap,
                              HashMap<Tree, ArrayList<Tree>> sentenceToNPTerminalMap,
                              HashMap<Tree, ArrayList<CandidateNP>> coRefToCandidateNPMap, HashMap<Tree, Node> corefTreetoCorefNode, String fileName) {
        this.coRefNodes = coRefNodes;
        this.sentenceParsedTrees = sentenceParsedTrees;
        this.coRefPhraseTreeToSentenceMap = coRefPhraseTreeToSentenceMap;
        this.sentenceToNPTerminalMap = sentenceToNPTerminalMap;
        this.coRefToCandidateNPMap = coRefToCandidateNPMap;
        this.corefTreetoCorefNode = corefTreetoCorefNode;
        this.fileName = fileName;
    }


    public String evaluateCandidateNPsForCoRefs() throws IOException{
        if(coRefPhraseTreeToSentenceMap.size() != coRefToCandidateNPMap.size()){
            getLogger().debug("WTF IS WRONG");
        }

        for (Tree coRef: coRefNodes) {
            ArrayList<CandidateNP> candidateNPs = coRefToCandidateNPMap.get(coRef);
            //First Check in candidateNPs which are candidates from the same sentence.
            for (CandidateNP candidateNP:  candidateNPs) {
                boolean featureMatched = FeatureMatcher.doesFeatureMatch(coRef, coRefPhraseTreeToSentenceMap.get(coRef), candidateNP, candidateNP.getSentenceRoot());
                if(featureMatched){
                    if(TreeHelper.getInstance().getTextValueForTree(candidateNP.getNounPhrase(), true).length() == 0){
                        continue;
                    }
                    if(coRefToSuccessCandidateMap.containsKey(coRef)){
                        ArrayList<CandidateNP> successCandidates = coRefToSuccessCandidateMap.get(coRef);

                        successCandidates.add(candidateNP);
                        coRefToSuccessCandidateMap.put(coRef, successCandidates);
                    }
                    else {
                        ArrayList<CandidateNP> successCandidates = new ArrayList<CandidateNP>();
                        successCandidates.add(candidateNP);
                        coRefToSuccessCandidateMap.put(coRef, successCandidates);
                    }
                }
            }

            // Now check if any candidates were found.
            if(!coRefToSuccessCandidateMap.containsKey(coRef)){
                //No Success candidates were found
            }
            else {
                //At least come success candidates were found.
            }

        }

//        getLogger().debug("###############");
//        getLogger().debug("OUTPUT");
//        getLogger().debug("Total CORefTags:" + coRefNodes.size());
//        getLogger().debug("Total CORef To Success Map Size:" + coRefToSuccessCandidateMap.size());
//        for (Tree coRef:coRefToSuccessCandidateMap.keySet()) {
//            getLogger().info("-------------");
//            getLogger().info("COREFERENCE ANAPHORA : " + coRef.toString());
//            for (CandidateNP cNP: coRefToSuccessCandidateMap.get(coRef)) {
//                getLogger().info(cNP.getNounPhrase());
//            }
//            getLogger().info("-------------");
//        }
     
       return getOutputToPrint();
       

    }
    
//    public void printOutput() throws IOException{
//    	 PrintWriter out = new PrintWriter(new FileWriter(fileName+".response"));
//    	 out.println("<TXT>");
//
//         int j = 1;
//
//         for (Tree coRef:coRefToSuccessCandidateMap.keySet()) {
//        	 Node cn = corefTreetoCorefNode.get(coRef);
//        	 String ref = "";
//             ArrayList<CandidateNP> cand = coRefToSuccessCandidateMap.get(coRef);
//             for(int i = cand.size()-1; i >= 0; i--){
//             	//check if its a coref
//             	Tree cNP = cand.get(i).getNounPhrase();
//             	if(coRefPhraseTreeToSentenceMap.containsKey(cNP)){
//             		if(coRefToSuccessCandidateMap.containsKey(cNP)){
//             			continue;
//             		}
//             		Node n = corefTreetoCorefNode.get(cNP);
//             		if(n.getAttributes().getLength() == 1){
//
//             			out.println("<COREF ID=\""+n.getAttributes().item(0).getNodeValue()+"\">"+n.getTextContent()+"</COREF>");
//             			if(i == cand.size()-1) ref = n.getAttributes().item(0).getNodeValue();
//             		}
//             		else{
//             			out.println("<COREF ID=\""+n.getAttributes().item(0).getNodeValue()+"\" REF=\""+n.getAttributes().item(1).getNodeValue()+"\">"+n.getTextContent()+"</COREF>");
//             			if(i == cand.size()-1) ref = n.getAttributes().item(1).getNodeValue();
//             		}
//
//             	}
//             	else{
//             		out.println("<COREF ID=\"GA"+j+"\">"+TreeHelper.getInstance().getTextValueForTree(cNP, true)+"</COREF>");
//             		if(i == cand.size()-1) ref = "GA"+j;
//             		j++;
//             	}
//             }
//             out.println("<COREF ID=\""+cn.getAttributes().item(0).getNodeValue()+"\" REF=\""+ref+"\">"+cn.getTextContent()+"</COREF>");
//         }
//         out.println("</TXT>");
//         out.close();
//    }

    public String getOutputToPrint() throws IOException{
        StringBuilder builder = new StringBuilder();
        builder.append("<TXT>");
        builder.append("\n");
//        PrintWriter out = new PrintWriter(new FileWriter(fileName+".response"));
//        out.println("<TXT>");

        int xmlTagIDCounter = 1;

        for (Tree coRef:coRefToSuccessCandidateMap.keySet()) {
            Node coRefXMLNode = corefTreetoCorefNode.get(coRef);
            String ref = null;
            ArrayList<CandidateNP> candidateNPList = coRefToSuccessCandidateMap.get(coRef);
            HashMap<CandidateNP, String> candidateNPToXMLTextMap = new HashMap<CandidateNP, String>();
            for(int i = candidateNPList.size()-1; i >= 0; i--){
                //check if its a coref
                Tree cNP = candidateNPList.get(i).getNounPhrase();
                String xmlNodeTextToPrint = null;
                if(coRefToSuccessCandidateMap.containsKey(cNP)){
                    //TODO
                    Node xmlNode = corefTreetoCorefNode.get(cNP);
                    ref  = xmlNode.getAttributes().item(0).getNodeValue();
                }
                else {
                    String ID = "GA" + xmlTagIDCounter;
                    xmlTagIDCounter++;
                    if(i == candidateNPList.size()-1){
                        ref = ID;
                        builder.append(constructXMLNode(ID, null, cNP));
                        builder.append("\n");
                        //out.println(constructXMLNode(ID, null, cNP));
                    }
                    //else {
                      //  xmlNodeTextToPrint = constructXMLNode(ID, ref, cNP);
                    //}

                }
            }
            String x = constructXMLNode(coRefXMLNode.getAttributes().item(0).getNodeValue(), ref, coRefXMLNode.getTextContent());
            builder.append(x);
            builder.append("\n");
            //out.println(x);
        }
        builder.append("</TXT>");
        builder.append("\n");
        //out.println("</TXT>");
        //out.close();
        return builder.toString();
    }

    private String constructXMLNode(String IDtoAdd, String referenceTag, Tree tree){
        return  constructXMLNode(IDtoAdd, referenceTag, TreeHelper.getInstance().getTextValueForTree(tree, true));
    }

    private String constructXMLNode(String IDtoAdd, String referenceTag, String textContent){
        String xmlTextToSend = null;
        if(referenceTag == null){
            xmlTextToSend =  "<COREF ID=\""+ IDtoAdd + "\">"+textContent+"</COREF>";
        }
        else {
            xmlTextToSend =  "<COREF ID=\""+ IDtoAdd + "\" REF=\""+ referenceTag + "\">"+textContent+"</COREF>";
        }
        return xmlTextToSend;
    }


    private Logger getLogger() {
        return Logger.getLogger(CandidateEvaluator.class);
    }
}
