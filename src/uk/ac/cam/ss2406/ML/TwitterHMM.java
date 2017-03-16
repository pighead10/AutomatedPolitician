package uk.ac.cam.ss2406.ML;

import uk.ac.cam.ss2406.twitter.Tweet;

import java.util.*;

/**
 * Created by Samuel on 15/03/2017.
 */
public class TwitterHMM {
    private int order;
    private Map<String, Map<String, Double>> transition_matrix;
    private Map<String, Double> initial_probs;
    private Map<String, Double> final_probs;
    private List<String> words;

    public TwitterHMM(int order, List<Tweet> tweets){
        this.words = new ArrayList<>();
        this.order = order;
        generateTransitionMatrix(tweets);
    }

    public String generateTweet(){
        List<String> words = new ArrayList<>();

        //pick starting k words
        double n = Math.random();
        double rt = 0.0;

        int charcount = 0;
        boolean found = false;
        String key_found = null;
        for(Map.Entry<String, Double> entry : initial_probs.entrySet()){
            rt += entry.getValue();
            if(n <= rt){
                found = true;
                key_found = entry.getKey();
                words.addAll(Arrays.asList(entry.getKey().split(" ")));
                break;
            }
        }
        boolean tweeting = true;

        while(tweeting){
            //determine whether the tweet should end
            n = Math.random();
            List<String> kwords = null;
            try {
                kwords = words.subList(words.size() - order, words.size());
            }catch(Exception e){
                System.out.println("");
            }
            String wordstr = null;
            try {
                wordstr = String.join(" ", kwords);
            }catch(Exception e){
                System.out.println("");
            }
            double prob = final_probs.getOrDefault(wordstr, 0.0);

            //Naive method to attempt to end tweets in time
            if((charcount > 70 && prob > 0.0) || n <= prob || charcount > 110){
                tweeting = false;
            }else{
                n = Math.random();
                Map<String, Double> next = transition_matrix.get(wordstr);
                if(next != null) {
                    rt = 0.0;
                    for (Map.Entry<String, Double> entry : next.entrySet()) {
                        rt += entry.getValue();
                        if (n <= rt) {
                            charcount += entry.getKey().length();
                            words.add(entry.getKey());
                            break;
                        }
                    }
                }else{
                    //No transitions from wordstr in training data. Smoothing would solve this, but we didn't smooth.
                    //So pick a word randomly. (Issue: this can just lead to a chain of no transition cases).
                    int index = 0;
                    try {
                        index = new Random().nextInt(words.size());
                    }catch(Exception e){
                        System.out.println(key_found);
                        System.out.println("wtf");
                    }
                    charcount += words.get(index).length();
                    words.add(words.get(index));
                }
            }
        }
        return String.join(" ", words);
    }

    private void generateTransitionMatrix(List<Tweet> tweets){
        transition_matrix = new HashMap<>();
        initial_probs = new HashMap<>();
        final_probs = new HashMap<>();

        Map<String, Integer> transitions_from = new HashMap<>();

        int tweet_count = tweets.size();
        for(Tweet tweet : tweets){
            String text = tweet.getText();
            List<String> raw_words = Arrays.asList(text.split(" "));

            //Ignore @ replies and retweets (which are often normal tweets that start with speech marks).
            if(raw_words.size() > 0 && raw_words.get(0).length() > 0
                    && (raw_words.get(0).startsWith("@") || raw_words.get(0).startsWith("\""))
                    || tweet.isRetweet())
            {
                tweet_count--;
                continue;
            }

            //Delete any instances of two consecutive spaces.
            //Also, links are annoying, remove them.
            List<String> words = new ArrayList<>();
            for(String word : raw_words){
                if(word.contains("http") || word.equals(" ") || word.equals("")){
                    continue;
                }
                String w = word.replaceAll("  "," ");
                words.add(w);
            }
            this.words.addAll(words);
            int index = order;

            //initial count
            if(index < words.size()){
                List<String> kwords = words.subList(0, index);
                String wordstr = String.join(" ",kwords);
                if(wordstr.equals(" ")){
                    System.out.println("wtf??");
                }
                initial_probs.putIfAbsent(wordstr, 0.0);
                initial_probs.put(wordstr, initial_probs.get(wordstr)+1.0);
            }else{
                //tweet too short
                tweet_count--;
                continue;
            }

            //transition count
            while(index < words.size()){
                String word = words.get(index);
                List<String> kwords = words.subList(index-order, index);
                String wordstr = String.join(" ", kwords);

                transition_matrix.putIfAbsent(wordstr, new HashMap<>());
                transition_matrix.get(wordstr).putIfAbsent(word, 0.0);
                transition_matrix.get(wordstr).put(word, transition_matrix.get(wordstr).get(word) + 1.0);

                transitions_from.putIfAbsent(wordstr, 0);
                transitions_from.put(wordstr, transitions_from.get(wordstr)+1);
                index++;
            }

            //final count
            List<String> kwords = words.subList(index - order, index);
            String wordstr = String.join(" ",kwords);
            final_probs.putIfAbsent(wordstr, 0.0);
            final_probs.put(wordstr, final_probs.get(wordstr) + 1.0);
        }

        //divide by total number tweets to get start/final probabilities
        for(Map.Entry<String, Double> entry : initial_probs.entrySet()){
            entry.setValue(entry.getValue() / (double)tweet_count);
        }
        for(Map.Entry<String, Double> entry : final_probs.entrySet()){
            double p = entry.getValue() / (double)tweet_count;
            entry.setValue(p);
        }

        //divide by total number of transitions from to get transition probabilities
        for(Map.Entry<String, Map<String, Double>> trans_from : transition_matrix.entrySet()){
            for(Map.Entry<String, Double> trans_to : trans_from.getValue().entrySet()){
                trans_to.setValue(trans_to.getValue() / transitions_from.get(trans_from.getKey()));
            }
        }
    }
}
