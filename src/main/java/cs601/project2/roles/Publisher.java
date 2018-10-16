package cs601.project2.roles;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cs601.project2.broker.Broker;
import cs601.project2.brokerImpl.AsyncUnorderedDispatchBroker;

/**
 * It will read the publisher file convert them to Review object
 * and publish it to the Message Broker.
 * 
 * @author kmkhetia
 * 
 */ 
public class Publisher<T> implements Runnable {
	private Path filePath;
	private Class<?> type;
	private Broker<T> broker;
	
	private final static Logger log = LogManager.getLogger(Publisher.class);
	
	public Publisher(Path filePath, Class<?> type, Broker<T> broker) {
		this.filePath = filePath;
		this.type = type;
		this.broker = broker;
		log.info("Initialized publisher for file=" + filePath.getFileName());
	}
	
	/**
	 * It will read the publisher file convert them to Review object
	 * and publish it to the Message Broker.
	 * 
	 */ 
	@Override
	public void run() {
		try {
			BufferedReader in = Files.newBufferedReader(filePath, StandardCharsets.ISO_8859_1);
			String data;
			Gson gson = new Gson();
			while((data = in.readLine()) != null) {
				try {
					broker.publish((T) gson.fromJson(data, type));
				} catch(JsonSyntaxException e) {
					log.debug("Received JsonSyntaxException, ", e);
				}
			}
			in.close();
		} catch (IOException e) {
			log.error("Received IOException, ", e);
		}
	}
}
