/**
 * Created by tejas on 30/10/16.
 */

import edu.stanford.nlp.simple.Document;
import edu.stanford.nlp.simple.Sentence;

public class Test2 {
    public static void main(String[] args) {
        // Create a document. No computation is done yet.
        //Document doc = new Document("John F Kennedy gifted a car to Mary");
        Document doc = new Document("Barack Obama, the president of the United States of America addressed the crowd.");
//        Document doc = new Document("The Navy suspended operations for its entire fleet\n" +
//                "of F-14 airplanes Thursday after the third \"Tomcat\" crash in a\n" +
//                "month  -  the second in a week");
        for (Sentence sent : doc.sentences()) {  // Will iterate over two sentences
            // We're only asking for words -- no need to load any models yet
            System.out.println("The second word of the sentence '" + sent + "' is " + sent.word(1));
            // When we ask for the lemma, it will load and run the part of speech tagger
            System.out.println("The third lemma of the sentence '" + sent + "' is " + sent.lemma(2));
            // When we ask for the parse, it will load and run the parser
            System.out.println("The parse of the sentence '" + sent + "' is " + sent.parse());
            // ...
            System.out.println("Named Entities of the sentence '" + sent + "' is " + sent.nerTags());
            //
            System.out.println("CORef of the sentence '" + sent + "' is " + sent.coref());
        }
    }
}
