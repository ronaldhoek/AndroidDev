package nl.componentagro.feedparser;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;


public abstract class BaseFeedParser implements FeedParser {

	// names of the XML tags
	static final String CHANNEL = "channel";
	static final String PUB_DATE = "pubDate";
	static final String DESCRIPTION = "description";
	static final String LINK = "link";
	static final String TITLE = "title";
	static final String ITEM = "item";
	static final String IMAGE = "image";
	static final String URL = "url";

	
	private final URL feedUrl;
	protected String customdateformat;
	
	protected BaseFeedParser(String feedUrl){
		try {
			this.feedUrl = new URL(feedUrl);
		} catch (MalformedURLException e) {
			throw new RuntimeException(e);
		}
	}

	protected InputStream getInputStream() {
		try {
			return feedUrl.openConnection().getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	protected InputStream getInputStream(String postdata) {
		try {
			HttpURLConnection connection = (HttpURLConnection)feedUrl.openConnection();
			
			connection.setDoOutput(true);
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            connection.setRequestMethod("POST");
			
            OutputStreamWriter request = new OutputStreamWriter(connection.getOutputStream());
            request.write(postdata);
            request.flush();
            request.close(); 
            
			return connection.getInputStream();
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}	
	
	protected boolean hasCustomDateFormat() {
		return customdateformat.length() > 0;
	}
	
	public void setCustomDateFormat(String dateformat) {
		customdateformat = dateformat;
	}
}