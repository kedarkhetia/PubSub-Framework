package cs601.project2.model;

/**
 * This class is data model to represent Amazon reviews.
 * 
 * @author kmkhetia
 *
 */ 
public class Review implements Comparable<Review>{
	private String reviewerID;
	private String asin;
	private String reviewerName;
	private int[] helpful;
	private String reviewText;
	private double overall;
	private String summary;
	private int unixReviewTime;
	private String reviewTime;

	Review(String reviewerID, String asin, String reviewerName, int[] helpful, String reviewText, double overall, String summary, int unixReviewTime, String reviewTime) {
		this.reviewerID = reviewerID;
		this.asin = asin;
		this.reviewerName = reviewerName;
		this.helpful = helpful;
		this.reviewText = reviewText;
		this.overall = overall;
		this.summary = summary;
		this.unixReviewTime = unixReviewTime;
		this.reviewTime = reviewTime;
	}

	public int[] getHelpful() {
		return helpful;
	}

	public void setHelpful(int[] helpful) {
		this.helpful = helpful;
	}

	public String getSummary() {
		return summary;
	}

	public void setSummary(String summary) {
		this.summary = summary;
	}

	public String getReviewTime() {
		return reviewTime;
	}

	public void setReviewTime(String reviewTime) {
		this.reviewTime = reviewTime;
	}

	public void setOverall(double overall) {
		this.overall = overall;
	}
	
	public String getReviewerID() {
		return reviewerID;
	}

	public void setReviewerID(String reviewerID) {
		this.reviewerID = reviewerID;
	}

	public String getAsin() {
		return asin;
	}

	public void setAsin(String asin) {
		this.asin = asin;
	}

	public String getReviewerName() {
		return reviewerName;
	}

	public void setReviewerName(String reviewerName) {
		this.reviewerName = reviewerName;
	}

	public String getReviewText() {
		return reviewText;
	}

	public void setReviewText(String reviewText) {
		this.reviewText = reviewText;
	}

	public double getOverall() {
		return overall;
	}

	public void setOverall(int overall) {
		this.overall = overall;
	}

	public int getUnixReviewTime() {
		return unixReviewTime;
	}

	public void setUnixReviewTime(int unixReviewTime) {
		this.unixReviewTime = unixReviewTime;
	}
	
	
	
	@Override
	public String toString() {
		return "{\"reviewerID\": \""+ reviewerID +
			"\", \"asin\": \""+ asin +
			"\", \"reviewerName\": \""+ reviewerName +
			"\", \"helpful\": ["+ helpful[0] + ", " + helpful[1] + "]" +
			", \"reviewText\": \""+ reviewText +
			"\", \"overall\": "+ overall +
			", \"summary\": \""+ summary +
			"\", \"unixReviewTime\": "+ unixReviewTime +
			", \"reviewTime\": \""+ reviewTime +"\"}";
	}

	@Override
	public int compareTo(Review review) {
		return (this.unixReviewTime - review.getUnixReviewTime());
	}
	
}
