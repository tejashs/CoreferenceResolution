package ga.coreference.main;

import edu.stanford.nlp.trees.Tree;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by tejas on 06/11/16.
 */
public class CandidateNP {
    private Tree nounPhrase;
    private Tree sentenceRoot;

    CandidateNP(Tree nounPhrase, Tree sentenceRoot){
        this.nounPhrase = nounPhrase;
        this.sentenceRoot = sentenceRoot;
    }

    public Tree getNounPhrase(){
        return nounPhrase;
    }

    public Tree getSentenceRoot(){
        return sentenceRoot;
    }

    public static CandidateNP getCandidateNPFromTree(Tree tree, Tree sentence){
        return new CandidateNP(tree, sentence);
    }

    public static ArrayList<CandidateNP> getCandidateNPFromTree(List<Tree> listOfTrees, Tree sentence){
        ArrayList<CandidateNP> candidateNPsToReturn = new ArrayList<CandidateNP>();
        for(Tree tree : listOfTrees){
            candidateNPsToReturn.add(new CandidateNP(tree, sentence));
        }
        return candidateNPsToReturn;
    }
}
