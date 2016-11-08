package ga.coreference.main;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.simple.Sentence;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.StringUtils;

import java.util.List;
import java.util.Properties;

/**
 * Created by tejas on 06/11/16.
 */
public class FeatureMatcher {

    public static void main(String[] args) {
        doesGenderMatch(null,null,null);
    }

    public static boolean doesFeatureMatch(Tree markedNode, Tree sentenceForMarkedNode, CandidateNP candidateNode, Tree sentenceForCandidateNode){
	    boolean completeFeatureMatch = false;
		String markedNodeText = TreeHelper.getInstance().getTextValueForTree(markedNode, true);
		String candidateNodeText = TreeHelper.getInstance().getTextValueForTree(candidateNode.getNounPhrase(), true);
		if(candidateNodeText.contains("COREF")){
			return false;
		}

        boolean NERMatch = false;
        boolean stringMatch = false;
        boolean numberMatch = false;
		if(doesStringMatch(markedNodeText, candidateNodeText)){
			stringMatch = true;
		}


    	//NER match
    	//NERMatch = doesNERMatch(markedNode, sentenceForMarkedNode, candidateNode);
    	
    	//Number agreement
        //numberMatch = doesNumberMatch(markedNode, sentenceForMarkedNode, candidateNode);

        //Gender match

        return stringMatch;
    }
	
	public static boolean doesFeatureMatch(Tree markedNode, CandidateNP candidateNode){
		String markedNodeText = TreeHelper.getInstance().getTextValueForTree(markedNode, true);
    	String candidateNodeText = TreeHelper.getInstance().getTextValueForTree(candidateNode.getNounPhrase(), true);
        if(candidateNodeText.contains("COREF")){
            return false;
        }
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
	
	private static boolean doesNERMatch(Tree markedNode, Tree markedNodeSentence, CandidateNP candidateNode){
        String markedNodeNERTag = "";
        String candidateNodeNERTag = "";
        List<Sentence> markedNodeSent = TreeHelper.getInstance().getSentenceForTree(markedNodeSentence, true);
        List<Sentence> candidateNodeSent = TreeHelper.getInstance().getSentenceForTree(candidateNode.getSentenceRoot(), true);
        for(Sentence sent1: markedNodeSent){
            markedNodeNERTag = TreeHelper.getInstance().findNERTagForNP(sent1, markedNode);
        }
        for(Sentence sent2: candidateNodeSent){
            candidateNodeNERTag = TreeHelper.getInstance().findNERTagForNP(sent2, candidateNode.getNounPhrase());
        }
        if(markedNodeNERTag.equals(candidateNodeNERTag)){
            return true;
        }
        return false;
	}

	private static boolean doesNumberMatch(Tree markedNode, Tree sentenceForMarkedNode, CandidateNP candidateNP){
	    boolean numberMatch = false;
        boolean isMarkedNodeSingular = false;
        boolean isCandidateNodeSingular = false;
        isMarkedNodeSingular = POSUtility.isSingular(sentenceForMarkedNode, markedNode);
        isCandidateNodeSingular = POSUtility.isSingular(candidateNP.getSentenceRoot(), candidateNP.getNounPhrase());
        if(isCandidateNodeSingular == isMarkedNodeSingular){
            numberMatch = true;
        }
        return numberMatch;
	}

	private static boolean doesGenderMatch(Tree markedNode, Tree sentenceForMarkedNode, CandidateNP candidateNP){
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize,ssplit,pos,parse,gender");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        Annotation document = new Annotation("Ellen has expressed concern\n" +
                "that a plane crash into a plutonium storage bunker at Pantex could\n" +
                "spread radioactive smoke for miles. Plutonium, a highly radioactive\n" +
                "element, causes cancer if inhaled");
        pipeline.annotate(document);

        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.print(token.value());
                System.out.print(", Gender: ");
                System.out.println(token.get(MachineReadingAnnotations.GenderAnnotation.class));
            }
        }
        return false;
    }
}
