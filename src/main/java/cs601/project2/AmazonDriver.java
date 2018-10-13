package cs601.project2;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.net.UnknownHostException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.LinkedList;
import java.util.List;

import com.google.gson.Gson;

import cs601.project2.broker.Broker;
import cs601.project2.brokerImpl.AsyncOrderedDispatchBroker;
import cs601.project2.brokerImpl.AsyncUnorderedDispatchBroker;
import cs601.project2.brokerImpl.RemoteBroker;
import cs601.project2.brokerImpl.SynchronousOrderedDispatchBroker;
import cs601.project2.model.Config;
import cs601.project2.model.Review;
import cs601.project2.roles.NewReviewsSubscriber;
import cs601.project2.roles.OldReviewsSubscriber;
import cs601.project2.roles.Publisher;
import cs601.project2.roles.RemoteSubscriberProxy;

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
	 * 	java -cp project2.jar cs601.project2.AmazonDriver -config <Config json file> -type <server, client, local>
	 * 
	 * Example:
	 * 	java -cp project2.jar cs601.project2.AmazonDriver -config config.json -type server
	 * 
	 * @param args
	 * @throws IOException
	 * @return void
	 */
	public static void main(String[] args) throws IOException {
		if(!validateInput(args)) {
			System.out.println("Invalid Argument passed. Please run command in below format.");
			System.out.println("java -cp project2.jar cs601.project2.AmazonDriver -config <Config json file> -type <server, client, local>");
			return;
		}
		Gson gson = new Gson();
		Config config = gson.fromJson(readFile(Paths.get(args[1])), Config.class);
		String type = args[3];
		if(type.equalsIgnoreCase("server")) {
			serverConfig(config);
		}
		else if(type.equalsIgnoreCase("client")){
			clientConfig(config);
		}
		else if(type.equalsIgnoreCase("local")){
			localConfig(config);
		}
		else {
			System.out.println("Invalid Type provided. Type must be either server, client or local");
		}
	}
	
	/**
	 * The function validates the input parameters passed 
	 * while executing the program.
	 * 
	 * @param args
	 * @return boolean
	 */
	private static boolean validateInput(String[] args) {
		if((args.length != 4) || !args[0].equals("-config") || !args[2].equals("-type")) {
			return false;
		}
		return true;
	}
	
	/**
	 * It configures publishers and subscribers for local execution.
	 * 
	 * @param config
	 * @throws IOException
	 */
	private static void localConfig(Config config) throws IOException {
		List<Thread> publisherThreads = new LinkedList<Thread>();
		File inputDirectory = new File(config.getInputDirectory());
		Broker<Review> broker = getBroker(config.getBrokerType());
		for(File file : inputDirectory.listFiles()) {
			publisherThreads.add(new Thread(new Publisher<Review>(Paths.get(file.getAbsolutePath()), Review.class, broker)));
		}
		for(String str : config.getLocalSubscribers()) {
			if(str.equals("NewReviewSubscriber")) {
				new NewReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/NewReviews.json"), broker);
			}
			else if(str.equals("OldReviewSubscriber")) {
				new OldReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/OldReviews.json"), broker);
			}
		}
		long start = System.currentTimeMillis();
		execute(publisherThreads, broker);
		long end = System.currentTimeMillis();
		System.out.println("Time: " + (end-start) / 1000.0 + " Milliseconds");
	}

	/**
	 * It configures server for execution.
	 * 
	 * @param config
	 * @throws IOException
	 */
	public static void serverConfig(Config config) throws IOException {
		List<Thread> publisherThreads = new LinkedList<Thread>();
		File inputDirectory = new File(config.getInputDirectory());
		Broker<Review> broker = getBroker(config.getBrokerType());
		for(File file : inputDirectory.listFiles()) {
			publisherThreads.add(new Thread(new Publisher<Review>(Paths.get(file.getAbsolutePath()), Review.class, broker)));
		}
		RemoteSubscriberProxy<Review> remoteSubscriber = new RemoteSubscriberProxy<Review>(broker, config.getPort());
		new Thread(remoteSubscriber).start();
		for(String str : config.getLocalSubscribers()) {
			if(str.equals("NewReviewSubscriber")) {
				new NewReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/NewReviews.json"), broker);
			}
			else if(str.equals("OldReviewSubscriber")) {
				new OldReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/OldReviews.json"), broker);
			}
		}
		long start = System.currentTimeMillis();
		execute(publisherThreads, broker);
		remoteSubscriber.shutdown();
		long end = System.currentTimeMillis();
		System.out.println((end-start) / 1000.0);
	}
	
	/**
	 * It configures client and execute it. 
	 * 
	 * @param config
	 * @throws UnknownHostException 
	 * @throws IOException
	 */
	public static void clientConfig(Config config) throws UnknownHostException, IOException {
		RemoteBroker<Review> remoteBroker = new RemoteBroker<Review>(config.getHost(), config.getPort());
		new Thread(remoteBroker).start();
		for(String str : config.getRemoteSubscribers()) {
			if(str.equals("NewReviewSubscriber")) {
				new NewReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/NewReviews.json"), remoteBroker);
			}
			else if(str.equals("OldReviewSubscriber")) {
				new OldReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/OldReviews.json"), remoteBroker);
			}
		}
	}
	
	/**
	 * It will executed the created threads along with producers and subscribers
	 * 
	 * @param publisherThreads, broker
	 * @throws IOException
	 */
	private static void execute(List<Thread> publisherThreads, Broker<Review> broker) throws IOException {
		Thread brokerThread = null;
		try {
			for(Thread i : publisherThreads) {
				i.start();
			}
			if(broker instanceof AsyncOrderedDispatchBroker) {
				brokerThread = new Thread((AsyncOrderedDispatchBroker<Review>) broker);
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