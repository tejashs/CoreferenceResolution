package ga.coreference.main;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CollectionUtils;

import java.util.List;

/**
 * Created by tejas on 06/11/16.
 */
public class CollectionUtility {
    public static final String COREF = "COREF";

    public static String getStringFromLeafNode(Tree node){
        List<Word> words = node.yieldWords();
        StringBuilder sb = new StringBuilder();
        for (Word word: words) {
            if(word.value().contains(COREF)){
                continue;
            }
            sb.append(word);
            if(!POSUtility.doesStringContainOnlySpecialCharacters(word.value())){
                sb.append(" ");
            }
        }
        return sb.toString().trim();
    }

}
