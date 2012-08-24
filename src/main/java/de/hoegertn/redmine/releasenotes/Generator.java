package de.hoegertn.redmine.releasenotes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.apache.http.HttpResponse;
import org.codehaus.jackson.map.ObjectMapper;

import de.taimos.httputils.WS;

/**
 * @author thoeger
 * 
 */
@SuppressWarnings("unchecked")
public class Generator {

	private static final ObjectMapper mapper = new ObjectMapper();

	private static final String REDMINE_URL = System.getenv("REDMINE_URL");
	private static final String REDMINE_KEY = System.getenv("REDMINE_KEY");

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final String project;
		final String version;

		if (args.length == 2) {
			project = args[0];
			version = args[1];
		} else if (args.length == 1) {
			project = args[0];
			final List<String> versions = Generator.getVersions(args[0]);
			System.out.println("Usage ./releasenotes <project> [<version>]");
			System.out.println();
			System.out.println("Please select a version for project: " + args[0]);
			for (final String v : versions) {
				System.out.println("  > " + v);
			}
			System.out.print("Version: ");

			try (BufferedReader br = new BufferedReader(new InputStreamReader(System.in))) {
				version = br.readLine();
			} catch (final Exception e) {
				return;
			}
		} else {
			System.out.println("Usage ./releasenotes <project> [<version>]");
			return;
		}

		final List<Ticket> tickets = Generator.getClosedTickets(project, version);
		Collections.sort(tickets);
		for (final Ticket ticket : tickets) {
			System.out.println(ticket.toString());
		}
	}

	private static List<Ticket> getClosedTickets(final String project, final String version) {
		// http://redmine/issues.json?project_id=<project>&fixed_version_id=<version>&status_id=closed
		final Integer versionId = Generator.getVersionId(project, version);
		final List<Ticket> tickets = new ArrayList<>();

		int offset = 0;
		int count = Integer.MAX_VALUE;
		while ((tickets.size() < count) && (offset < count)) {
			final String url = "/issues.json?project_id=" + project + "&fixed_version_id=" + versionId + "&status_id=closed&offset="
					+ offset;
			final HashMap<String, Object> map = Generator.getResponseAsMap(url);
			final List<HashMap<String, Object>> issues = (List<HashMap<String, Object>>) map.get("issues");
			count = (int) map.get("total_count");
			offset += 25;

			for (final HashMap<String, Object> hashMap : issues) {
				tickets.add(Ticket.convertMap(hashMap));
			}
		}
		return tickets;
	}

	private static List<String> getVersions(final String project) {
		final List<HashMap<String, Object>> object = Generator.getVersionObjects(project);
		final List<String> versions = new ArrayList<String>();
		for (final HashMap<String, Object> hashMap : object) {
			versions.add((String) hashMap.get("name"));
		}
		return versions;
	}

	private static Integer getVersionId(final String project, final String version) {
		final List<HashMap<String, Object>> object = Generator.getVersionObjects(project);
		for (final HashMap<String, Object> hashMap : object) {
			if (hashMap.get("name").equals(version)) {
				return (Integer) hashMap.get("id");
			}
		}
		return null;
	}

	private static List<HashMap<String, Object>> getVersionObjects(final String project) {
		final HashMap<String, Object> map = Generator.getResponseAsMap("/projects/" + project + "/versions.json");
		final List<HashMap<String, Object>> object = (List<HashMap<String, Object>>) map.get("versions");
		return object;
	}

	private static HashMap<String, Object> getResponseAsMap(final String url) {
		try {
			final HttpResponse response = WS.url(Generator.REDMINE_URL + url).header("X-Redmine-API-Key", Generator.REDMINE_KEY).get();
			final String responseAsString = WS.getResponseAsString(response);
			return Generator.mapper.readValue(responseAsString, HashMap.class);
		} catch (final Exception e) {
			e.printStackTrace();
		}
		return new HashMap<>();
	}
}
