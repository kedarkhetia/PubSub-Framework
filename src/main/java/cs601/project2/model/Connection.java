package cs601.project2.model;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;

public class Connection {
	private Socket client;
	private ObjectInputStream in;
	private ObjectOutputStream out;
	
	public Connection(Socket client, ObjectInputStream in, ObjectOutputStream out) {
		this.client = client;
		this.in = in;
		this.out = out;
	}
	
	public Socket getClient() {
		return client;
	}
	public void setClient(Socket client) {
		this.client = client;
	}
	public ObjectInputStream getIn() {
		return in;
	}
	public void setIn(ObjectInputStream in) {
		this.in = in;
	}
	public ObjectOutputStream getOut() {
		return out;
	}
	public void setOut(ObjectOutputStream out) {
		this.out = out;
	}
}
