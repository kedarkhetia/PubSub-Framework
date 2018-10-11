package cs601.project2.brokerImpl;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import cs601.project2.broker.Broker;
import cs601.project2.roles.ExecutorServiceHelper;
import cs601.project2.roles.Subscriber;

/**
 * AsyncUnorderedDispatchBroker is used to deliver events
 * that are published by publishers to subscribers, Asynchronously
 * and in an unordered manner.
 * 
 * @author kmkhetia
 *
 */ 
public class AsyncUnorderedDispatchBroker<T> implements Broker<T> {

	private volatile boolean shutdownFlag;
	private List<Subscriber<T>> subscribers;
 	private ExecutorService threadPool;
 	private final int POOL_SIZE = 7;
	
 	/**
	 * Constructor for AsyncUnorderedDispatchBroker.
	 */
	public AsyncUnorderedDispatchBroker() {
		this.subscribers = new LinkedList<Subscriber<T>>();
		this.shutdownFlag = false;
		this.threadPool = Executors.newFixedThreadPool(POOL_SIZE);
	}
	
	/**
	 * Called by a publisher to publish a new item. The 
	 * item will be delivered to all current subscribers.
	 * 
	 * @param item
	 */
	@Override
	public void publish(T item) {
			if(!shutdownFlag) {
				threadPool.execute(new ExecutorServiceHelper<T>(subscribers, item));
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
		threadPool.shutdown();
		try {
			threadPool.awaitTermination(300, TimeUnit.MILLISECONDS);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		shutdownFlag = true;
	}
}
