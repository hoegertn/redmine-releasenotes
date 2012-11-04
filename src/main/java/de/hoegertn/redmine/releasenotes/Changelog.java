package de.hoegertn.redmine.releasenotes;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.List;

import de.hoegertn.redmine.releasenotes.model.Ticket;
import de.hoegertn.redmine.releasenotes.model.Version;

/**
 * @author thoeger
 * 
 */
public class Changelog {

	/**
	 * @param args
	 */
	public static void main(final String[] args) {
		final Redmine redmine = new Redmine(System.getenv("REDMINE_URL"), System.getenv("REDMINE_KEY"));

		if (args.length == 1) {
			final String project = args[0];
			final List<Version> versions = redmine.getVersions(project);
			System.out.println("Found versions for project: " + args[0]);
			for (final Version v : versions) {
				System.out.println(String.format("  > %s (%s) [%s]", v.getName(), v.getStatus(), v.getUpdated_on().toString()));
			}

			System.out.println();

			final SimpleDateFormat sdf = new SimpleDateFormat("EEE MMM dd yyyy");
			// Sort versions
			Collections.sort(versions);
			// Newest first
			Collections.reverse(versions);

			final StringBuilder changelog = new StringBuilder();

			for (final Version v : versions) {
				if (v.getStatus().equals("closed")) {
					final String date = sdf.format(v.getUpdated_on());
					final String user = "Thorsten Hoeger <thorsten.hoeger@hoegernet.com>";
					final String versionString = v.getName() + "-1";
					changelog.append(String.format("* %s %s %s \n", date, user, versionString));
					final List<Ticket> tickets = redmine.getClosedTickets(project, v.getId());
					Collections.sort(tickets);
					for (final Ticket ticket : tickets) {
						changelog.append("- ");
						changelog.append(ticket.toString());
						changelog.append('\n');
					}
					changelog.append("\n");
				}
			}

			System.out.println(changelog.toString());
		} else {
			System.out.println("Usage ./releasenotes <project>");
			return;
		}

	}
}
