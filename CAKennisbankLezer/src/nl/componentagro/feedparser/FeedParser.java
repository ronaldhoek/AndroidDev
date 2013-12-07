package nl.componentagro.feedparser;
import java.util.List;


public interface FeedParser {
	List<Message> parse();
	List<Message> parse(String postdata);
	void setCustomDateFormat(String dateformat);
}
