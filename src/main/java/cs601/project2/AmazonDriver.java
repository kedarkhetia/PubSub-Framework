package cs601.project2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import cs601.project2.broker.Broker;
import cs601.project2.brokerImpl.AsyncOrderedDispatchBroker;
import cs601.project2.brokerImpl.AsyncUnorderedDispatchBroker;
import cs601.project2.brokerImpl.SynchronousOrderedDispatchBroker;
import cs601.project2.model.Config;
import cs601.project2.model.Review;
import cs601.project2.roles.NewReviewsSubscriber;
import cs601.project2.roles.OldReviewsSubscriber;
import cs601.project2.roles.Publisher;
import cs601.project2.roles.Subscriber;

/**
 * AmazonDriver is main which will parse the JSON config file 
 * and create required Publishers, Subscribers and Broker.
 * 
 * @author kmkhetia
 *
 */ 
public class AmazonDriver {
	/**
	 * It reads config file and create Broker, Subscriber and publishers based
	 * on the configuration provided in config.json.
	 * 
	 * Expected usage:
	 * 	java -cp project2.jar cs601.project2.AmazonDriver -c <Config json file>
	 * 
	 * Example:
	 * 	java -cp project2.jar cs601.project2.AmazonDriver -c config.json
	 * 
	 * @param args
	 * @throws IOException
	 * @return void
	 */
	public static void main(String[] args) throws IOException {
		Gson gson = new Gson();
		Config config = gson.fromJson(readFile(Paths.get(args[1])), Config.class);
		List<Thread> publisherThreads = new LinkedList<Thread>();
		File inputDirectory = new File(config.getInputDirectory());
		Broker<Review> broker = getBroker(config.getBrokerType());
		if(broker == null) {
			System.exit(0);
		}
		for(File file : inputDirectory.listFiles()) {
			publisherThreads.add(new Thread(new Publisher<Review>(Paths.get(file.getAbsolutePath()), Review.class, broker)));
		}
		new OldReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/OldReviews.json"), broker);
		new NewReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/NewReviews.json"), broker);
		long start = System.currentTimeMillis();
		execute(publisherThreads, broker);
		long end = System.currentTimeMillis();
		System.out.println((end-start) / 1000.0);
	}
	
	/**
	 * It will executed the created threads along with producers and subscribers
	 * 
	 * @param publisherThreads, broker
	 * @throws IOException
	 * @return void
	 */
	private static void execute(List<Thread> publisherThreads, Broker<Review> broker) throws IOException {
		Thread brokerThread = null;
		try {
			for(Thread i : publisherThreads) {
				i.start();
			}
			if(broker instanceof AsyncOrderedDispatchBroker) {
				brokerThread = new Thread((AsyncOrderedDispatchBroker) broker);
				brokerThread.start();
			}
			for(Thread i : publisherThreads) {
				i.join();
			}
			broker.shutdown();
			if(brokerThread != null) {
				brokerThread.join();
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * This function reads configuration file from the provided path.
	 * 
	 * @param path
	 * @throws IOException
	 * @return String
	 */
	public static String readFile(Path path) throws IOException {
		StringBuilder sb = new StringBuilder();
		BufferedReader in = Files.newBufferedReader(path);
		String line;
		while((line = in.readLine()) != null) {
			sb.append(line);
		}
		return sb.toString();
	}
	
	/**
	 * To decide which type of broker must be created for the execution.
	 * 
	 * @param broker
	 * @return Broker<Review>
	 */
	public static Broker<Review> getBroker(String broker) {
		if(broker.equals("SynchronousOrderedDispatchBroker")) {
			return new SynchronousOrderedDispatchBroker<Review>();
		}
		else if(broker.equals("AsyncOrderedDispatchBroker")) {
			return new AsyncOrderedDispatchBroker<Review>();
		}
		else if(broker.equals("AsyncUnorderedDispatchBroker")) {
			return new AsyncUnorderedDispatchBroker<Review>();
		}
		else {
			return null;
		}
	}
}