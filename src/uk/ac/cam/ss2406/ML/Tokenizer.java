package uk.ac.cam.ss2406.ML;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import edu.stanford.nlp.ling.Word;
import edu.stanford.nlp.process.PTBTokenizer;
import edu.stanford.nlp.process.WordTokenFactory;

public class Tokenizer {
    public static List<String> tokenize(String text) {
        List<String> result = new ArrayList<String>();
        try (StringReader reader = new StringReader(text)) {
            PTBTokenizer<Word> tokenizer = new PTBTokenizer<Word>(reader, new WordTokenFactory(),
                    "untokenizable=noneDelete");
            while (tokenizer.hasNext()) {
                String token = tokenizer.next().word().toLowerCase();
                result.add(token);
            }
        }
        return result;
    }

}