package cs601.project2.roles;

import java.util.concurrent.ConcurrentLinkedQueue;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs601.project2.AmazonDriver;

/**
 * Class represents a job that will publish data to
 * subscribers and will be executed by ExecutorService
 * thread pool.
 * 
 * @author kmkhetia
 *
 */ 
public class ExecutorServiceHelper<T> implements Runnable {
	private ConcurrentLinkedQueue<Subscriber<T>> subscribers;
	private T item;
	
	private final static Logger log = LogManager.getLogger(AmazonDriver.class);
	 
	public ExecutorServiceHelper(ConcurrentLinkedQueue<Subscriber<T>> subscribers, T item) {
		this.subscribers = subscribers;
		this.item = item;
	}
	
	/**
	 * Method to publish data to all the subscribers.
	 */
	@Override
	public void run() {
		for(Subscriber<T> i : subscribers) {
			if(item == null) {
				log.debug("Received null event");
			}
			i.onEvent(item);
		}
	}

}
