package ga.coreference.main;

import edu.stanford.nlp.util.StringUtils;

import java.util.ArrayList;

/**
 * Created by tejas on 03/11/16.
 */
public class POSUtility {
    private static ArrayList<String> phrases;
    private static ArrayList<String> nounRelatedTags;
    static{
        phrases = new ArrayList<String>();
        phrases.add("S");
        phrases.add("SBAR");
        phrases.add("SBARQ");
        phrases.add("SINV");
        phrases.add("SQ");
        phrases.add("NP");
        phrases.add("VP");
        phrases.add("PP");
        phrases.add("ADJP");
        phrases.add("ADVP");
        phrases.add("CONJP");

        nounRelatedTags = new ArrayList<String>();
        nounRelatedTags.add("NP");
        nounRelatedTags.add("NN");
        nounRelatedTags.add("NNS");
        nounRelatedTags.add("NNP");
        nounRelatedTags.add("NNPS");
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
