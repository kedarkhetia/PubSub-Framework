package cs601.project2.roles;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.log4j.Logger;

import cs601.project2.broker.Broker;
import cs601.project2.brokerImpl.AsyncUnorderedDispatchBroker;
import cs601.project2.model.Review;

/**
 * It will filter new reviews from obtained reviews and write
 * them to the file.
 * 
 * @author kmkhetia
 *
 */ 
public class NewReviewsSubscriber implements Subscriber<Review> {
	private int baseUnixReviewTime;
	private BufferedWriter out;
	
	private final static Logger log = Logger.getLogger(NewReviewsSubscriber.class);
	
	public NewReviewsSubscriber(int baseUnixReviewTime, Path filePath, Broker<Review> broker) throws IOException {
		this.baseUnixReviewTime = baseUnixReviewTime;
		this.out = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1);
		broker.subscribe(this);
		log.info("Subscribed NewReviewsSubscriber to broker.");
	}
	
	/**
	 * It will filter new reviews from obtained reviews by provided unixReviewTime
	 * and write them to the file.
	 * 
	 * @param item
	 */ 
	@Override
	public void onEvent(Review item) {
		if(item.getUnixReviewTime() > baseUnixReviewTime) {
			try {
				out.write(item + "\n");
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
}
