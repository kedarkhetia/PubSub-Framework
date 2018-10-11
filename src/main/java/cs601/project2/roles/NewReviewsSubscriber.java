package cs601.project2.roles;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import cs601.project2.broker.Broker;
import cs601.project2.model.Review;

/**
 * It will filter new reviews from obtained reviews and write
 * them to the file.
 * 
 * @author kmkhetia
 *
 */ 
public class NewReviewsSubscriber implements Subscriber<Review> {
	private Broker<Review> broker;
	private int baseUnixReviewTime;
	private BufferedWriter out;
	public int count = 0;
	
	public NewReviewsSubscriber(int baseUnixReviewTime, Path filePath, Broker<Review> broker) throws IOException {
		this.broker = broker;
		this.baseUnixReviewTime = baseUnixReviewTime;
		this.out = Files.newBufferedWriter(filePath, StandardCharsets.ISO_8859_1);
		this.broker.subscribe(this);
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
