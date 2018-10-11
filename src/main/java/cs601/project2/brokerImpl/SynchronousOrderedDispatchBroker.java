package cs601.project2.brokerImpl;

import java.util.LinkedList;
import java.util.List;

import cs601.project2.broker.Broker;
import cs601.project2.roles.Subscriber;

/**
 * SynchronousOrderedDispatchBroker is used to deliver events
 * that are published by publishers to subscribers in Synchronous
 * and in ordered manner.
 * 
 * @author kmkhetia
 *
 */ 
public class SynchronousOrderedDispatchBroker<T> implements Broker<T> {
	private List<Subscriber<T>> subscribers;
	
	/**
	 * Constructor for SynchronousOrderedDispatchBroker.
	 */
	public SynchronousOrderedDispatchBroker() {
		subscribers = new LinkedList<Subscriber<T>>();
	}
	
	/**
	 * Called by a publisher to publish a new item. The 
	 * item will be delivered to all current subscribers.
	 * 
	 * @param item
	 */
	@Override
	public synchronized void publish(T item) {
		//System.out.println("Received Data from publisher, pushing to subscriber.");
		for(Subscriber<T> subscriber : subscribers) {
			subscriber.onEvent(item);
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
	public synchronized void subscribe(Subscriber<T> subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Indicates this broker should stop accepting new
	 * items to be published and shut down all threads.
	 * The method will block until all items that have been
	 * published have been delivered to all subscribers.
	 */
	@Override
	public void shutdown() {}

}
