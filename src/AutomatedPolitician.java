/**
 * Created by Samuel on 14/03/2017.
 */

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import uk.ac.cam.ss2406.ML.NaiveBayes;
import uk.ac.cam.ss2406.ML.Politician;
import uk.ac.cam.ss2406.ML.TwitterHMM;
import uk.ac.cam.ss2406.twitter.Tweet;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.lang.reflect.Type;
import java.util.*;

public class AutomatedPolitician {
    public static void main(String[] args){
        Gson gson = new Gson();
        System.out.println("DONALD TRUMP:");
        List<Tweet> all_trump_tweets = new ArrayList<>();
        for(int year=2009; year <= 2017; year++){
            FileReader reader = null;
            try {
                reader = new FileReader("data/trump_"+Integer.toString(year)+".json");
            }catch(FileNotFoundException e){
                System.out.println("Cannot find file.");
            }
            Type collectionType = new TypeToken<Collection<Tweet>>(){}.getType();
            Collection<Tweet> tweetCol = gson.fromJson(reader, collectionType);
            all_trump_tweets.addAll(tweetCol);
        }

        System.out.println("Collected all " + all_trump_tweets.size() + " tweets.");

        int max_trump_tweets = all_trump_tweets.size();
        List<Tweet> tweets = new ArrayList<>();
        for(int i=0; i<max_trump_tweets; i++){
            tweets.add(all_trump_tweets.get(i));
        }
        System.out.println("Truncated tweet list to " + max_trump_tweets + " tweets.");
        int order = 2;
        TwitterHMM hmm = new TwitterHMM(order, tweets);
        System.out.println("Generated HMM with order " + order);

        System.out.println("Generating 10 Trump tweets:");
        for(int i=1; i<=10; i++){
            String text = hmm.generateTweet();
            System.out.println(i + ": " + text);
        }

        System.out.println();
        System.out.println();
        System.out.println("SHERIFF CLARKE:");
        FileReader reader = null;
        try {
            reader = new FileReader("data/sheriffclarke.json");
        }catch(FileNotFoundException e){
            System.out.println("Cannot find file.");
        }
        Type collectionType = new TypeToken<Collection<Tweet>>(){}.getType();
        List<Tweet> clarke_tweets = gson.fromJson(reader, collectionType);
        System.out.println("Collected all " + clarke_tweets.size() + " tweets.");

        TwitterHMM hmm2 = new TwitterHMM(order, clarke_tweets);
        System.out.println("Generated HMM with order " + order);

        System.out.println("Generating 10 Clarke tweets:");
        for(int i=1; i<=10; i++){
            String text = hmm2.generateTweet();
            System.out.println(i + ": " + text);
        }

        System.out.println();
        System.out.println();
        //naive bayes
        //generate training data of 10k tweets for each
        System.out.println("Generating training dataset...");
        Map<String, Politician> training = new HashMap<>();
        for(int i = 0; i<10000;i++){
            training.put(hmm.generateTweet(), Politician.TRUMP);
            training.put(hmm2.generateTweet(), Politician.CLARKE);
        }

        NaiveBayes nb = new NaiveBayes();

        Map<Politician, Double> class_probs = nb.calculateClassProbabilities(training);
        Map<String, Map<Politician, Double>> smoothed_probs = nb.calculateSmoothedLogProbs(training);

        System.out.println("Generating test dataset...");
        //generate test set of 100 tweets for each
        Map<String, Politician> test = new HashMap<>();
        for(int i = 0; i<100;i++){
            test.put(hmm.generateTweet(), Politician.TRUMP);
            test.put(hmm2.generateTweet(), Politician.CLARKE);
        }

        Map<String, Politician> predicted_answers = nb.naiveBayes(test.keySet(), smoothed_probs, class_probs);
        for(Map.Entry<String, Politician> entry : test.entrySet()){
            System.out.println("Tweet: " + entry.getKey());
            System.out.println("Actual author: " + entry.getValue());
            System.out.println("Predicted author: " + predicted_answers.get(entry.getKey()));
        }

        System.out.println();
        System.out.println("Overall accuracy: " + nb.calculateAccuracy(test, predicted_answers));
    }
}
