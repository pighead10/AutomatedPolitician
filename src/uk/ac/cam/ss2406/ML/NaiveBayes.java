package uk.ac.cam.ss2406.ML;

import uk.ac.cam.ss2406.twitter.Tweet;

import java.io.IOException;
import java.util.*;

import com.sun.javafx.fxml.expression.Expression;

/**
 * Created by Samuel on 16/03/2017.
 */
public class NaiveBayes {
    public double calculateAccuracy(Map<String, Politician> trueValues, Map<String, Politician> predictedValues){
        int total = 0;
        int correct = 0;
        for(Map.Entry<String,Politician> entry : predictedValues.entrySet()){
            String t = entry.getKey();
            if(trueValues.get(t) != null){
                total++;
                if(trueValues.get(t) == entry.getValue()){
                    correct++;
                }
            }
        }
        return (double)correct / (double)total;
    }

    public Map<Politician, Double> calculateClassProbabilities(Map<String, Politician> trainingSet) {
        Map<Politician, Double> count = new HashMap<>();
        for(Map.Entry<String, Politician> entry : trainingSet.entrySet()){
            count.putIfAbsent(entry.getValue(), 0.0);
            count.put(entry.getValue(), count.get(entry.getValue()) + 1.0);
        }
        for(Map.Entry<Politician, Double> entry : count.entrySet()){
            entry.setValue(entry.getValue() / (double)trainingSet.size());
        }
        return count;
    }

    public Map<String, Map<Politician, Double>> calculateSmoothedLogProbs(Map<String, Politician> trainingSet){
        Map<Politician, Integer> nwords = new HashMap<>();
        for(Politician p : Politician.values()){
            nwords.put(p, 0);
        }
        Map<String, Map<Politician, Integer>> wcount = new HashMap<>();
        HashSet<String> all_words = new HashSet<>();

        for(Map.Entry<String, Politician> entry : trainingSet.entrySet()){
            Politician p = entry.getValue();
            List<String> words = Tokenizer.tokenize(entry.getKey());
            nwords.put(p, nwords.get(p) + words.size());
            for(String w : words){
                all_words.add(w);
                if(!wcount.containsKey(w)){
                    Map<Politician, Integer> m = new HashMap<>();
                    for(Politician v : Politician.values()){
                        m.put(v, 1);
                        nwords.put(v, nwords.get(v) + 1);
                    }
                    wcount.put(w, m);
                }
                wcount.get(w).put(p, wcount.get(w).get(p) + 1);
            }
        }

        Map<String, Map<Politician, Double>> answer = new HashMap<>();
        for(String word : all_words){
            Map<Politician, Double> probs = new HashMap<>();
            Map<Politician, Integer> wmap = wcount.get(word);
            for(Politician p : Politician.values()){
                double prob = wmap.get(p) / (double)(nwords.get(p));
                probs.put(p, Math.log(prob));
            }
            answer.put(word, probs);
        }

        return answer;
    }

    public Map<String, Politician> naiveBayes(Set<String> testSet, Map<String, Map<Politician, Double>> tokenLogProbs,
                                             Map<Politician, Double> classProbabilities){
        List<Politician> classes = Arrays.asList(Politician.values());
        Map<String, Politician> answer = new HashMap<>();

        for(String tweet : testSet){
            List<String> words = Tokenizer.tokenize(tweet);
            Map<Politician, Double> args = new HashMap<>();
            for(Politician p : classes){
                double sum = classProbabilities.get(p);
                for(String word : words){
                    if(tokenLogProbs.containsKey(word) && tokenLogProbs.get(word).containsKey(p)){
                        sum += tokenLogProbs.get(word).get(p);
                    }
                }
                args.put(p, sum);
            }
            Politician p = null;
            double cmax = Double.NEGATIVE_INFINITY;
            for(Map.Entry<Politician, Double> entry : args.entrySet()){
                if(entry.getValue() >= cmax){
                    cmax = entry.getValue();
                    p = entry.getKey();
                }
            }
            answer.put(tweet, p);
        }
        return answer;
    }
}
