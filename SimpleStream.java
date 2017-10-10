import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;

import javax.net.ssl.HttpsURLConnection;

import twitter4j.FilterQuery;
import twitter4j.StallWarning;
import twitter4j.Status;
import twitter4j.StatusDeletionNotice;
import twitter4j.StatusListener;
import twitter4j.TwitterStream;
import twitter4j.TwitterStreamFactory;
import twitter4j.User;
import twitter4j.conf.ConfigurationBuilder;

public class SimpleStream {

	/**
	 * Using the positive and negative words from https://github.com/jeffreybreen/twitter-sentiment-analysis-tutorial-201107/tree/master/data/opinion-lexicon-English
	 * 
	 */
	public static boolean includeSentimentAnalysis = true;
	
	String[] sentimentPositiveWords = null;
	String[] sentimentNegativeWords = null;
	
	public SimpleStream() {
		//void readInSentimentInfo(sentimentPositiveWords, sentimentNegativeWords);
		if(SimpleStream.includeSentimentAnalysis){
			//Read in the positive words file
			try{
				List positiveLines = Files.readAllLines(Paths.get("sentiment-positive-words.txt"), Charset.defaultCharset());
				sentimentPositiveWords = (String[]) positiveLines.toArray(new String[positiveLines.size()]);
			}catch(Exception e){
				System.out.println("Attempting to read in Positive words file 'sentiment-positive-words.txt' but failed.  Turning off sentiment route.");
				e.printStackTrace();
				SimpleStream.includeSentimentAnalysis = false;
			}
			
			//Read in the negative words file
			try{
				List negativeLines = Files.readAllLines(Paths.get("sentiment-negative-words.txt"), Charset.defaultCharset());
				sentimentNegativeWords = (String[]) negativeLines.toArray(new String[negativeLines.size()]);
			}catch(Exception e){
				System.out.println("Attempting to read in Positive words file 'sentiment-negative-words.txt' but failed.  Turning off sentiment route.");
				e.printStackTrace();
				SimpleStream.includeSentimentAnalysis = false;
			}
			
		}
	}
	
	
	public static void main(String[] args) {
		
		/**
		 * Simply reads the Twitter feed for keywords.  See the bottom of the file:
		 * {"newrelic","New Relic","Ireland"};      enter multiple search terms like this.
		 */
		
		ConfigurationBuilder cb = new ConfigurationBuilder();
        cb.setDebugEnabled(true);
        cb.setOAuthConsumerKey("CHANGE_ME");
        cb.setOAuthConsumerSecret("CHANGE_ME");
        cb.setOAuthAccessToken("CHANGE_ME");
        cb.setOAuthAccessTokenSecret("CHANGE_ME");

        TwitterStream twitterStream = new TwitterStreamFactory(cb.build()).getInstance();

        StatusListener listener = new StatusListener() {

        	
        	SimpleStream stream = new SimpleStream();
        	

            @Override
            public void onException(Exception arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onDeletionNotice(StatusDeletionNotice arg0) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onScrubGeo(long arg0, long arg1) {
                // TODO Auto-generated method stub

            }

            @Override
            public void onStatus(Status status) {
                User user = status.getUser();
                
                System.out.println("\n NEW TWEET ++++++++++++++");
                // gets Username
                String username = status.getUser().getScreenName();
                //System.out.println("username = "+username);
                String profileLocation = user.getLocation();
                //System.out.println("profileLocation = "+profileLocation);
                long tweetId = status.getId(); 
                //System.out.println("tweetId = "+tweetId);
                String content = status.getText();
                //System.out.println("content = "+content);
                String createdAt = status.getCreatedAt().toString();
                //System.out.println("createdAt = "+createdAt);
                int favoriteCount = status.getFavoriteCount();
                //System.out.println("favoriteCount = "+favoriteCount);
                int retweetCount = status.getRetweetCount();
                //System.out.println("retweetCount = "+retweetCount);
                
                String sentiment = "na";
                if(includeSentimentAnalysis){
                	if(stringContainsItemFromList(content, stream.sentimentPositiveWords)){
                		sentiment = "positive";
                	}else if(stringContainsItemFromList(content, stream.sentimentNegativeWords)){
                		sentiment = "negative";
                	}else{
                		sentiment = "none";
                	}
                }
                
                //System.out.println(status);
                //System.out.println("\n");
                //System.out.println("\n");
                
                String MyJSON = "[{";
                
                //MyJSON +="\"<>\":\""+<>+"\",";
                MyJSON +="\"eventType\":\"twittest\",";
                MyJSON +="\"createdAt\":\""+createdAt+"\",";
                MyJSON +="\"username\":\""+username+"\",";
                MyJSON +="\"profileLocation\":\""+profileLocation+"\",";
                MyJSON +="\"tweetId\":"+tweetId+",";
                MyJSON +="\"content\":\""+content+"\",";
                MyJSON +="\"sentiment\":\""+sentiment+"\",";
                MyJSON +="\"favoriteCount\":"+favoriteCount+",";
                MyJSON +="\"retweetCount\":"+retweetCount+"";
                
                MyJSON += "}]";
                
                System.out.println(MyJSON);
                
                try {
					stream.sendPost(MyJSON);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					System.err.println("Cannot send the following JSON doc to Inisghts:" + MyJSON);
					e.printStackTrace();
				}

            }

            @Override
            public void onTrackLimitationNotice(int arg0) {
                // TODO Auto-generated method stub

            }

			@Override
			public void onStallWarning(StallWarning arg0) {
				// TODO Auto-generated method stub
				
			}

        };
        
        //String keywords[] = {"newrelic","New Relic","Berlin","Zalando","Bauer","@sweetlew","#NRUG","#AWSSummit"};
        String keywords[] = {"newrelic","New Relic","Zalando","Bauer","@sweetlew","#NRUG","#AWSSummit","@AWS"};
        
        //get the query strings from a file called 'tweetfilter.ini'
        try {
        	
        	/**
			for (String line : Files.readAllLines(Paths.get("tweetfilter.ini"), null)) {
			    if(!line.startsWith("#")){
			    	keywordsFromINI = line;
			    }
			}
			*/
        	
        	List<String> lines = Files.readAllLines(Paths.get("tweetfilter.ini"), Charset.defaultCharset());
        	keywords = lines.toArray(new String[lines.size()]);
        	
        	
		} catch (IOException e) {
			// TODO Auto-generated catch block
			System.err.println("Unable to read config file:  Looking for \"tweetfilter.ini\" in same directory as Jar.");
			e.printStackTrace();
		}
        
        
        
        FilterQuery fq = new FilterQuery();
    

        System.out.println("TWITTEST by Manesh Tailor \n  [STARTING] \n The Keywords we are going to subscribe to are: "+ Arrays.toString(keywords));

        fq.track(keywords);

        twitterStream.addListener(listener);
        twitterStream.filter(fq); 

	}
	
	
	
	// HTTP POST request to New Relic Insights
		private void sendPost(String content) throws Exception {
	 
			String url = "https://insights-collector.newrelic.com/v1/accounts/739886/events";
			URL obj = new URL(url);
			HttpsURLConnection con = (HttpsURLConnection) obj.openConnection();
	 
			//add reuqest header
			con.setRequestMethod("POST");
			con.setRequestProperty("Content-Type", "application/json");
			con.setRequestProperty("X-Insert-Key", "CHANGE_ME");
	 
			String urlParameters = content;
	 
			// Send post request
			con.setDoOutput(true);
			DataOutputStream wr = new DataOutputStream(con.getOutputStream());
			wr.writeBytes(urlParameters);
			wr.flush();
			wr.close();
	 
			int responseCode = con.getResponseCode();
			System.out.println("\nSending 'POST' request to URL : " + url);
			System.out.println("Post parameters : " + urlParameters);
			System.out.println("Response Code : " + responseCode);
	 
			BufferedReader in = new BufferedReader(
			        new InputStreamReader(con.getInputStream()));
			String inputLine;
			StringBuffer response = new StringBuffer();
	 
			while ((inputLine = in.readLine()) != null) {
				response.append(inputLine);
			}
			in.close();
	 
			//print result
			System.out.println(response.toString());
		}
		
		/*
		public static void readInSentimentInfo(String[] positiveWords, String[] negativeWords){
			if(SimpleStream.includeSentimentAnalysis){
				//Read in the positive words file
				try{
					List positiveLines = Files.readAllLines(Paths.get("sentiment-positive-words.txt"), Charset.defaultCharset());
					positiveWords = (String[]) positiveLines.toArray(new String[positiveLines.size()]);
				}catch(Exception e){
					System.out.println("Attempting to read in Positive words file 'sentiment-positive-words.txt' but failed.  Turning off sentiment route.");
					e.printStackTrace();
					SimpleStream.includeSentimentAnalysis = false;
				}
				
				//Read in the negative words file
				try{
					List negativeLines = Files.readAllLines(Paths.get("sentiment-negative-words.txt"), Charset.defaultCharset());
					negativeWords = (String[]) negativeLines.toArray(new String[negativeLines.size()]);
				}catch(Exception e){
					System.out.println("Attempting to read in Positive words file 'sentiment-negative-words.txt' but failed.  Turning off sentiment route.");
					e.printStackTrace();
					SimpleStream.includeSentimentAnalysis = false;
				}
				
			}
		}
		*/
		
		
		public static boolean stringContainsItemFromList(String inputString, String[] items)
		{
		    for(int i =0; i < items.length; i++)
		    {
		        if(inputString.contains(items[i]))
		        {
		            return true;
		        }
		    }
		    return false;
		}

}
