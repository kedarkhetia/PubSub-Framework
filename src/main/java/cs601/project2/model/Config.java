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
	
}
