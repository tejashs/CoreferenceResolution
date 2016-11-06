package ga.coreference.main;

import java.util.ArrayList;

/**
 * Created by tejas on 03/11/16.
 */
public class POSUtility {
    private static ArrayList<String> phrases;
    private static ArrayList<String> nounRelatedTags;
    static{
        phrases = new ArrayList<String>();
        //CLAUSES as Per PENN TREEBANK
        phrases.add("S");
        phrases.add("SBAR");
        phrases.add("SBARQ");
        phrases.add("SINV");
        phrases.add("SQ");
        //PHRASES as per PENN TREEBANK
        phrases.add("ADJP");
        phrases.add("ADVP");
        phrases.add("CONJP");
        phrases.add("FRAG");
        phrases.add("INTJ");
        phrases.add("LST");
        phrases.add("NAC");
        phrases.add("NP");
        phrases.add("NX");
        phrases.add("PP");
        phrases.add("PRN");
        phrases.add("PRP");
        phrases.add("QP");
        phrases.add("RRC");
        phrases.add("UCP");
        phrases.add("VP");
        phrases.add("WHADJP");
        phrases.add("WHAVP");
        phrases.add("WHNP");
        phrases.add("WHPP");
        phrases.add("X");


        nounRelatedTags = new ArrayList<String>();
        nounRelatedTags.add("NP");
        nounRelatedTags.add("NN");
        nounRelatedTags.add("NX");
        nounRelatedTags.add("NNS");
        nounRelatedTags.add("NNP");
        nounRelatedTags.add("NNPS");
        nounRelatedTags.add("WHNP");
    }

    public static boolean isTerminalTag(String tag){
        if(phrases.contains(tag)){
            return false;
        }
        else {
            return true;
        }
    }

    public static boolean checkIfTagIsNounRelated(String tag){
        return nounRelatedTags.contains(tag);
    }

    public static String stripCoRefTags(String text) {
        return text.replaceAll("\\<[^\\>]+\\>", "");
    }

    public static boolean doesStringContainOnlySpecialCharacters(String stringText){
        String splChrs = "-/@#$%^&_+=()" ;
        return stringText.matches("[" + splChrs + "]+");
    }
}
