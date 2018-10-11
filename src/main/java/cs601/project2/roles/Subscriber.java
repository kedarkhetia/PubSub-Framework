package cs601.project2.roles;

public interface Subscriber<T> {
	/**
	 * Called by the Broker when a new item
	 * has been published.
	 * @param item
	 */
	public void onEvent(T item);
}
