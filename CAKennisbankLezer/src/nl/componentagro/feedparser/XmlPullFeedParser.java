package nl.componentagro.feedparser;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

import org.xmlpull.v1.XmlPullParser;

import android.util.Log;
import android.util.Xml;

public class XmlPullFeedParser extends BaseFeedParser {

	private class Channel {
		 private ImageRef image;

		public ImageRef getImage() {
			if (image == null) {
				image = new ImageRef();
			}
			return image;
		}
	}
	
	public XmlPullFeedParser(String feedUrl) {
		super(feedUrl);
	}

	private void parseImageNode(XmlPullParser parser, ImageRef image) throws Exception {
		int eventType = parser.getEventType();
		boolean done = false;
		while (eventType != XmlPullParser.END_DOCUMENT && !done) {
			switch (eventType) {	
				case XmlPullParser.START_TAG:
					if (parser.getName().equalsIgnoreCase(URL)) {
						image.url = parser.nextText();
					} else if (parser.getName().equalsIgnoreCase(TITLE)) {
						image.title = parser.nextText();
					}					
					break;
				case XmlPullParser.END_TAG:		
					if (parser.getName().equalsIgnoreCase(IMAGE)) {
						done = true;
					}
					break;
			}
			eventType = parser.next();
		}
	}
	
	private List<Message> parseStream(InputStream stream) {
		List<Message> messages = null;
		XmlPullParser parser = Xml.newPullParser();
		try {
			// auto-detect the encoding from the stream
			parser.setInput(stream, null);
			int eventType = parser.getEventType();
			Message currentMessage = null;
			Channel currentchannel = null;
			boolean done = false;
			while (eventType != XmlPullParser.END_DOCUMENT && !done){
				String name = null;
				switch (eventType){
					case XmlPullParser.START_DOCUMENT:
						messages = new ArrayList<Message>();
						break;
					case XmlPullParser.START_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(CHANNEL)) {
							currentchannel = new Channel();
						} else if (currentchannel != null) {
							if (name.equalsIgnoreCase(IMAGE)) {
								// image retrieval
								if (currentMessage != null) {
									parseImageNode(parser, currentMessage.getImage());
								} else {
									parseImageNode(parser, currentchannel.getImage());
								}
							} else if (name.equalsIgnoreCase(ITEM)) {
								currentMessage = new Message();
								// Apply a custom dateformat to the message  (If needed)
								if (hasCustomDateFormat()) {
									currentMessage.setDateFormat(customdateformat);
								}
							} else if (currentMessage != null) {
								if (name.equalsIgnoreCase(LINK)) {
									currentMessage.setLink(parser.nextText());
								} else if (name.equalsIgnoreCase(DESCRIPTION)){
									currentMessage.setDescription(parser.nextText());
								} else if (name.equalsIgnoreCase(PUB_DATE)){
									currentMessage.setDateStr(parser.nextText());
								} else if (name.equalsIgnoreCase(TITLE)){
									currentMessage.setTitle(parser.nextText());
								}	
							}							
						}
						break;
					case XmlPullParser.END_TAG:
						name = parser.getName();
						if (name.equalsIgnoreCase(ITEM) && currentMessage != null) {
							if (currentchannel != null && !currentMessage.hasImage()) {
								currentMessage.setImage(currentchannel.getImage());
							}
							messages.add(currentMessage);
							currentMessage = null;
						} else if (name.equalsIgnoreCase(CHANNEL) && currentchannel != null){
							currentchannel = null;
						}
						break;
				}
				eventType = parser.next();
			}
		} catch (Exception e) {
			Log.e("AndroidNews::PullFeedParser", e.getMessage(), e);
			throw new RuntimeException(e);
		}
		return messages;		
	}
	
	public List<Message> parse() {
		return parseStream(this.getInputStream());
	}
	
	public List<Message> parse(String postdata) {
		return parseStream(this.getInputStream(postdata));
	}
}
