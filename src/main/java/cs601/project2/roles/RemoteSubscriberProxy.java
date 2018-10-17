package cs601.project2.roles;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.LinkedList;
import java.util.List;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import cs601.project2.broker.Broker;
import cs601.project2.model.Connection;

/**
 * Class implements remote subscriber, which will register
 * itself to local broker communicates with remote broker
 * to pass events.
 * 
 * @author kmkhetia
 *
 */ 
public class RemoteSubscriberProxy<T> implements Subscriber<T>, Runnable {
	private volatile boolean shutdownFlag;
	private ServerSocket server;
	private List<Connection> subscribers;
	private static int count = 0;
	
	private final static Logger log = LogManager.getLogger(RemoteSubscriberProxy.class);
	
	public RemoteSubscriberProxy(Broker<T> broker, int port) throws IOException {
		this.shutdownFlag = false;
		this.subscribers = new LinkedList<Connection>();
		this.server = new ServerSocket(port);
		broker.subscribe(this);
		log.info("Subscriber RemoteSubscriberProxy to Broker.");
		Socket client = server.accept();
		ObjectInputStream in = new ObjectInputStream(client.getInputStream());
		ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
		subscribers.add(new Connection(client, in, out));
		log.info("Accepted connection from=" + client);
	}
	
	/**
	 * Called by the Broker when a new item
	 * has been published.
	 * 
	 * @param item
	 */
	@Override
	public synchronized void onEvent(T item) {
		for(Connection subscriber : subscribers) {
			try {
				subscriber.getOut().writeObject(item);
			} catch (IOException e) {
				log.error("Received IOExcption, ", e);
			}
		}
		count++;
	}
	
	/**
	 * It allows multiple clients to connect with the server
	 * and create a thread for each of this new connections.
	 */
	@Override
	public void run() {
		while(!shutdownFlag) {
			try {
				Socket client = server.accept();
				ObjectOutputStream out = new ObjectOutputStream(client.getOutputStream());
				ObjectInputStream in = new ObjectInputStream(client.getInputStream());
				subscribers.add(new Connection(client, in, out));
				log.info("Accepted connection from=" + client);
			} catch (IOException e) {
				if(server.isClosed()) {
					log.debug("Server is now Closed.");
				}
				else {
					log.error("Received IOException, ", e);
				}
			}
		}
		
	}
	
	/**
	 * It closes all the existing client connections and 
	 * usually called after all the data has been passed
	 * to RemoteBroker. 
	 */
	public void shutdown() {
		log.info("Shutdown RemoteSubscriber called.");
		shutdownFlag = true;
		try {
			for(Connection subscriber : subscribers) {
				subscriber.getOut().close();
				subscriber.getIn().close();
				subscriber.getClient().close();
			}
			server.close();
			log.info("shutdownFlag=" + shutdownFlag);
		} catch (IOException e) {
			log.error("Received IOException, ", e);
		}
	}
	
	/**
	 * Returns count of items received.
	 * 
	 * @return count;
	 */
	public static int getCount() {
		return count;
	}
}
