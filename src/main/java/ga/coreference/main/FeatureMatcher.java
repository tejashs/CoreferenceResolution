package ga.coreference.main;

import java.util.DoubleSummaryStatistics;

import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.StringUtils;

/**
 * Created by tejas on 06/11/16.
 */
public class FeatureMatcher {
	/*
	public static int doesFeatureMatch(Tree markedNode, Tree markedNodeRoot, Tree candidateNode, Tree candidateNodeRoot){
    	boolean NERMatch = false;
    	boolean stringMatch = false;
    	boolean numberMatch = false;
    	int score = 0;
    	
        //String match
    	String markedNodeText = TreeHelper.getInstance().getTextValueForTree(markedNode, true);
    	String candidateNodeText = TreeHelper.getInstance().getTextValueForTree(candidateNode, true);
    	if(markedNodeText.equalsIgnoreCase(candidateNodeText)) return 100;
    	if(candidateNodeText.matches(markedNodeText) || StringUtils.find(markedNodeText, candidateNodeText)){
    		stringMatch = true;
    	}
    	
    	//NER match
    	String markedNodeNERTag = "";
    	String candidateNodeNERTag = "";
    	List<Sentence> markedNodeSent = TreeHelper.getInstance().getSentenceForTree(markedNode, true);
    	List<Sentence> candidateNodeSent = TreeHelper.getInstance().getSentenceForTree(candidateNode, true);
    	for(Sentence sent1: markedNodeSent){
    		markedNodeNERTag = TreeHelper.getInstance().findNERTagForNP(sent1, markedNode);
    	}
    	for(Sentence sent2: candidateNodeSent){
    		candidateNodeNERTag = TreeHelper.getInstance().findNERTagForNP(sent2, candidateNode);
    	}
    	if(markedNodeNERTag.equals(candidateNodeNERTag)){
    		NERMatch = true;
    	}
    	
    	//Number agreement
    	boolean isMarkedNodeSingular = false;
    	boolean isCandidateNodeSingular = false;
    	isMarkedNodeSingular = POSUtility.isSingular(markedNodeRoot, markedNode);
    	isCandidateNodeSingular = POSUtility.isSingular(candidateNodeRoot, candidateNode);
    	if(isCandidateNodeSingular == isMarkedNodeSingular){
    		numberMatch = true;
    	}
    	
    	
    	if(stringMatch){
    		if(NERMatch){
    			if(numberMatch){
    				return 90;
    			}
    			else{
    				score = 75;
    			}
    		}
    		else{
    			score = 50;
    		}
    	}
    	
    	if(NERMatch){
    		if(numberMatch){
    			score = 75;
    		}
    		else{
    			score = 50;
    		}
    	}
    	
    	return score;
    }*/
	
	public static boolean doesFeatureMatch(Tree markedNode, CandidateNP candidateNode){
		String markedNodeText = TreeHelper.getInstance().getTextValueForTree(markedNode, true);
    	String candidateNodeText = TreeHelper.getInstance().getTextValueForTree(candidateNode.getNounPhrase(), true);
    	if(doesStringMatch(markedNodeText, candidateNodeText)){
    		return true;
    	}
    	return false;
	}
	
	private static boolean doesStringMatch(String markedNodeText, String candidateNodeText){
    	if(markedNodeText.equalsIgnoreCase(candidateNodeText)) return true;
    	else if(candidateNodeText.matches(markedNodeText) || StringUtils.find(markedNodeText, candidateNodeText)){
    		return true;
    	}
    	return false;
	}
	
//	private boolean doesNERMatch(Tree markedNode, Tree candidateNode){
//		
//	}
//	
//	private boolean doesNumberMatch(Tree markedNode, Tree candidateNode){
//		
//	}
}
