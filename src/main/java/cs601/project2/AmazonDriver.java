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

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

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
	private final static Logger log = LogManager.getLogger(AmazonDriver.class);
	
	/**
	 * It reads config file and create Broker, Subscriber and publishers based
	 * on the configuration provided in config.json.
	 * 
	 * Expected usage:
	 * 	java -Dlog4j.configurationFile=src/main/resources/log4j2.properties -cp project2.jar cs601.project2.AmazonDriver - config <configFile Path> -type <server, client, local>
	 * 
	 * Example:
	 * 	 java -Dlog4j.configurationFile=src/main/resources/log4j2.properties -cp project2.jar cs601.project2.AmazonDriver -config config.json -type local
	 * 
	 * @param args
	 * @throws IOException
	 * @return void
	 */
	public static void main(String[] args) {
		if(!validateInput(args)) {
			System.out.println("Invalid Argument passed. Please run command in below format.");
			System.out.println("java -cp project2.jar cs601.project2.AmazonDriver -config <Config json file> -type <server, client, local>");
			log.debug("Invalid argument passed to server! args=" + args);
			return;
		}
		try {
			Gson gson = new Gson();
			Config config = gson.fromJson(readFile(Paths.get(args[1])), Config.class);
			log.info("Read config file, config=" + config);
			String type = args[3];
			if(type.equalsIgnoreCase("server")) {
				log.info("Configuring Server.");
				serverConfig(config);
			}
			else if(type.equalsIgnoreCase("client")){
				log.info("Configuring Client.");
				clientConfig(config);
			}
			else if(type.equalsIgnoreCase("local")){
				log.info("Running locally.");
				localConfig(config);
			}
			else {
				log.debug("Unknown type provided as argument! type=" + type);
				log.debug("Invalid Type provided. Type must be either server, client or local.");
				System.out.println("Invalid Type provided. Type must be either server, client or local.");
			}
		} catch (IOException e) {
			log.error("Received IOException, ", e);
		}
	}
	
	/**
	 * Prints the Object processed by Publishers, Subscribers and RemoteSubscribers.
	 */
	private static void print() {
		System.out.println("Publisher Published: " + Publisher.getCount() + " Objects");
		System.out.println("RemoteSubscriber Processed: " + RemoteSubscriberProxy.getCount() + " Objects");
		System.out.println("NewReviewSubscriber Processed: " + NewReviewsSubscriber.getCount() + " Objects");
		System.out.println("OldReviewSubscriber Processed: " + OldReviewsSubscriber.getCount() + " Objects"); 
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
		log.info("Starting broker=" + config.getBrokerType());
		for(File file : inputDirectory.listFiles()) {
			log.info("Creating publisher for file=" + file.getName());
			publisherThreads.add(new Thread(new Publisher<Review>(Paths.get(file.getAbsolutePath()), Review.class, broker)));
		}
		configSubscribers(config, broker, config.getLocalSubscribers());
		long start = System.currentTimeMillis();
		log.info("Start time of execution, startTime=" + start);
		execute(publisherThreads, broker);
		long end = System.currentTimeMillis();
		log.info("End time of execution, endTime=" + end);
		System.out.println("Time: " + (end-start) / 1000.0 + " Seconds");
		log.info("Time: " + (end-start) / 1000.0 + " Seconds");
		print();
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
		log.info("Starting broker=" + config.getBrokerType());
		for(File file : inputDirectory.listFiles()) {
			log.info("Creating publisher for file=" + file.getName());
			publisherThreads.add(new Thread(new Publisher<Review>(Paths.get(file.getAbsolutePath()), Review.class, broker)));
		}
		RemoteSubscriberProxy<Review> remoteSubscriber = new RemoteSubscriberProxy<Review>(broker, config.getPort());
		new Thread(remoteSubscriber).start();
		configSubscribers(config, broker, config.getLocalSubscribers());
		long start = System.currentTimeMillis();
		log.info("Start time of execution, startTime=" + start);
		execute(publisherThreads, broker);
		remoteSubscriber.shutdown();
		long end = System.currentTimeMillis();
		log.info("End time of execution, endTime=" + end);
		System.out.println((end-start) / 1000.0 + " Seconds");
		log.info("Time: " + (end-start) / 1000.0 + " Seconds");
		print();
	}
	
	/**
	 * It configures client and execute it. 
	 * 
	 * @param config
	 * @throws UnknownHostException 
	 * @throws IOException
	 */
	public static void clientConfig(Config config) throws IOException {
		log.info("Starting broker=RemoteBroker");
		RemoteBroker<Review> remoteBroker = new RemoteBroker<Review>(config.getHost(), config.getPort());
		new Thread(remoteBroker).start();
		configSubscribers(config, remoteBroker, config.getRemoteSubscribers());
	}
	
	/**
	 * It configures remote and local subscribers.
	 * 
	 * @throws IOException 
	 * @throws NumberFormatException 
	 */
	public static void configSubscribers(Config config, Broker<Review> broker, String[] subscribers) throws NumberFormatException, IOException {
		for(String str : subscribers) {
			if(str.equals("NewReviewSubscriber")) {
				log.info("Creating new Review Remote Subscriber");
				new NewReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/NewReviews.json"), broker);
			}
			else if(str.equals("OldReviewSubscriber")) {
				log.info("Creating old Review Remote Subscriber");
				new OldReviewsSubscriber(Integer.parseInt(config.getUnixReviewTime()) , Paths.get(config.getOutputDirectory() + "/OldReviews.json"), broker);
			}
		}
	}
	
	/**
	 * It will executed the created threads along with producers and subscribers
	 * 
	 * @param publisherThreads, broker
	 * @throws IOException
	 */
	private static void execute(List<Thread> publisherThreads, Broker<Review> broker) {
		Thread brokerThread = null;
		log.info("Execution Started!");
		try {
			log.info("Starting publisher Threads.");
			for(Thread i : publisherThreads) {
				i.start();
			}
			log.info("Checking broker type to initialize helper threads.");
			if(broker instanceof AsyncOrderedDispatchBroker) {
				log.info("Starting thread.");
				brokerThread = new Thread((AsyncOrderedDispatchBroker<Review>) broker);
				brokerThread.start();
			}
			log.info("joining all publisher threads.");
			for(Thread i : publisherThreads) {
				i.join();
			}
			log.info("Publisher published all data.");
			broker.shutdown();
			log.info("Shutdown broker.");
			if(brokerThread != null) {
				log.info("Joining broker");
				brokerThread.join();
			}
		} catch (InterruptedException e) {
			log.error("Received InterruptedException, ", e);
		}
	}
	
	/**
	 * This function reads configuration file from the provided path.
	 * 
	 * @param path
	 * @throws IOException
	 * @return String
	 */
	public static String readFile(Path path) {
		StringBuilder sb = new StringBuilder();
		try {
			BufferedReader in = Files.newBufferedReader(path);
			String line;
			while((line = in.readLine()) != null) {
				sb.append(line);
			}
		} catch (IOException e) {
			log.error("Please check provided File IOException occured, ", e);
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
			log.debug("Unidentified Broker!");
			System.exit(0);
		}
		return null;
	}
}