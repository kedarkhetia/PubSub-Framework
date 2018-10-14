package cs601.project2.model;

/**
 * This class is data model to represent config file.
 * 
 * @author kmkhetia
 *
 */ 

public class Config {
	private String inputDirectory;
	private String outputDirectory;
	private String brokerType;
	private String unixReviewTime;
	private String host;
	private int port;
	private String[] remoteSubscribers;
	private String[] localSubscribers;

	public String[] getLocalSubscribers() {
		return localSubscribers;
	}

	public void setLocalSubscribers(String[] localSubscribers) {
		this.localSubscribers = localSubscribers;
	}

	public String[] getRemoteSubscribers() {
		return remoteSubscribers;
	}

	public void setRemoteSubscribers(String[] remoteSubscribers) {
		this.remoteSubscribers = remoteSubscribers;
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public int getPort() {
		return port;
	}

	public void setPort(int port) {
		this.port = port;
	}

	public String getInputDirectory() {
		return inputDirectory;
	}
	
	public void setInputDirectory(String inputDirectory) {
		this.inputDirectory = inputDirectory;
	}
	
	public String getOutputDirectory() {
		return outputDirectory;
	}
	
	public void setOutputDirectory(String outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	
	public String getBrokerType() {
		return brokerType;
	}
	
	public void setBrokerType(String brokerType) {
		this.brokerType = brokerType;
	}
	
	public String getUnixReviewTime() {
		return unixReviewTime;
	}
	
	public void setUnixReviewTime(String unixReviewTime) {
		this.unixReviewTime = unixReviewTime;
	}
	
	@Override
	public String toString() {
		return "inputDirectory: " + inputDirectory +
				" outputDirectory: " + outputDirectory +
				" brokerType: " + brokerType +
				" unixReviewTime: " + unixReviewTime +
				" host: " + host +
				" port: " + port;
	}
}
