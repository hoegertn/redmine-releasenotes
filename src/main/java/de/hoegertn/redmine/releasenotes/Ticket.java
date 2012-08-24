package de.hoegertn.redmine.releasenotes;

import java.util.HashMap;

/**
 * @author thoeger
 * 
 */
public class Ticket implements Comparable<Ticket> {

	private Integer id;

	private String tracker;

	private String subject;

	/**
	 * @return the id
	 */
	public Integer getId() {
		return this.id;
	}

	/**
	 * @param id
	 *            the id to set
	 */
	public void setId(Integer id) {
		this.id = id;
	}

	/**
	 * @return the tracker
	 */
	public String getTracker() {
		return this.tracker;
	}

	/**
	 * @param tracker
	 *            the tracker to set
	 */
	public void setTracker(String tracker) {
		this.tracker = tracker;
	}

	/**
	 * @return the subject
	 */
	public String getSubject() {
		return this.subject;
	}

	/**
	 * @param subject
	 *            the subject to set
	 */
	public void setSubject(String subject) {
		this.subject = subject;
	}

	@Override
	public String toString() {
		final StringBuilder sb = new StringBuilder();
		sb.append(this.tracker);
		sb.append(" #");
		sb.append(this.id);
		sb.append(": ");
		sb.append(this.subject);
		return sb.toString();
	}

	/**
	 * @param map
	 * @return the converted {@link Ticket}
	 */
	@SuppressWarnings("unchecked")
	public static Ticket convertMap(HashMap<String, Object> map) {
		try {
			final Ticket t = new Ticket();
			t.setId((Integer) map.get("id"));
			t.setSubject((String) map.get("subject"));

			final HashMap<String, Object> tracker = (HashMap<String, Object>) map.get("tracker");
			t.setTracker((String) tracker.get("name"));
			return t;
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return null;
	}

	@Override
	public int compareTo(Ticket o) {
		int res = this.tracker.compareTo(o.tracker);
		if (res == 0) {
			res = this.id - o.id;
		}
		return res;
	}
}
