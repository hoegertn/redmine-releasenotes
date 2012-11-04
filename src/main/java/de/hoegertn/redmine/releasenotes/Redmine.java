package de.hoegertn.redmine.releasenotes;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.DeserializationConfig;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.JsonNodeFactory;
import org.codehaus.jackson.node.ObjectNode;

import de.hoegertn.redmine.releasenotes.model.Ticket;
import de.hoegertn.redmine.releasenotes.model.Version;
import de.taimos.httputils.HTTPRequest;
import de.taimos.httputils.WS;
import de.taimos.httputils.WSConstants;

/**
 * @author hoegertn
 * 
 */
public class Redmine {

	private final ObjectMapper mapper;

	private final String redmineUrl;
	private final String redmineKey;

	/**
	 * @param redmineUrl
	 *            the URL of the Redmine server
	 * @param redmineKey
	 *            the API KEy to connect to Redmine
	 */
	public Redmine(final String redmineUrl, final String redmineKey) {
		this.redmineUrl = redmineUrl;
		this.redmineKey = redmineKey;

		this.mapper = new ObjectMapper();
		this.mapper.configure(DeserializationConfig.Feature.FAIL_ON_UNKNOWN_PROPERTIES, false);
	}

	/**
	 * @param project
	 * @param version
	 * @return list of {@link Ticket}
	 */
	@SuppressWarnings("unchecked")
	public List<Ticket> getClosedTickets(final String project, final Integer version) {
		// http://redmine/issues.json?project_id=<project>&fixed_version_id=<version>&status_id=closed
		final List<Ticket> tickets = new ArrayList<>();

		int offset = 0;
		int count = Integer.MAX_VALUE;
		while ((tickets.size() < count) && (offset < count)) {
			final String url = "/issues.json?project_id=" + project + "&fixed_version_id=" + version + "&status_id=closed&offset=" + offset;
			final HashMap<String, Object> map = this.getResponseAsMap(url);
			final List<HashMap<String, Object>> issues = (List<HashMap<String, Object>>) map.get("issues");
			count = (int) map.get("total_count");
			offset += 25;

			for (final HashMap<String, Object> hashMap : issues) {
				tickets.add(this.mapper.convertValue(hashMap, Ticket.class));
			}
		}
		return tickets;
	}

	/**
	 * @param project
	 * @param version
	 * @return the {@link Version}
	 */
	public Version getVersion(final String project, final String version) {
		final List<Version> object = this.getVersions(project);
		for (final Version v : object) {
			if (v.getName().equals(version)) {
				return v;
			}
		}
		return null;
	}

	/**
	 * @param project
	 * @return array of {@link Version}
	 */
	@SuppressWarnings("unchecked")
	public List<Version> getVersions(final String project) {
		final HashMap<String, Object> map = this.getResponseAsMap("/projects/" + project + "/versions.json");
		final List<HashMap<String, Object>> object = (List<HashMap<String, Object>>) map.get("versions");
		final Version[] versions = this.mapper.convertValue(object, Version[].class);
		return Arrays.asList(versions);
	}

	@SuppressWarnings("unchecked")
	private HashMap<String, Object> getResponseAsMap(final String url) {
		try {
			final HttpResponse response = this.createRequest(url).get();
			final String responseAsString = WS.getResponseAsString(response);
			return this.mapper.readValue(responseAsString, HashMap.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return new HashMap<>();
	}

	private HTTPRequest createRequest(final String url) {
		System.out.println(this.redmineUrl + url);
		return WS.url(this.redmineUrl + url).header("X-Redmine-API-Key", this.redmineKey);
	}

	public void closeVersion(final String project, final Version version) {
		try {
			final ObjectNode update = JsonNodeFactory.instance.objectNode();
			update.put("name", version.getName());
			update.put("status", "closed");
			final ObjectNode root = JsonNodeFactory.instance.objectNode();
			root.put("version", update);

			final String body = this.mapper.writeValueAsString(root);
			System.out.println(body);
			final HTTPRequest req = this.createRequest("/versions/" + version.getId() + ".json");
			req.header(WSConstants.HEADER_CONTENT_TYPE, "application/json");
			final HttpResponse put = req.body(body).put();
			System.out.println(put);
			System.out.println(WS.getResponseAsString(put));
		} catch (final Exception e) {
			e.printStackTrace();
		}
	}

	public static void main(final String[] args) {
		final Redmine redmine = new Redmine("http://192.168.1.4", "c2dfed3ff0e15fc2d3c05e423e8befb7562ea76a");
		final List<Version> versions = redmine.getVersions("test");
		for (final Version v : versions) {
			if (v.getName().equals("1.0.0")) {
				redmine.closeVersion("test", v);
			}
		}
	}
}
