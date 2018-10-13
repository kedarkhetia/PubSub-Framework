package cs601.project2.brokerImpl;

import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.LinkedList;
import java.util.List;

import cs601.project2.broker.Broker;
import cs601.project2.roles.Subscriber;

/**
 * Class implements remote broker, which will register remote subscriber
 * and acts as a proxy Broker for remote subscriber.
 * 
 * @author kmkhetia
 *
 */ 
public class RemoteBroker<T> implements Broker<T>, Runnable {
	
	private List<Subscriber<T>> subscribers;
	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public RemoteBroker(String hostname, int port) throws UnknownHostException, IOException {
		this.subscribers = new LinkedList<Subscriber<T>>();
		this.client = new Socket(hostname, port);
		this.out = new ObjectOutputStream(client.getOutputStream());
		this.in = new ObjectInputStream(client.getInputStream());
	}
	
	/**
	 * It will deliver all published items to all 
	 * current subscribers.
	 * 
	 * @param item
	 */
	@Override
	public void publish(T item) {
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
	public void subscribe(Subscriber<T> subscriber) {
		subscribers.add(subscriber);
	}

	/**
	 * Indicates that communication between RemoteBroker 
	 * and RemoteSubscriber is completed and RemoteBroker
	 * can now disconnect.
	 */
	@Override
	public void shutdown() {
		try {
			in.close();
			out.close();
			client.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	/**
	 * It will receive all data published by RemoteSubscriber
	 * and pass it to publish to all the current subscribers of
	 * remote broker.
	 */
	@Override
	public void run() {
		T element;
		try {
			while((element = (T) in.readObject()) != null) {
				publish(element);
			}
		} catch (EOFException e) {
			
		} catch (ClassNotFoundException | IOException e) {
			e.printStackTrace();
		}
	}

}
