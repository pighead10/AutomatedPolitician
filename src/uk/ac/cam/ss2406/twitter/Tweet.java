package uk.ac.cam.ss2406.twitter;

/**
 * Created by Samuel on 15/03/2017.
 */
public class Tweet {
    private String id_str;
    private boolean is_retweet;
    private String text;
    private String source;
    private int retweet_count;
    private int favorite_count;
    private String created_at;

    public Tweet(){
    }

    public Tweet(String id_str, boolean is_retweet, String text, String source,
                 int retweet_count, int favorite_count, String created_at){
        this.id_str = id_str;
        this.is_retweet = is_retweet;
        this.text = text;
        this.source = source;
        this.retweet_count = retweet_count;
        this.favorite_count = favorite_count;
        this.created_at = created_at;
    }

    public String getIdStr(){
        return id_str;
    }

    public boolean isRetweet(){
        return is_retweet;
    }

    public String getText(){
        return text;
    }

    public String getSource(){
        return source;
    }

    public int getRetweetCount(){
        return retweet_count;
    }

    public int getFavoriteCount(){
        return favorite_count;
    }

    public String getCreatedAt(){
        return created_at;
    }
}
