package cs601.project2.collections;

import java.util.Date;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A Blocking Queue implementation that can be used by
 * Asynchronous broker to deliver message in an ordered
 * manner.
 * 
 * @author kmkhetia
 *
 */ 
public class AsyncBlockingQueue<T> {
	private T[] items;
	private int start;
	private int end;
	private int size;
	
	private final static Logger log = LogManager.getLogger(AsyncBlockingQueue.class);
	
	
	public AsyncBlockingQueue(int size) {
		this.items = (T[]) new Object[size];
		this.start = 0;
		this.end = - 1;
		this.size = 0;
		log.info("Initialized blocking queue");
	}
	
	/**
	 * The function is used to put the data into the queue.
	 * 
	 * @param item
	 */
	public synchronized void put(T item) {
		while(size == items.length) {
			try {
				this.wait();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}				
		int next = (end+1)%items.length;
		items[next] = item;
		end = next;		
		size++;
		if(size == 1) {
			this.notifyAll();
		}
	}
	
	/**
	 * Method is used to get the data from the queue. 
	 * If the queue is empty the method waits for timeout time
	 * to see if some thread puts the data on the queue, if the 
	 * queue still remains empty after timeout time 
	 * 
	 * @param timeout
	 */
	public synchronized T poll(long timeout) {
		try {
			if(size == 0) {
				Date now = new Date();
				long diff = (new Date()).getTime() - now.getTime();
				while(size == 0 && diff < timeout) {
					this.wait(timeout - diff);
					diff = (new Date()).getTime() - now.getTime();
				}
			}
			if(size == 0) {
				return null;
			}
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		T item = items[start];
		start = (start+1)%items.length;
		size--;
		if(size == items.length-1) {
			this.notifyAll();
		}
		return item;
	}
	
	/**
	 * Checks if queue is empty of not.
	 * 
	 */
	public synchronized boolean isEmpty() {
		return size == 0;
	}
}
