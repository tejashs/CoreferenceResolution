package ga.coreference.main;

import edu.stanford.nlp.trees.Tree;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

/**
 * Created by tejas on 06/11/16.
 */
public class CandidateEvaluator {
    private ArrayList<Tree> sentenceParsedTrees;
    private HashMap<Tree, Tree> coRefPhraseTreeToSentenceMap;
    private HashMap<Tree, ArrayList<Tree>> sentenceToNPTerminalMap = new HashMap<Tree, ArrayList<Tree>>();
    private HashMap<Tree, ArrayList<CandidateNP>> coRefToCandidateNPMap = new HashMap<Tree, ArrayList<CandidateNP>>();
    private HashMap<Tree, ArrayList<CandidateNP>> coRefToSuccessCandidateMap = new HashMap<Tree, ArrayList<CandidateNP>>();

    public CandidateEvaluator(ArrayList<Tree> sentenceParsedTrees, HashMap<Tree, Tree> coRefPhraseTreeToSentenceMap, HashMap<Tree, ArrayList<Tree>> sentenceToNPTerminalMap, HashMap<Tree, ArrayList<CandidateNP>> coRefToCandidateNPMap) {
        this.sentenceParsedTrees = sentenceParsedTrees;
        this.coRefPhraseTreeToSentenceMap = coRefPhraseTreeToSentenceMap;
        this.sentenceToNPTerminalMap = sentenceToNPTerminalMap;
        this.coRefToCandidateNPMap = coRefToCandidateNPMap;
    }


    public void evaluateCandidateNPsForCoRefs(){
        if(coRefPhraseTreeToSentenceMap.size() != coRefToCandidateNPMap.size()){
            getLogger().debug("WTF IS WRONG");
        }

        Set<Tree> coRefs = coRefToCandidateNPMap.keySet();
        for (Tree coRef: coRefs) {
            ArrayList<CandidateNP> candidateNPs = coRefToCandidateNPMap.get(coRef);
            //First Check in candidateNPs which are candidates from the same sentence.
            for (CandidateNP candidateNP:  candidateNPs) {
                boolean featureMatched = FeatureMatcher.doesFeatureMatch(coRef, candidateNP);
                if(featureMatched){
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




    }

    private Logger getLogger() {
        return Logger.getLogger(CandidateEvaluator.class);
    }
}