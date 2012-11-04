package de.hoegertn.redmine.releasenotes;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.List;

import de.hoegertn.redmine.releasenotes.model.Ticket;
import de.hoegertn.redmine.releasenotes.model.Version;

/**
 * @author thoeger
 * 
 */
public class Generator {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Redmine redmine = new Redmine(System.getenv("REDMINE_URL"), System.getenv("REDMINE_KEY"));

		final String project;
		final String version;

		if (args.length == 2) {
			project = args[0];
			version = args[1];
		} else if (args.length == 1) {
			project = args[0];
			final List<Version> versions = redmine.getVersions(args[0]);
			System.out.println("Usage ./releasenotes <project> [<version>]");
			System.out.println();
			System.out.println("Please select a version for project: " + args[0]);
			for (final Version v : versions) {
				System.out.println(String.format("  > %s (%s) [%s]", v.getName(), v.getStatus(), v.getUpdated_on().toString()));
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

		final Version versionObject = redmine.getVersion(project, version);
		final List<Ticket> tickets = redmine.getClosedTickets(project, versionObject.getId());
		Collections.sort(tickets);
		for (final Ticket ticket : tickets) {
			System.out.println(ticket.toString());
		}
	}

}
