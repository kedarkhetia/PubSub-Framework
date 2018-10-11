package cs601.project2.roles;

import java.io.BufferedReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;

import com.google.gson.Gson;
import com.google.gson.JsonSyntaxException;

import cs601.project2.broker.Broker;

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
	
	public Publisher(Path filePath, Class<?> type, Broker<T> broker) {
		this.filePath = filePath;
		this.type = type;
		this.broker = broker;
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
			//int count = 0; // Remove this
			while((data = in.readLine()) != null) {
				try {
					//count++; // Remove this
					broker.publish((T) gson.fromJson(data, type));
					//if(count > 100000) break; // Remove this
				} catch(JsonSyntaxException e) {
					//Ignoring JsonSyntaxException
					//System.out.println(e.getMessage());
				}
			}
			in.close();
		} catch (IOException e) {
			System.out.println(e.getMessage());
		}
	}
}
