package cs601.project2.roles;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs601.project2.broker.Broker;
import cs601.project2.model.Review;

/**
 * It will filter old reviews from obtained reviews and write
 * them to the file.
 * 
 * @author kmkhetia
 *
 */ 
public class OldReviewsSubscriber implements Subscriber<Review> {
	private int baseUnixReviewTime;
	private BufferedWriter out;
	private ReentrantLock lock;
	private static int count = 0;
	
	private final static Logger log = LogManager.getLogger(NewReviewsSubscriber.class);
	
	public OldReviewsSubscriber(int baseUnixReviewTime, Path filePath, Broker<Review> broker) throws IOException {
		this.baseUnixReviewTime = baseUnixReviewTime;
		this.out = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1);
		broker.subscribe(this);
		lock = new ReentrantLock();
		log.info("Subscribed OldReviewsSubscriber to broker.");
	}
	
	/**
	 * It will filter old reviews from obtained reviews by provided unixReviewTime
	 * and write them to the file.
	 * 
	 * @param item
	 */ 
	@Override
	public void onEvent(Review item) {
		if(item != null && item.getUnixReviewTime() <= baseUnixReviewTime) {
			try {
				lock.lock();
				count++;
				lock.unlock();
				out.write(item + "\n");
			} catch (IOException e) {
				log.error("Received IOException, ", e);
			}
		}
	}
	
	/**
	 * Returns count of items received.
	 * 
	 * @return count
	 */
	public static int getCount() {
		return count;
	}

}
