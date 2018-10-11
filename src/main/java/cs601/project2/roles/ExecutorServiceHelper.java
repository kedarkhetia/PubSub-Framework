package cs601.project2.roles;

import java.util.List;

/**
 * Class represents a job that will publish data to
 * subscribers and will be executed by ExecutorService
 * thread pool.
 * 
 * @author kmkhetia
 *
 */ 
public class ExecutorServiceHelper<T> implements Runnable {
	private List<Subscriber<T>> subscribers;
	private T item;
	 
	public ExecutorServiceHelper(List<Subscriber<T>> subscribers, T item) {
		this.subscribers = subscribers;
		this.item = item;
	}
	
	/**
	 * Method to publish data to all the subscribers.
	 */
	@Override
	public void run() {
		for(Subscriber<T> i : subscribers) {
			i.onEvent(item);
		}
	}

}
