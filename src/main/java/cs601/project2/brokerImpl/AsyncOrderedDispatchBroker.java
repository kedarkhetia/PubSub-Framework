package cs601.project2.brokerImpl;

import java.util.LinkedList;
import java.util.List;

import org.apache.log4j.Logger;

import cs601.project2.broker.Broker;
import cs601.project2.collections.AsyncBlockingQueue;
import cs601.project2.roles.Subscriber;

/**
 * AsyncOrderedDispatchBroker is used to deliver events
 * that are published by publishers to subscribers, Asynchronously
 * and in ordered manner.
 * 
 * @author kmkhetia
 *
 */ 
public class AsyncOrderedDispatchBroker<T> implements Broker<T>, Runnable {
	private volatile boolean shutdownFlag;
	private List<Subscriber<T>> subscribers;
	private AsyncBlockingQueue<T> blockingQueue;
	private int QUEUE_SIZE = 1000;
	
	private final static Logger log = Logger.getLogger(AsyncOrderedDispatchBroker.class);
	
	/**
	 * Constructor for AsyncOrderedDispatchBroker.
	 */
	public AsyncOrderedDispatchBroker() {
		subscribers = new LinkedList<Subscriber<T>>();
		log.info("Created Blocking queue with size=" + QUEUE_SIZE);
		blockingQueue = new AsyncBlockingQueue<T>(QUEUE_SIZE);
		shutdownFlag = false;
	}
	
	/**
	 * Run method that will send published data
	 * Asynchronously.
	 * 
	 * @param item
	 */
	@Override
	public void run() {
		T element = blockingQueue.poll(300);
		log.info("Starting to publish data on blocking queue.");
		while(!shutdownFlag || !blockingQueue.isEmpty()) {
			if(element != null) {
				for(Subscriber<T> i : subscribers) {
					i.onEvent(element);
				}
			}
			element = blockingQueue.poll(300);
		}
		log.info("Completed publishing data to blocking queue.");
	}
	
	/**
	 * Called by a publisher to publish a new item. The 
	 * item will be delivered to all current subscribers.
	 * 
	 * @param item
	 */
	@Override
	public synchronized void publish(T item) {
		if(!shutdownFlag) {
			blockingQueue.put(item);
		}
	}
	
	/**
	 * Called once by each subscriber. Subscriber will be 
	 * registered and receive notification of all future
	 * published items.
	 * 
	 * @param subscriber
	 */
	@Override
	public void subscribe(Subscriber<T> subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Indicates this broker should stop accepting new
	 * items to be published and shut down all threads.
	 * The method will block until all items that have been
	 * published have been delivered to all subscribers.
	 */
	@Override
	public void shutdown() {
		log.info("Shutdown broker called");
		shutdownFlag = true;
		log.info("shutdownFlag=" + shutdownFlag);
	}
}
